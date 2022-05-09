package edu.ecnu.aidadblab.spatial;

import cn.hutool.core.lang.Console;
import com.alibaba.fastjson.JSONObject;
import edu.ecnu.aidadblab.algorithm.mcloest.entities.matching.MClosestEntitiesMatchingAlgorithm;
import edu.ecnu.aidadblab.algorithm.mcloest.entities.matching.impl.ExactEnumAlgorithm;
import edu.ecnu.aidadblab.algorithm.mcloest.entities.matching.impl.ExactScanAlgorithm;
import edu.ecnu.aidadblab.algorithm.mcloest.entities.matching.impl.FCircleScanAlgorithm;
import edu.ecnu.aidadblab.algorithm.mcloest.entities.matching.impl.NoArcFCircleScanAlgorithm;
import edu.ecnu.aidadblab.config.GlobalConfig;
import edu.ecnu.aidadblab.constant.LabelConst;
import edu.ecnu.aidadblab.constant.LocationComponent;
import edu.ecnu.aidadblab.data.model.Graph;
import edu.ecnu.aidadblab.data.model.MatchGroup;
import edu.ecnu.aidadblab.data.model.Vertex;
import edu.ecnu.aidadblab.importer.YelpImporter;
import edu.ecnu.aidadblab.processor.ExactCheckerProcessor;
import edu.ecnu.aidadblab.processor.FuzzyMatchProcessor;
import edu.ecnu.aidadblab.tool.GlobalData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CorrectnessTest {


    private final MClosestEntitiesMatchingAlgorithm exactEnum = new ExactEnumAlgorithm();

    private int queryGraphSize = 5;

    private int querGraphCount = 3;

    @Test
    public void CircleScanAlgorithmCorrectnessTest() {
        GlobalConfig.DEBUG = false;
        MClosestEntitiesMatchingAlgorithm circleScan = new ExactScanAlgorithm();
        for (int i = 1; i <= 1000; ++i) {
            Graph dataGraph = generateRandomDataGraph(ThreadLocalRandom.current().nextInt(10, 30));
            List<Graph> queryGraphs = generateRandomQueryGraph(dataGraph);
            MatchGroup exactEnumResult = exactEnum.query(dataGraph, queryGraphs);
            GlobalData.rightMatchGroup = exactEnumResult;
            MatchGroup circlesScanResult = circleScan.query(dataGraph, queryGraphs);
            Assertions.assertEquals(exactEnumResult.diameter, circlesScanResult.diameter);
        }
    }

    @Test
    public void F2ECorrectnessTest() {
        GlobalConfig.DEBUG = false;
        GlobalConfig.ENABLE_INDEX = false;
        MClosestEntitiesMatchingAlgorithm F2E = new FCircleScanAlgorithm();
        MClosestEntitiesMatchingAlgorithm NoArcF2E = new NoArcFCircleScanAlgorithm();
        for (int i = 1; i <= 1000; ++i) {
            Graph dataGraph = generateRandomDataGraph(ThreadLocalRandom.current().nextInt(10, 30));
            List<Graph> queryGraphs = generateRandomQueryGraph(dataGraph);
            MatchGroup exactEnumResult = exactEnum.query(dataGraph, queryGraphs);
            MatchGroup F2EResult = F2E.query(dataGraph, queryGraphs);
            MatchGroup NoArcF2EResult = NoArcF2E.query(dataGraph, queryGraphs);
            Assertions.assertEquals(exactEnumResult.diameter, F2EResult.diameter);
            Assertions.assertEquals(exactEnumResult.matchVertex.size(), F2EResult.matchVertex.size());
            Assertions.assertEquals(exactEnumResult.diameter, NoArcF2EResult.diameter);
            Assertions.assertEquals(exactEnumResult.matchVertex.size(), NoArcF2EResult.matchVertex.size());
        }
    }

    @Test
    public void mClosestCorrectnessTest() {
        GlobalConfig.DEBUG = false;
        for (int i = 1; i <= 10000; ++i) {
            Graph dataGraph = generateRandomDataGraph(ThreadLocalRandom.current().nextInt(10, 30));
            List<Graph> queryGraphs = generateRandomQueryGraph(dataGraph);
            Assertions.assertEquals(exactEnum.query(dataGraph, queryGraphs).diameter, exactEnum.query(dataGraph, queryGraphs).diameter);
            Assertions.assertEquals(exactEnum.query(dataGraph, queryGraphs).matchVertex.size(), exactEnum.query(dataGraph, queryGraphs).matchVertex.size());
        }
    }


    @Test
    public void exactMatchTest() {
        ExactCheckerProcessor exactCheckerProcessor = new ExactCheckerProcessor();
        for (int i = 1; i <= 20; ++i) {
            Graph dataGraph = generateRandomDataGraph(10 * i);
            List<Graph> queryGraphs = generateRandomQueryGraph(dataGraph);
            for (Graph queryGraph : queryGraphs) {
                boolean check = exactCheckerProcessor.check(queryGraph.entityVertexes.iterator().next(), dataGraph, queryGraph);
                Assertions.assertTrue(check);
            }
        }
    }

    @Test
    public void exactMatchTest2() {
        GlobalConfig.DEBUG = false;
        GlobalConfig.ENABLE_INDEX = false;
        for (int i = 1; i <= 20; ++i) {
            Graph dataGraph = generateRandomDataGraph(10 * i);
            List<Graph> queryGraphs = generateRandomQueryGraph(dataGraph);
            FuzzyMatchProcessor fuzzyMatchProcessor = new FuzzyMatchProcessor(dataGraph, queryGraphs);
            for (Graph queryGraph : queryGraphs) {
                boolean check = fuzzyMatchProcessor.checkExact(queryGraph.entityVertexes.iterator().next());
                Assertions.assertTrue(check);
            }
        }
    }

    private Graph generateRandomDataGraph(int size) {
        Graph dataGraph = new Graph();
        List<Vertex> vertexes = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
            Vertex entityVertex = new Vertex(LabelConst.ENTITY_LABEL);
            Vertex locationVertex = generateRandomLocationVertex();
            dataGraph.addVertex(entityVertex);
            dataGraph.addVertex(locationVertex);
            dataGraph.addEdge(entityVertex, locationVertex);
            for (int j = 0, len = size / 2; j < len; ++j) {
                Vertex vertex = new Vertex(Character.toString((char) ('A' + ThreadLocalRandom.current().nextInt(26))));
                vertexes.add(vertex);
                dataGraph.addVertex(vertex);
                dataGraph.addEdge(entityVertex, vertex);
            }
        }
        for (int i = 0; i < vertexes.size(); ++i) {
            for (int j = i + 1; j < vertexes.size(); ++j) {
                if (ThreadLocalRandom.current().nextDouble() < 0.2) {
                    dataGraph.addEdge(vertexes.get(i), vertexes.get(j));
                }
            }
        }
        return dataGraph;
    }

    private Vertex generateRandomLocationVertex() {
        JSONObject location = new JSONObject();
        location.put(LocationComponent.LATITUDE, ThreadLocalRandom.current().nextDouble(-90, 90));
        location.put(LocationComponent.LONGITUDE, ThreadLocalRandom.current().nextDouble(-180, 180));
        return new Vertex(location.toJSONString(), LabelConst.LOCATION_LABEL);
    }

    private List<Graph> generateRandomQueryGraph(Graph dataGraph) {
        List<Graph> randomQueryGraphs = new ArrayList<>();
        for (Vertex v : dataGraph.adjList.keySet()) {
            if (LabelConst.ENTITY_LABEL.equals(v.label)) {
                Graph queryGraph = new Graph();
                queryGraph.addVertex(v);
                dfs(v, dataGraph, queryGraph);
                if (hasLocationVertex(queryGraph)) {
                    randomQueryGraphs.add(queryGraph);
                }
            }

            if (randomQueryGraphs.size() == querGraphCount) {
                break;
            }
        }

        return randomQueryGraphs;
    }

    private void dfs(Vertex v, Graph dataGraph, Graph queryGraph) {

        for (Vertex u : dataGraph.getNeighbors(v)) {
            if (queryGraph.adjList.keySet().size() >= queryGraphSize) return;
            if (!queryGraph.hasVertex(u) && !LabelConst.ENTITY_LABEL.equals(u.label)) {
                queryGraph.addVertex(u);
                if (!queryGraph.hasEdge(v, u)) {
                    queryGraph.addEdge(v, u);
                }
                dfs(u, dataGraph, queryGraph);
            }
        }
    }

    private boolean hasLocationVertex(Graph queryGraph) {
        for (Vertex u : queryGraph.getNeighbors(queryGraph.entityVertexes.iterator().next())) {
            if (LabelConst.LOCATION_LABEL.equals(u.label)) return true;
        }
        return false;
    }

    private Graph loadDataGraph() {
        Graph dataGraph = new Graph();
        YelpImporter yelpImporter = new YelpImporter();
        System.out.println("============start loading data graph...============");
        long startTime = System.currentTimeMillis();
        yelpImporter.loadDataGraph(dataGraph);
        Console.log("============load data graph complete cost time {} s | data vertex number: {}============", (System.currentTimeMillis() - startTime) / 1000, dataGraph.adjList.size());
        return dataGraph;
    }
}
