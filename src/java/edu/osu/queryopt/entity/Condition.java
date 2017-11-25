/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.osu.queryopt.entity;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author kathy
 */
public class Condition {
    private Attribute attr1, attr2;
    public enum Sign {Equals, None}
    private Sign sign;
    public enum ConditionType {Inequality, Singular}
    private ConditionType conditionType;
    
    public Condition(String a){
        attr1 = new Attribute(a);
        sign = Sign.None;
        conditionType = ConditionType.Singular;
    }
    
    public Condition(String a1, String a2, String op){
        attr1 = new Attribute(a1);
        attr2 = new Attribute(a2);
        sign = StringToOperator(op);
        conditionType = ConditionType.Inequality;
    }
    
    public String[] GetRelations(){
        List<String> relations = new ArrayList<>();
        switch (conditionType){
            case Inequality:
                relations.addAll(attr1.GetRelations());
                break;
            default:
                relations.addAll(attr1.GetRelations());
                relations.addAll(attr2.GetRelations());
        }
        return relations.toArray(new String[0]);
    }
    
    public String toString(){
        String result = attr1.toString();
        String operator = OperatorToString();
        if(!operator.isEmpty()){
            result += " " + operator + " " + attr2.toString();
        }
        return result;
    }
    
    private Sign StringToOperator(String op){
        Sign sign;
        switch(op){
            case "=":
                sign = Sign.Equals;
                break;
            default:
                sign = Sign.None;
        }
        return sign;
    }
    
    private String OperatorToString(){
        String op;
        switch(sign){
            case Equals:
                op = "=";
                break;
            default:
                op = "";
        }
        return op;
    }
}
