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
    static NodeStructure CascadeSelect(NodeStructure nodeStruct){
        NodeStructure result = nodeStruct;
        if(!nodeStruct.children.isEmpty()){
            if(nodeStruct.nodeType.equals(NodeType.Select) && nodeStruct.conditions.size() > 1){
                result = nodeStruct.children.get(0);
                for (int i = 0; i < nodeStruct.conditions.size(); i++){
                    NodeStructure temp = new NodeStructure(NodeType.Select);
                    String cond = nodeStruct.conditions.get(i);
                    temp.conditions.add(cond);
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
}
