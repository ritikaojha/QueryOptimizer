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
        result.add(GroupProject(result.get(result.size()-1)));
        result.add(CascadeProject(result.get(result.size()-1)));
        //Condition c = new Condition("B.X", "C.X", "=", ConditionType.Join);
        Condition c = new Condition("C.X", "0", "=", ConditionType.Select);
        result.add(PushDownSelect(c, result.get(result.size()-1)));
        
        return result;
    }
    
    private static NodeStructure PushDownSelect(Condition condition, NodeStructure nodeStruct){
        NodeStructure result = nodeStruct;
        int maxCommutes = nodeStruct.GetHeight() - 2;
        int counter = 0;
        while (counter < maxCommutes){
            result = CommuteSelect(condition, result);
            counter++;
        }
        counter = 0;
        while (counter < maxCommutes){
            result = CommuteSelectJoin(condition, result);
            counter++;
        }
        
        return result;
    }
    
    private static NodeStructure CascadeSelect(NodeStructure nodeStruct){
        NodeStructure result = nodeStruct;
        if(!nodeStruct.ChildrenIsEmpty()){
            if(nodeStruct.nodeType.equals(NodeType.Select) && nodeStruct.NumConditions() > 1){
            result = nodeStruct.GetChild(0);
                for (int i = 0; i < nodeStruct.NumConditions(); i++){
                    NodeStructure temp = new NodeStructure(NodeType.Select);
                    temp.AddCondition(nodeStruct.GetCondition(i));
                    temp.AddChild(result);
                    result = temp;
                }
            }
            for (int i = 0; i < nodeStruct.NumChildren(); i++){
                nodeStruct.AddChild(i, CascadeSelect(nodeStruct.RemoveChild(i)));
            }
        }
        return result;
    }
    
    private static NodeStructure CascadeProject(NodeStructure nodeStruct){
        NodeStructure result = nodeStruct.GetChild(0);
        NodeStructure temp, temp2, ptr;
        if(nodeStruct.nodeType.equals(NodeType.Project)){
            temp = new NodeStructure(NodeType.Project);
            temp.AddCondition(nodeStruct.GetCondition(0));
            ptr = temp;
            for(int i = 1; i < nodeStruct.NumConditions(); i++){
                temp2 = new NodeStructure(NodeType.Project);
                temp2.AddCondition(nodeStruct.GetCondition(i));
                ptr.children.add(temp2);
                ptr = temp2;
            }
            ptr.AddChild(result);
            result = temp;
        }
        return result;
    }
    
    private static NodeStructure GroupProject(NodeStructure nodeStruct){
        NodeStructure result = nodeStruct;
        if(!nodeStruct.ChildrenIsEmpty()){
            if(nodeStruct.nodeType.equals(NodeType.Project)){
                NodeStructure nextChild = nodeStruct.RemoveChild(0);
                while(nextChild.nodeType.equals(NodeType.Project)){
                    nextChild = nextChild.RemoveChild(0);
                }
                nodeStruct.AddChild(0, nextChild);
            }
            for (int i = 0; i < nodeStruct.NumChildren(); i++){
                nodeStruct.AddChild(i, GroupProject(nodeStruct.RemoveChild(i)));
            }
        }
        return result;
    }
    //Pushes down select with condition by commuting between selections
    private static NodeStructure CommuteSelect(Condition condition, NodeStructure nodeStruct){
        NodeStructure result = nodeStruct;
        if(nodeStruct.nodeType.equals(NodeType.Select) && nodeStruct.HasCondition(condition)
                && nodeStruct.GetChild(0).nodeType.equals(NodeType.Select)){
            NodeStructure targetSelect = nodeStruct;
            result = targetSelect.RemoveChild(0);
            targetSelect.AddChild(result.RemoveChild(0));
            result.AddChild(targetSelect);
        } else {
            for (int i = 0; i < nodeStruct.NumChildren(); i++){
                nodeStruct.AddChild(i, CommuteSelect(condition, nodeStruct.RemoveChild(i)));
            }
        }
        return result;
    }

    private static NodeStructure CommuteSelectJoin(Condition condition, NodeStructure nodeStruct){
        NodeStructure result = nodeStruct;
        if(nodeStruct.nodeType.equals(NodeType.Select) && nodeStruct.HasCondition(condition)
                && (nodeStruct.GetChild(0).nodeType.equals(NodeType.Join) 
                || nodeStruct.GetChild(0).nodeType.equals(NodeType.Cartesian))){
            
            NodeStructure targetSelect = nodeStruct;
            result = targetSelect.RemoveChild(0);
            
            NodeStructure targetSelectClone = targetSelect.CloneTopNode();
                    
            NodeStructure joinLeft = result.GetChild(0);
            NodeStructure joinRight = result.GetChild(1);
            
            if(condition.conditionType.equals(ConditionType.Join)){
                if(!joinRight.nodeType.equals(NodeType.Relation) ||
                        !(Schema.AttrInRelation(condition.attr1, joinRight.text.name) ||
                        Schema.AttrInRelation(condition.attr2, joinRight.text.name))){
                    targetSelect.AddChild(0, joinLeft);
                    result.RemoveChild(0);
                    result.AddChild(0, targetSelect);
                } else {
                    targetSelect.AddChild(result);
                    result = targetSelect;
                }
            } else {
                if(!joinRight.nodeType.equals(NodeType.Relation) ||
                        Schema.AttrInRelation(condition.attr1, joinRight.text.name)){
                    targetSelect.AddChild(0, joinRight);
                    result.RemoveChild(1);
                    result.AddChild(1, targetSelect);
                }
                
                if(!joinLeft.nodeType.equals(NodeType.Relation) ||
                        Schema.AttrInRelation(condition.attr1, joinLeft.text.name)){
                    targetSelectClone.AddChild(0, joinLeft);
                    result.RemoveChild(0);
                    result.AddChild(0, targetSelectClone);
                }
            }        
        } else {
            for (int i = 0; i < nodeStruct.NumChildren(); i++){
                nodeStruct.AddChild(i, CommuteSelectJoin(condition, nodeStruct.RemoveChild(i)));
            }
        }
        return result;
    }
    
    private static NodeStructure CommuteJoin(NodeStructure nodeStruct){
        NodeStructure result = nodeStruct;
        if(nodeStruct.nodeType.equals(NodeType.Cartesian) ||
                nodeStruct.nodeType.equals(NodeType.Join)){
            result.AddChild(result.RemoveChild(0));
        }
        return result;
    }
    
    private static NodeStructure CommuteProjectWithSelect(NodeStructure nodeStruct){
        NodeStructure result = nodeStruct;
        if(nodeStruct.nodeType.equals(NodeType.Select) && nodeStruct.GetChild(0).nodeType.equals(NodeType.Project)){
            NodeStructure project = nodeStruct;
            result = project.RemoveChild(0);
            project.AddChild(result.RemoveChild(0));
            result.AddChild(project);
        }
        return result;
    }
    
    private static NodeStructure CommuteSelectWithProject(NodeStructure nodeStruct){
        NodeStructure result = nodeStruct;
        if(nodeStruct.nodeType.equals(NodeType.Project) && nodeStruct.GetChild(0).nodeType.equals(NodeType.Select)){
            NodeStructure select = nodeStruct;
            result = select.RemoveChild(0);
            select.AddChild(result.RemoveChild(0));
            result.AddChild(select);
        }
        return result;
    }
}
