package edu.ecnu.aidadblab.processor;

import cn.hutool.core.lang.Console;
import com.alibaba.fastjson.JSONObject;
import edu.ecnu.aidadblab.config.GlobalConfig;
import edu.ecnu.aidadblab.constant.IndexType;
import edu.ecnu.aidadblab.constant.LabelConst;
import edu.ecnu.aidadblab.data.model.*;
import edu.ecnu.aidadblab.index.bplustree.BPlusTreeEntry;
import edu.ecnu.aidadblab.tool.Filter;
import edu.ecnu.aidadblab.util.SpatialUtil;
import lombok.Getter;

import java.util.*;

public class FuzzyMatchProcessor {

    private Graph dataGraph;

    private int QUERY_NUM;

    private List<List<Vertex>> candidateDataEntityVertexes;

    private List<ReduceMap> reduceMaps;

    private Map<Vertex, JSONObject> locationMap;

    private Map<Vertex, Boolean> exactMatchCache;

    private Map<CheckCacheKey, Boolean> checkCache = new HashMap<>();

    private Set<Vertex> visitedGVertex;

    private Map<Vertex, Vertex> pivotMap;

    private Filter filter;

    private Vertex[] matchingOrder;

    private int QUERY_VERTEX_NUM;

    ExactCheckerProcessor exactCheckerProcessor = new ExactCheckerProcessor();

    @Getter
    private FuzzyMatchIntermediate fuzzyMatchIntermediate;

    private boolean exactMatchResult;

    public FuzzyMatchProcessor(Graph dataGraph, List<Graph> queryGraphs) {
        if (GlobalConfig.ENABLE_INDEX) {
            switch (GlobalConfig.INDEX_TYPE) {
                case IndexType.BLOOM:
                    this.init2(dataGraph, queryGraphs);
                    break;
                case IndexType.TALE:
                    this.init3(dataGraph, queryGraphs);
                    break;
                default:
                    throw new IllegalArgumentException("illegal index type");
            }
        } else {
            this.init(dataGraph, queryGraphs);
        }

    }

    public boolean checkExact(Vertex v) {
        return checkExact(v, fuzzyMatchIntermediate.structTagMap.get(v));
    }

    public boolean checkExact(Vertex v, List<Integer> tags) {
        for (int tag : tags) {
            Graph queryGraph = reduceMaps.get(tag).reducedQueryGraph;
            CheckCacheKey checkCacheKey = new CheckCacheKey(v, tag);
            if (!checkCache.containsKey(checkCacheKey)) {
                checkCache.put(checkCacheKey, exactCheckerProcessor.check(v, dataGraph, queryGraph));
            }
            if (checkCache.get(checkCacheKey)) {
                return true;
            }
        }

        return false;
    }

    public boolean checkAllExact(Vertex v, List<Integer> tags) {
        for (int tag : tags) {
            Graph queryGraph = reduceMaps.get(tag).reducedQueryGraph;
            CheckCacheKey checkCacheKey = new CheckCacheKey(v, tag);
            if (!checkCache.containsKey(checkCacheKey)) {
                checkCache.put(checkCacheKey, exactCheckerProcessor.check(v, dataGraph, queryGraph));
            }
            if (!checkCache.get(checkCacheKey)) {
                return false;
            }
        }

        return true;
    }

    public boolean checkExact(Vertex v, int tag) {
        Graph queryGraph = reduceMaps.get(tag).reducedQueryGraph;
        CheckCacheKey checkCacheKey = new CheckCacheKey(v, tag);
        if (!checkCache.containsKey(checkCacheKey)) {
            checkCache.put(checkCacheKey, exactCheckerProcessor.check(v, dataGraph, queryGraph));
        }
        return checkCache.get(checkCacheKey);
    }

    public boolean exactMatch(Vertex v) {
        if (exactMatchCache.containsKey(v)) {
            return exactMatchCache.get(v);
        }

        this.exactMatchResult = false;
        ReduceMap reduceMap = reduceMaps.get(0);
        QUERY_VERTEX_NUM = reduceMap.reducedQueryGraph.adjList.keySet().size();
        pivotMap.clear();
        visitedGVertex.clear();
        matchingOrder = generateMatchingOrder(reduceMap);
        filter = new Filter(dataGraph, reduceMap.reducedQueryGraph, matchingOrder);


        int level = 0;
        Vertex u = matchingOrder[0];
        Match curMatch = new Match();
        curMatch.add(u, v);
        visitedGVertex.add(v);
        enumerate(curMatch, level + 1);
        exactMatchCache.put(v, exactMatchResult);
        return exactMatchResult;
    }

    private void enumerate(Match curMatch, int level) {
        if (level == QUERY_VERTEX_NUM) {
            exactMatchResult = true;
            return;
        }

        Vertex u = matchingOrder[level];
        for (Vertex v : dataGraph.getNeighbors(curMatch.get(pivotMap.get(u)))) {
            if (exactMatchResult) break;

            if (!visitedGVertex.contains(v) && filter.checkCondition(curMatch, level, v)) {
                curMatch.add(u, v);
                visitedGVertex.add(v);
                enumerate(curMatch, level + 1);
                visitedGVertex.remove(v);
                curMatch.remove(u);
            }
        }
    }

    private Vertex[] generateMatchingOrder(ReduceMap reduceMap) {
        final int N = reduceMap.reducedQueryGraph.adjList.size();
        Vertex[] matchingOrder = new Vertex[N];
        int cnt = 0;
        Vertex u = reduceMap.queryEntityVertex;
        Queue<Vertex> queue = new LinkedList<>();
        Set<Vertex> vis = new HashSet<>(N);
        queue.add(u);
        while (!queue.isEmpty()) {
            Vertex next = queue.poll();
            vis.add(next);
            matchingOrder[cnt++] = next;
            for (Vertex neighbor : reduceMap.reducedQueryGraph.getNeighbors(next)) {
                if (!vis.contains(neighbor)) {
                    queue.add(neighbor);
                    pivotMap.put(neighbor, next);
                }
            }
        }
        return matchingOrder;
    }

    public MatchGroup findFeasibleSolution() {
        double curMinRadius = -1;
        List<Vertex> solutionVertexes = new ArrayList<>(QUERY_NUM);
        Vertex vStar = findFirstExactMatchEntityVertex();
        solutionVertexes.add(vStar);
        for (int i = 1; i < QUERY_NUM; ++i) {
            Vertex u = findNearestEntityVertex(vStar, i);
            //Console.log("finding solution {}", i);
            solutionVertexes.add(u);
            for (int j = 0; j < i; ++j) {
                Vertex v = solutionVertexes.get(j);
                double curDistance = calculateDistance(u, v);
                curMinRadius = Math.max(curMinRadius, curDistance);
            }
        }

        return new MatchGroup(solutionVertexes, curMinRadius);
    }

    private void init(Graph dataGraph, List<Graph> queryGraphs) {
        this.dataGraph = dataGraph;
        this.QUERY_NUM = queryGraphs.size();
        this.candidateDataEntityVertexes = new ArrayList<>(QUERY_NUM);
        this.reduceMaps = new ArrayList<>();
        this.exactMatchCache = new HashMap<>();
        this.pivotMap = new HashMap<>();
        this.visitedGVertex = new HashSet<>();
        List<Vertex> vertexList = new ArrayList<>();
        Map<Vertex, List<Integer>> structTagMap = new HashMap<>();
        for (Graph queryGraph : queryGraphs) {
            ReduceMap reduceMap = generateReduceMap(queryGraph);
            reduceMaps.add(reduceMap);
            candidateDataEntityVertexes.add(reduceMap.dataEntityVertexes);
            vertexList.addAll(reduceMap.dataEntityVertexes);
        }
        candidateDataEntityVertexes.sort(Comparator.comparingInt(List::size));
        reduceMaps.sort(Comparator.comparingInt(m -> m.dataEntityVertexes.size()));
        for (int i = 0; i < QUERY_NUM; ++i) {
            for (Vertex entityVertex : candidateDataEntityVertexes.get(i)) {
                if (!structTagMap.containsKey(entityVertex)) {
                    structTagMap.put(entityVertex, new ArrayList<>());
                }
                structTagMap.get(entityVertex).add(i);
            }
        }

        this.locationMap = dataGraph.locationMap;
        this.fuzzyMatchIntermediate = new FuzzyMatchIntermediate(candidateDataEntityVertexes, reduceMaps, locationMap, structTagMap, vertexList);
    }

    private boolean hashValid(List<Long> queryHashKeys, List<Long> dataHashKeys) {
        for (int i = 0; i < queryHashKeys.size(); ++i) {
            long queryHash = queryHashKeys.get(i);
            if ((dataHashKeys.get(i) & queryHash) != queryHash) return false;
        }
        return true;
    }

    private void init2(Graph dataGraph, List<Graph> queryGraphs) {
        this.dataGraph = dataGraph;
        this.QUERY_NUM = queryGraphs.size();
        this.candidateDataEntityVertexes = new ArrayList<>(QUERY_NUM);
        this.reduceMaps = new ArrayList<>();
        this.exactMatchCache = new HashMap<>();
        this.pivotMap = new HashMap<>();
        this.visitedGVertex = new HashSet<>();
        List<Vertex> vertexList = new ArrayList<>();
        Map<Vertex, List<Integer>> structTagMap = new HashMap<>();
        for (Graph queryGraph : queryGraphs) {
            Vertex queryEntity = queryGraph.entityVertexes.iterator().next();
            List<Long> hashKeys = queryGraph.getEntityVertexHashKeys(queryEntity);
            List<Vertex> candidates = new ArrayList<>();
            for (Vertex v : dataGraph.bloomIndex.keySet()) {
                if (hashValid(hashKeys, dataGraph.bloomIndex.get(v))) {
                    candidates.add(v);
                }
            }

            candidateDataEntityVertexes.add(candidates);
            reduceMaps.add(new ReduceMap(queryGraph, queryEntity, candidates));
            vertexList.addAll(candidates);
        }
        candidateDataEntityVertexes.sort(Comparator.comparingInt(List::size));
        reduceMaps.sort(Comparator.comparingInt(m -> m.dataEntityVertexes.size()));
        for (int i = 0; i < QUERY_NUM; ++i) {
            for (Vertex entityVertex : candidateDataEntityVertexes.get(i)) {
                if (!structTagMap.containsKey(entityVertex)) {
                    structTagMap.put(entityVertex, new ArrayList<>());
                }
                structTagMap.get(entityVertex).add(i);
            }
        }

        this.locationMap = dataGraph.locationMap;
        this.fuzzyMatchIntermediate = new FuzzyMatchIntermediate(candidateDataEntityVertexes, reduceMaps, locationMap, structTagMap, vertexList);
    }


    private void init3(Graph dataGraph, List<Graph> queryGraphs) {
        this.dataGraph = dataGraph;
        this.QUERY_NUM = queryGraphs.size();
        this.candidateDataEntityVertexes = new ArrayList<>(QUERY_NUM);
        this.reduceMaps = new ArrayList<>();
        this.exactMatchCache = new HashMap<>();
        this.pivotMap = new HashMap<>();
        this.visitedGVertex = new HashSet<>();
        List<Vertex> vertexList = new ArrayList<>();
        Map<Vertex, List<Integer>> structTagMap = new HashMap<>();
        for (Graph queryGraph : queryGraphs) {
            Vertex queryEntity = queryGraph.entityVertexes.iterator().next();
            List<Long> hashKeys = queryGraph.getEntityVertexHashKeys(queryEntity);
            BPlusTreeEntry searchEntry = new BPlusTreeEntry(queryGraph.getDegree(queryEntity), queryGraph.getNeighborConnection(queryEntity));
            List<Vertex> candidates = new ArrayList<>();
            for (Vertex v : dataGraph.bPlusTree.search(searchEntry)) {
                if (hashValid(hashKeys, dataGraph.bloomIndex.get(v))) {
                    candidates.add(v);
                }
            }
            candidateDataEntityVertexes.add(candidates);
            reduceMaps.add(new ReduceMap(queryGraph, queryEntity, candidates));
            vertexList.addAll(candidates);
        }
        candidateDataEntityVertexes.sort(Comparator.comparingInt(List::size));
        reduceMaps.sort(Comparator.comparingInt(m -> m.dataEntityVertexes.size()));
        for (int i = 0; i < QUERY_NUM; ++i) {
            for (Vertex entityVertex : candidateDataEntityVertexes.get(i)) {
                if (!structTagMap.containsKey(entityVertex)) {
                    structTagMap.put(entityVertex, new ArrayList<>());
                }
                structTagMap.get(entityVertex).add(i);
            }
        }

        this.locationMap = dataGraph.locationMap;
        this.fuzzyMatchIntermediate = new FuzzyMatchIntermediate(candidateDataEntityVertexes, reduceMaps, locationMap, structTagMap, vertexList);
    }

    private ReduceMap generateReduceMap(Graph queryGraph) {
        Vertex queryEntityVertex = queryGraph.entityVertexes.iterator().next();
        List<Vertex> dataEntityVertexes = new ArrayList<>();

        long startTime = System.currentTimeMillis();
        for (Vertex entityVertex : dataGraph.entityVertexes) {
            if (checkOneHop(entityVertex, queryEntityVertex, queryGraph, dataGraph)) {
                dataEntityVertexes.add(entityVertex);
            }
        }

        if (GlobalConfig.DEBUG) {
            Console.log("check one hop cost: {}", System.currentTimeMillis() - startTime);
        }

        Graph reduceQueryGraph = queryGraph.clone();
        for (Iterator<Vertex> it = reduceQueryGraph.adjList.keySet().iterator(); it.hasNext(); ) {
            Vertex v = it.next();
            if (reduceQueryGraph.getDegree(v) == 1 && reduceQueryGraph.hasEdge(queryEntityVertex, v) && !LabelConst.LOCATION_LABEL.equals(v.label)) {
                it.remove();
                reduceQueryGraph.getNeighbors(queryEntityVertex).remove(v);
            }
        }

        return new ReduceMap(reduceQueryGraph, queryEntityVertex, dataEntityVertexes);
    }

    private boolean checkOneHop(Vertex dataEntity, Vertex queryEntity, Graph queryGraph, Graph dataGraph) {
        List<Vertex> queryNeighbors = queryGraph.getNeighbors(queryEntity);
        List<Vertex> dataNeighbors = dataGraph.getNeighbors(dataEntity);
        if (dataNeighbors.size() < queryNeighbors.size()) return false;

        Set<String> labels = new HashSet<>();
        Set<String> matched = new HashSet<>();
        for (Vertex queryNeighbor : queryNeighbors) {
            labels.add(queryNeighbor.label);
        }

        for (Vertex entityNeighbor : dataGraph.getNeighbors(dataEntity)) {
            if (labels.contains(entityNeighbor.label)) {
                matched.add(entityNeighbor.label);
                if (matched.size() == labels.size()) return true;
            }

        }

        return false;
    }


    private Vertex findNearestEntityVertex(Vertex v, int index) {
        double minDistance = Double.MAX_VALUE;
        Vertex res = null;
        for (Vertex u : candidateDataEntityVertexes.get(index)) {
            double curDistance = calculateDistance(u, v);
            if (curDistance < minDistance && checkExact(u, index)) {
                minDistance = curDistance;
                res = u;
            }
        }
        if (res == null) {
            throw new IllegalArgumentException("there is no validate vertex. Please check if the index is working properly");
        }
        return res;
    }

    private Vertex findFirstExactMatchEntityVertex() {
        for (Vertex u : candidateDataEntityVertexes.get(0)) {
            if (checkExact(u, 0)) {
                return u;
            }
        }
        throw new IllegalArgumentException("there is no validate vertex. Please check if the index is working properly");
    }

    public double calculateDistance(Vertex u, Vertex v) {
        return SpatialUtil.calculateDistance(locationMap.get(u), locationMap.get(v));
    }
}
