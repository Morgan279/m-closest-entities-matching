package edu.ecnu.aidadblab;

import edu.ecnu.aidadblab.tool.ExperimentExecutor;
import org.junit.jupiter.api.Test;

public class Experiment {

    private final ExperimentExecutor experimentExecutor = new ExperimentExecutor();

    @Test
    public void timeRatioStatisticsWithQueryNodesVaried() {
        experimentExecutor.timeRatioStatisticsWithQueryNodesVaried();
    }

    @Test
    public void timeRatioStatisticsWithDataGraphSizeVaried() {
        experimentExecutor.timeRatioStatisticsWithDataGraphSizeVaried();
    }

    @Test
    public void efficientTest() {
        experimentExecutor.efficientTestVariedM();
    }

    @Test
    public void indexTest() {
        experimentExecutor.indexTest();
    }


    @Test
    public void effective() {
        experimentExecutor.effective();
    }


    @Test
    public void test() {
        experimentExecutor.arcScaleableTest();
    }
}
