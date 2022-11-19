package cp2022.tests.pggp_tests.utility;

import cp2022.base.Workplace;
import cp2022.base.WorkplaceId;
import cp2022.base.Workshop;
import cp2022.solution.WorkshopFactory;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.*;

/*
    SimulationWithBugCheck:
        - creates threads for workers,
        - wraps Workshop object, such that before and after enter, switch and leave,
          security and liveliness conditions are checked,
        - checks whether every worker invoked use() method of workplace if she wanted to do so.
 */
public class SimulationWithBugCheck implements Workshop {
    private final Semaphore mutex; // Mutex for internal use of SimulationWithBugCheck class.
    private final Workshop wrappedWorkshop;
    public final Integer verbose;

    public volatile boolean errorBoolean = false;

    // Workplace info.
    private final int numberOfWorkplaces;
    private final  ArrayList<WorkplaceId> workplaceIds;


    // Properties of the workplace.
    public final int timeOfOneWork;

    // Information about the workers in the workshop. Used to check the safety property of the workshop implementation.

    private final Map <Thread, WorkerId> threadToWorkerId;
    private final Map <WorkerId, Thread> workerIdToThread;

    private final Map<WorkplaceId, WorkerId> workplaceToOwnerLazy;
    private final Map<WorkerId, WorkplaceId> ownerToWorkplace;

    // Used to check liveliness property of the workshop implementation.
    private final Map<WorkerId, Integer> enteredAfterWorkerRequest;
    private final Map<WorkerId, Integer> requestAge;
    private int globalAge = 0;
    private final boolean doCheckLiveliness;
    private final Semaphore orderWait;

    public static int timeOfWaitBetweenActionsWhenOrderMatters = 10;

    // Used to check whether workplace.use() is fact invoke original workplace.use().
    private final Map<WorkplaceId, Integer> usagesOfWorkplace;

    public int getUsages(WorkplaceId id) {
        return this.usagesOfWorkplace.get(id);
    }

    public Integer getWorkplaceIntId(Workplace currentWorkplace) {
        for (int i = 0; i < this.numberOfWorkplaces; i++) {
            if(workplaceIds.get(i).compareTo(currentWorkplace.getId()) == 0) {
                return i;
            }
        }

        throw new RuntimeException("Test error - wrong workplace id.");
    }

    public Semaphore getEnsureOrderSemaphore() {
        return orderWait;
    }


    // Implementation of the workplace, which collaborated with SimulationWithBugCheck to check bugs.
    private class WorkplaceImplementation extends Workplace {
        public final int timeOfWork;
        private final SimulationWithBugCheck workshop;
        private final WorkplaceIdImplementation id;

        protected WorkplaceImplementation(int id, int timeInMilliseconds, SimulationWithBugCheck workshop) {
            super(new WorkplaceIdImplementation(id));
            this.id = (WorkplaceIdImplementation) super.getId();
            this.timeOfWork = timeInMilliseconds;
            this.workshop = workshop;
        }

        /*
            Method does two things:
                - checks whether process trying to work in this workplace, possesses this workplace,
                - writes in the history that this workplace was used.
         */
        @Override
        public void use() {
            try {
                mutex.acquire();
                WorkerId workplaceOwnerId = workshop.workplaceToOwnerLazy.get(this.id);
                WorkerId currentWorkerId = workshop.getWorkerIdOfCurrentThread();
                if (verbose == 2) {
                    Thread.sleep(timeOfWork);
                    System.out.println("Worker " + currentWorkerId.id + " starts using workplace " + id.id);
                }

                // We check whether our worker posses workplace we want to work on.
                if(workplaceOwnerId != currentWorkerId) {
                    // If not, we throw an exception.
                    errorBoolean = true;
                    throw new RuntimeException("[Exception] Worker " + currentWorkerId.id + " tried to work on the workplace "
                            + id.id + ", " + "which is occupied by the worker " + workplaceOwnerId.id);
                }

                // We increase stored number of usages.
                usagesOfWorkplace.put(this.id, usagesOfWorkplace.get(this.id) + 1);

                if (verbose == 2) System.out.println("Worker " + currentWorkerId.id + " stops using workplace " + id.id );
                mutex.release();
                Thread.sleep(timeOfWork);
            } catch (InterruptedException e) {
                throw new RuntimeException("Test panic - error in the tests. There should not be any interruption.");
            }
        }

    }

    public SimulationWithBugCheck(int numberOfWorkplaces,
                                  int timeOfOneWork,
                                  Worker[] workers,
                                  int verbose,
                                  boolean doCheckLiveliness) {

        this.mutex = new Semaphore(1, true); // Mutex used by the simulation.

        // If verbose is true, simulation will print logs into console.
        this.verbose = verbose;

        // Lot of initialization...

        this.timeOfOneWork = timeOfOneWork;
        this.numberOfWorkplaces = numberOfWorkplaces;

        this.threadToWorkerId = new ConcurrentHashMap<>();
        this.workerIdToThread = new ConcurrentHashMap<>();

        // Can contain old values, but if a has workplace b, then workplaceToOwnerLazy.get(b) = a.
        this.workplaceToOwnerLazy = new ConcurrentHashMap<>();
        this.ownerToWorkplace = new ConcurrentHashMap<>();

        this.enteredAfterWorkerRequest = new ConcurrentHashMap<>();
        this.requestAge = new ConcurrentHashMap<>();
        this.doCheckLiveliness = doCheckLiveliness;


        ArrayList<Workplace> workplaces = new ArrayList<>();
        this.workplaceIds = new ArrayList<>();
        this.usagesOfWorkplace = new ConcurrentHashMap<>();
        for (int i = 0; i < numberOfWorkplaces; i++) {
            WorkplaceImplementation workplace = new WorkplaceImplementation(i, timeOfOneWork, this);
            workplaces.add(workplace);
            workplaceIds.add(workplace.id);
            usagesOfWorkplace.put(workplace.id, 0);
        }


        this.orderWait = new Semaphore(1, true);

        // Initialization of workers.

        for (Worker worker : workers) {
            Thread worker_thread = worker.createThreadWithDoingWork(this);
            this.threadToWorkerId.put(worker_thread, worker.getId());
            if(workerIdToThread.get(worker.getId()) != null) {
                throw new RuntimeException("Test error - two workers have the same id.");
            }

            this.workerIdToThread.put(worker.getId(), worker_thread);
        }

        wrappedWorkshop = WorkshopFactory.newWorkshop(workplaces);
    }

    public boolean start() {

        // Start all workers.
        for(Map.Entry<WorkerId, Thread> p : workerIdToThread.entrySet()) {
            p.getValue().setName("Worker " + p.getKey().id + " thread.");
            p.getValue().start();
        }

        for (Thread thread : workerIdToThread.values()) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException("Test error. Should not happen.");
            }
        }

        return !this.errorBoolean;
    }

    public WorkerId getWorkerIdOfCurrentThread() {
        return threadToWorkerId.get(Thread.currentThread());
    }
    @Override
    public Workplace enter(WorkplaceId wid) {
        acquireMutexOrPanic();


        // We put the request to enter into proper Map.
        putRequest(getWorkerIdOfCurrentThread());
        mutex.release();

        Workplace workplace = wrappedWorkshop.enter(wid);

        acquireMutexOrPanic();

        // Since one worker entered the workshop,
        // every value of the enteredAfterWorkerRequest must be increased by 1.
        int finished_request_time = requestAge.get(getWorkerIdOfCurrentThread());
        removeRequest(getWorkerIdOfCurrentThread());
        increaseAgeByOneForYoungerThan(finished_request_time);

        checkWhetherLivelinessIsAbused();

        // Update maps.
        workplaceToOwnerLazy.put(wid, getWorkerIdOfCurrentThread());
        ownerToWorkplace.put(getWorkerIdOfCurrentThread(), wid);

        mutex.release();

        return workplace;
    }

    private void increaseAgeByOneForYoungerThan(int time) {
        for(Map.Entry<WorkerId, Integer> workerId : this.requestAge.entrySet()) {
            if(workerId.getValue() < time) {
                workerId.setValue(workerId.getValue() + 1);
            }
        }
    }

    @Override
    public Workplace switchTo(WorkplaceId wid) {
        // Insert request in the map.
        acquireMutexOrPanic();
        putRequest(getWorkerIdOfCurrentThread());
        mutex.release();

        Workplace workplace = wrappedWorkshop.switchTo(wid);

        // Remove request in the map.
        acquireMutexOrPanic();
        removeRequest(getWorkerIdOfCurrentThread());

        // Update info about workshop ownership.
        ownerToWorkplace.put(getWorkerIdOfCurrentThread(), wid);
        workplaceToOwnerLazy.put(wid, getWorkerIdOfCurrentThread());

        mutex.release();

        return workplace;
    }

    private void putRequest(WorkerId workerId) {
        enteredAfterWorkerRequest.put(workerId, 0);
        requestAge.put(workerId, this.globalAge);
        globalAge++;
    }

    private void removeRequest(WorkerId workerId) {
        enteredAfterWorkerRequest.remove(workerId);
        requestAge.remove(workerId);
    }

    @Override
    public void leave() {
        // Remove request in the map.
        acquireMutexOrPanic();
        removeRequest(getWorkerIdOfCurrentThread());

        // Remove info about workshop ownership.
        WorkplaceId workplace = ownerToWorkplace.get(getWorkerIdOfCurrentThread());
        ownerToWorkplace.remove(getWorkerIdOfCurrentThread());
        workplaceToOwnerLazy.remove(workplace);

        mutex.release();
        wrappedWorkshop.leave();
    }

    public WorkplaceId getWorkplaceId(int id) {
        if(id < 0 && id > numberOfWorkplaces) {
            throw new RuntimeException("Test error - wrong workplace id.");
        }
        return this.workplaceIds.get(id);
    }

    private void acquireMutexOrPanic() {
        try {
            this.mutex.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException("Panic: test was interrupted.");
        }
    }

    private void checkWhetherLivelinessIsAbused() {
        if(!doCheckLiveliness) {
            return;
        }
        for(WorkerId id : enteredAfterWorkerRequest.keySet()) {
            if(enteredAfterWorkerRequest.get(id) >= 2 * numberOfWorkplaces) {
                errorBoolean = true;
                throw new RuntimeException("Error - 2*N workers entered workshop before worker " +
                        id.id + " make a change.");
            }
        }
    }

    public boolean getDoCheckLiveliness() {
        return doCheckLiveliness;
    }
}