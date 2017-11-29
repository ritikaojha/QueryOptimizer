/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.osu.queryopt.entity;
import edu.osu.queryopt.Unicode;
import edu.osu.queryopt.entity.Condition.ConditionType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ritika
 */
public class NodeStructure implements Serializable {
    
    public enum NodeType { Project, Select, Join, Cartesian, Relation, None};
    public NodeType nodeType;
    public Text text;
    private List<Condition> conditions;
    public List<NodeStructure> children;
    public int selectivity;                          //estimated execution cost
    public int size;                           //size of query
    private int height = 1;
    private int maxChildHeight = 0;
    
    public NodeStructure() {
        text = new Text();
        children = new ArrayList();
        conditions = new ArrayList();
        UpdateSize();
    }
    
    public NodeStructure(String name) {
        text = new Text(name);
        children = new ArrayList();
        conditions = new ArrayList();
        UpdateSize();
        StringToNode(name);
        //NodeToString();
    }
    
    public NodeStructure(String name, NodeType type) {
        text = new Text(name);
        children = new ArrayList();
        conditions = new ArrayList();
        nodeType = type;
        UpdateSize();
        StringToNode(name);
        NodeToString();
    }
    
    public NodeStructure(NodeType type) {
        text = new Text();
        children = new ArrayList();
        conditions = new ArrayList();
        nodeType = type;
        UpdateSize();
        NodeToString();
    }
    public void AddChild(NodeStructure n){
        children.add(n);
        if(n.height > maxChildHeight){
            maxChildHeight = n.height;
            height = n.height + 1;
        }
    }
    
    public void AddChild(int index, NodeStructure n){
        children.add(index, n);
        if(n.height > maxChildHeight){
            maxChildHeight = n.height;
            height = n.height + 1;
        }
    }
    
    public NodeStructure GetChild(int index){
        return children.get(index);
    }
    
    public NodeStructure RemoveChild(int index){
        NodeStructure child = children.remove(index);
        if(child.height == maxChildHeight){
            maxChildHeight = 0;
            for (NodeStructure temp:children){
                if(temp.height > maxChildHeight)
                    maxChildHeight = temp.height; 
            }
        }
        return child;
    }
    
    public boolean ChildrenIsEmpty(){
        return children.isEmpty();
    }
    
    public int NumChildren(){
        return children.size();
    }
    
    public void AddCondition(Condition c){
        conditions.add(c);
        NodeToString();
    }
    
    public void AddCondition(String a){
        conditions.add(new Condition(a));
        NodeToString();
    }
    
    public void AddCondition(String a1, String a2, String op, ConditionType ct){
        conditions.add(new Condition(a1, a2, op, ct));
        NodeToString();
    }
    
    public void RemoveCondition(Condition c){
        conditions.remove(c);
        NodeToString();
    }
    
    public int NumConditions(){
        return conditions.size();
    }
    
    public Condition GetCondition(int i){
        return conditions.get(i);
    }
    
    public boolean HasCondition(Condition c){
        boolean result = false;
        for (Condition cond:conditions){
            if(cond.ConditionEquals(c)){
                result = true;
            }
        }
        return result;
    }
    
    public int GetHeight(){
        return height;
    }
    
    //TO be implemented
    public void UpdateSize(){
        if(children.size() == 0) //leaf node
            //size = size of relation
            size = 0;
        else if (children.size() == 1) //nonjoin operation
            size = children.get(0).size;
        else //join opeartion
        {
            UpdateSelectivity();
            size = selectivity * children.get(0).size * children.get(1).size;
        }
    }
    
    private void UpdateSelectivity(){
        Text relation1;
        Text relation2;
        Text attribute1;
        Text attribute2;
        
        int distinctA1 = 0; //select count(distict attribute) from relation
        int distinctA2 = 0;
        
        selectivity = Math.max(distinctA1, distinctA2);
        
    }
    
    private void NodeToString(){
        if(!nodeType.equals(NodeType.Relation)){
            String result = "";
            String separator = " ";
            if (nodeType.equals(NodeType.Cartesian)) {
                result = Unicode.CROSSPRODUCT;
            } else {
                if(nodeType.equals(NodeType.Select)){
                    separator = " AND";
                    result = Unicode.SELECT;
                } else if (nodeType.equals(NodeType.Project)) {
                    separator = ",";
                    result = Unicode.PROJECT;
                }
                for (int i = 0; i < conditions.size(); i++){
                    if(i > 0 && i < conditions.size()){
                        result += separator;
                    }
                    result += " " + conditions.get(i).ToString();
                }
            }
            text.name = result;
        }
    }
    
    private void StringToNode(String str){
        String[] rawTokens = new String[0];
        if(str.contains("WHERE")){
            str = str.replaceAll("WHERE\\s+", "");
            rawTokens = str.split("\\s+AND\\s+");
            nodeType = NodeType.Select;
        }
        else if(str.contains("SELECT")){
            str = str.replaceAll("SELECT\\s+", "");
            rawTokens = str.split(",\\s+");
            nodeType = NodeType.Project;
        }
         
        for (int i = 0; i < rawTokens.length; i++){
            if(!conditions.contains(rawTokens[i]))
                AddCondition(rawTokens[i]);
        }
    }
    
    public NodeStructure CloneTopNode(){
        NodeStructure clone = new NodeStructure(this.text.name, this.nodeType);
        for(Condition c: this.conditions)
            clone.conditions.add(new Condition(c));
        clone.NodeToString();
        return clone;
    }
}
