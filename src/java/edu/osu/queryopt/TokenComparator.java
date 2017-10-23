/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.osu.queryopt;

import java.util.Comparator;

/**
 *
 * @author ritika
 */
public class TokenComparator implements Comparator<String> {

    public TokenComparator() {
    }

    @Override
    public int compare(String o1, String o2) {
        return getRank(o1) - getRank(o2);
    }
    
    int getRank(String s) {
        String firstWord = s.split(" ")[0];
        switch(firstWord) {
            case "SELECT": return 1;
            case "WHERE": return 2;
            case "FROM": return 3;
            default: return 0;
        }
    }
    
}
