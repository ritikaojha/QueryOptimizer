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
    private String relation;
    private String attribute;
    
    public Attribute(String str){
        String[] tokens = str.split(".");
        if(tokens.length == 1){
            relation = "";
            attribute = tokens[0];
        } else {
            relation = tokens[0];
            attribute = tokens[1];
        }
    }
    
    public String toString(){
        if(relation.isEmpty())
            return attribute;
        else
            return relation + "." + attribute;
    }
    
    public List<String> GetRelations(){
        List<String> relations = new ArrayList<>();
        if(relation.isEmpty())
            relations = Schema.GetRelations(attribute);
        else
            relations.add(relation);
        return relations;
    }
}
