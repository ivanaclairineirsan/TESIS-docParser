/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ahmad Fauzan
 */
public class DatabaseHelper {
    static String DRIVER = "com.mysql.jdbc.Driver";
    static String DB_URL = "jdbc:mysql://localhost:3306/newsaggregator";
    static Connection conn;
    static Statement smt;
    
    public static void Connect() {
        
        Disconnect();
        try {
            Class.forName(DRIVER);
//            System.out.println("Database config: " + databaseConfig.getConfigValue().get("DB.host") + " - " + databaseConfig.getConfigValue().get("DB.username")+" - "+ 
//                    databaseConfig.getConfigValue().get("DB.password"));

            conn = DriverManager.getConnection(DB_URL, "root", "");
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static ResultSet executeQuery(String sql) {
        try {
            smt = conn.createStatement();
            ResultSet result = smt.executeQuery(sql);
            return result;
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    
    public static ResultSet executeQuery(String sql,String[] parameter) {
        try {
            smt = conn.prepareStatement(sql);
            PreparedStatement pSmt = (PreparedStatement) smt;
            for(int i=0; i < parameter.length; i++) {
                pSmt.setString(i+1, parameter[i]);
            }
            //System.out.println(pSmt);
            ResultSet result = pSmt.executeQuery();
            return result;
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static int executeAutoInc(String sql) {
        try {
            smt = conn.createStatement();
            smt.execute(sql, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = smt.getGeneratedKeys();
            if (rs.next()) {
               return rs.getInt(1);
            }
            return -1;
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }     
    }
    
    public static boolean execute(String sql) {
        try {
            smt = conn.createStatement();
            smt.execute(sql);
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }     
    }
    
    public static boolean execute(String sql, String[] parameter) {
        try {
            smt = conn.prepareStatement(sql);
            PreparedStatement pSmt = (PreparedStatement) smt;
            for(int i=0; i < parameter.length; i++) {
                pSmt.setString(i+1, parameter[i]);
            }
            //System.out.println(pSmt);
            pSmt.execute();
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }     
    }
    
    public static int executeInsert(String sql) {
        try {
            smt = conn.createStatement();
            smt.executeUpdate(sql,Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = smt.getGeneratedKeys();
            if(rs.next()) {
                return rs.getInt(1);
            } else {
                return -1;
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }     
    }
    
    public static int executeUpdate(String sql) {
        try {
            //System.out.println("HASIL");
            smt = conn.createStatement();
            smt.executeUpdate(sql,Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = smt.getGeneratedKeys();
            if(rs.next()) {
                return rs.getInt(1);
            } else {
                return -1;
            }
        } catch (SQLException ex) {
            System.out.println("GAGAL");
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }     
    }
    
    public static int executeInsert(String sql, String[] parameter) {
        try {
            smt = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
            PreparedStatement pSmt = (PreparedStatement) smt;
            for(int i=0; i < parameter.length; i++) {
                pSmt.setString(i+1, parameter[i]);
            }
            pSmt.executeUpdate();
            ResultSet rs = pSmt.getGeneratedKeys();
            if(rs.next()) {
                return rs.getInt(1);
            } else {
                return -1;
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }     
    }
    
    public static int executeUpdate(String sql, String[] parameter) {
        try {
            System.out.println("HASIL");
            smt = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
            PreparedStatement pSmt = (PreparedStatement) smt;
            for(int i=0; i < parameter.length; i++) {
                pSmt.setString(i+1, parameter[i]);
            }
            return pSmt.executeUpdate();
//            ResultSet rs = pSmt.getGeneratedKeys();            
//            if(rs.next()) {
//                return rs.getInt(1);
//            } else {
//                System.out.println(pSmt);
//                return -1;
//            }
        } catch (SQLException ex) {
            System.out.println("GAGAL");
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }     
    }
    
    public static void Disconnect() {
        if(conn != null) {
            try {
                if(smt != null)
                    smt.close();
                conn.close();
                conn = null;
            } catch (SQLException ex) {
                Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Unable to close");
            }
        }
    }
}
