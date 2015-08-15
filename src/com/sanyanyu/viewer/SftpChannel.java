package com.sanyanyu.viewer;

import javax.swing.JTextArea;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SftpChannel {

	//收集操作日志到滚动面板
	private JTextArea textArea;
	
	private static Logger logger = LoggerFactory.getLogger(SftpChannel.class);
	
	public SftpChannel(JTextArea textArea){
		this.textArea = textArea;
	}
	
	
	
	public static void main(String[] args) {
		

	}

}
