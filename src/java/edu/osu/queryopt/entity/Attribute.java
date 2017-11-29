/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.osu.queryopt.entity;

import edu.osu.queryopt.Schema;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author kathy
 */
public class Attribute {
    public String relation;
    private String attribute;
    
    public Attribute(String str){
        String[] tokens = str.split("\\.");
        switch(tokens.length){
            case 0:
                relation = "";
                attribute = str;
                break;
            case 1:
                relation = "";
                attribute = tokens[0];
                break;
            default:
                relation = tokens[0];
                attribute = tokens[1];
        }
    }
    
    public Attribute(Attribute attr){
        this.relation = attr.relation;
        this.attribute = attr.attribute;
    }
    
    public String ToString(){
        if(relation.isEmpty())
            return attribute;
        else
            return relation + "." + attribute;
    }
    
    public String GetRelation(){
        return relation;
    }
    
    public boolean AttributeEquals(Attribute a){
        return this.relation.equals(a.relation) && this.attribute.equals(a.attribute);
    }
}
