package com.sanyanyu.viewer.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 文件操作工具类
 * 
 * @Description: 实现文件的创建、删除、复制以及目录的创建、删除、复制等功能
 * @author Ivan 2862099249@qq.com
 * @date 2015年3月19日 下午8:30:35
 * @version V1.0
 */
public class FileUtils{


	/***
	 * 将内容添加到文件中
	 * @param fileName
	 * @param content
	 */
	
	public static void appendFile(String fileName, String content) {
		try {
			// 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
			FileWriter writer = new FileWriter(fileName, true);
			writer.write(content);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
     * 检测目录是否存在,如不在，新建
     * @param path
     */
	public static void createDir(String path) {
		File file  = new File(path);
		if(!file.isDirectory()){
			file.mkdirs();
		}
	}

}
