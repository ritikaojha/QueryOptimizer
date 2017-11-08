/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.osu.queryopt;
import edu.osu.queryopt.entity.NodeStructure;
import java.util.LinkedList;
/**
 * NOT TESTED!!!
 * @author kathy
 */
public class JoinOrderOptimizer {
    static LinkedList<NodeStructure> produceJoinPlan(LinkedList<NodeStructure> subtrees){
        LinkedList<NodeStructure> result = new LinkedList<>();
        //find cardinality of subtree
        if(subtrees.size() == 1)
            result.add(subtrees.peek());
        else if (subtrees.size() == 2){
            //merge into one subtree
            NodeStructure newTree = new NodeStructure("JOIN");
            newTree.children.addAll(subtrees);
            newTree.UpdateSize();
            result.add(newTree);
        } else {
            //remove first nodestruct
            LinkedList<NodeStructure> subQuery = new LinkedList<>();
            LinkedList<NodeStructure> checked = new LinkedList<>();
            
            NodeStructure ns = subtrees.getFirst();
            subQuery.add(ns);
            subQuery.add(subtrees.get(1));
            result = produceJoinPlan(subQuery);
            int i = 2;
            //while subtrees.size > 0, remove next nodestruct and merge to one subtree
            while(i < subtrees.size()){
                subQuery.remove(subtrees.get(i - 1));
                subQuery.add(subtrees.get(i));
                LinkedList<NodeStructure> tempResult = produceJoinPlan(subQuery);
                if(tempResult.peek().selectivity < result.peek().selectivity)
                    result = tempResult;
                else
                    checked.add(tempResult.getFirst());
                i--;
            }
            //track nodestruct w/ best cost
            //plan = best cost nodestruct + rest of subtrees
            result.addAll(checked);
            result = produceJoinPlan(result);
        }
        return result;
    }
}
