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
        //test cascade select
        NodeStructure temp = nodeStruct.CloneTopNode();
        temp.AddChild(CascadeSelect(nodeStruct.GetChild(0)));
        result.add(temp);
        
        //test cascade project
        result.add(CascadeProject(result.get(result.size()-1)));
        
        //test group project
        result.add(GroupProject(result.get(result.size()-1)));
        
        //test push down select
        temp = nodeStruct.CloneTopNode();
        temp.AddChild(PushDownSelect(result.get(result.size()-1).GetChild(0)));
        result.add(temp);
        return result;
    }
    
    private static NodeStructure PushDownSelect(NodeStructure nodeStruct){
        NodeStructure result = nodeStruct;
        NodeStructure ptr;
        Condition condition;
        if(nodeStruct.nodeType.equals(NodeType.Select)){
            condition = nodeStruct.GetCondition(0);
            ptr = nodeStruct.GetChild(0);
            switch (ptr.nodeType){
                case Select:
                    result = CommuteSelect(result);
                    break;
                case Join:
                    result = CommuteSelectJoin(result);
                    break;
                case Cartesian:
                    result = CommuteSelectJoin(result);
                    break;
            }
            if(result != nodeStruct){
                ptr = result.CloneTopNode();
                for(int i = 0; i < result.NumChildren(); i++){
                    NodeStructure child = result.GetChild(i);
                    if(child.HasCondition(condition))
                        ptr.AddChild(PushDownSelect(child));
                    else {
                        ptr.AddChild(child);
                    }
                }
                result = ptr;
            }
        }
        
        return result;
    }
    
    private static NodeStructure CascadeSelect(NodeStructure nodeStruct){
        NodeStructure result = nodeStruct;
        if(nodeStruct.nodeType.equals(NodeType.Select) && nodeStruct.NumConditions() > 1){
            result = nodeStruct.GetChild(0);
            for (int i = 0; i < nodeStruct.NumConditions(); i++){
                NodeStructure temp = new NodeStructure(NodeType.Select);
                temp.AddCondition(nodeStruct.GetCondition(i));
                temp.AddChild(result);
                result = temp;
            }
        }
        return result;
    }
    
    private static NodeStructure CascadeProject(NodeStructure nodeStruct){
        NodeStructure result = nodeStruct;
        NodeStructure temp, temp2, ptr;
        if(nodeStruct.nodeType.equals(NodeType.Project) && nodeStruct.NumConditions() > 1){
            result = nodeStruct.GetChild(0);
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
        NodeStructure temp;
        if(nodeStruct.nodeType.equals(NodeType.Project) && 
                nodeStruct.children.get(0).nodeType.equals(NodeType.Project)){
            result = nodeStruct.GetChild(0);
            temp = nodeStruct.CloneTopNode();
            while(result.nodeType.equals(NodeType.Project)){
                temp.AddCondition(result.GetCondition(0));
                result = result.GetChild(0);
            }
            temp.AddChild(result);
            result = temp;
        }
        return result;
    }
    //Pushes down select with condition by commuting between selections
    private static NodeStructure CommuteSelect(NodeStructure nodeStruct){
        NodeStructure result = nodeStruct;
        NodeStructure ptr, resultPtr;
        if(nodeStruct.nodeType.equals(NodeType.Select)
                && nodeStruct.GetChild(0).nodeType.equals(NodeType.Select)){
            ptr = nodeStruct.GetChild(0);
            result = ptr.CloneTopNode();
            resultPtr = result;
            resultPtr.AddChild(nodeStruct.CloneTopNode());
            
            ptr = ptr.GetChild(0);
            resultPtr = resultPtr.GetChild(0);
            
            resultPtr.AddChild(ptr);
        }
        return result;
    }

    private static NodeStructure CommuteSelectJoin(NodeStructure nodeStruct){
        NodeStructure result = nodeStruct;
        NodeStructure ptr, resultPtr;
        Condition condition;
        if(nodeStruct.nodeType.equals(NodeType.Select)
                && (nodeStruct.GetChild(0).nodeType.equals(NodeType.Join) 
                || nodeStruct.GetChild(0).nodeType.equals(NodeType.Cartesian))){
            condition = nodeStruct.GetCondition(0);
            ptr = nodeStruct.GetChild(0);
                    
            NodeStructure joinLeft = ptr.GetChild(0);
            NodeStructure joinRight = ptr.GetChild(1);
            
            if(condition.conditionType.equals(ConditionType.Join)){
                if(!joinRight.nodeType.equals(NodeType.Relation) ||
                        !(Schema.AttrInRelation(condition.attr1, joinRight.text.name) ||
                        Schema.AttrInRelation(condition.attr2, joinRight.text.name))){
                    result = ptr.CloneTopNode();
                    result.AddChild(nodeStruct.CloneTopNode());
                    resultPtr = result.GetChild(0);
                    resultPtr.AddChild(joinLeft);
                    result.AddChild(joinRight);
                }
            } else {
                if(!joinLeft.nodeType.equals(NodeType.Relation) ||
                        Schema.AttrInRelation(condition.attr1, joinLeft.text.name)){
                    result = ptr.CloneTopNode();
                    result.AddChild(nodeStruct.CloneTopNode());
                    resultPtr = result.GetChild(0);
                    resultPtr.AddChild(joinLeft);
                }
                
                if(!joinRight.nodeType.equals(NodeType.Relation) ||
                        Schema.AttrInRelation(condition.attr1, joinRight.text.name)){
                    if(result == nodeStruct){
                        result = ptr.CloneTopNode();
                        result.AddChild(ptr.GetChild(0));
                    }
                    result.AddChild(nodeStruct.CloneTopNode());
                    resultPtr = result.GetChild(1);
                    resultPtr.AddChild(joinRight);
                } else if(result != nodeStruct){
                    result.AddChild(ptr.GetChild(1));
                }
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
