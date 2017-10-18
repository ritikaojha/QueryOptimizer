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
public class Chart implements Serializable {
    
    public String container;
    
    public Connectors connectors;
    
    public Node node;
    
    public Chart() {
        container = "#ChartQueryVisualizer";
        connectors = new Connectors();
        node = new Node();
    }
}
