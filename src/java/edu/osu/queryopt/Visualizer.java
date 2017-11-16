/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.osu.queryopt;

import edu.osu.queryopt.entity.Config;
import edu.osu.queryopt.entity.NodeStructure;
import edu.osu.queryopt.entity.NodeStructure.NodeType;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.StringJoiner;

/**
 *
 * @author ritika
 */
public class Visualizer {
    
    static Set<String> keywords = new HashSet<>(Arrays.asList("SELECT", "FROM", "WHERE", "JOIN", "HAVING",
            "GROUP", "ORDER", "UNION", "INTERSECT", "EXCEPT"));
    
    static Config buildExpressionTree(String query) {
        Config config = new Config();
        PriorityQueue<String> tokens = tokenize(query.toUpperCase());
        System.out.println(tokens.toString());
        NodeStructure node = buildTree(tokens);
        config.nodeStructure = HeuristicOptimizer.Optimize(node);
        return config;
    }
    
    static PriorityQueue<String> tokenize(String query) {
        Comparator<String> comparator = new TokenComparator();
        PriorityQueue<String> tokens = new PriorityQueue<>(comparator);
        String[] rawTokens = query.split("\\s+");
        StringJoiner joiner = new StringJoiner(" ");
        for (String s:rawTokens) {
            if (keywords.contains(s) && joiner.length()>0) {
                tokens.add(joiner.toString());
                joiner = new StringJoiner(" ");
            }
            joiner.add(s);
        }
        if (joiner.length()>0)
            tokens.add(joiner.toString());
        return tokens;
    }
    
    // TODO: Validate the query
    static NodeStructure buildTree(PriorityQueue<String> tokens) {
        if (tokens.isEmpty())
            return null;
        NodeStructure node = new NodeStructure();
        String token = tokens.poll();
        NodeType nodeType;
        if(token.contains("WHERE"))
            nodeType = NodeType.Select;
        else if(token.contains("SELECT"))
            nodeType = NodeType.Project;
        else if(token.contains("JOIN"))
            nodeType = NodeType.Cartesian;
        else if(token.contains("FROM"))
            nodeType = NodeType.Relation;
        else
            nodeType = NodeType.None;
        
        if(token.contains("WHERE") || token.contains("SELECT")){
            node = new NodeStructure(token, nodeType);
            NodeStructure child = buildTree(tokens);
            if (child != null)
                node.children.add(child);
        }
        else if ( token.contains("FROM")) {
            node.text.name = format(token);
            node.nodeType = nodeType;
            NodeStructure child = buildTree(tokens);
            if (child != null)
                node.children.add(child);
        }
        else if (token.contains("JOIN")) {
            node.text.name = token;
            node.nodeType = nodeType;
            NodeStructure child = buildTree(tokens);
            if (child != null)
                node.children.add(child);
        }
        return node;
    }
    
    static String format(String s) {
        if (s.contains("SELECT"))
            return s.replace("SELECT", "\u03C0");
        if (s.contains("FROM"))
            return s.replace("FROM", "");
        if (s.contains("WHERE"))
            return s.replace("WHERE", "\u03C3");
        if (s.contains("JOIN"))
            return s.replace("JOIN", "\u03A7");
        return s;
    }
}

