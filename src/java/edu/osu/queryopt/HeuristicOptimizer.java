/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.osu.queryopt;
import edu.osu.queryopt.entity.Attribute;
import edu.osu.queryopt.entity.Condition;
import edu.osu.queryopt.entity.Condition.ConditionType;
import edu.osu.queryopt.entity.NodeStructure;
import edu.osu.queryopt.entity.NodeStructure.NodeType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
/**
 *
 * @author kathy
 */
public class HeuristicOptimizer {
    static List<NodeStructure> Optimize(NodeStructure nodeStruct){
        List<NodeStructure> result = new ArrayList<>();
        //Step 1: Break up the SELECT operations
        NodeStructure temp = nodeStruct.CloneTopNode();
        temp.AddChild(CascadeSelect(nodeStruct.GetChild(0)));
        result.add(temp);
        
        //Step 2: Push down the SELECT operations
        NodeStructure topSelect = result.get(result.size()-1).GetChild(0);
        Set<String> usedConditions = new HashSet<>();
        while(topSelect.nodeType.equals(NodeType.Select) 
                && topSelect.GetCondition(0) != null 
                && !usedConditions.contains(topSelect.GetCondition(0).ToString())){
            usedConditions.add(topSelect.GetCondition(0).ToString());
            temp = nodeStruct.CloneTopNode();
            temp.AddChild(PushDownSelect(topSelect));
            result.add(temp);
            topSelect = result.get(result.size()-1).GetChild(0);
        }
        
        //Step 3: Rearrange the leaf nodes
        
        //Step 4: Change CARTESIAN PRODUCT to JOIN operations
        
        //Step 5: Break up and push down PROJECT operations
        result.add(PushDownProject(result.get(result.size()-1)));
        
        //create joins
        result.addAll(CreateJoin(result.get(result.size()-1)));
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
                case Project:
                    result = CommuteSelectWithProject(result);
                    break;
                case Relation:
                    if(!result.GetCondition(0).attr1.GetRelation().equals(ptr.text.name))
                        result = result.GetChild(0);
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
    
    private static NodeStructure PushDownProject(NodeStructure nodeStruct){
        NodeStructure result = nodeStruct;
        NodeStructure ptr;
        Condition condition;
        if(nodeStruct.nodeType.equals(NodeType.Project)){
            condition = nodeStruct.GetCondition(0);
            ptr = nodeStruct.GetChild(0);
            switch (ptr.nodeType){
                case Select:
                    result = CommuteProjectWithSelect(result);
                    break;
                case Join:
                    result = CommuteProjectJoin(result);
                    break;
                case Cartesian:
                    result = CommuteProjectJoin(result);
                    break;
                case Relation:
                    List<Condition> toRemove = new ArrayList<>();
                    for(int i = 0; i < result.NumConditions(); i++){
                        if(!result.GetCondition(i).attr1.GetRelation().equals(ptr.text.name))
                            toRemove.add(result.GetCondition(i));
                    }
                    if(toRemove.size() == result.NumConditions()){
                        result = result.GetChild(0);
                    } else {
                        for(Condition c: toRemove){
                            result.RemoveCondition(c);
                        }
                    }
                    
                    break;
            }
            if(result != nodeStruct){
                ptr = result.CloneTopNode();
                for(int i = 0; i < result.NumChildren(); i++){
                    NodeStructure child = result.GetChild(i);
                    if(child.HasCondition(condition))
                        ptr.AddChild(PushDownProject(child));
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
                if(!joinLeft.nodeType.equals(NodeType.Relation) &&
                        (!joinRight.nodeType.equals(NodeType.Relation) ||
                        !(Schema.AttrInRelation(condition.attr1, joinRight.text.name) ||
                        Schema.AttrInRelation(condition.attr2, joinRight.text.name)))){
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
                if(result == nodeStruct){
                    result = result.GetChild(0);
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
        if(nodeStruct.nodeType.equals(NodeType.Project) && nodeStruct.GetChild(0).nodeType.equals(NodeType.Select)){
            NodeStructure select = nodeStruct.GetChild(0);
            
            boolean commuteAllowed = AttrIsSubset(select, nodeStruct);
            if(commuteAllowed){
                result = select.CloneTopNode();
                result.AddChild(nodeStruct.CloneTopNode());
                result.GetChild(0).AddChild(select.GetChild(0));
            }
        }
        return result;
    }

    private static NodeStructure CommuteSelectWithProject(NodeStructure nodeStruct){
        NodeStructure result = nodeStruct;
        if(nodeStruct.nodeType.equals(NodeType.Select) && nodeStruct.GetChild(0).nodeType.equals(NodeType.Project)){
            NodeStructure project = nodeStruct.GetChild(0);
            
            boolean commuteAllowed = AttrIsSubset(nodeStruct, project);
            if(commuteAllowed){
                result = project.CloneTopNode();
                result.AddChild(nodeStruct.CloneTopNode());
                result.GetChild(0).AddChild(project.GetChild(0));
            }
        }
        return result;
    }
    
    //Not tested
    private static NodeStructure CommuteProjectJoin(NodeStructure nodeStruct){
        NodeStructure result = nodeStruct;
        if(nodeStruct.nodeType.equals(NodeType.Project) && 
                (nodeStruct.GetChild(0).nodeType.equals(NodeType.Cartesian) ||
                nodeStruct.GetChild(0).nodeType.equals(NodeType.Join))){
            NodeStructure join = nodeStruct.GetChild(0);
            NodeStructure resultPtr, resultPtr2, ptr;
            Set<String> projectAttr = new HashSet<>();
            Set<String> joinAttr = new HashSet<>();
            
            for(int i = 0; i < nodeStruct.NumConditions(); i++){
                projectAttr.add(nodeStruct.GetCondition(i).attr1.ToString());
            }
            for(int i = 0; i < join.NumConditions(); i++){
                joinAttr.add(join.GetCondition(i).attr1.ToString());
                joinAttr.add(join.GetCondition(i).attr2.ToString());
            }
            
            boolean allJoinAttrInProject = projectAttr.containsAll(joinAttr);
            
            if(allJoinAttrInProject){
                result = nodeStruct.GetChild(0).CloneTopNode();
                resultPtr = result;
            } else {
                result = nodeStruct.CloneTopNode();
                result.AddChild(nodeStruct.GetChild(0).CloneTopNode());
                projectAttr.addAll(joinAttr);
                resultPtr = result.GetChild(0);
            }
            
            ptr = nodeStruct.GetChild(0);
            NodeStructure tempRightProject = new NodeStructure(NodeType.Project);
            NodeStructure tempLeftProject = new NodeStructure(NodeType.Project);
            for(String attr: projectAttr){
                Attribute a = new Attribute(attr);
                if(!ptr.GetChild(1).nodeType.equals(NodeType.Relation) ||
                        a.GetRelation().equals(ptr.GetChild(1).text.name)){
                    tempRightProject.AddCondition(attr);
                } 
                else if(!ptr.GetChild(0).nodeType.equals(NodeType.Relation)){
                    tempLeftProject.AddCondition(attr);
                }
                
                if(ptr.GetChild(0).nodeType.equals(NodeType.Relation) &&
                        a.GetRelation().equals(ptr.GetChild(0).text.name)){
                    tempLeftProject.AddCondition(attr);
                }
            }
            resultPtr2 = resultPtr;
            if(tempLeftProject.NumConditions() > 0){
                resultPtr.AddChild(tempLeftProject);
                resultPtr = resultPtr.GetChild(0);
            }
            resultPtr.AddChild(ptr.GetChild(0));
            if(tempRightProject.NumConditions() > 0){
                resultPtr2.AddChild(tempRightProject);
                resultPtr2 = resultPtr2.GetChild(1);
            }
            resultPtr2.AddChild(ptr.GetChild(1));
        }
        return result;
    }
    
    //Checks if all attr of nodeStruct1 are in attr of nodestruct2
    private static boolean AttrIsSubset(NodeStructure nodeStruct1, NodeStructure nodeStruct2){
        boolean isSubset = false;
        if(nodeStruct1.NumConditions() > 0 && nodeStruct2.NumConditions() > 0){
            Set<String> n1Attr = new HashSet<>();
            Set<String> n2Attr = new HashSet<>();

            for(int i = 0; i < nodeStruct1.NumConditions(); i++){
                n1Attr.add(nodeStruct1.GetCondition(i).attr1.ToString());
                if(nodeStruct1.GetCondition(i).conditionType.equals(ConditionType.Join))
                    n1Attr.add(nodeStruct1.GetCondition(i).attr2.ToString());
            }
            for(int i = 0; i < nodeStruct2.NumConditions(); i++){
                n2Attr.add(nodeStruct2.GetCondition(i).attr1.ToString());
                if(nodeStruct2.GetCondition(i).conditionType.equals(ConditionType.Join))
                    n2Attr.add(nodeStruct2.GetCondition(i).attr2.ToString());
            }
            isSubset = n2Attr.containsAll(n1Attr);
        }
        return isSubset;
    }
    
    private static NodeStructure AssociativeJoin(NodeStructure nodeStruct) {
        NodeStructure result = new NodeStructure(NodeType.Join);
        if(nodeStruct.nodeType.equals(NodeType.Join) && nodeStruct.GetChild(0).equals(NodeType.Join)) {
            NodeStructure join = nodeStruct.RemoveChild(0);
            NodeStructure left = join.RemoveChild(0);
            NodeStructure right = join.RemoveChild(0);
            result.AddChild(0,left);
            join.AddChild(0, right);
            join.AddChild(1, nodeStruct.RemoveChild(0));
            result.AddChild(1, join);
        } else if (nodeStruct.nodeType.equals(NodeType.Join) && nodeStruct.GetChild(1).equals(NodeType.Join)) {
            NodeStructure join = nodeStruct.RemoveChild(1);
            NodeStructure left = join.RemoveChild(0);
            NodeStructure right = join.RemoveChild(0);
            join.AddChild(0, nodeStruct.RemoveChild(0));
            join.AddChild(1, left);
            result.AddChild(0, join);
            result.AddChild(1,right);
        }
        return result;
    }
    
    private static List<NodeStructure> CreateJoin(NodeStructure nodeStruct){
        List<NodeStructure> result = new ArrayList<>();
        NodeStructure temp = nodeStruct.CloneAllNodes();;
        Queue<NodeStructure> bfs = new LinkedList<>();
        bfs.add(temp);
        while(!bfs.isEmpty()){
            NodeStructure node = bfs.poll();
            for (int i = 0; i < node.children.size(); i++) {
                NodeStructure child = node.children.get(i);
                bfs.add(child);
                for (Condition c:child.conditions) {
                    if (c.conditionType == ConditionType.Join) {
                        NodeStructure x = findNearestX(child);
                        if (x != null) {
                            x.text.name = child.text.name.replace("\u03C3", "\u2A1D");
                            node.children.remove(child);
                            node.children.addAll(child.children);
                            result.add(temp);
                        }
                    }
                }
            }
        }
        return result;
    }
    
    private static NodeStructure findNearestX(NodeStructure node) {
        while(node.children != null && node.children.size() == 1) {
            if (node.children.get(0).nodeType == NodeType.Cartesian)
                return node.children.get(0);
            node = node.children.get(0);
        }
        return null;
    }
    
}
