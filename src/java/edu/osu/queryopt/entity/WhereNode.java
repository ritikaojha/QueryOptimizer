/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.osu.queryopt.entity;

/**
 *
 * @author ritika
 */
public class WhereNode {
    public String exp1;
    public SelectFromWhereNode queryExp1;
    public String opr;
    public String exp2;
    public SelectFromWhereNode queryExp2;
    
    public WhereNode(String exp1, String opr, String exp2) {
        this.exp1 = exp1;
        this.opr = opr;
        this.exp2 = exp2;
    }
    
    public WhereNode(String exp1, SelectFromWhereNode queryExp1, 
            String opr, String exp2, SelectFromWhereNode queryExp2) {
        this.exp1 = exp1;
        this.queryExp1 = queryExp1;
        this.opr = opr;
        this.exp2 = exp2;
        this.queryExp2 = queryExp2;
    }
}
