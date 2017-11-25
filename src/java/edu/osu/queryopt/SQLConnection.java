/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.osu.queryopt;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.xml.transform.Result;
/**
 *
 * @author kathy
 */
public class SQLConnection {
    // Declare the JDBC objects.
    private static Connection con = null;
    private static Statement stmt = null;
    private static ResultSet rs = null;
    
    public static List<List<String>> Query(String query, int numCols){
        Connect();
        List<List<String>> result = null;
        rs = null;
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            result = new ArrayList<>();
            // Iterate through the data in the result set and display it.
            while (rs.next()) {
                List<String> row = new ArrayList<>();
                for(int i = 1; i <= numCols; i++){
                    row.add(rs.getString(i));
                }
                result.add(row);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        Disconnect();
        return result;
    }
    
    private static void Connect() {
		
        // Create a variable for the connection string.
        String connectionUrl = "jdbc:sqlserver://localhost:1433;" +  
                "databaseName=queryopt;user=queryoptUser;password=query123;";

        try {
            // Establish the connection.
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            con = DriverManager.getConnection(connectionUrl);
            
            System.out.println("Connected to database !");
        }

        // Handle any errors that may have occurred.
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void Disconnect(){
        if (rs != null) try { rs.close(); } catch(Exception e) {}
        if (stmt != null) try { stmt.close(); } catch(Exception e) {}
        if (con != null) try { con.close(); } catch(Exception e) {}
    }
}
