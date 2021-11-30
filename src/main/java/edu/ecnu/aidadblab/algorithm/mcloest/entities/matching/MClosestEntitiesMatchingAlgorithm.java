package edu.ecnu.aidadblab.algorithm.mcloest.entities.matching;

import edu.ecnu.aidadblab.data.model.Graph;
import edu.ecnu.aidadblab.data.model.MatchGroup;

import java.util.List;

public interface MClosestEntitiesMatchingAlgorithm {

    MatchGroup query(Graph dataGraph, List<Graph> queryGraphs);

}
