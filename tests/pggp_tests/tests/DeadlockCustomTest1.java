package cp2022.tests.pggp_tests.tests;

import cp2022.tests.pggp_tests.utility.SimulationWithBugCheck;
import cp2022.tests.pggp_tests.utility.Test;
import cp2022.tests.pggp_tests.utility.Worker;
import cp2022.tests.pggp_tests.utility.workshop_actions.Action;

public class DeadlockCustomTest1 extends Test {
    public DeadlockCustomTest1() {
        timeOfAuthor = 610L;
    }
    public boolean run(int verbose) {
        Action[] victim1 = {
                enter(3),
                sleep(2000),
                switchTo(1),
                leave()
        };
        Action[] victim2 = {
                sleep(50),
                enter(1),
                switchTo(3),
                leave()
        };
        Action[] victim3 = {
                sleep(100),
                enter(1),
                switchTo(3),
                leave()
        };
        Action[] victim4 = {
                sleep(50),
                enter(3),
                switchTo(1),
                leave()
        };

        Worker[] workers = {new Worker(1, victim1), new Worker(2, victim2), new Worker(3, victim3), new Worker(6, victim4)};

        SimulationWithBugCheck wrapper = new SimulationWithBugCheck(4, 100, workers, verbose, true);
        return wrapper.start();
    }
}