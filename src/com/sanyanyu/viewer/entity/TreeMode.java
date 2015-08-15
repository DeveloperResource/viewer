package com.sanyanyu.viewer.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreeMode {
	private List<String> root = new ArrayList<String>();
	private Map<String,List<String>> level1 = new HashMap<String,List<String>>();
	private Map<String,List<String>> level2 = new HashMap<String,List<String>>();
	private Map<String,List<String>> level3 = new HashMap<String,List<String>>();
	public List<String> getRoot() {
		return root;
	}
	public void setRoot(List<String> root) {
		this.root = root;
	}
	public Map<String, List<String>> getLevel1() {
		return level1;
	}
	public void setLevel1(Map<String, List<String>> level1) {
		this.level1 = level1;
	}
	public Map<String, List<String>> getLevel2() {
		return level2;
	}
	public void setLevel2(Map<String, List<String>> level2) {
		this.level2 = level2;
	}
	public Map<String, List<String>> getLevel3() {
		return level3;
	}
	public void setLevel3(Map<String, List<String>> level3) {
		this.level3 = level3;
	}
	
	
	
}
