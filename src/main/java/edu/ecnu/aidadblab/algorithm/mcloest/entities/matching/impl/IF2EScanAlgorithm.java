package edu.ecnu.aidadblab.algorithm.mcloest.entities.matching.impl;

import edu.ecnu.aidadblab.algorithm.mcloest.entities.matching.MClosestEntitiesMatchingAlgorithm;
import edu.ecnu.aidadblab.config.GlobalConfig;
import edu.ecnu.aidadblab.data.model.Graph;
import edu.ecnu.aidadblab.data.model.MatchGroup;

import java.util.List;

public class IF2EScanAlgorithm implements MClosestEntitiesMatchingAlgorithm {

    private final FCircleScanAlgorithm fCircleScanAlgorithm = new FCircleScanAlgorithm();

    @Override
    public MatchGroup query(Graph dataGraph, List<Graph> queryGraphs) {
        GlobalConfig.ENABLE_INDEX = true;
        return fCircleScanAlgorithm.query(dataGraph, queryGraphs);
    }
}
