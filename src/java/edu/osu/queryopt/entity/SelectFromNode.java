/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.osu.queryopt.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ritika
 */
public class SelectFromNode {
    public List<String> selectList = new ArrayList<>();
    public Map<String, List<WhereNode>> fromMap = new HashMap<>();
    public List<String[]> joinOn = new ArrayList<>();
}
