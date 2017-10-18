/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.osu.queryopt.entity;

import java.io.Serializable;

/**
 *
 * @author ritika
 */
public class Config implements Serializable {
    
    public Chart chart;
    public NodeStructure nodeStructure;
    
    public Config() {
        chart = new Chart();
        nodeStructure = new NodeStructure();
    }
    
    public Config(NodeStructure nodeStructure) {
        chart = new Chart();
        this.nodeStructure = nodeStructure;
    }
}
