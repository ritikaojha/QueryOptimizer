/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.osu.queryopt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.osu.queryopt.entity.Config;

/**
 *
 * @author ritika
 */
public class SessionHandler {
    
    public String getExpressionTree(String query) {
        Config config = Visualizer.buildExpressionTree(query);
        Gson gson = new GsonBuilder().create();
        return gson.toJson(config);
    }
}
