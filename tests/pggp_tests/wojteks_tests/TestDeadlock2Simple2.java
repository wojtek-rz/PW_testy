package cp2022.tests.pggp_tests.wojteks_tests;

import cp2022.tests.pggp_tests.utility.SimulationWithBugCheck;
import cp2022.tests.pggp_tests.utility.Test;
import cp2022.tests.pggp_tests.utility.Worker;
import cp2022.tests.pggp_tests.utility.workshop_actions.Action;

public class TestDeadlock2Simple2 extends Test {
        public TestDeadlock2Simple2() {
            timeOfAuthor = 610L;
        }
    public boolean run(int verbose) {
            // Ci dwaj się przeplatają, a reszta się próbuje dopchać.
        Action[] victim1 = {
                enter(3),
                sleep(1000),
                use(),
                switchTo(1),
                use(),
                switchTo(3),
                use(),
                switchTo(1),
                use(),
                leave()
        };
        Action[] victim3 = {
                enter(1),
                sleep(50),
                use(),
                switchTo(3),
                use(),
                switchTo(1),
                use(),
                switchTo(3),
                use(),
                leave()
        };
        Action[] victim2 = {
                sleep(50),
                enter(3),
                use(),
                switchTo(1),
                use(),
                leave()
        };
        Action[] victim4 = {
                sleep(50),
                enter(1),
                use(),
                switchTo(3),
                use(),
                leave()
        };
        Action[] victim5 = {
                sleep(100),
                enter(1),
                use(),
                switchTo(3),
                use(),
                leave()
        };
        Action[] victim6 = {
                sleep(100),
                enter(3),
                use(),
                switchTo(1),
                use(),
                leave()
        };

        Worker[] workers = {new Worker(1, victim1), new Worker(2, victim2), new Worker(3, victim3), new Worker(4, victim4), new Worker(5, victim5), new Worker(6, victim6)};

        SimulationWithBugCheck wrapper = new SimulationWithBugCheck(4, 100, workers, verbose, true);
        return wrapper.start();
    }
}