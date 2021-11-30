package edu.ecnu.aidadblab.spatial;

import edu.ecnu.aidadblab.constant.LabelConst;
import edu.ecnu.aidadblab.data.model.MatchGroup;
import edu.ecnu.aidadblab.data.model.Vertex;
import edu.ecnu.aidadblab.index.arcforest.ArcForest;
import edu.ecnu.aidadblab.index.arctree.ArcTree;
import edu.ecnu.aidadblab.index.arctree.ArcTreeLeafNode;
import org.junit.jupiter.api.Test;

public class ArcTreeAndArcForestTest {


    @Test
    public void correctTest() {
        ArcForest arcForest = new ArcForest(10);
        for (int j = 0; j < 10; ++j) {
            ArcTree arcTree = new ArcTree(new Vertex(LabelConst.ENTITY_LABEL));
            for (int i = 0; i < 20; i += 2) {
                MatchGroup matchGroup = new MatchGroup();
                matchGroup.diameter = i + j;
                arcTree.addLeafNode(new ArcTreeLeafNode(matchGroup));
            }
            arcTree.constructArcTree();
            System.out.println(arcTree.getGroupDiameter());
            arcForest.add(arcTree);
        }

        while (arcForest.isRemainCandidate()) {
            ArcTree arcTree = arcForest.peek();
            ArcTreeLeafNode arcTreeLeafNode = arcTree.getBestGroupArcTreeLeafNode();
            System.out.println(arcTreeLeafNode.getGroupDiameter());
            arcTreeLeafNode.delete();
            if (!arcTree.isRemainCandidates()) {
                arcForest.pop();
            } else {
                arcForest.update(arcTree);
            }
        }
    }

}
