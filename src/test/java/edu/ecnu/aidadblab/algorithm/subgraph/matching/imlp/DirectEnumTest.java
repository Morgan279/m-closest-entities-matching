package edu.ecnu.aidadblab.algorithm.subgraph.matching.imlp;

import edu.ecnu.aidadblab.algorithm.subgraph.matching.SubgraphMatchingAlgorithm;
import edu.ecnu.aidadblab.data.TestGraph;
import org.junit.jupiter.api.Test;

class DirectEnumTest {

    @Test
    public void test() {
        SubgraphMatchingAlgorithm subgraphMatchingAlgorithm = new DirectEnum();
        TestGraph testGraph = new TestGraph();
        System.out.println(subgraphMatchingAlgorithm.match(testGraph.loadTestDataGraph3(), testGraph.loadTestQueryGraph3()));
    }

}
