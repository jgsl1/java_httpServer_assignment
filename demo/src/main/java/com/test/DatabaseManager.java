package com.test;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class DatabaseManager{
    public boolean validateUserFromDatabase(String username, String password) {          //验证用户信息，本质就是发送一个查找的SQL语句给mysql数据库
        String url = "jdbc:mysql://192.168.1.5:3306/user_db";
        String dbUsername = "root"; // 数据库用户名
        String dbPassword = "123456"; // 数据库密码
    
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
    
        try (Connection conn = DriverManager.getConnection(url, dbUsername, dbPassword);
            PreparedStatement stmt = conn.prepareStatement(sql)) {
    
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // 如果存在匹配的用户，返回 true
    
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean saveUserToDatabase(String username, String password) {            //保存用户信息的本质就是insert语句
        String url = "jdbc:mysql://192.168.1.5:3306/user_db";
        String dbUsername = "root"; // 替换为你的数据库用户名
        String dbPassword = "123456"; // 替换为你的数据库密码
    
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
    
        try (Connection conn = DriverManager.getConnection(url, dbUsername, dbPassword);
            PreparedStatement stmt = conn.prepareStatement(sql)) {
    
            stmt.setString(1, username);
            stmt.setString(2, password);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
    
        } catch (SQLException e) {
            return false;
        }
    }

}