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
    public Attribute attr1, attr2;
    public enum Sign {Equals, None};
    public Sign sign;
    public enum ConditionType {Join, Select, Singular};
    public ConditionType conditionType;
    private String value = "";
    
    public Condition(String a){
        attr1 = new Attribute(a);
        sign = Sign.None;
        conditionType = ConditionType.Singular;
    }
    
    public Condition(String a1, String a2, String op, ConditionType ct){
        attr1 = new Attribute(a1);
        conditionType = ct;
        if(ct.equals(ConditionType.Join))
            attr2 = new Attribute(a2);
        else
            value = a2;
        this.StringToOperator(op);
    }
    
    public Condition(Condition c){
        this.attr1 = c.attr1;
        this.sign = c.sign;
        this.conditionType = c.conditionType;
        if (this.conditionType.equals(ConditionType.Join)){
            this.attr2 = new Attribute(c.attr2);
        } else {
            this.value = c.value;
        }
    }
   /* 
    public String[] GetRelations(){
        List<String> relations = new ArrayList<>();
        switch (conditionType){
            case Singular:
                relations.addAll(attr1.GetRelations());
                break;
            case Select:
                relations.addAll(attr1.GetRelations());
                break;
            default:
                relations.addAll(attr1.GetRelations());
                relations.addAll(attr2.GetRelations());
        }
        return relations.toArray(new String[0]);
    }
    */
    public String ToString(){
        String result = attr1.ToString();
        String operator = this.OperatorToString();
        if(!operator.isEmpty()){
            if(conditionType.equals(ConditionType.Join))
                result += " " + operator + " " + attr2.ToString();
            else
                result += " " + operator + " " + value;
        }
        return result;
    }
    
    private void StringToOperator(String op){
        switch(op){
            case "=":
                sign = Sign.Equals;
                break;
            default:
                sign = Sign.None;
        }
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
    
    public boolean ConditionEquals(Condition c){
        boolean result = false;
        if(this.conditionType.equals(c.conditionType)){
            if(conditionType.equals(ConditionType.Join))
                result = this.attr2.AttributeEquals(c.attr2);
            else
                result = this.value.equals(c.value);
        }
        
        return result && this.attr1.AttributeEquals(c.attr1) &&
                this.conditionType.equals(c.conditionType) &&
                this.sign.equals(c.sign);
    }
}
