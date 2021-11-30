package edu.ecnu.aidadblab.algorithm;

import edu.ecnu.aidadblab.algorithm.subgraph.matching.SubgraphMatchingAlgorithm;
import edu.ecnu.aidadblab.algorithm.subgraph.matching.imlp.DirectEnum;
import edu.ecnu.aidadblab.algorithm.subgraph.matching.imlp.VC;
import edu.ecnu.aidadblab.config.GlobalConfig;
import edu.ecnu.aidadblab.data.TestGraph;
import edu.ecnu.aidadblab.data.model.Graph;
import edu.ecnu.aidadblab.data.model.Match;
import edu.ecnu.aidadblab.data.model.Vertex;
import io.netty.util.internal.ThreadLocalRandom;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SubgraphMatching {

    @Test
    public void correctnessTest() {
        GlobalConfig.DEBUG = false;
        SubgraphMatchingAlgorithm vc = new VC();
        SubgraphMatchingAlgorithm violent = new DirectEnum();
        TestGraph testGraph = new TestGraph();
        Assertions.assertEquals(vc.match(testGraph.loadTestDataGraph1(), testGraph.loadTestQueryGraph1()), violent.match(testGraph.loadTestDataGraph1(), testGraph.loadTestQueryGraph1()));
        Assertions.assertEquals(vc.match(testGraph.loadTestDataGraph2(), testGraph.loadTestQueryGraph2()), violent.match(testGraph.loadTestDataGraph2(), testGraph.loadTestQueryGraph2()));
        Assertions.assertEquals(vc.match(testGraph.loadTestDataGraph3(), testGraph.loadTestQueryGraph3()), violent.match(testGraph.loadTestDataGraph3(), testGraph.loadTestQueryGraph3()));
        for (int i = 1; i <= 50; ++i) {
            Graph dataGraph = generateRandomDataGraph(10 * i);
            Graph queryGraph = generateRandomQueryGraph(dataGraph);
            Set<Match> result1 = vc.match(dataGraph, queryGraph);
            Set<Match> result2 = violent.match(dataGraph, queryGraph);
//            System.out.println(result1);
//            System.out.println(result2);
            Assertions.assertEquals(result1, result2);
        }

    }


    private Graph generateRandomDataGraph(int size) {
        Graph dataGraph = new Graph();
        List<Vertex> vertexes = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
            Vertex vertex = new Vertex(Character.toString((char) ('A' + ThreadLocalRandom.current().nextInt(26))));
            vertexes.add(vertex);
            dataGraph.addVertex(vertex);
        }
        for (int i = 0; i < vertexes.size(); ++i) {
            for (int j = i + 1; j < vertexes.size(); ++j) {
                if (ThreadLocalRandom.current().nextDouble() < 0.5) {
                    dataGraph.addEdge(vertexes.get(i), vertexes.get(j));
                }
            }
        }
        return dataGraph;
    }

    public Graph generateRandomQueryGraph(Graph dataGraph) {
        Graph queryGraph = new Graph();
        Vertex v = dataGraph.adjList.keySet().stream().findFirst().orElseThrow(() -> new IllegalArgumentException("data graph can not be empty"));
        queryGraph.addVertex(v);
        dfs(v, dataGraph, queryGraph);
        return queryGraph;
    }

    private void dfs(Vertex v, Graph dataGraph, Graph queryGraph) {

        for (Vertex u : dataGraph.getNeighbors(v)) {
            if (queryGraph.adjList.keySet().size() > 5) return;
            if (!queryGraph.hasVertex(u)) {
                queryGraph.addVertex(u);
                if (!queryGraph.hasEdge(v, u)) {
                    queryGraph.addEdge(v, u);
                }
                dfs(u, dataGraph, queryGraph);
            }
        }
    }

}
