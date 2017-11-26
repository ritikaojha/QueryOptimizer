/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.osu.queryopt;
import edu.osu.queryopt.entity.Condition;
import edu.osu.queryopt.entity.Condition.ConditionType;
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
        result.add(CascadeSelect(nodeStruct));
        result.add(CascadeProject(result.get(result.size()-1)));
        //Condition c = new Condition("B.X", "C.X", "=", ConditionType.Join);
        //Condition c = new Condition("C.X", "0", "=", ConditionType.Select);
        //result.add(CommuteSelect(c, result.get(result.size()-1)));
        //result.add(CommuteSelect(c, result.get(result.size()-1)));
        //result.add(CommuteSelectJoin(c, result.get(result.size()-1)));
        //result.add(CommuteSelectJoin(c, result.get(result.size()-1)));
        return result;
    }
    
    private static NodeStructure CascadeSelect(NodeStructure nodeStruct){
        NodeStructure result = nodeStruct;
        if(!nodeStruct.children.isEmpty()){
            if(nodeStruct.nodeType.equals(NodeType.Select) && nodeStruct.NumConditions() > 1){
            result = nodeStruct.children.get(0);
                for (int i = 0; i < nodeStruct.NumConditions(); i++){
                    NodeStructure temp = new NodeStructure(NodeType.Select);
                    temp.AddCondition(nodeStruct.GetCondition(i));
                    temp.children.add(result);
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
    private static NodeStructure CommuteSelect(Condition condition, NodeStructure nodeStruct){
        NodeStructure result = nodeStruct;
        if(nodeStruct.nodeType.equals(NodeType.Select) && nodeStruct.HasCondition(condition)
                && nodeStruct.children.get(0).nodeType.equals(NodeType.Select)){
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

    private static NodeStructure CommuteSelectJoin(Condition condition, NodeStructure nodeStruct){
        NodeStructure result = nodeStruct;
        if(nodeStruct.nodeType.equals(NodeType.Select) && nodeStruct.HasCondition(condition)
                && (nodeStruct.children.get(0).nodeType.equals(NodeType.Join) 
                || nodeStruct.children.get(0).nodeType.equals(NodeType.Cartesian))){
            
            NodeStructure targetSelect = nodeStruct;
            result = targetSelect.children.remove(0);
            
            NodeStructure targetSelectClone = new NodeStructure(NodeType.Select);
            targetSelectClone.AddCondition(targetSelect.GetCondition(0));
                    
            NodeStructure joinLeft = result.children.get(0);
            NodeStructure joinRight = result.children.get(1);
            
            if(condition.conditionType.equals(ConditionType.Join)){
                if(!joinRight.nodeType.equals(NodeType.Relation) ||
                        !(Schema.AttrInRelation(condition.attr1, joinRight.text.name) ||
                        Schema.AttrInRelation(condition.attr2, joinRight.text.name))){
                    targetSelect.children.add(0, joinLeft);
                    result.children.remove(0);
                    result.children.add(0, targetSelect);
                } else {
                    targetSelect.children.add(result);
                    result = targetSelect;
                }
            } else {
                if(!joinRight.nodeType.equals(NodeType.Relation) ||
                        Schema.AttrInRelation(condition.attr1, joinRight.text.name)){
                    targetSelect.children.add(0, joinRight);
                    result.children.remove(1);
                    result.children.add(1, targetSelect);
                }
                
                if(!joinLeft.nodeType.equals(NodeType.Relation) ||
                        Schema.AttrInRelation(condition.attr1, joinLeft.text.name)){
                    targetSelectClone.children.add(0, joinLeft);
                    result.children.remove(0);
                    result.children.add(0, targetSelectClone);
                }
            }        
        } else {
            for (int i = 0; i < nodeStruct.children.size(); i++){
                nodeStruct.children.add(i, CommuteSelectJoin(condition, nodeStruct.children.remove(i)));
            }
        }
        return result;
    }
    
    private static String GetRelation(String condition){
        String[] tokens = condition.split("\\.");
        return tokens[0].toUpperCase();
    }
}
