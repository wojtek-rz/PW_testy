package cp2022.tests.wojtekr;

import cp2022.tests.pggp_tests.utility.SimulationWithBugCheck;
import cp2022.tests.pggp_tests.utility.Test;
import cp2022.tests.wojtekr.wojteks_tests.*;

// Important note: tests based on the pggp tests utility.
public class Main {
    public static void main(String[] args) {
        // If you want to log information, change to 1 or 2
        int verbose = 0;

        System.out.println("Parameter verbose = " + verbose + ". It can be changed in the code of the tests to print the logs.");

        if(verbose != 0) {
            System.out.println("If the test doesn't check the order of events, the order of logs may not be true.");
        }

        System.out.println("");

        // How much time will elapse between two following actions. Applied only when liveliness is checked.
        SimulationWithBugCheck.timeOfWaitBetweenActionsWhenOrderMatters = 30;

        Test[] tests = {
                new TestFor2NSimple(),
                new TestDeadlock2Simple(),
                new TestDeadlock2Simple2(),
                new TestDeadlock2Simple3(),
                new TestDeadlock2Simple4(),
        };

        int i = 1;
        for (Test test : tests) {
            System.out.println("Test " + i + " (author's time " + test.getTimeOfAuthor() + "ms)");
            if(test.getTimeLimit() != null) {
                System.out.println("Time limit = " + test.getTimeLimit() + "ms");
            }
            long start = System.currentTimeMillis();

            boolean passed = test.run(verbose); // Run the test.

            long finish = System.currentTimeMillis();
            long timeElapsed = finish - start;
            if(test.getTimeLimit() != null) {
                if(test.getTimeLimit() < timeElapsed) {
                    System.out.println("Test took " + timeElapsed + "ms");
                    System.out.println("Time limit exceeded.");
                    passed = false;
                }
            }

            if(passed) {
                System.out.println("PASSED in " + timeElapsed + "ms");
                System.out.println();
            }
            else {
                System.out.println("Not passed.");
                return;
            }
            i++;
        }
    }
}
