package edu.ecnu.aidadblab.tool;

import cn.hutool.core.lang.Console;
import com.alibaba.fastjson.JSONObject;
import edu.ecnu.aidadblab.algorithm.mcloest.entities.matching.MClosestEntitiesMatchingAlgorithm;
import edu.ecnu.aidadblab.algorithm.mcloest.entities.matching.impl.ExactEnumAlgorithm;
import edu.ecnu.aidadblab.algorithm.mcloest.entities.matching.impl.ExactScanAlgorithm;
import edu.ecnu.aidadblab.algorithm.mcloest.entities.matching.impl.F2EScanAlgorithm;
import edu.ecnu.aidadblab.algorithm.mcloest.entities.matching.impl.IF2EScanAlgorithm;
import edu.ecnu.aidadblab.algorithm.subgraph.matching.SubgraphMatchingAlgorithm;
import edu.ecnu.aidadblab.algorithm.subgraph.matching.imlp.VC;
import edu.ecnu.aidadblab.config.GlobalConfig;
import edu.ecnu.aidadblab.constant.Dataset;
import edu.ecnu.aidadblab.constant.IndexType;
import edu.ecnu.aidadblab.constant.LabelConst;
import edu.ecnu.aidadblab.data.model.Graph;
import edu.ecnu.aidadblab.data.model.MatchGroup;
import edu.ecnu.aidadblab.data.model.Vertex;
import edu.ecnu.aidadblab.importer.YelpImporter;
import edu.ecnu.aidadblab.processor.ExactMatchProcessor;
import io.netty.util.internal.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import org.openjdk.jol.info.GraphLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class ExperimentExecutor {

    private Graph dataGraph;

    private List<Graph> queryGraphs = new ArrayList<>();

    private static final int EXPERIMENT_TIMES = 20;

    private final double[] dataGraphSizes = {1e3, 1e4, 1e5, 1e6};

    double currentDataGraphSize = dataGraphSizes[0];

    MClosestEntitiesMatchingAlgorithm timeRatioStatisticsAlgorithm = new ExactScanAlgorithm();

    private final MClosestEntitiesMatchingAlgorithm indexTestAlgorithm = new IF2EScanAlgorithm();

    private List<MClosestEntitiesMatchingAlgorithm> testAlgorithms;


    public void indexTest() {
        GlobalConfig.ENABLE_INDEX = true;
        GraphGenerator.queryGraphCount = 4;
        GraphGenerator.queryGraphSize = 8;
        this.switchYelpDataSetWithSpecificReadLine();
        indexTestAlgorithm.query(dataGraph, GraphGenerator.generateRandomQueryGraph(dataGraph));
        for (double graphSize : dataGraphSizes) {
            currentDataGraphSize = graphSize;
            indexEfficientTest();
        }

        log.info("index efficient test over");

        for (double graphSize : dataGraphSizes) {
            currentDataGraphSize = graphSize;
            indexSpaceTest();
        }

    }

    public void efficientTestVariedM() {
        //for building index
        GlobalConfig.ENABLE_INDEX = true;
        GlobalConfig.MAX_READ_LINE = (int) 1e6;
        ExperimentDataset.init();

        testAlgorithms = new ArrayList<>(4);
        testAlgorithms.add(new ExactEnumAlgorithm());
        testAlgorithms.add(new ExactScanAlgorithm());
        testAlgorithms.add(new F2EScanAlgorithm());
        testAlgorithms.add(new IF2EScanAlgorithm());
        int[] variedSize = {8, 16};

        for (int queryGraphSize : variedSize) {
            GraphGenerator.queryGraphSize = queryGraphSize;

            this.switchYelpDataSet();
            this.conductEfficientTestVariedM();

            log.info("switching FS dataset");
            this.switchFoursquareDataSet();
            this.conductEfficientTestVariedM();

            log.info("switching WD dataset");
            this.switchWikiDataSet();
            this.conductEfficientTestVariedM();

            log.info("switching GW dataset");
            this.switchGowallaDataSet();
            this.conductEfficientTestVariedM();

            log.info("switching BK dataset");
            this.switchBrightkiteDataSet();
            this.conductEfficientTestVariedM();

            log.info("query graph size {} is going to switch", queryGraphSize);
        }

    }

    public void efficientTest() {
        //for building index
        GlobalConfig.ENABLE_INDEX = true;
        GlobalConfig.MAX_READ_LINE = (int) 1e6;
        ExperimentDataset.init();

        testAlgorithms = new ArrayList<>(4);
        testAlgorithms.add(new ExactEnumAlgorithm());
        testAlgorithms.add(new ExactScanAlgorithm());
        testAlgorithms.add(new F2EScanAlgorithm());
        testAlgorithms.add(new IF2EScanAlgorithm());

        int[] variedQueryGraphCount = {3, 6};

        for (int queryGraphCount : variedQueryGraphCount) {
            GraphGenerator.queryGraphCount = queryGraphCount;

            this.switchYelpDataSet();
            this.conductEfficientTest();

            log.info("switching FS dataset");
            this.switchFoursquareDataSet();
            this.conductEfficientTest();

            log.info("switching WD dataset");
            this.switchWikiDataSet();
            this.conductEfficientTest();

            log.info("switching GW dataset");
            this.switchGowallaDataSet();
            this.conductEfficientTest();

            log.info("switching BK dataset");
            this.switchBrightkiteDataSet();
            this.conductEfficientTest();

            log.info("query graph count {} is going to switch", queryGraphCount);
        }

    }

    public void efficientTestWithDataSizeVaried() {
        //for building index
        GlobalConfig.ENABLE_INDEX = true;


        testAlgorithms = new ArrayList<>(4);
        testAlgorithms.add(new ExactEnumAlgorithm());
        testAlgorithms.add(new ExactScanAlgorithm());
        testAlgorithms.add(new F2EScanAlgorithm());
        testAlgorithms.add(new IF2EScanAlgorithm());

        final int[] queryGraphSizes = {8, 16};
        final int[] queryGraphCounts = {3, 6};

        for (int i = 0; i < 2; ++i) {
            GraphGenerator.queryGraphSize = queryGraphSizes[i];
            GraphGenerator.queryGraphCount = queryGraphCounts[i];

            this.conductEfficientWithDataSizeVariedTest(Dataset.YELP);

            log.info("switching FS dataset");
            this.conductEfficientWithDataSizeVariedTest(Dataset.FOURSQUARE);

            log.info("switching WD dataset");
            this.conductEfficientWithDataSizeVariedTest(Dataset.WIKIDATA);

            log.info("switching GW dataset");
            this.conductEfficientWithDataSizeVariedTest(Dataset.GOWALLA);

            log.info("switching BK dataset");
            this.conductEfficientWithDataSizeVariedTest(Dataset.BRIGHTKITE);
        }

    }

    public void timeRatioStatisticsWithQueryNodesVaried() {
        GlobalConfig.TIME_RATIO_TEST = true;
        GlobalConfig.MAX_READ_LINE = (int) 1e5;
        GraphGenerator.queryGraphCount = 6;
        ExperimentDataset.init();

        final int[] queryGraphSizes = {4, 6, 8, 10, 12, 14, 16};
        for (int queryGraphSize : queryGraphSizes) {
            GraphGenerator.queryGraphSize = queryGraphSize;

            this.switchYelpDataSet();
            this.conductTimeRatioStatisticsWithQueryNodesVaried();

            log.info("switching FS dataset");
            this.switchFoursquareDataSet();
            this.conductTimeRatioStatisticsWithQueryNodesVaried();

            log.info("switching WD dataset");
            this.switchWikiDataSet();
            this.conductTimeRatioStatisticsWithQueryNodesVaried();

            log.info("switching GW dataset");
            this.switchGowallaDataSet();
            this.conductTimeRatioStatisticsWithQueryNodesVaried();

            log.info("switching BK dataset");
            this.switchBrightkiteDataSet();
            this.conductTimeRatioStatisticsWithQueryNodesVaried();
        }

    }

    public void timeRatioStatisticsWithDataGraphSizeVaried() {
        GlobalConfig.TIME_RATIO_TEST = true;
        GraphGenerator.queryGraphCount = 6;
        GraphGenerator.queryGraphSize = 12;

        for (double dataGraphSize : dataGraphSizes) {
            GlobalConfig.MAX_READ_LINE = (int) dataGraphSize;
            ExperimentDataset.init();

            this.switchYelpDataSet();
            this.conductTimeRatioStatisticsWithDataGraphSizeVaried();

            log.info("switching FS dataset");
            this.switchFoursquareDataSet();
            this.conductTimeRatioStatisticsWithDataGraphSizeVaried();

            log.info("switching WD dataset");
            this.switchWikiDataSet();
            this.conductTimeRatioStatisticsWithDataGraphSizeVaried();

            log.info("switching GW dataset");
            this.switchGowallaDataSet();
            this.conductTimeRatioStatisticsWithDataGraphSizeVaried();

            log.info("switching BK dataset");
            this.switchBrightkiteDataSet();
            this.conductTimeRatioStatisticsWithDataGraphSizeVaried();
        }

    }

    private void conductTimeRatioStatisticsWithQueryNodesVaried() {
        double totalTimeRatio = 0;
        final int queryGraphCountPerQuery = GraphGenerator.queryGraphCount;
        for (int i = 0; i < EXPERIMENT_TIMES; ++i) {
            this.queryGraphs = GraphGenerator.generateRandomQueryGraphWithSkip(dataGraph, i * queryGraphCountPerQuery);
            timeRatioStatisticsAlgorithm.query(dataGraph, queryGraphs);
            double radio = GlobalData.timeRatio;
            totalTimeRatio += radio;
            Console.log("{} round ratio: {}", i + 1, radio);
        }
        log.info("{} {}", GraphGenerator.queryGraphSize, totalTimeRatio / (double) EXPERIMENT_TIMES);
    }

    private void conductTimeRatioStatisticsWithDataGraphSizeVaried() {
        double totalTimeRatio = 0;
        final int queryGraphCountPerQuery = GraphGenerator.queryGraphCount;
        for (int i = 0; i < EXPERIMENT_TIMES; ++i) {
            this.queryGraphs = GraphGenerator.generateRandomQueryGraphWithSkip(dataGraph, i * queryGraphCountPerQuery);
            timeRatioStatisticsAlgorithm.query(dataGraph, queryGraphs);
            double radio = GlobalData.timeRatio;
            totalTimeRatio += GlobalData.timeRatio;
            Console.log("{} round ratio: {}", i + 1, radio);
        }
        log.info("$10^{}$ {}", (String.valueOf(GlobalConfig.MAX_READ_LINE)).length() - 1, totalTimeRatio / (double) EXPERIMENT_TIMES);
    }


    private void conductEfficientTestVariedM() {
        boolean isWarmUp = true;
        int[] variedCount = {2, 3, 4, 5, 6, 7};
        for (int queryGraphCount : variedCount) {
            GraphGenerator.queryGraphCount = queryGraphCount;
            this.queryGraphs = GraphGenerator.generateRandomQueryGraph(dataGraph);
            for (MClosestEntitiesMatchingAlgorithm algorithm : testAlgorithms) {
                if (isWarmUp) {
                    List<Graph> currentRoundQueryGraphs = GraphGenerator.generateRandomQueryGraph(dataGraph);
                    long startTime = System.currentTimeMillis();
                    algorithm.query(dataGraph, currentRoundQueryGraphs);
                    long cost = System.currentTimeMillis() - startTime;
                    Console.log("{} warm up, cost: {}", algorithm.getClass().getSimpleName(), cost);
                } else {
                    long totalCost = 0;
                    for (int i = 0; i < EXPERIMENT_TIMES; ++i) {
                        List<Graph> currentRoundQueryGraphs = GraphGenerator.generateRandomQueryGraphWithSkip(dataGraph, i * queryGraphCount);
                        long startTime = System.currentTimeMillis();
                        algorithm.query(dataGraph, currentRoundQueryGraphs);
                        long cost = System.currentTimeMillis() - startTime;
                        Console.log("{} {} round cost: {}", algorithm.getClass().getSimpleName(), i + 1, cost);
                        totalCost += cost;
                    }
                    log.info("{}:{}", algorithm.getClass().getSimpleName(), Math.round(totalCost / (double) EXPERIMENT_TIMES));
                }
            }
            isWarmUp = false;
        }
    }

    private void conductEfficientTest() {
        boolean isWarmUp = true;
        final int queryGraphCountPerQuery = GraphGenerator.queryGraphCount;
        int[] variedQueryGraphSizes = {6, 8, 10, 12, 14, 16};
        for (int queryGraphSize : variedQueryGraphSizes) {
            GraphGenerator.queryGraphSize = queryGraphSize;
            for (MClosestEntitiesMatchingAlgorithm algorithm : testAlgorithms) {
                if (isWarmUp) {
                    List<Graph> currentRoundQueryGraphs = GraphGenerator.generateRandomQueryGraph(dataGraph);
                    long startTime = System.currentTimeMillis();
                    algorithm.query(dataGraph, currentRoundQueryGraphs);
                    long cost = System.currentTimeMillis() - startTime;
                    Console.log("{} warm up, cost: {}", algorithm.getClass().getSimpleName(), cost);
                } else {
                    long totalCost = 0;
                    for (int i = 0; i < EXPERIMENT_TIMES; ++i) {
                        List<Graph> currentRoundQueryGraphs = GraphGenerator.generateRandomQueryGraphWithSkip(dataGraph, i * queryGraphCountPerQuery);
                        long startTime = System.currentTimeMillis();
                        algorithm.query(dataGraph, currentRoundQueryGraphs);
                        long cost = System.currentTimeMillis() - startTime;
                        Console.log("{} {} round cost: {}", algorithm.getClass().getSimpleName(), i + 1, cost);
                        totalCost += cost;
                    }
                    log.info("{}:{}", algorithm.getClass().getSimpleName(), Math.round(totalCost / (double) EXPERIMENT_TIMES));
                }
            }
            isWarmUp = false;
        }
    }

    private void conductEfficientWithDataSizeVariedTest(String datasetName) {
        boolean isWarmUp = true;
        final int queryGraphCountPerQuery = GraphGenerator.queryGraphCount;
        for (double dataGraphSize : dataGraphSizes) {
            GlobalConfig.MAX_READ_LINE = (int) dataGraphSize;
            ExperimentDataset.init();
            this.dataGraph = ExperimentDataset.switchDataset(datasetName);
            for (MClosestEntitiesMatchingAlgorithm algorithm : testAlgorithms) {
                if (isWarmUp) {
                    List<Graph> currentRoundQueryGraphs = GraphGenerator.generateRandomQueryGraph(dataGraph);
                    long startTime = System.currentTimeMillis();
                    algorithm.query(dataGraph, currentRoundQueryGraphs);
                    long cost = System.currentTimeMillis() - startTime;
                    Console.log("{} warm up, cost: {}", algorithm.getClass().getSimpleName(), cost);
                } else {
                    long totalCost = 0;
                    for (int i = 0; i < EXPERIMENT_TIMES; ++i) {
                        List<Graph> currentRoundQueryGraphs = GraphGenerator.generateRandomQueryGraphWithSkip(dataGraph, i * queryGraphCountPerQuery);
                        long startTime = System.currentTimeMillis();
                        algorithm.query(dataGraph, currentRoundQueryGraphs);
                        long cost = System.currentTimeMillis() - startTime;
                        Console.log("{} {} round cost: {}", algorithm.getClass().getSimpleName(), i + 1, cost);
                        totalCost += cost;
                    }
                    log.info("{}:{}", algorithm.getClass().getSimpleName(), Math.round(totalCost / (double) EXPERIMENT_TIMES));
                }
            }
            isWarmUp = false;
        }
    }


    public void effective() {
        GlobalConfig.MAX_READ_LINE = (int) 1e6;
        ExperimentDataset.init();
        GraphGenerator.queryGraphCount = 3;
        GraphGenerator.queryGraphSize = 3;

        this.switchYelpDataSet();
        queryGraphs.clear();
        queryGraphs.add(generateQueryGraph3());
        queryGraphs.add(generateQueryGraph1());
        queryGraphs.add(generateQueryGraph2());
        this.conductCaseStudy();

        log.info("switching FS dataset");
        this.switchFoursquareDataSet();
        queryGraphs = GraphGenerator.generateRandomQueryGraph(dataGraph);
        this.conductCaseStudy();

        log.info("switching WD dataset");
        this.switchWikiDataSet();
        queryGraphs = GraphGenerator.generateRandomQueryGraph(dataGraph);
        this.conductCaseStudy();

        log.info("switching GW dataset");
        this.switchGowallaDataSet();
        queryGraphs.clear();
        queryGraphs.add(generateQueryGraph3());
        queryGraphs.add(generateQueryGraph1());
        queryGraphs.add(generateQueryGraph2());
        this.conductCaseStudy();

        log.info("switching BK dataset");
        this.switchBrightkiteDataSet();
        queryGraphs.clear();
        queryGraphs.add(generateQueryGraph3());
        queryGraphs.add(generateQueryGraph1());
        queryGraphs.add(generateQueryGraph2());
        this.conductCaseStudy();
    }

    SubgraphMatchingAlgorithm vc = new VC();

    private void conductCaseStudy() {
        final int m = GraphGenerator.queryGraphCount;
        int skip = 0;


        ExactMatchProcessor exactMatchProcessor = new ExactMatchProcessor(dataGraph, queryGraphs, vc);

//        do {
//            queryGraphs = GraphGenerator.generateRandomQueryGraphWithSkip(dataGraph, skip++);
//            exactMatchProcess = new ExactMatchProcess(dataGraph, queryGraphs, vc);
//        } while (checkIsSpecificAnswer(exactMatchProcess.candidateDataEntityVertexes));

//        for (Set<Vertex> exacts : exactMatchProcess.candidateDataEntityVertexes) {
//            System.out.println(exacts.size());
//            combinations *= exacts.size();
//        }

        List<List<Vertex>> candidates = exactMatchProcessor.candidateDataEntityVertexes.stream().
                map(ArrayList::new).collect(Collectors.toList());
//        for (int i = 0; i < m; ++i) {
//            log.info("Q{}:", i + 1);
//            for (Vertex vertex : candidates.get(i)) {
//                log.info("{}", dataGraph.locationMap.get(vertex));
//            }
//        }
        List<Vertex> selectedVertexes = new ArrayList<>(m);
        double randomSelectTotalDiameter = 0;
        for (int i = 0; i < 100; ++i) {
            selectedVertexes.clear();
            for (int j = 0; j < m; ++j) {
                List<Vertex> curQueryVertexes = candidates.get(j);
                selectedVertexes.add(curQueryVertexes.get(ThreadLocalRandom.current().nextInt(curQueryVertexes.size())));
            }
            double diameter = exactMatchProcessor.getGroupDiameter(selectedVertexes);
            randomSelectTotalDiameter += diameter;
            List<JSONObject> locations = selectedVertexes.stream()
                    .map(item -> dataGraph.locationMap.get(item))
                    .collect(Collectors.toList());
            log.info("Random Select, Round {}: #diameter:{}#locations:{}", i + 1, diameter, locations);
        }
        log.info("Random Select Average Diameter: {}", randomSelectTotalDiameter / 100);
        MatchGroup matchGroup = timeRatioStatisticsAlgorithm.query(dataGraph, queryGraphs);
        List<JSONObject> matchLocations = matchGroup.matchVertex.stream()
                .map(item -> dataGraph.locationMap.get(item))
                .collect(Collectors.toList());
        log.info("Query Answer: #diameter:{}#locations:{}", matchGroup.diameter, matchLocations);
        //exactMatchProcess.candidateDataEntityVertexes
    }

    private Graph generateQueryGraph1() {
        Graph queryGraph = new Graph();
        Vertex bank = new Vertex(LabelConst.ENTITY_LABEL);
        Vertex location = new Vertex(new JSONObject().toJSONString(), LabelConst.LOCATION_LABEL);
        Vertex category = new Vertex("Banks & Credit Unions");
        Vertex city = new Vertex("Gilbert");
        Vertex state = new Vertex("AZ");
        Vertex isOpen = new Vertex("1");
        //Vertex byAppointmentOnly = new Vertex("ByAppointmentOnly");

        queryGraph.addVertex(bank);
        //queryGraph.addVertex(city);
        //queryGraph.addVertex(state);
        queryGraph.addVertex(location);
        queryGraph.addVertex(category);
        queryGraph.addVertex(isOpen);
        //queryGraph.addVertex(byAppointmentOnly);

        queryGraph.addEdge(bank, location);
        queryGraph.addEdge(bank, category);
        queryGraph.addEdge(bank, isOpen);
        //queryGraph.addEdge(bank, city);
        //queryGraph.addEdge(bank, state);
        //queryGraph.addEdge(bank, byAppointmentOnly);

        return queryGraph;
    }

    private Graph generateQueryGraph2() {
        Graph queryGraph = new Graph();
        Vertex hotel = new Vertex(LabelConst.ENTITY_LABEL);
        Vertex location = new Vertex(new JSONObject().toJSONString(), LabelConst.LOCATION_LABEL);
        Vertex category = new Vertex("Hotels & Travel");
        //Vertex city = new Vertex("Gilbert");
        //Vertex state = new Vertex("AZ");
        Vertex stars = new Vertex("5.0");
        Vertex isOpen = new Vertex("1");

        queryGraph.addVertex(hotel);
        //queryGraph.addVertex(city);
        //queryGraph.addVertex(state);
        queryGraph.addVertex(location);
        queryGraph.addVertex(category);
        queryGraph.addVertex(category);
        queryGraph.addVertex(stars);
        queryGraph.addVertex(isOpen);

        queryGraph.addEdge(hotel, location);
        queryGraph.addEdge(hotel, category);
        queryGraph.addEdge(hotel, stars);
        queryGraph.addEdge(hotel, isOpen);
        //queryGraph.addEdge(hotel, city);
        //queryGraph.addEdge(hotel, state);

        return queryGraph;
    }

    private Graph generateQueryGraph3() {
        Graph queryGraph = new Graph();
        Vertex restaurant = new Vertex(LabelConst.ENTITY_LABEL);
        Vertex location = new Vertex(new JSONObject().toJSONString(), LabelConst.LOCATION_LABEL);
        Vertex category = new Vertex("Restaurants");
        Vertex category2 = new Vertex("Shopping");
        //Vertex city = new Vertex("Gilbert");
        //Vertex state = new Vertex("AZ");
        Vertex stars = new Vertex("5.0");
        Vertex isOpen = new Vertex("1");
        //Vertex businessAcceptsCreditCards = new Vertex("BusinessAcceptsCreditCards");

        queryGraph.addVertex(restaurant);
        //queryGraph.addVertex(city);
        //queryGraph.addVertex(state);
        queryGraph.addVertex(location);
        queryGraph.addVertex(category);
        queryGraph.addVertex(stars);
        queryGraph.addVertex(isOpen);
        queryGraph.addVertex(category2);
        //queryGraph.addVertex(businessAcceptsCreditCards);

        queryGraph.addEdge(restaurant, location);
        queryGraph.addEdge(restaurant, category);
        queryGraph.addEdge(restaurant, stars);
        queryGraph.addEdge(restaurant, isOpen);
        queryGraph.addEdge(restaurant, category2);
        //queryGraph.addEdge(restaurant, city);
        //queryGraph.addEdge(restaurant, state);
        //queryGraph.addEdge(restaurant, businessAcceptsCreditCards);

        return queryGraph;
    }

    private boolean checkIsSpecificAnswer(List<Set<Vertex>> candidates) {
        for (Set<Vertex> candidate : candidates) {
            if (candidate.size() < 3) return true;
        }
        return false;
    }

    private void indexEfficientTest() {
        this.switchYelpDataSet();
        this.queryGraphs = GraphGenerator.generateRandomQueryGraph(dataGraph);
        GlobalConfig.INDEX_TYPE = IndexType.BLOOM;
        long bloomCost = this.conductIndexEfficientTest();

        GlobalConfig.INDEX_TYPE = IndexType.TALE;
        this.conductIndexEfficientTest();
        long taleCost = this.conductIndexEfficientTest();
        log.info("{}-{}", bloomCost, taleCost);
    }

    private long conductIndexEfficientTest() {
        long startTime = System.currentTimeMillis();
        indexTestAlgorithm.query(dataGraph, queryGraphs);
        return System.currentTimeMillis() - startTime;
    }

    private void indexSpaceTest() {
        GlobalConfig.INDEX_TYPE = IndexType.BLOOM;
        this.switchYelpDataSet();
        long bloomIndexSize = GraphLayout.parseInstance(dataGraph.bloomIndex).totalSize();

        GlobalConfig.INDEX_TYPE = IndexType.TALE;
        this.switchYelpDataSet();
        long bloomIndexSize2 = GraphLayout.parseInstance(dataGraph.bloomIndex).totalSize();
        long bPlusTreeSize = GraphLayout.parseInstance(dataGraph.bPlusTree).totalSize();
        long tableIndexSize = bloomIndexSize2 + bPlusTreeSize;

        log.info("{}-{}", bloomIndexSize / 1e6, tableIndexSize / 1e6);
    }


    private void switchYelpDataSetWithSpecificReadLine() {
        //avoid OutOfMemoryError
        GlobalConfig.MAX_READ_LINE = (int) currentDataGraphSize;
        this.dataGraph = new Graph();
        System.gc();
        YelpImporter yelpImporter = new YelpImporter();
        yelpImporter.loadDataGraph(dataGraph);
        if (GlobalConfig.ENABLE_INDEX) {
            dataGraph.constructIndex();
        }
    }

    private void switchYelpDataSet() {
        this.dataGraph = ExperimentDataset.switchDataset(Dataset.YELP);
//        if (!datasets.containsKey(Dataset.YELP)) {
//            //avoid OutOfMemoryError
//            GlobalConfig.MAX_READ_LINE = (int) 1e6;
//            Graph graph = new Graph();
//            //System.gc();
//            YelpImporter2 yelpImporter = new YelpImporter2();
//            yelpImporter.loadDataGraph(graph);
//            if (GlobalConfig.ENABLE_INDEX) {
//                graph.constructIndex();
//            }
//            datasets.put(Dataset.YELP, graph);
//        }
//        this.dataGraph = datasets.get(Dataset.YELP);
    }

    private void switchWikiDataSet() {
        this.dataGraph = ExperimentDataset.switchDataset(Dataset.WIKIDATA);
//        if (!datasets.containsKey(Dataset.WIKIDATA)) {
//            //avoid OutOfMemoryError
//            GlobalConfig.MAX_READ_LINE = (int) 1e6;
//            Graph graph = new Graph();
//            //System.gc();
//            WikiImporter wikiImporter = new WikiImporter();
//            wikiImporter.loadDataGraph(graph);
//            if (GlobalConfig.ENABLE_INDEX) {
//                graph.constructIndex();
//            }
//            datasets.put(Dataset.WIKIDATA, graph);
//        }
//        this.dataGraph = datasets.get(Dataset.WIKIDATA);
    }

    private void switchFoursquareDataSet() {
        this.dataGraph = ExperimentDataset.switchDataset(Dataset.FOURSQUARE);
//        if (!datasets.containsKey(Dataset.FOURSQUARE)) {
//            //avoid OutOfMemoryError
//            GlobalConfig.MAX_READ_LINE = (int) 1e6;
//            Graph graph = new Graph();
//            //System.gc();
//            FoursquareImporter foursquareImporter = new FoursquareImporter();
//            foursquareImporter.loadDataGraph(graph);
//            if (GlobalConfig.ENABLE_INDEX) {
//                graph.constructIndex();
//            }
//            datasets.put(Dataset.FOURSQUARE, graph);
//        }
//        this.dataGraph = datasets.get(Dataset.FOURSQUARE);
    }

    private void switchGowallaDataSet() {
        this.dataGraph = ExperimentDataset.switchDataset(Dataset.GOWALLA);

//        if (!datasets.containsKey(Dataset.GOWALLA)) {
//            GlobalConfig.MAX_READ_LINE = (int) 1e4;
//            Graph graph = new Graph();
//            //System.gc();
//            GowallaImporter gowallaImporter = new GowallaImporter();
//            gowallaImporter.loadDataGraph(graph);
//            if (GlobalConfig.ENABLE_INDEX) {
//                graph.constructIndex();
//            }
//            datasets.put(Dataset.GOWALLA, graph);
//        }
//        this.dataGraph = datasets.get(Dataset.GOWALLA);
    }

    private void switchBrightkiteDataSet() {
        this.dataGraph = ExperimentDataset.switchDataset(Dataset.BRIGHTKITE);
//        if (!datasets.containsKey(Dataset.BRIGHTKITE)) {
//            GlobalConfig.MAX_READ_LINE = (int) 1e3;
//            Graph graph = new Graph();
//            //System.gc();
//            BrightkiteImporter brightkiteImporter = new BrightkiteImporter();
//            brightkiteImporter.loadDataGraph(graph);
//            if (GlobalConfig.ENABLE_INDEX) {
//                graph.constructIndex();
//            }
//            datasets.put(Dataset.BRIGHTKITE, graph);
//        }
//        this.dataGraph = datasets.get(Dataset.BRIGHTKITE);
    }

    public void datasetStatistics() {
        GlobalConfig.MAX_READ_LINE = (int) 1e6;
        ExperimentDataset.init();
        ExperimentDataset.datasetStatistics();
    }
}
