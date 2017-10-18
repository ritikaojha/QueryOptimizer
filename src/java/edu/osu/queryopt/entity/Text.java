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
public class Text implements Serializable {
    
    public String name;
    
    public Text() {
        name = new String();
    }
    
    public Text(String name) {
        this.name = name;
    }
}
