package cp2022.tests.wojtekr.wojteks_tests;

import cp2022.tests.pggp_tests.utility.SimulationWithBugCheck;
import cp2022.tests.pggp_tests.utility.Test;
import cp2022.tests.pggp_tests.utility.Worker;
import cp2022.tests.pggp_tests.utility.workshop_actions.Action;

public class TestDeadlock2Simple3 extends Test {
    public TestDeadlock2Simple3() {
        timeOfAuthor = 2180L;
        timeLimit = 3 * timeOfAuthor;
    }
    public boolean run(int verbose) {
        Action[] victim1 = {
                enter(3),
                sleep(1000),
                use(),
                switchTo(0),
                use(),
                leave()
        };
        Action[] victim2 = {
                sleep(50),
                enter(2),
                use(),
                switchTo(3),
                use(),
                switchTo(0),
                use(),
                leave()
        };
        Action[] victim3 = {
                sleep(50),
                enter(0),
                sleep(500),
                use(),
                switchTo(3),
                use(),
                leave()
        };
        Action[] victim4 = {
                sleep(100),
                enter(1),
                use(),
                switchTo(0),
                use(),
                switchTo(3),
                use(),
                leave()
        };

        Worker[] workers = {new Worker(1, victim1), new Worker(2, victim2), new Worker(3, victim3), new Worker(4, victim4)};

        SimulationWithBugCheck wrapper = new SimulationWithBugCheck(4, 100, workers, verbose, false);
        return wrapper.start();
    }
}