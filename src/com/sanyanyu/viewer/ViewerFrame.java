package com.sanyanyu.viewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.sql.rowset.CachedRowSet;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanyanyu.viewer.constants.FinalConstants;
import com.sanyanyu.viewer.entity.TreeMode;
import com.sanyanyu.viewer.util.DBUtil;
import com.sanyanyu.viewer.util.PropertiesUtil;
import com.sanyanyu.viewer.util.SFTPChannel;

/**
 * 电商数据查看器
 * 
 * @Description: 1、实现查看器的页面布局 2、实现多线程下载电商数据，包含开始下载，停止下载，下载进度，下载日志更新
 *               3、其他功能，例如：设置、退出、说明、关于
 * @author Ivan 2862099249@qq.com
 * @date 2015年4月2日 下午4:14:35
 * @version V1.0
 */
public class ViewerFrame extends JFrame {

	private static Logger logger = LoggerFactory.getLogger(ViewerFrame.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9018796196248623467L;

	// 数据源
	private SFTPChannel sftpChannel;
	// 树
	private JTree tree;
	// 更新下载日志的线程
	private Runnable updateTextArea;
	// 更新树的线程
	private Runnable updateTree;
	//树的滚动面板
	private JScrollPane treeScroll;
	// 下载进度条
	// private JProgressBar progressBar;
	// 需要下载的文件
	private List<String[]> filePathList;
	// 下载日志
	static private JTextArea textArea;
	// 启动下载按钮
	private JButton startButton;
	
	// 新增按钮
	private JButton addButton;
	// 查询按钮
	private JButton searchButton;
	// 删除按钮
	private JButton delButton;

	// 下载线程
	private DownLoadThread downLoadThread;
	// 同步电商数据线程
	private SyncThread syncThread;
	//同步菜单项
	private JMenuItem sync;
	
	//下载的内容面板
	private JPanel contentPanel1;
	//新增的内容面板
	private JPanel contentPanel2;
	
	//站点下拉
	private JComboBox<Item> comboBox;
	private JTextField catText;
	private JTextField urlText;
	
	public JPanel createContentPanel1(){
		// 创建右侧的内容面板
		contentPanel1 = new JPanel();
		// 设置上下布局的方式
		BoxLayout layout = new BoxLayout(contentPanel1, BoxLayout.Y_AXIS);
		contentPanel1.setLayout(layout);

		// 创建内容面板的上部区域
		JPanel topPanel = new JPanel();
		// 靠左对齐的流式布局
		topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		Border topEtched = BorderFactory.createEtchedBorder();
		Border topTitled = BorderFactory.createTitledBorder(topEtched, "下载说明");
		topPanel.setBorder(topTitled);

		// 创建上部区域的内容面板
		JPanel topContentPanel = new JPanel();
		// 设置它为上下布局的方式
		topContentPanel.setLayout(new BoxLayout(topContentPanel, BoxLayout.Y_AXIS));
		JLabel tip1 = new JLabel("<html><p>1、一次性最多只能下载<font size=\"+1\"><strong>"
				+ PropertiesUtil.getValue("download.max.site") + "</strong></font>个电商网站的数据</p></html>");
		JLabel blank1 = new JLabel(" ");
		JLabel tip2 = new JLabel("<html><p>2、默认下载文件存放在安装目录下的<font size=\"+1\"><strong>"
				+ PropertiesUtil.getValue("download.file.dir") + "</strong></font>中");
		JLabel blank2 = new JLabel(" ");
		// JLabel blank3 = new JLabel(" ");
		topContentPanel.add(tip1);
		topContentPanel.add(blank1);
		topContentPanel.add(tip2);
		topContentPanel.add(blank2);
		startButton = new StartButton();
		topContentPanel.add(startButton);
		// progressBar = new JProgressBar();
		// topContentPanel.add(blank3);
		// topContentPanel.add(progressBar);
		topPanel.add(topContentPanel);

		contentPanel1.add(topPanel);

		// 创建下部区域的下载日志显示的滚动面板
		textArea = new JTextArea(20, 25);
		textArea.setTabSize(10);
		// textArea.setFont(new Font("标楷体", Font.BOLD, 16));
		textArea.setLineWrap(true);// 激活自动换行功能
		textArea.setWrapStyleWord(true);// 激活断行不断字功能
		// textArea.setBackground(Color.pink);
		JScrollPane textAreaScroll = new JScrollPane(textArea);
		Border textAreaEtched = BorderFactory.createEtchedBorder();
		Border textAreaTitled = BorderFactory.createTitledBorder(textAreaEtched, "日志记录");
		textAreaScroll.setBorder(textAreaTitled);
		textAreaScroll.setPreferredSize(new Dimension(0, 500));
		
		contentPanel1.add(textAreaScroll);
		return contentPanel1;
	}
	
	class Item {
		private String siteCode;
		private String siteName;

		public Item(String siteCode, String siteName) {
			this.siteCode = siteCode;
			this.siteName = siteName;
		}

		public String getSiteCode() {
			return siteCode;
		}

		public String getSiteName() {
			return siteName;
		}

		public String toString() {
			return siteName;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof Item){
				Item other = (Item) obj;
				if(this.siteName != null &&
					this.siteName.equals(other.getSiteName())){
					return true;
				}
			}
			return false;
		}
	}
	
	private Connection con;
	private boolean scrolling;
	private ResultSetTableModel tableModel;
	
	@SuppressWarnings("rawtypes")
	public void fitTableColumns(JTable myTable){  
	    JTableHeader header = myTable.getTableHeader();  
	     int rowCount = myTable.getRowCount();  
	  
	     Enumeration columns = myTable.getColumnModel().getColumns();  
	     while(columns.hasMoreElements()){  
	         TableColumn column = (TableColumn)columns.nextElement();  
	         int col = header.getColumnModel().getColumnIndex(column.getIdentifier());  
	         int width = (int)myTable.getTableHeader().getDefaultRenderer()  
	                 .getTableCellRendererComponent(myTable, column.getIdentifier()  
	                         , false, false, -1, col).getPreferredSize().getWidth();  
	         for(int row = 0; row<rowCount; row++){  
	             int preferedWidth = (int)myTable.getCellRenderer(row, col).getTableCellRendererComponent(myTable,  
	               myTable.getValueAt(row, col), false, false, row, col).getPreferredSize().getWidth();  
	             width = Math.max(width, preferedWidth);  
	         }  
	         header.setResizingColumn(column); // 此行很重要  
	         column.setWidth(width+myTable.getIntercellSpacing().width);  
	     }  
	}  
	
	public Vector<Item> getComboBoxModel(){
		Vector<Item> model = new Vector<Item>();
		
		try {
			Statement stat = con.createStatement();
			ResultSet rs = stat.executeQuery("select sitecode,sitename from sys_config_url group by sitecode,sitename");    
		    while(rs.next()){
		    	model.addElement(new Item(rs.getString(1), rs.getString(2)));
		    }
		}catch(Exception e){
			logger.info("获取站点数据异常:"+e.getMessage());
		}
		return model;
	}
	
	public ResultSetTableModel getTableModel(String sql){
		ResultSetTableModel tableModel = null;
		try {
			Statement stat = con.createStatement();
			ResultSet rs = stat.executeQuery(sql);    
			if(scrolling){
				tableModel = new ResultSetTableModel(rs);
			}else{
				CachedRowSet crs = new com.sun.rowset.CachedRowSetImpl();
				crs.populate(rs);
				
				tableModel = new ResultSetTableModel(rs);
			}
			
			
		}catch(Exception e){
			logger.info("获取类目列表数据异常:"+e.getMessage());
		}
		 return tableModel;
	}
	
	private JTable table;
	
	public JPanel createContentPanel2(){
		
		//初始化数据库连接
		try {
			con = DBUtil.getConnection();
			DatabaseMetaData meta = con.getMetaData();
			if(meta.supportsResultSetType(ResultSet.TYPE_SCROLL_INSENSITIVE)){
				scrolling = true;
			}else{
				scrolling = false;
			}
		} catch (SQLException e1) {
			logger.info("初始化数据库连接异常:"+e1.getMessage());
		}
		
		// 创建右侧的内容面板
		contentPanel2 = new JPanel();
		// 设置上下布局的方式
		BoxLayout layout = new BoxLayout(contentPanel2, BoxLayout.Y_AXIS);
		contentPanel2.setLayout(layout);

		// 创建内容面板的上部区域
		JPanel topPanel = new JPanel();
		// 靠左对齐的流式布局
		topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		Border topEtched = BorderFactory.createEtchedBorder();
		Border topTitled = BorderFactory.createTitledBorder(topEtched, "新增/查询类目");
		topPanel.setBorder(topTitled);

		// 创建上部区域的内容面板
		JPanel topContentPanel = new JPanel();
		topContentPanel.add(new JLabel("站点:", JLabel.RIGHT));
		
		Vector<Item> model = getComboBoxModel();
		comboBox = new JComboBox<Item>(model);;

		topContentPanel.add(comboBox);
		
		topContentPanel.add(new JLabel("类目名称:", JLabel.RIGHT));
		catText = new JTextField(15);
		topContentPanel.add(catText);
		topContentPanel.add(new JLabel("类目URL:", JLabel.RIGHT));
		urlText = new JTextField(40);
		topContentPanel.add(urlText);
		
		searchButton = new SearchButton();
		topContentPanel.add(searchButton);
		
		addButton = new AddButton();
		topContentPanel.add(addButton);
		
		delButton = new DelButton();
		topContentPanel.add(delButton);
		
		// progressBar = new JProgressBar();
		// topContentPanel.add(blank3);
		// topContentPanel.add(progressBar);
		topPanel.add(topContentPanel);

		contentPanel2.add(topPanel);

		String sql = "select id, sitename,smallcat,url from sys_config_url order by id desc";
		tableModel = getTableModel(sql);
		table = new JTable(tableModel);  
		fitTableColumns(table);
		
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				
				int i = table.getSelectedRow();
				if(i != -1){
					String siteName = (String) tableModel.getValueAt(i, 1);
					String catName = (String) tableModel.getValueAt(i, 2);
					String catUrl = (String) tableModel.getValueAt(i, 3);
					
					for(int j = 0; j < comboBox.getItemCount(); j++){
						Item item = (Item) comboBox.getItemAt(j);
						if(siteName.equals(item.getSiteName())){
							comboBox.setSelectedIndex(j);
						}
					}
					
					catText.setText(catName);
					urlText.setText(catUrl);
				}
			}
		});
		
		JScrollPane tableScroll = new JScrollPane(table);  
		table.setFillsViewportHeight(true);  
		
		Border tableEtched = BorderFactory.createEtchedBorder();
		Border tableTitled = BorderFactory.createTitledBorder(tableEtched, "类目列表("+tableModel.getRowCount()+"条)");
		tableScroll.setBorder(tableTitled);
		tableScroll.setPreferredSize(new Dimension(0, 500));
		
		contentPanel2.add(tableScroll);
		return contentPanel2;
	}
	
	public ViewerFrame() {

		// 初始化数据源
		sftpChannel = new SFTPChannel();

		// 设置窗体名称
		this.setTitle("电商数据查看器 "+PropertiesUtil.getValue("viewer.version"));

		// 设置大小
		this.setSize(1100, 700);

		setCenterLocation(this);

		// 设置LOGO
		try {
			Image image = ImageIO.read(new File("config/logo.png"));
			this.setIconImage(image);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// 创建菜单栏
		this.setJMenuBar(new ViewMenuBar(this));

		// 创建自定义的树
		tree = buildTree();

		// 创建放置树的滚动面板
		treeScroll = new JScrollPane(tree);
		// 设置标记线
		Border treeEtched = BorderFactory.createEtchedBorder();
		Border treeTitled = BorderFactory.createTitledBorder(treeEtched, "电商数据");
		treeScroll.setBorder(treeTitled);
		// 设置大小
		treeScroll.setPreferredSize(new Dimension(220, 220));
		// 放在窗体的西部
		this.add(treeScroll, BorderLayout.WEST);

		// 创建右侧的内容面板
		contentPanel1 = createContentPanel1();

		// 放在窗体的中间
		this.add(contentPanel1, BorderLayout.CENTER);
		

		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});
		
		//工具栏
		JToolBar bar = new JToolBar();
		bar.add(new ToolBarAction("下载", new ImageIcon("config/archive.png"), "下载电商数据"));
		bar.addSeparator();
		bar.add(new ToolBarAction("新增", new ImageIcon("config/zoom_in.png"), "新增类目信息"));
		bar.addSeparator();
		bar.add(new ToolBarAction("同步", new ImageIcon("config/install2.png"), "同步电商信息"));
		this.add(bar, BorderLayout.NORTH);

		updateTextArea = new Runnable() {// 更新下载日志的线程
			public void run() {
				
				if (!startButton.isEnabled()) {
					startButton.setEnabled(true);
				}
				
				//textArea.setText("");
				for (String[] str : filePathList) {
					appendLogger(str[2] + "：" + str[0] + "/" + str[1]);
				}
				appendLogger("电商数据下载完成！！！");
			}
		};

		updateTree = new Runnable() {// 更新树的线程，并显示同步完成
			public void run() {

				appendLogger("电商数据同步完成！！！");
				if (!sync.isEnabled()) {
					sync.setEnabled(true);
				}
				
				tree = buildTree();//重新加载数据 
				tree.updateUI();//刷新视图
				//更新JTree的UI外观  
		        SwingUtilities.updateComponentTreeUI(tree);  

				JOptionPane.showMessageDialog(null, "同步电商数据成功", "温馨提示", JOptionPane.INFORMATION_MESSAGE);
			}
		};
		
		

	}

	public static String getLogPrefix(){
		return getDate() + " ==> ";
	}
	
	/**
	 * 当前日期
	 * @return
	 */
	public static String getDate() {
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		return sdf.format(new Date());
	}
	
	/**
	 * 退出系统
	 */
	public void exit() {
		Object[] options = { "确定", "取消" };
		JOptionPane pane2 = new JOptionPane("确定退出吗?", JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION, null,
				options, options[1]);
		JDialog dialog = pane2.createDialog(this, "警告");
		dialog.setVisible(true);
		Object selectedValue = pane2.getValue();
		if (selectedValue == null || selectedValue == options[1]) {
			setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // 这个是关键
		} else if (selectedValue == options[0]) {
			setDefaultCloseOperation(EXIT_ON_CLOSE);
			//DBUtil.releasecon(con);
		}
	}

	class SyncThread extends Thread {
		@Override
		public void run() {

			// 同步数据的逻辑代码
//			sftpChannel.updateTreeMode();
			
			// 通知textArea
			SwingUtilities.invokeLater(updateTree);// 将对象排到事件派发线程的队列中

			syncThread = null;// 销毁
		}
	}

	class DownLoadThread extends Thread {
		@Override
		public void run() {

			sftpChannel.downLoadAllFile(filePathList, PropertiesUtil.getValue("download.file.dir"));

			// 通知textArea
			SwingUtilities.invokeLater(updateTextArea);// 将对象排到事件派发线程的队列中

			downLoadThread = null;// 销毁
		}
	}

	/**
	 * 递归获取选中的叶子节点数据，并封装到List集合中
	 * 
	 * @param filePathList
	 * @param node
	 */
	@SuppressWarnings("unchecked")
	public static void setFilePathList(List<String[]> filePathList, CheckBoxTreeNode node) {

		if (node.isLeaf() && node.isSelected) {

			// 电商数据
			// 中粮我买
			// 剃须刀
			// 2015-03-29
			// product.csv@192.168.1.163
			TreeNode[] treeNodes = node.getPath();
			String[] filePaths = new String[treeNodes.length - 1];

			filePaths[0] = treeNodes[1].toString() + "/" + treeNodes[2].toString() + "/" + treeNodes[3].toString();
			String[] files = treeNodes[4].toString().split("@");
			filePaths[1] = files[0];
			filePaths[2] = files[1];

			filePathList.add(filePaths);
		} else {

			Enumeration<CheckBoxTreeNode> childs = node.children();
			while (childs.hasMoreElements()) {
				CheckBoxTreeNode child = childs.nextElement();

				setFilePathList(filePathList, child);
			}

		}
	}

	class StartButton extends JButton implements ActionListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = -2582666891414232694L;

		public StartButton() {

			this.setText("开始下载");
			this.setIcon(new ImageIcon("config/down.png"));

			this.addActionListener(this);

		}

		@Override
		public void actionPerformed(ActionEvent e) {

			filePathList = new ArrayList<String[]>();

			// 获取选中的checkbox
			TreeModel treeModel = tree.getModel();
			CheckBoxTreeNode root = (CheckBoxTreeNode) treeModel.getRoot();
			if (root.isSelected) {
				JOptionPane.showMessageDialog(null, "不能一次性下载全部电商数据", "温馨提示", JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			int maxSite = 0;

			// 校验
			for (int i = 0; i < root.getChildCount(); i++) {
				CheckBoxTreeNode node1 = (CheckBoxTreeNode) root.getChildAt(i);
				if (node1.isSelected) {
					maxSite++;

					if (maxSite > FinalConstants.DOWNLOAD_MAX_SITE) {
						JOptionPane.showMessageDialog(null, "一次性最多只能下载" + PropertiesUtil.getValue("download.max.site")
								+ "个电商网站的数据", "温馨提示", JOptionPane.INFORMATION_MESSAGE);
						return;
					}
				}
			}

			setFilePathList(filePathList, root);

			if (filePathList.size() > 0) {
				if (this.isEnabled()) {
					this.setEnabled(false);
				}
				// 开始下载
				if (downLoadThread == null) {
					appendLogger("正在下载电商数据，请稍后...");
					downLoadThread = new DownLoadThread();
					downLoadThread.start();
				}
			} else {
				JOptionPane.showMessageDialog(null, "请选择需要下载的文件", "温馨提示", JOptionPane.INFORMATION_MESSAGE);
				return;
			}

		}
	}
	
	class AddButton extends JButton implements ActionListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = -2582666891414232694L;

		public AddButton() {

			this.setText("新增");
			this.setIcon(new ImageIcon("config/add.png"));

			this.addActionListener(this);

		}

		@Override
		public void actionPerformed(ActionEvent e) {

			//新增
			Item item = (Item)comboBox.getSelectedItem();
			String siteCode = item.getSiteCode();
			String siteName = item.getSiteName();
			String catName = catText.getText();
			String url = urlText.getText();
			
			//System.out.println(catName);
			
			//校验
			if(catName == null || "".equals(catName.trim())){
				JOptionPane.showMessageDialog(null, "类目名称不能为空！", "温馨提示", JOptionPane.WARNING_MESSAGE);
				return;
			}
			if(url == null || "".equals(url.trim())){
				JOptionPane.showMessageDialog(null, "类目URL不能为空！", "温馨提示", JOptionPane.WARNING_MESSAGE);
				return;
			}else{
				//校验唯一性
				String sql = "select count(0) from sys_config_url where url = '"+url+"'";
				try {
					Statement stat = con.createStatement();
					ResultSet rs = stat.executeQuery(sql);
					if(rs.next() && rs.getInt(1) > 0){
						JOptionPane.showMessageDialog(null, "类目URL已经存在！", "温馨提示", JOptionPane.WARNING_MESSAGE);
						return;
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
					logger.info("类目URL校验唯一性异常:"+e1.getMessage());
				}
				
			}
			
			try {
				String sql = "insert into sys_config_url(sitecode,sitename,smallcat,url) values(?,?,?,?)";
				PreparedStatement pstmt = con.prepareStatement(sql);
				
				pstmt.setString(1, siteCode);
				pstmt.setString(2, siteName);
				pstmt.setString(3, catName);
				pstmt.setString(4, url);
				
				pstmt.execute();
				
				JOptionPane.showMessageDialog(null, "类目信息新增成功！", "温馨提示", JOptionPane.INFORMATION_MESSAGE);
				
				sql = "select id, sitename,smallcat,url from sys_config_url order by id desc";
				
				tableModel = getTableModel(sql);//更新Table model
				//构建table
				table.setModel(tableModel);
				
				table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
					
					@Override
					public void valueChanged(ListSelectionEvent e) {
						
						int i = table.getSelectedRow();
						if(i != -1){
							String siteName = (String) tableModel.getValueAt(i, 1);
							String catName = (String) tableModel.getValueAt(i, 2);
							String catUrl = (String) tableModel.getValueAt(i, 3);
							
							for(int j = 0; j < comboBox.getItemCount(); j++){
								Item item = (Item) comboBox.getItemAt(j);
								if(siteName.equals(item.getSiteName())){
									comboBox.setSelectedIndex(j);
								}
							}
							
							catText.setText(catName);
							urlText.setText(catUrl);
						}
					}
				});
				
				fitTableColumns(table);
				
				table.repaint();
		        table.updateUI();
			}catch(Exception ex){
				JOptionPane.showMessageDialog(null, "类目信息新增失败！", "温馨提示", JOptionPane.ERROR_MESSAGE);
				logger.info("保存类目信息数据异常:"+ex.getMessage());
			}
		}
	}
	
	
	
	class SearchButton extends JButton implements ActionListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = -2582666891414232694L;

		public SearchButton() {

			this.setText("查询");
			this.setIcon(new ImageIcon("config/search2.png"));

			this.addActionListener(this);

		}

		@Override
		public void actionPerformed(ActionEvent e) {

			//查询
			Item item = (Item)comboBox.getSelectedItem();
			String siteCode = item.getSiteCode();
			String catName = catText.getText();
			String url = urlText.getText();
			
			String sql = "select id, sitename,smallcat,url from sys_config_url where sitecode = '"+siteCode+"'";
			
			if(catName != null && !"".equals(catName.trim())){
				sql += " and smallcat = '"+catName+"'";
			}
			if(url != null && !"".equals(url.trim())){
				sql += " and url = '"+url+"'";
			}
			
			sql += " order by id desc";
			
			tableModel = getTableModel(sql);//更新Table model
			//构建table
			table.setModel(tableModel);
			
			table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				
				@Override
				public void valueChanged(ListSelectionEvent e) {
					
					int i = table.getSelectedRow();
					if(i != -1){
						String siteName = (String) tableModel.getValueAt(i, 1);
						String catName = (String) tableModel.getValueAt(i, 2);
						String catUrl = (String) tableModel.getValueAt(i, 3);
						
						for(int j = 0; j < comboBox.getItemCount(); j++){
							Item item = (Item) comboBox.getItemAt(j);
							if(siteName.equals(item.getSiteName())){
								comboBox.setSelectedIndex(j);
							}
						}
						
						catText.setText(catName);
						urlText.setText(catUrl);
					}
				}
			});
			
			fitTableColumns(table);
			
			table.repaint();
	        table.updateUI();
			
			//table.updateUI();//刷新视图
			//更新JTree的UI外观  
	        //SwingUtilities.updateComponentTreeUI(table);  

		}
	}

	class DelButton extends JButton implements ActionListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = -2582666891414232694L;

		public DelButton() {

			this.setText("删除");
			this.setIcon(new ImageIcon("config/del.png"));

			this.addActionListener(this);

		}

		@Override
		public void actionPerformed(ActionEvent e) {

			//获取model  
			ResultSetTableModel model = (ResultSetTableModel) table.getModel();  
			
			int[] selections = table.getSelectedRows();  
			
			if(selections.length > 0){
				//和tableModel统一序号  
				for (int i = 0; i < selections.length; i++) {  
				    selections[i] = table.convertRowIndexToModel(selections[i]);  
				}  
				
				
				//删除数据库的 
				String ids = "";
				for (int i : selections) {  
				    
				    int id = (Integer) model.getValueAt(i, 0);
				    	
				    ids += id;
				    if(i < selections.length - 1){
				    	ids += ",";
				    }
				}  
				
				
				String sql = "delete from sys_config_url where id in ("+ids+")";
				//System.out.println(sql);
				
				try {
					Statement stat = con.createStatement();
					stat.execute(sql);
					
					JOptionPane.showMessageDialog(null, "类目信息删除成功！", "温馨提示", JOptionPane.INFORMATION_MESSAGE);
					
					sql = "select id, sitename,smallcat,url from sys_config_url order by id desc";
					
					tableModel = getTableModel(sql);//更新Table model
					//构建table
					table.setModel(tableModel);
					
					table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
						
						@Override
						public void valueChanged(ListSelectionEvent e) {
							
							int i = table.getSelectedRow();
							if(i != -1){
								String siteName = (String) tableModel.getValueAt(i, 1);
								String catName = (String) tableModel.getValueAt(i, 2);
								String catUrl = (String) tableModel.getValueAt(i, 3);
								
								for(int j = 0; j < comboBox.getItemCount(); j++){
									Item item = (Item) comboBox.getItemAt(j);
									if(siteName.equals(item.getSiteName())){
										comboBox.setSelectedIndex(j);
									}
								}
								
								catText.setText(catName);
								urlText.setText(catUrl);
							}
						}
					});
					
					fitTableColumns(table);
					
					table.repaint();
			        table.updateUI();
					
				} catch (SQLException e1) {
					logger.info("保存类目信息数据异常:"+e1.getMessage());
					JOptionPane.showMessageDialog(null, "类目信息删除失败！", "温馨提示", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}
	
	/**
	 * 构建树
	 * 
	 * @return
	 */
	private JTree buildTree() {

		if(tree == null){
			tree = new JTree();
			
			tree.addMouseListener(new CheckBoxTreeNodeSelectionListener());//只能在第一次绑定事件监听器，否则第二次会失效
			
			tree.setCellRenderer(new CheckBoxTreeCellRenderer());
		}
		// 创建根节点
		CheckBoxTreeNode rootNode = new CheckBoxTreeNode("全部");

		// 构建树
		TreeMode treeMode = sftpChannel.initTreeMode();
		List<String> rootList = treeMode.getRoot();
		Map<String, List<String>> level1 = treeMode.getLevel1();
		Map<String, List<String>> level2 = treeMode.getLevel2();
		Map<String, List<String>> level3 = treeMode.getLevel3();

		Iterator<String> rootItor = rootList.iterator();
		while (rootItor.hasNext()) {
			String siteName = rootItor.next();

			CheckBoxTreeNode node1 = new CheckBoxTreeNode(siteName);

			List<String> typeList = level1.get(siteName);
			Iterator<String> typeItor = typeList.iterator();
			while (typeItor.hasNext()) {
				String type = typeItor.next();

				CheckBoxTreeNode node2 = new CheckBoxTreeNode(type);

				List<String> dateList = level2.get(siteName + type);
				Iterator<String> dateItor = dateList.iterator();
				while (dateItor.hasNext()) {
					String date = dateItor.next();

					CheckBoxTreeNode node3 = new CheckBoxTreeNode(date);

					List<String> fileList = level3.get(siteName + type + date);
					Iterator<String> fileItor = fileList.iterator();
					while (fileItor.hasNext()) {

						String file = fileItor.next();

						CheckBoxTreeNode node4 = new CheckBoxTreeNode(file);

						node3.add(node4);

					}
					node2.add(node3);
				}
				node1.add(node2);
			}
			rootNode.add(node1);
		}

		DefaultTreeModel model = new DefaultTreeModel(rootNode);
		tree.setModel(model);
		
		return tree;
	}

	class ViewMenuBar extends JMenuBar {

		final ViewerFrame frame;

		/**
		 * 
		 */
		private static final long serialVersionUID = -513385198851953609L;

		public ViewMenuBar(final ViewerFrame frame) {

			this.frame = frame;

			JMenu sysMenu = new JMenu("系统");

			// JMenuItem setting = new JMenuItem("设置", new
			// ImageIcon(this.getClass().getResource("/setting.png")));
			// setting.addActionListener(new ActionListener() {
			//
			// @Override
			// public void actionPerformed(ActionEvent e) {
			//
			// System.out.println("Menu item [" + e.getActionCommand() +
			// "] was pressed.");
			//
			// }
			// });
			// sysMenu.add(setting);

			sync = new JMenuItem("同步电商数据", new ImageIcon("config/sync.png"));
			sync.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					Object[] options = { "确定", "取消" };
					JOptionPane pane2 = new JOptionPane("确定同步电商数据吗?", JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION, null,
							options, options[1]);
					JDialog dialog = pane2.createDialog(sync, "警告");
					dialog.setVisible(true);
					Object selectedValue = pane2.getValue();
					if (selectedValue == null || selectedValue == options[1]) {
						
						setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // 这个是关键
					} else if (selectedValue == options[0]) {
						
						if(sync.isEnabled()){
							sync.setEnabled(false);
						}
						// 开始同步
						if (syncThread == null) {
							appendLogger("正在同步电商数据，请稍后...");
							syncThread = new SyncThread();
							syncThread.start();
						}
					}
					
					
				}
			});
			sysMenu.add(sync);

			JMenuItem exit = new JMenuItem("退出", new ImageIcon("config/exit.png"));
			exit.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					// System.exit(-1);
					// 发送WINDOW_CLOSING的消息给frame，然后frame就会关闭，并且windowClosing的也会响应
					frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));

				}
			});
			sysMenu.add(exit);

			JMenu helpMenu = new JMenu("帮助");
			// JMenuItem notebook = new JMenuItem("说明", new
			// ImageIcon(this.getClass().getResource("/notebook.png")));
			// notebook.addActionListener(new ActionListener() {
			//
			// @Override
			// public void actionPerformed(ActionEvent e) {
			//
			// System.out.println("Menu item [" + e.getActionCommand() +
			// "] was pressed.");
			//
			// }
			// });
			// helpMenu.add(notebook);

			final ImageIcon helpIcon = new ImageIcon("config/help.png");
			JMenuItem help = new JMenuItem("关于", helpIcon);
			help.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					final JDialog about = new JDialog(frame);

					about.setTitle("关于电商数据查看器");
					String text = "<html>" + "<p>&nbsp;Version "+PropertiesUtil.getValue("viewer.version")+"</p><br>"
							+ "<p>&nbsp;Copyright @2015</p><br>"
							+ "<p>&nbsp;Email contact@sanyanyu.com</p><br></html>";
					JLabel label = new JLabel(text);
					about.add(label, BorderLayout.CENTER);

					JPanel panel = new JPanel();
					JButton ok = new JButton("OK");
					ok.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							about.setVisible(false);

						}
					});

					panel.add(ok);
					about.add(panel, BorderLayout.SOUTH);

					about.setSize(250, 165);

					setCenterLocation(about);

					about.setVisible(true);

				}
			});
			helpMenu.add(help);

			this.add(sysMenu);
			this.add(helpMenu);

		}

	}
	
	/**
	 * 工具栏动作
	 * 
	 * @Description: TODO
	 * @author Ivan 2862099249@qq.com
	 * @date 2015年5月13日 下午4:54:21 
	 * @version V1.0
	 */
	class ToolBarAction extends AbstractAction{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public ToolBarAction(String name, Icon icon, String desc){
			this.putValue(Action.NAME, name);
			this.putValue(Action.SMALL_ICON, icon);
			this.putValue(Action.SHORT_DESCRIPTION, desc);
		}
		
		@Override
		public void actionPerformed(ActionEvent event) {
			
			//TODO 点击工具栏的按钮需要触发的动作
			String name = (String)this.getValue(Action.NAME);
			if("新增".equals(name)){
				ViewerFrame.this.remove(contentPanel1);
				contentPanel2 = createContentPanel2();
				ViewerFrame.this.add(contentPanel2,BorderLayout.CENTER);           
				ViewerFrame.this.setVisible(true);
	            
			}else if("下载".equals(name)){
				if(contentPanel2 != null){
					ViewerFrame.this.remove(contentPanel2);
				}
				contentPanel1 = createContentPanel1();
				ViewerFrame.this.add(contentPanel1,BorderLayout.CENTER);           
				ViewerFrame.this.setVisible(true);
			}else if("同步".equals(name)){
				Object[] options = { "确定", "取消" };
				JOptionPane pane2 = new JOptionPane("确定同步电商数据吗?", JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION, null,
						options, options[1]);
				JDialog dialog = pane2.createDialog(sync, "警告");
				dialog.setVisible(true);
				Object selectedValue = pane2.getValue();
				if (selectedValue == null || selectedValue == options[1]) {
					
					setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // 这个是关键
				} else if (selectedValue == options[0]) {
					
					if(sync.isEnabled()){
						sync.setEnabled(false);
					}
					// 开始同步
					if (syncThread == null) {
						appendLogger("正在同步电商数据，请稍后...");
						syncThread = new SyncThread();
						syncThread.start();
					}
				}
			}
			
			
		}
		
	}

	/**
	 * 设置窗体居中显示
	 * 
	 * @param window
	 */
	private void setCenterLocation(Window window) {
		int windowWidth = window.getWidth(); // 获得窗口宽
		int windowHeight = window.getHeight(); // 获得窗口高
		Toolkit kit = Toolkit.getDefaultToolkit(); // 定义工具包
		Dimension screenSize = kit.getScreenSize(); // 获取屏幕的尺寸
		int screenWidth = screenSize.width; // 获取屏幕的宽
		int screenHeight = screenSize.height; // 获取屏幕的高
		window.setLocation(screenWidth / 2 - windowWidth / 2, screenHeight / 2 - windowHeight / 2);// 设置窗口居中显示

	}

	public static void main(String[] args) {

		// 设置系统当前风格
		String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
		try {
			UIManager.setLookAndFeel(lookAndFeel);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		ViewerFrame viewer = new ViewerFrame();

		viewer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		viewer.setVisible(true);
		
		appendLogger("电商数据查看器启动成功！！！");

	}
	
	private static void appendLogger(String log){
		logger.info(log);//添加日志到日志文件或者控制台
		
		textArea.append(getLogPrefix() + log + "\r\n");//添加日志到显示滚动面板
	}

}
