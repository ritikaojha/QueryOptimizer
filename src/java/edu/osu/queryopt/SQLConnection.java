/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.osu.queryopt;
import java.sql.*;
import javax.xml.transform.Result;
/**
 *
 * @author kathy
 */
public class SQLConnection {
    public static void Connect(String[] args) {
		
        // Create a variable for the connection string.
        String connectionUrl = "jdbc:sqlserver://localhost:1433;" +  
                "databaseName=queryopt;user=queryoptUser;password=query123;";

        // Declare the JDBC objects.
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
		
        try {
            // Establish the connection.
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            con = DriverManager.getConnection(connectionUrl);
            
            System.out.println("Connected to database !");
            
            // Create and execute an SQL statement that returns some data.
            String SQL = "SELECT TOP 10 * FROM dbo.GeneralInfo";
            stmt = con.createStatement();
            rs = stmt.executeQuery(SQL);

            // Iterate through the data in the result set and display it.
            while (rs.next()) {
                System.out.println(rs.getString(4) + " " + rs.getString(6));
            }
        }

        // Handle any errors that may have occurred.
        catch (Exception e) {
            e.printStackTrace();
        }

        finally {
            if (rs != null) try { rs.close(); } catch(Exception e) {}
            if (stmt != null) try { stmt.close(); } catch(Exception e) {}
            if (con != null) try { con.close(); } catch(Exception e) {}
        }
    }
}
