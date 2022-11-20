package cp2022.tests.pggp_tests.wojteks_tests;

import cp2022.tests.pggp_tests.utility.SimulationWithBugCheck;
import cp2022.tests.pggp_tests.utility.Test;
import cp2022.tests.pggp_tests.utility.Worker;
import cp2022.tests.pggp_tests.utility.workshop_actions.Action;

public class TestFor2NSimple extends Test {
    // Jeden pracownik zmienia miejsca pracy i używa ich.
    public TestFor2NSimple() {
        timeOfAuthor = 610L;
    }
    public boolean run(int verbose) {
        Action[] firstWorkerActions = {
                enter(0),
                sleep(2000),
                use(),
                leave()
        };
        Action[] secondWorker = {
                sleep(20),
                enter(1),
                use(),
                switchTo(0),
                use(),
                leave()
        };
        Action[] thirdWorker = concat(
                sleep(160),
                repeat(new Action[] {enter(2), use(), leave()}, 10)
        );

        Worker[] workers = {new Worker(1, firstWorkerActions), new Worker(2, secondWorker), new Worker(3, thirdWorker)};

        SimulationWithBugCheck wrapper = new SimulationWithBugCheck(4, 100, workers, verbose, true);
        return wrapper.start();
    }
}