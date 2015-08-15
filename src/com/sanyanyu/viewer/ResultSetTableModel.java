package com.sanyanyu.viewer;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.swing.table.AbstractTableModel;

/**
 * JTable的model实现
 * 
 * @Description: TODO
 * @author Ivan 2862099249@qq.com
 * @date 2015年5月14日 下午12:03:20
 * @version V1.0
 */
public class ResultSetTableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ResultSet rs;
	private ResultSetMetaData rsmd;

	public ResultSetTableModel(ResultSet aResultSet) {
		rs = aResultSet;

		try {
			rsmd = rs.getMetaData();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getColumnName(int column) {
		if(column == 0){
			return "ID";
		}else if(column == 1){
			return "站点";
		}else if(column == 2){
			return "类目名称";
		}else if(column == 3){
			return "类目URL";
		}
		return "";
	}

	@Override
	public int getRowCount() {

		try {
			rs.last();
			return rs.getRow();
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}

	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {

		try {
			rs.absolute(rowIndex + 1);
			return rs.getObject(columnIndex + 1);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public int getColumnCount() {
		try {
			return rsmd.getColumnCount();
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}

}
