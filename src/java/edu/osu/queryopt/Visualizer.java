/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.osu.queryopt;

import edu.osu.queryopt.entity.Config;
import edu.osu.queryopt.entity.NodeStructure;
import edu.osu.queryopt.entity.NodeStructure.NodeType;
import edu.osu.queryopt.entity.SelectFromNode;
import edu.osu.queryopt.entity.WhereNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ritika
 */
public class Visualizer {
    
    static Set<String> keywords = new HashSet<>(Arrays.asList("SELECT", "FROM", "WHERE", "JOIN", "HAVING",
            "GROUP", "ORDER", "UNION", "INTERSECT", "EXCEPT"));
    
    static Config buildExpressionTree(String query) {
        Config config = new Config();
        String[] rawTokens = query.toUpperCase()
                .replace("=", " = ")
                .replace(",", " , ")
                .replace("(", " ( ")
                .replace(")", " ) ")
                .split("\\s+");
        Queue<String> tokens = new LinkedList<>(Arrays.asList(rawTokens));
        SelectFromNode queryTree;
        try {
            queryTree = buildQueryTree(tokens);
            NodeStructure node = buildSelectNode(queryTree);
            config.nodeStructure = HeuristicOptimizer.Optimize(node);
            return config;
        } catch (Exception ex) {
            Logger.getLogger(Visualizer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    // TODO: Validate the query
    static SelectFromNode buildQueryTree(Queue<String> tokens) throws Exception {
        if (tokens.isEmpty())
            return null;
        if (!tokens.poll().equals("SELECT"))
            throw new Exception("Invalid query");
        SelectFromNode query = new SelectFromNode();
        while(!tokens.peek().equals("FROM")) {
            String token = tokens.poll();
            if (!token.equals(","))
                query.selectList.add(token);
        }
        tokens.poll();
        while(!tokens.isEmpty() && !tokens.peek().equals("WHERE")) {
            String token = tokens.poll();
            if (!token.equals(","))
                query.fromMap.put(token, new ArrayList<>());
        }
        
        if (!tokens.isEmpty())
            tokens.poll();
        while(!tokens.isEmpty()) {
            String exp1 = null;
            SelectFromNode queryExp1 = null;
            String table1 = null;
            String opr = null;
            String exp2 = null;
            SelectFromNode queryExp2 = null;
            String table2 = null;
            
            String temp = tokens.poll();
            if (temp.equals("(") && tokens.peek().equals("SELECT")) {
                Queue subQuery = new LinkedList<>();
                int bracketCount = 0;
                while (bracketCount != 0 || !tokens.peek().equals(")")) {
                    String token = tokens.poll();
                    if (token.equals("("))
                        bracketCount++;
                    else if (token.equals(")"))
                        bracketCount--;
                    subQuery.add(token);
                }
                queryExp1 = buildQueryTree(subQuery);
                tokens.poll();
            }
            else {
                StringJoiner sj = new StringJoiner(" ");
                sj.add(temp);
                System.out.print(temp);
                while(!tokens.peek().equals("IN")
                        && !tokens.peek().equals("=")
                        && !tokens.peek().equals("LIKE")){
                    sj.add(tokens.poll());System.out.print(tokens.peek());}
                exp1 = sj.toString();
                table1 = exp1.split("\\.")[0];
            }
            
            opr = tokens.poll();
            System.out.print(opr);
            
            temp = tokens.poll();
            if (temp != null && temp.equals("(") && tokens.peek().equals("SELECT")) {
                Queue subQuery = new LinkedList<>();
                int bracketCount = 0;
                while (bracketCount != 0 || !tokens.peek().equals(")")) {
                    String token = tokens.poll();
                    if (token.equals("("))
                        bracketCount++;
                    else if (token.equals(")"))
                        bracketCount--;
                    subQuery.add(token);
                }
                queryExp2 = buildQueryTree(subQuery);
                tokens.poll();
            }
            else {
                StringJoiner sj = new StringJoiner(" ");
                sj.add(temp);
                while(!tokens.isEmpty()
                        && !tokens.peek().equals("AND"))
                    sj.add(tokens.poll());
                exp2 = sj.toString();
                table2 = exp2.split("\\.")[0];
            }
            
            if (table1 != null
                    && table2 != null
                    && query.fromMap.containsKey(table1)
                    && query.fromMap.containsKey(table2))
                query.joinOn.add(new String[]{table1, table2,
                        exp1 + " " + opr + " " + exp2});
            else {
                query.fromMap.get(table1).add(
                        new WhereNode(exp1, queryExp1, opr, exp2, queryExp2));
            }
        }
        return query;
    }
    
    static NodeStructure buildSelectNode(SelectFromNode queryTree) {
        NodeStructure node = new NodeStructure();
        node.nodeType = NodeType.Project;
        StringJoiner sj = new StringJoiner(",");
        for (String select:queryTree.selectList)
            sj.add(select);
        node.text.name = "\u03C0 " + sj.toString();
        if (!queryTree.joinOn.isEmpty()) {
            Set<String> tablesJoined = new HashSet<>();
            Map<NodeStructure, Set<String>> tempNodes = new HashMap<>();
            for (String[] relation:queryTree.joinOn) {
                if (!tablesJoined.contains(relation[0])
                        && !tablesJoined.contains(relation[1])) {
                    NodeStructure temp = new NodeStructure();
                    temp.text.name = "\u22C8 " + relation[2];
                    temp.nodeType = NodeType.Cartesian;
                    temp.children.add(buildFromNode(relation[0], queryTree.fromMap));
                    temp.children.add(buildFromNode(relation[1], queryTree.fromMap));
                    tempNodes.put(temp, new HashSet<>());
                    tempNodes.get(temp).add(relation[0]);
                    tempNodes.get(temp).add(relation[1]);
                    tablesJoined.add(relation[0]);
                    tablesJoined.add(relation[1]);
                }
                else if (!tablesJoined.contains(relation[0])){
                    Iterator it = tempNodes.entrySet().iterator();
                    Map.Entry<NodeStructure, Set<String>> pair = null;
                    while (it.hasNext()) {
                        pair = (Map.Entry)it.next();
                        Set<String> value = (Set<String>) pair.getValue();
                        if (value.contains(relation[1])) {
                            tempNodes.remove(pair.getKey());
                            break;
                        }
                    }
                    NodeStructure temp = new NodeStructure();
                    temp.text.name = "\u22C8 " + relation[2];
                    temp.nodeType = NodeType.Cartesian;
                    temp.children.add(pair.getKey());
                    temp.children.add(buildFromNode(relation[0], queryTree.fromMap));
                    tempNodes.put(temp, new HashSet<>());
                    tempNodes.get(temp).add(relation[0]);
                    for (String table:pair.getValue())
                        tempNodes.get(temp).add(table);
                    tablesJoined.add(relation[0]);
                }
                else if (!tablesJoined.contains(relation[1])){
                    Iterator it = tempNodes.entrySet().iterator();
                    Map.Entry<NodeStructure, Set<String>> pair = null;
                    while (it.hasNext()) {
                        pair = (Map.Entry)it.next();
                        Set<String> value = (Set<String>) pair.getValue();
                        if (value.contains(relation[0])) {
                            tempNodes.remove(pair.getKey());
                            break;
                        }
                    }
                    NodeStructure temp = new NodeStructure();
                    temp.text.name = "\u22C8 " + relation[2];
                    temp.nodeType = NodeType.Cartesian;
                    temp.children.add(pair.getKey());
                    temp.children.add(buildFromNode(relation[1], queryTree.fromMap));
                    tempNodes.put(temp, new HashSet<>());
                    tempNodes.get(temp).add(relation[1]);
                    for (String table:pair.getValue())
                        tempNodes.get(temp).add(table);
                    tablesJoined.add(relation[1]);
                }
                else {
                    Iterator it = tempNodes.entrySet().iterator();
                    Map.Entry<NodeStructure, Set<String>> left = null;
                    while (it.hasNext()) {
                        left = (Map.Entry)it.next();
                        Set<String> value = (Set<String>) left.getValue();
                        if (value.contains(relation[1])) {
                            tempNodes.remove(left.getKey());
                            break;
                        }
                    }
                    Map.Entry<NodeStructure, Set<String>> right = null;
                    while (it.hasNext()) {
                        right = (Map.Entry)it.next();
                        Set<String> value = (Set<String>) right.getValue();
                        if (value.contains(relation[1])) {
                            tempNodes.remove(right.getKey());
                            break;
                        }
                    }
                    NodeStructure temp = new NodeStructure();
                    temp.text.name = "\u22C8 " + relation[2];
                    temp.nodeType = NodeType.Cartesian;
                    temp.children.add(left.getKey());
                    temp.children.add(right.getKey());
                    tempNodes.put(temp, new HashSet<>());
                    for (String table:left.getValue())
                        tempNodes.get(temp).add(table);
                    for (String table:left.getValue())
                        tempNodes.get(temp).add(table);
                }
            }
            for (NodeStructure joinChild:tempNodes.keySet())
                node.children.add(joinChild);
        }
        else {
            for (String from:queryTree.fromMap.keySet())
            node.children.add(buildFromNode(from, queryTree.fromMap));
        }
        return node;
    }

    private static NodeStructure buildFromNode(String from, Map<String, List<WhereNode>> fromMap) {
        if (fromMap.get(from).isEmpty())
            return new NodeStructure(from, NodeType.Relation);
        NodeStructure node = new NodeStructure();
        StringJoiner sj = new StringJoiner(" ");
        for (WhereNode where:fromMap.get(from)) {
            sj.add(where.exp1);
            sj.add(where.opr);
            sj.add(where.exp2);
        }
        node.text.name = "\u03C3 " + sj.toString();
        node.nodeType = NodeType.Select;
        node.children.add(new NodeStructure(from, NodeType.Relation));
        return node;
    }
}