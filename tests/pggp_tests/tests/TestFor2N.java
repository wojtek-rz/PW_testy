package cp2022.tests.pggp_tests.tests;

import cp2022.tests.pggp_tests.utility.SimulationWithBugCheck;
import cp2022.tests.pggp_tests.utility.Test;
import cp2022.tests.pggp_tests.utility.Worker;
import cp2022.tests.pggp_tests.utility.workshop_actions.Action;

public class TestFor2N extends Test {
    // Jeden pracownik zmienia miejsca pracy i używa ich.
    public TestFor2N() {
        timeOfAuthor = 610L;
    }
    public boolean run(int verbose) {
        Action[] firstWorkerActions = {
                enter(0),
                sleep(3000),
                leave()
        };
        Action[] secondWorker = {
                sleep(20),
                enter(1),
                switchTo(0),
                leave()
        };
        Action[] thirdWorker = {
                sleep(160),
                enter(3),    // 1
                leave(),
                enter(3),
                leave(),
                enter(3),
                leave(),
                enter(3),
                leave(),
                enter(3),
                leave(),
                enter(3),
                leave(),
                enter(3),
                leave(),
                enter(3), // to się nie wykona
                leave(),
                enter(3),
                leave(),
                enter(3),
                leave(),
        };

        Worker[] workers = {new Worker(1, firstWorkerActions), new Worker(2, secondWorker), new Worker(3, thirdWorker)};

        SimulationWithBugCheck wrapper = new SimulationWithBugCheck(4, 100, workers, verbose, true);
        return wrapper.start();
    }
}