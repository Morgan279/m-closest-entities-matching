package edu.ecnu.aidadblab.algorithm.subgraph.matching.imlp;

import cn.hutool.core.lang.Console;
import edu.ecnu.aidadblab.config.GlobalConfig;
import edu.ecnu.aidadblab.data.model.Graph;
import edu.ecnu.aidadblab.importer.YelpImporter;
import org.junit.jupiter.api.Test;

public class VCTest {

    private Graph loadDataGraph() {
        Graph dataGraph = new Graph();
        YelpImporter yelpImporter = new YelpImporter();
        System.out.println("============start loading data graph...============");
        long startTime = System.currentTimeMillis();
        yelpImporter.loadDataGraph(dataGraph);
        Console.log("============load data graph complete cost time {} s | data vertex number: {}============", (System.currentTimeMillis() - startTime) / 1000, dataGraph.adjList.size());
//        int cnt = 0;
//        for (Vertex u : dataGraph.adjList.keySet()) {
//            if (LabelConst.ENTITY_LABEL.equals(u.label)) {
//                ++cnt;
//            }
//        }
//        Console.log("index: {} data graph: {}", oneHopIndex.getEntityVertexes().size(), cnt);
//        System.out.println("label count: " + oneHopIndex.getLabelCount());
        if (GlobalConfig.ENABLE_INDEX) {
            System.out.println("============start constructIndex...============");
            startTime = System.currentTimeMillis();
            dataGraph.constructIndex();
        }
        Console.log("============constructIndex complete, cost time {} ms============", (System.currentTimeMillis() - startTime));
        return dataGraph;
    }


    @Test
    public void testMatch() {
        GlobalConfig.MAX_READ_LINE = (int) 1e5;
        GlobalConfig.DEBUG = false;
        GlobalConfig.ENABLE_INDEX = true;

        Graph dataGraph = loadDataGraph();
        ExperimentHelper experimentHelper = new ExperimentHelper(dataGraph);
        experimentHelper.startExperiment();
    }


}
