package com.sanyanyu.viewer.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.sanyanyu.viewer.constants.FinalConstants;
import com.sanyanyu.viewer.entity.TreeMode;

public class SFTPChannel {
    Session session = null;
    Channel channel = null;


    public ChannelSftp getChannel(Map<String, String> sftpDetails, int timeout) throws JSchException {

        String ftpHost = sftpDetails.get(FinalConstants.SFTP_REQ_HOST);
        String port = sftpDetails.get(FinalConstants.SFTP_REQ_PORT);
        String ftpUserName = sftpDetails.get(FinalConstants.SFTP_REQ_USERNAME);
        String ftpPassword = sftpDetails.get(FinalConstants.SFTP_REQ_PASSWORD);

        int ftpPort = Integer.parseInt(PropertiesUtil.getValue("sftp_port"));
        if (port != null && !port.equals("")) {
            ftpPort = Integer.valueOf(port);
        }

        JSch jsch = new JSch(); // 创建JSch对象
        session = jsch.getSession(ftpUserName, ftpHost, ftpPort); // 根据用户名，主机ip，端口获取一个Session对象
        if (ftpPassword != null) {
            session.setPassword(ftpPassword); // 设置密码
        }
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config); // 为Session对象设置properties
        session.setTimeout(timeout); // 设置timeout时间
        session.connect(); // 通过Session建立链接
        channel = session.openChannel("sftp"); // 打开SFTP通道
        channel.connect(); // 建立SFTP通道的连接
        return (ChannelSftp) channel;
    }

    public void closeChannel() throws Exception {
        if (channel != null) {
            channel.disconnect();
        }
        if (session != null) {
            session.disconnect();
        }
    }
    
    public void downLoadFile(String host,String filePath,String fileName,String dest){
		Map<String, String> sftpDetails = new HashMap<String, String>();
		// 设置主机ip，端口，用户名，密码
		sftpDetails.put(FinalConstants.SFTP_REQ_HOST, host);
		sftpDetails.put(FinalConstants.SFTP_REQ_USERNAME, PropertiesUtil.getValue("user_name"));
		sftpDetails.put(FinalConstants.SFTP_REQ_PASSWORD, PropertiesUtil.getValue("password"));
		sftpDetails.put(FinalConstants.SFTP_REQ_PORT, PropertiesUtil.getValue("sftp_port"));
		
		try {
			ChannelSftp chSftp = getChannel(sftpDetails, 600000);
			chSftp.cd(PropertiesUtil.getValue("workpath"));
			chSftp.cd(filePath);
			chSftp.get(fileName,dest);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
    }
    
    public void downLoadAllFile(List<String[]> list,String dest){
    	Map<String, String> sftpDetails = null;
    	Map<String,List<String[]>> map = new HashMap<String,List<String[]>>();   //key = host; value = fileList
    	
    	List<String[]> temp = null;
    	for(String arr[]:list){
    		temp = map.get(arr[2]);
    		if(temp == null){
    			temp = new ArrayList<String[]>();
    		}
    		temp.add(new String[]{arr[0],arr[1]});
    		map.put(arr[2], temp);
    	}
    	String host_info = PropertiesUtil.getValue("host_list");
    	String hostArr[] = host_info.split(",");
    	for(String host:hostArr){
    		temp = map.get(host);
    		if(temp != null){
	    		sftpDetails = new HashMap<String, String>();
	    		// 设置主机ip，端口，用户名，密码
	    		sftpDetails.put(FinalConstants.SFTP_REQ_HOST, host);
	    		sftpDetails.put(FinalConstants.SFTP_REQ_USERNAME, PropertiesUtil.getValue("user_name"));
	    		sftpDetails.put(FinalConstants.SFTP_REQ_PASSWORD, PropertiesUtil.getValue("password"));
	    		sftpDetails.put(FinalConstants.SFTP_REQ_PORT, PropertiesUtil.getValue("sftp_port"));
	    		
	    		ChannelSftp chSftp = null;
    		
	    		try {
					chSftp = getChannel(sftpDetails, 60000);
					for(String arr[]:temp){
						chSftp.cd(PropertiesUtil.getValue("workpath"));
						chSftp.cd(arr[0]);
						FileUtils.createDir(dest+"/"+arr[0]+"/");
						chSftp.get(arr[1],dest+"/"+arr[0]+"/"+arr[1]);
		    		}
					chSftp.quit();
			        closeChannel();
				} catch (Exception e) {
					//连接失败
				}
	    		
	    		
	    	}
    	}
    }
    
    
    /**
     * 初始化树
     * @return
     */
    public TreeMode initTreeMode(){
    	TreeMode treeMode = new TreeMode();
    	List<String[]> fileList = new ArrayList<String[]>();
		Connection con = null;
		try {
			con = DBUtil.getConnection();
			ResultSet rs = con.createStatement().executeQuery("select filepath,filename,hostip from file_path where status is null order by filepath,filename");    
		    String filePath = null;
		    String hostIp = null;
		    String fileName = null;
		    while(rs.next()){
		    	filePath = rs.getString(1);
		    	fileName = rs.getString(2);
		    	hostIp = rs.getString(3);
		    	fileList.add(new String[]{filePath,fileName,hostIp});
		    }
		    DBUtil.releasers(rs);
		    DBUtil.releasecon(con);
		    
		    
		    List<String> root = new ArrayList<String>();
		    Map<String,List<String>> level1 = new HashMap<String,List<String>>();
		    Map<String,List<String>> level2 = new HashMap<String,List<String>>();
		    Map<String,List<String>> level3 = new HashMap<String,List<String>>();
		    String cur[] = null;
		    List<String> temp = null;
		    for(String[] arr:fileList){
		    	cur = arr[0].split("/");
		    	if(!root.contains(cur[1])){
		    		root.add(cur[1]);		// 添加一级目录
		    	}
		    	temp = level1.get(cur[1]);
		    	if(temp == null){
		    		temp = new ArrayList<String>();
		    	}
		    	if(!temp.contains(cur[2])){ //添加二级目录
		    		temp.add(cur[2]);
		    		level1.put(cur[1], temp);   
		    	}
		    	temp = level2.get(cur[1]+cur[2]);
		    	if(temp == null){
		    		temp = new ArrayList<String>();
		    	}
		    	if(!temp.contains(cur[3])){
		    		temp.add(cur[3]);
		    		level2.put(cur[1]+cur[2], temp);   
		    	}
		    	temp = level3.get(cur[1]+cur[2]+cur[3]);
		    	if(temp == null){
		    		temp = new ArrayList<String>();
		    	}
		    	if(!temp.contains(arr[1])){
		    		temp.add(arr[1]+"@"+arr[2]);
		    		level3.put(cur[1]+cur[2]+cur[3], temp);   
		    	}
		    }
		    
		    treeMode.setRoot(root);
		    treeMode.setLevel1(level1);
		    treeMode.setLevel2(level2);
		    treeMode.setLevel3(level3);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return treeMode;
	    
    }
    
    /**
     * 更新树结构
     */
    public List<String> updateTreeMode(){
    	List<String> messageList = new ArrayList<String>();
    	try {
    		
			String host_info = PropertiesUtil.getValue("host_list");
			String hostArr[] = host_info.split(",");
			List<String[]> recordList = new ArrayList<String[]>();
			for(String host:hostArr){
				Map<String, String> sftpDetails = new HashMap<String, String>();
			     //设置主机ip，端口，用户名，密码
				sftpDetails.put(FinalConstants.SFTP_REQ_HOST, host);
	    		sftpDetails.put(FinalConstants.SFTP_REQ_USERNAME, PropertiesUtil.getValue("user_name"));
	    		sftpDetails.put(FinalConstants.SFTP_REQ_PASSWORD, PropertiesUtil.getValue("password"));
	    		sftpDetails.put(FinalConstants.SFTP_REQ_PORT, PropertiesUtil.getValue("sftp_port"));
			    
			    
			    ChannelSftp chSftp = null;
			    try {
					chSftp = getChannel(sftpDetails, 600000);
				} catch (Exception e) {
					messageList.add("主机("+host+")连接失败");
					continue;
				}
			    String path = PropertiesUtil.getValue("workpath");
			    chSftp.cd(path);
			    @SuppressWarnings("unchecked")
				Vector<LsEntry> v = chSftp.ls("*");
			    for(LsEntry ls:v){
			    	String dir = ls.getFilename();
			    	
			    	if(!"root".equals(dir)){
			        	chSftp.cd(path +"/"+ dir);  //进入网站目录 
			        	@SuppressWarnings("unchecked")
						Vector<LsEntry> v1 = chSftp.ls("*");
			        	for(LsEntry ls1:v1){
			        		String dir1 = ls1.getFilename();
			        		chSftp.cd(path+"/"+dir+"/"+dir1);  //进入类别目录
			        		@SuppressWarnings("unchecked")
							Vector<LsEntry> v2 = chSftp.ls("*");
			        		for(LsEntry ls2:v2){
			        			String dir2 = ls2.getFilename();
			        			chSftp.cd(path+"/"+dir+"/"+dir1+"/"+dir2);
								@SuppressWarnings("unchecked")
								Vector<LsEntry> v3 = chSftp.ls("*");	//进入日期目录
			        			for(LsEntry ls3:v3){
			        				recordList.add(new String[]{host,path,"/"+dir+"/"+dir1+"/"+dir2,ls3.getFilename()});
			        			}
			        		}
			        	}
			    	}
			    }
			    
			    chSftp.quit();
			    closeChannel();
			}
			Connection con = DBUtil.getConnection();
			con.createStatement().execute("delete from file_path");
			PreparedStatement ps = con.prepareStatement("insert into file_path(hostip,workpath,filepath,filename) values(?,?,?,?)");
			for(String[] arr:recordList){
				ps.setString(1, arr[0]);
				ps.setString(2, arr[1]);
				ps.setString(3, arr[2]);
				ps.setString(4, arr[3]);
				ps.addBatch();
			}
			ps.executeBatch();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return  messageList;
    }
    
    public static void main(String[] args){
    	SFTPChannel sftpChannel = new SFTPChannel();
//    	updateTreeMode
//    	sftpChannel.initTreeMode();
//    	FileUtils.createDir("亚马逊/冷柜/2015-03-31/");
//    	List<String[]> list = new ArrayList<String[]>();
//    	list.add(new String[]{"中粮我买/方便面/2015-03-29","product.csv","192.168.1.164"});
//    	sftpChannel.downLoadAllFile(list,"E://");
//    	sftpChannel.downLoadFile("192.168.1.164", "/home/hadoop/crawler/data/crawl/中粮我买/方便面/2015-03-29", "product.csv", "E:");
    	sftpChannel.updateTreeMode();
    }
    
    
    
}