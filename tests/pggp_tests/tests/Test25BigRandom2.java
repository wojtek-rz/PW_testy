package cp2022.tests.pggp_tests.tests;

import cp2022.tests.pggp_tests.utility.SimulationWithBugCheck;
import cp2022.tests.pggp_tests.utility.Test;
import cp2022.tests.pggp_tests.utility.Worker;

public class Test25BigRandom2 extends Test {
    // Podobne jak wyżej, tylko z nieco innymi parametrami np. jak często ludzie wychodzą.
    public Test25BigRandom2() {
        timeOfAuthor = 1196L;
    }
    public boolean run(int verbose) {
        Worker[] workers = new Worker[100];

        for (int i = 0; i < 100; i++) {
            workers[i] = workerRandomActions(i, 100, 3, 50);
        }

        SimulationWithBugCheck wrapper =
                new SimulationWithBugCheck(3, 10, workers, verbose, false);
        return wrapper.start();
    }
}
