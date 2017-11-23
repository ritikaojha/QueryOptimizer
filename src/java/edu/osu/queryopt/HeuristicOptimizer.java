/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.osu.queryopt;
import edu.osu.queryopt.entity.NodeStructure;
import edu.osu.queryopt.entity.NodeStructure.NodeType;
/**
 *
 * @author kathy
 */
public class HeuristicOptimizer {
    static NodeStructure Optimize(NodeStructure nodeStruct){
        NodeStructure result;
        result = CascadeSelect(nodeStruct);
        result = CascadeProject(result);
        result = CommuteSelect("C.X = 0", result);
        return result;
    }
    
    private static NodeStructure CascadeSelect(NodeStructure nodeStruct){
        NodeStructure result = nodeStruct;
        if(!nodeStruct.children.isEmpty()){
            if(nodeStruct.nodeType.equals(NodeType.Select) && nodeStruct.NumConditions() > 1){
                result = nodeStruct.children.get(0);
                for (int i = 0; i < nodeStruct.NumConditions(); i++){
                    NodeStructure temp = new NodeStructure(NodeType.Select);
                    String cond = nodeStruct.GetCondition(i);
                    temp.AddCondition(cond);
                    temp.children.add(result);
                    temp.NodeToString();
                    result = temp;
                }
            }
            for (int i = 0; i < nodeStruct.children.size(); i++){
                nodeStruct.children.add(i, CascadeSelect(nodeStruct.children.remove(i)));
            }
        }
        return result;
    }
    private static NodeStructure CascadeProject(NodeStructure nodeStruct){
        NodeStructure result = nodeStruct;
        if(!nodeStruct.children.isEmpty()){
            if(nodeStruct.nodeType.equals(NodeType.Project)){
                NodeStructure nextChild = nodeStruct.children.remove(0);
                while(nextChild.nodeType.equals(NodeType.Project)){
                    nextChild = nextChild.children.remove(0);
                }
                nodeStruct.children.add(0, nextChild);
            }
            for (int i = 0; i < nodeStruct.children.size(); i++){
                nodeStruct.children.add(i, CascadeProject(nodeStruct.children.remove(i)));
            }
        }
        return result;
    }
    //Pushes down select with condition by commuting between selections
    private static NodeStructure CommuteSelect(String condition, NodeStructure nodeStruct){
        NodeStructure result = nodeStruct;
        if(nodeStruct.nodeType.equals(NodeType.Select) && nodeStruct.HasCondition(condition)){
            NodeStructure targetSelect = nodeStruct;
            result = targetSelect.children.remove(0);
            targetSelect.children.add(result.children.remove(0));
            result.children.add(targetSelect);
        } else {
            for (int i = 0; i < nodeStruct.children.size(); i++){
                nodeStruct.children.add(i, CommuteSelect(condition, nodeStruct.children.remove(i)));
            }
        }
        return result;
    }
}
