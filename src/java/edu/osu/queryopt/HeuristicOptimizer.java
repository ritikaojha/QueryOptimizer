/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.osu.queryopt;
import edu.osu.queryopt.entity.NodeStructure;
import edu.osu.queryopt.entity.NodeStructure.NodeType;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author kathy
 */
public class HeuristicOptimizer {
    static List<NodeStructure> Optimize(NodeStructure nodeStruct){
        List<NodeStructure> result = new ArrayList<>();
        result.addAll(CascadeSelect(nodeStruct));
        result.addAll(CascadeProject(result.get(result.size()-1)));
        result.addAll(CommuteSelect("C.X = 0", result.get(result.size()-1)));
        return result;
    }
    
    private static List<NodeStructure> CascadeSelect(NodeStructure nodeStruct){
        List<NodeStructure> result = new ArrayList<>();
        result.add(nodeStruct);
        if(!nodeStruct.children.isEmpty()){
            if(nodeStruct.nodeType.equals(NodeType.Select) && nodeStruct.NumConditions() > 1){
                for (int i = 0; i < nodeStruct.NumConditions(); i++){
                    NodeStructure temp = new NodeStructure(NodeType.Select);
                    String cond = nodeStruct.GetCondition(i);
                    temp.AddCondition(cond);
                    temp.children.add(result.get(result.size()-1));
                    temp.NodeToString();
                    result.add(temp);
                }
            }
            for (int i = 0; i < nodeStruct.children.size(); i++){
                List<NodeStructure> cascadeSelect = CascadeSelect(nodeStruct.children.remove(i));
                nodeStruct.children.add(i, cascadeSelect.get(cascadeSelect.size() - 1));
            }
        }
        return result;
    }
    private static List<NodeStructure> CascadeProject(NodeStructure nodeStruct){
        List<NodeStructure> result = new ArrayList<>();
        result.add(nodeStruct);
        if(!nodeStruct.children.isEmpty()){
            if(nodeStruct.nodeType.equals(NodeType.Project)){
                NodeStructure nextChild = nodeStruct.children.remove(0);
                while(nextChild.nodeType.equals(NodeType.Project)){
                    nextChild = nextChild.children.remove(0);
                }
                nodeStruct.children.add(0, nextChild);
            }
            for (int i = 0; i < nodeStruct.children.size(); i++){
                List<NodeStructure> cascadeProject = CascadeProject(nodeStruct.children.remove(i));
                nodeStruct.children.add(i, cascadeProject.get(cascadeProject.size() - 1));
            }
        }
        return result;
    }
    //Pushes down select with condition by commuting between selections
    private static List<NodeStructure> CommuteSelect(String condition, NodeStructure nodeStruct){
        List<NodeStructure> result = new ArrayList<>();
        result.add(nodeStruct);
        if(nodeStruct.nodeType.equals(NodeType.Select) && nodeStruct.HasCondition(condition)){
            NodeStructure targetSelect = nodeStruct;
            NodeStructure temp = targetSelect.children.remove(0);
            targetSelect.children.add(temp.children.remove(0));
            temp.children.add(targetSelect);
            result.add(temp);
        } else {
            for (int i = 0; i < nodeStruct.children.size(); i++){
                List<NodeStructure> commuteSelect = CommuteSelect(condition, nodeStruct.children.remove(i));
                nodeStruct.children.add(i, commuteSelect.get(commuteSelect.size() - 1));
            }
        }
        return result;
    }
}
