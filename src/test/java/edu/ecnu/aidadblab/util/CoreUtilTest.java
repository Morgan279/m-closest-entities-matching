package edu.ecnu.aidadblab.util;

import cn.hutool.core.lang.Console;
import edu.ecnu.aidadblab.data.model.Graph;
import edu.ecnu.aidadblab.data.model.Vertex;
import edu.ecnu.aidadblab.processor.CoreProcessor;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class CoreUtilTest {

    @Test
    public void test() {
        Graph graph = new Graph();
        Vertex v1 = new Vertex("v1", "A");
        Vertex v2 = new Vertex("v2", "B");
        Vertex v3 = new Vertex("v3", "C");
        Vertex v4 = new Vertex("v4", "A");
        Vertex v5 = new Vertex("v5", "D");
        graph.addVertex(v1);
        graph.addVertex(v2);
        graph.addVertex(v3);
        graph.addVertex(v4);
        graph.addVertex(v5);
        graph.addEdge(v1, v2);
        graph.addEdge(v2, v3);
        graph.addEdge(v3, v4);
        graph.addEdge(v4, v5);
        graph.addEdge(v5, v2);
        CoreProcessor coreProcessor = new CoreProcessor();
        Map<Vertex, Integer> coreMap = coreProcessor.getCoreValueMap(graph);
        for (Vertex vertex : coreMap.keySet()) {
            Console.log("vertex: {} core: {}", vertex.id, coreMap.get(vertex));
        }

    }
}
