package cp2022.tests.wojtekr.wojteks_tests;

import cp2022.tests.pggp_tests.utility.SimulationWithBugCheck;
import cp2022.tests.pggp_tests.utility.Test;
import cp2022.tests.pggp_tests.utility.Worker;
import cp2022.tests.pggp_tests.utility.workshop_actions.Action;

public class TestDeadlock2Simple4 extends Test {
    public TestDeadlock2Simple4() {
        timeOfAuthor = 1875L;
        timeLimit = 3 * timeOfAuthor;
    }
    public boolean run(int verbose) {
        Worker[] workers = new Worker[12];
        workers[0] = new Worker(1, new Action[] {
                enter(1),
                sleep(1000),
                use(),
                switchTo(2),
                use(),
                leave()
        });
        workers[1] = new Worker(2, new Action[] {
                enter(2),
                sleep(800),
                use(),
                switchTo(3),
                use(),
                switchTo(4),
                use(),
                leave()
        });
        workers[2] = new Worker(3, new Action[] {
                enter(3),
                sleep(600),
                use(),
                switchTo(4),
                use(),
                switchTo(1),
                use(),
                leave()
        });
        workers[3] = new Worker(4, new Action[] {
                enter(4),
                sleep(400),
                use(),
                switchTo(1),
                use(),
                leave()
        });
        for (int i = 5; i <= 8; i++){;
            workers[i - 1] = new Worker(i - 4 + 10,
                    new Action[]  {
                            sleep(50),
                            enter(i),
                            use(),
                            switchTo(i - 4),
                            use(),
                            leave()
                    });
        }
        for (int i = 9; i <= 12; i++){;
            workers[i - 1] = new Worker(i - 8 + 100,
                    new Action[]  {
                            sleep(100),
                            enter(i - 8),
                            use(),
                            leave()
                    });
        }

        SimulationWithBugCheck wrapper = new SimulationWithBugCheck(9, 100, workers, verbose, false);
        return wrapper.start();
    }
}