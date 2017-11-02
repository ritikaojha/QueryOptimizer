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
    public float cost;                          //estimated execution cost
    public int size;                            //size of query
    
    public NodeStructure() {
        text = new Text();
        children = new ArrayList();
        UpdateCost();
        UpdateSize();
    }
    
    public NodeStructure(String name) {
        text = new Text(name);
        children = new ArrayList();
        UpdateCost();
        UpdateSize();
    }
    
    //TO be implemented
    public void UpdateSize(){
        size = 0;
    }
    
    public void UpdateCost(){
        cost = 0;
    }
}
