package com.sanyanyu.viewer.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBUtil{
	
	
	static {
		String driver = PropertiesUtil.getValue("driver");
	  try {
		   Class.forName(driver);
	  } catch (ClassNotFoundException e) {
		  throw new ExceptionInInitializerError(e);
	  }
	}
	
	public static Connection getConnection() throws SQLException {
		
		 String url = PropertiesUtil.getValue("dburl");
		 String username = PropertiesUtil.getValue("dbusername");
		 String password = PropertiesUtil.getValue("dbpassword");
		 return DriverManager.getConnection(url, username, password);
	}
	
	public static void releaseps(PreparedStatement ps){
		try {
			if(ps != null){
				ps.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void releasers(ResultSet rs){
		try {
			if(rs != null){
				rs.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void releasecon(Connection con){
		try {
			if(con != null){
				con.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void free(ResultSet rs, Statement stmt, Connection conn) {
		try {
			if (rs != null) {
				rs.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				try {
					if (conn != null) {
						conn.close();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	
	
	
	public DBUtil(){
		
	}
	
	
	
	
}