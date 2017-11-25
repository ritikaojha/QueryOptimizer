/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.osu.queryopt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.osu.queryopt.entity.Config;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ritika
 */
public class SessionHandler {
    
    public List<String> getExpressionTree(String query) {
        List<Config> configList = Visualizer.buildExpressionTree(query);
        Gson gson = new GsonBuilder().create();
        List<String> outputTrees = new ArrayList<>();
        for (Config config: configList)
            outputTrees.add(gson.toJson(config));
        return outputTrees;
    }
}
