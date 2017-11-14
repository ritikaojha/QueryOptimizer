/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.osu.queryopt.entity;
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
    public List<String> conditions;
    public List<NodeStructure> children;
    public int selectivity;                          //estimated execution cost
    public int size;                            //size of query
    
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
    
    public void NodeToString(){
        String result = "";
        String separator = " ";
        
        if(nodeType.equals(NodeType.Select)){
            separator = " AND";
            result = "\u03C3";
        } else if (nodeType.equals(NodeType.Project)) {
            separator = ",";
            result = "\u03C0";
        }
        for (int i = 0; i < conditions.size(); i++){
            if(i > 0 && i < conditions.size()){
                result += separator;
            }
            result += " " + conditions.get(i);
        }
        text.name = result;
    }
    
    private void StringToNode(String str){
        String[] rawTokens = new String[0];
        if(str.contains("WHERE")){
            str = str.replaceAll("WHERE\\s+", "");
            rawTokens = str.split("\\s+AND\\s+");
        }
        else if(str.contains("SELECT")){
            str = str.replaceAll("SELECT\\s+", "");
            rawTokens = str.split(",\\s+");
        }
         
        for (int i = 0; i < rawTokens.length; i++){
            if(!conditions.contains(rawTokens[i]))
                conditions.add(rawTokens[i]);
        }
    }
}
