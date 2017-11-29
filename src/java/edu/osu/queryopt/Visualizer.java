/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.osu.queryopt;

import edu.osu.queryopt.entity.Condition;
import edu.osu.queryopt.entity.Condition.ConditionType;
import edu.osu.queryopt.entity.Config;
import edu.osu.queryopt.entity.NodeStructure;
import edu.osu.queryopt.entity.NodeStructure.NodeType;
import edu.osu.queryopt.entity.SelectFromWhereNode;
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
    static Set<String> keywords = new HashSet<>(Arrays.asList("SELECT", "FROM", "WHERE", "AND"));
    
    static List<Config> buildExpressionTree(String query) {
        List<Config> config = new ArrayList();
        String[] rawTokens = query.toUpperCase()
                .replace("=", " = ")
                .replace(",", " , ")
                .replace("(", " ( ")
                .replace(")", " ) ")
                .split("\\s+");
        Queue<String> tokens = new LinkedList<>(Arrays.asList(rawTokens));
        SelectFromWhereNode queryTree;
        try {
            //Schema.Initialize();
            queryTree = buildQueryTree(tokens);
            NodeStructure node = buildSelectNode(queryTree);
            config.add(new Config(node));
            List<NodeStructure> nodeList = HeuristicOptimizer.Optimize(node);
            for (NodeStructure n:nodeList)
                config.add(new Config(n));
            return config;
        } catch (Exception ex) {
            Logger.getLogger(Visualizer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    // TODO: Validate the query
    static SelectFromWhereNode buildQueryTree(Queue<String> tokens) throws Exception {
        if (tokens.isEmpty())
            return null;
        if (!tokens.poll().equals("SELECT"))
            throw new Exception("Invalid query");
        SelectFromWhereNode query = new SelectFromWhereNode();
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
            SelectFromWhereNode queryExp1 = null;
            String table1 = null;
            String opr = null;
            String exp2 = null;
            SelectFromWhereNode queryExp2 = null;
            String table2 = null;
            
            String temp = tokens.poll();
            if(!tokens.isEmpty()&&temp.equals("AND"))
                temp = tokens.poll();
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
                while(!tokens.isEmpty()&&!tokens.peek().equals("IN")
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
            
           /* if (table1 != null
                    && table2 != null
                    && query.fromMap.containsKey(table1)
                    && query.fromMap.containsKey(table2))
                query.joinOn.add(new String[]{table1, table2,
                        exp1 + " " + opr + " " + exp2});
                
            else {
                query.whereList.add(
                        new WhereNode(exp1, queryExp1, opr, exp2, queryExp2));
            }*/
            query.whereList.add(
                        new WhereNode(exp1, queryExp1, opr, exp2, queryExp2));
        }
        return query;
    }
    
    static NodeStructure buildSelectNode(SelectFromWhereNode queryTree) {
        NodeStructure node = new NodeStructure(NodeType.Project);
        NodeStructure ptr = node;
        
        for (String select:queryTree.selectList)
            ptr.AddCondition(select);

        NodeStructure whereNode = new NodeStructure(NodeType.Select);
        for (WhereNode w:queryTree.whereList) {
            if(w.exp2.matches("[-+]?\\d*\\.?\\d+") || 
                    w.exp2.contains("\"") || 
                    w.exp2.contains("'"))
                whereNode.AddCondition(w.exp1, w.exp2, w.opr, ConditionType.Select);
            else
                whereNode.AddCondition(w.exp1, w.exp2, w.opr, ConditionType.Join);
            //ptr.children.add(buildWhereCondition(w));
            //ptr = ptr.children.get(0);
        }
        ptr.AddChild(whereNode);
        ptr = ptr.GetChild(0);
        /*
        if (!queryTree.joinOn.isEmpty()) {
            Set<String> tablesJoined = new HashSet<>();
            Map<NodeStructure, Set<String>> tempNodes = new HashMap<>();
            for (String[] relation:queryTree.joinOn) {
                if (!tablesJoined.contains(relation[0])
                        && !tablesJoined.contains(relation[1])) {
                    NodeStructure cartesianSubtree = new NodeStructure(NodeType.Cartesian);
                    NodeStructure selectSubtree = new NodeStructure(NodeType.Select);
                    selectSubtree.AddCondition(relation[2]);
                    
                    cartesianSubtree.children.add(buildFromNode(relation[0], queryTree.fromMap));
                    cartesianSubtree.children.add(buildFromNode(relation[1], queryTree.fromMap));
                    selectSubtree.children.add(cartesianSubtree);
                    tempNodes.put(selectSubtree, new HashSet<>());
                    tempNodes.get(selectSubtree).add(relation[0]);
                    tempNodes.get(selectSubtree).add(relation[1]);
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
                    NodeStructure cartesianSubtree = new NodeStructure(NodeType.Cartesian);
                    NodeStructure selectSubtree = new NodeStructure(NodeType.Select);
                    selectSubtree.AddCondition(relation[2]);
                    
                    cartesianSubtree.children.add(pair.getKey());
                    cartesianSubtree.children.add(buildFromNode(relation[0], queryTree.fromMap));
                    selectSubtree.children.add(cartesianSubtree);
                    tempNodes.put(selectSubtree, new HashSet<>());
                    tempNodes.get(selectSubtree).add(relation[0]);
                    for (String table:pair.getValue())
                        tempNodes.get(selectSubtree).add(table);
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
                    NodeStructure cartesianSubtree = new NodeStructure(NodeType.Cartesian);
                    NodeStructure selectSubtree = new NodeStructure(NodeType.Select);
                    selectSubtree.AddCondition(relation[2]);
                    
                    cartesianSubtree.children.add(pair.getKey());
                    selectSubtree.children.add(cartesianSubtree);
                    cartesianSubtree.children.add(buildFromNode(relation[1], queryTree.fromMap));
                    tempNodes.put(selectSubtree, new HashSet<>());
                    tempNodes.get(selectSubtree).add(relation[1]);
                    for (String table:pair.getValue())
                        tempNodes.get(selectSubtree).add(table);
                    tablesJoined.add(relation[1]);
                }
                else {
                    Iterator it = tempNodes.entrySet().iterator();
                    Map.Entry<NodeStructure, Set<String>> left = null;
                    while (it.hasNext()) {
                        left = (Map.Entry)it.next();
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
                    NodeStructure cartesianSubtree = new NodeStructure(NodeType.Cartesian);
                    NodeStructure selectSubtree = new NodeStructure(NodeType.Select);
                    selectSubtree.AddCondition(relation[2]);
                    
                    cartesianSubtree.children.add(left.getKey());
                    cartesianSubtree.children.add(right.getKey());
                    selectSubtree.children.add(cartesianSubtree);
                    tempNodes.put(selectSubtree, new HashSet<>());
                    for (String table:left.getValue())
                        tempNodes.get(selectSubtree).add(table);
                    for (String table:left.getValue())
                        tempNodes.get(selectSubtree).add(table);
                }
            }
            for (NodeStructure joinChild:tempNodes.keySet())
                ptr.children.add(joinChild);
        }
        else {*/
            NodeStructure fromNode = new NodeStructure(NodeType.Cartesian);
            int counter = 0;
            for (String from:queryTree.fromMap.keySet()){
                if(counter < 2){
                    fromNode.AddChild(buildFromNode(from, queryTree.fromMap));
                    counter++;
                }
                else {
                    //create left deep join
                    NodeStructure temp = new NodeStructure(NodeType.Cartesian);
                    temp.AddChild(fromNode);
                    temp.AddChild(buildFromNode(from, queryTree.fromMap));
                    fromNode = temp;
                }
            }
            ptr.AddChild(fromNode);
        //}
        return node;
    }
    /*
    private static String buildWhereCondition(WhereNode where) {
        StringJoiner sj = new StringJoiner(" ");
        sj.add(where.exp1);
        sj.add(where.opr);
        sj.add(where.exp2);
        return sj.toString();
    }
    
    private static NodeStructure buildWhereNode(WhereNode where) {
        NodeStructure node = new NodeStructure(NodeType.Select);
        StringJoiner sj = new StringJoiner(" ");
        sj.add(where.exp1);
        sj.add(where.opr);
        sj.add(where.exp2);
        node.AddCondition(sj.toString());
        return node;
    }*/

    private static NodeStructure buildFromNode(String from, Map<String, List<WhereNode>> fromMap) {
        if (fromMap.get(from).isEmpty())
            return new NodeStructure(from, NodeType.Relation);
        NodeStructure node = null;
        NodeStructure ptr = node;
        for (WhereNode where:fromMap.get(from)) {
            if (node == null) {
                node = new NodeStructure(NodeType.Select);
            }
            node.AddCondition(where.exp1, where.exp2, where.opr, ConditionType.Join);
            //else {
                //ptr.children.add(buildWhereNode(where));
                //ptr = ptr.children.get(0);
            //}
        }
        ptr = ptr.GetChild(0);
        ptr.AddChild(new NodeStructure(from));
        return node;
    }
}
