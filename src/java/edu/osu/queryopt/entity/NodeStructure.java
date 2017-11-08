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
    
    public Text text;
    public List<NodeStructure> children;
    public int selectivity;                          //estimated execution cost
    public int size;                            //size of query
    
    public NodeStructure() {
        text = new Text();
        children = new ArrayList();
        UpdateSize();
    }
    
    public NodeStructure(String name) {
        text = new Text(name);
        children = new ArrayList();
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
}
