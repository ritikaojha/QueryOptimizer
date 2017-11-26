/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.osu.queryopt;
import edu.osu.queryopt.entity.Attribute;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/**
 *
 * @author kathy
 */
public class Schema {
    static List<List<String>> schema;
    
    public static boolean Initialize(){
        schema = SQLConnection.Query("SELECT T.[name] AS [table_name], AC.[name] AS [column_name] " +
                "FROM sys.[tables] AS T, sys.[all_columns] AS AC " +
                "WHERE T.[object_id] = AC.[object_id];", 2);
        
        return !schema.isEmpty();
    }
    
    public static List<String> GetRelations(String attribute){
        List<String> result = new ArrayList<>();
        if(!schema.isEmpty()){
            //schema has two lists: relation and attribute
            for(int i = 0; i < schema.get(1).size(); i++){
                if(schema.get(1).get(i).equals(attribute)){
                    result.add(schema.get(0).get(i));
                }
            }
        }
        return result;
    }
    
    public static boolean AttrInRelation(Attribute attr, String rel){
        String relation = attr.GetRelation();
        if(relation.isEmpty() && !schema.isEmpty()){
            for (List<String> pair:schema){
                if(pair.get(0).equals(rel) && attr.ToString().equals(pair.get(1)))
                    return true;
            }
        } else if (relation.equals(rel)){
            return true;
        }
        return false;
    }
}
