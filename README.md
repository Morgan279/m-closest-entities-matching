# Efficient *m*-Closest Entities Matching over HINs



### Start Up

You can run algorithms locally by using jupiter:

```java
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
    
}
```



or you can run algorithms by executing "java -jar" after packaging (mvn clean package)

```bash
java -jar ./target/m-closest-entities-matching-1.0-SNAPSHOT-jar-with-dependencies.jar
```



### Dataset

Datasets Yelp, Gowalla, Brightkite are too large to upload to the repository. Here are their download links:

yelp: https://www.yelp.com/dataset

gowalla: http://snap.stanford.edu/data/loc-Gowalla.html

brightkite: http://snap.stanford.edu/data/loc-Brightkite.html



------

Copyright© 2021 AIDA-DB Lab