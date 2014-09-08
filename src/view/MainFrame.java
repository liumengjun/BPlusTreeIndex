package view;

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.tree.*;

import bplustree.*;
import table.*;
import fileio.*;

public class MainFrame extends JFrame implements ActionListener,TreeSelectionListener {
	private static final long serialVersionUID = 1L;
	
	private TableWithIndex dataTable = null;
	private JTable tableDataTable;
	private JTextArea sqlTextArea = null;
	private JTextArea sqlRunTextArea = null;
	private JTree tablesListTree = null;
	//private BPlusTreePanel treePanel;

	/**
	 * 测试
	 * @param args
	 */
	public static void main(String[] args) {
		JFrame frame = new MainFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	/**
	 * This is the default constructor
	 */
	public MainFrame() {
		super();
		initGUI();
	}

	/**
	 * This method initializes this
	 * @return void
	 */
	private void initGUI() {
		// get screen dimensions
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screenSize = kit.getScreenSize();
		int screenWidth = screenSize.width;
		int screenHeight = screenSize.height;
		// center frame in screen
		this.setSize(screenWidth / 2, screenHeight / 2);
		this.setLocation(screenWidth / 4, screenHeight / 4);
		// set frame icon and title
		Image img = kit.getImage("res/mainicon.png");
		this.setIconImage(img);
		this.setTitle("Table");

		// 添加菜单
		this.addMenu();
		/*添加sql执行文本框*/
		this.addSQLTextArea();
		//添加表格列表
		this.addTableListPanel();
		//添加表格数据显示Panel,和sqlRunTextArea
		this.addCenterPanel();
		//添加B+树显示Panel
		//this.addBPlusTreePanel();
	}
	
	/**
	 * 添加B+树显示Panel
	private void addBPlusTreePanel(){
		treePanel = new BPlusTreePanel(null);
		JScrollPane bptpane = new JScrollPane(treePanel);
		//bptpane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		//bptpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		bptpane.setPreferredSize(new Dimension(200,100));
		this.add(bptpane,BorderLayout.EAST);
	}
	 */
	
	/**
	 * 表格表格数据显示Panel,和sqlRunTextArea
	 */
	private void addCenterPanel(){
		JPanel centerP = new JPanel(new GridLayout(2,1));
		//表格数据显示Panel
		tableDataTable = new JTable();
		tableDataTable.setEnabled(false);
		centerP.add(new JScrollPane(tableDataTable));
		//sqlRunTextArea
		sqlRunTextArea = new JTextArea(10,40);
		sqlRunTextArea.setBackground(Color.WHITE);
		sqlRunTextArea.setEditable(false);
		centerP.add(new JScrollPane(sqlRunTextArea));
		
		add(centerP,BorderLayout.CENTER);
	}
	
	/**
	 * 表的名字列表Panel
	 */
	private void addTableListPanel() {
		File tablesDir = new File(Table.rootDir);
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Tables");
		if (tablesDir.isDirectory()) {
			String[] tableNames = tablesDir.list();
			//System.out.println("table个数："+tableNames.length);
			for (int i = 0; i < tableNames.length; i++) {
				// System.out.println(tableNames[i]);
				DefaultMutableTreeNode curTable = new DefaultMutableTreeNode(
						tableNames[i]);
				root.add(curTable);
			}
		}
		DefaultTreeModel treeModel = new DefaultTreeModel(root);
		tablesListTree = new JTree(treeModel);
		tablesListTree.addTreeSelectionListener(this);
		int mode = TreeSelectionModel.SINGLE_TREE_SELECTION;
		tablesListTree.getSelectionModel().setSelectionMode(mode);
		JScrollPane tableListPanel = new JScrollPane(tablesListTree);
		add(tableListPanel, BorderLayout.WEST);
	}
	
	/**
	 * 添加sql执行文本框
	 */
	private void addSQLTextArea(){
		JPanel sqlPanel = new JPanel(new BorderLayout());
		
		sqlTextArea = new JTextArea(3,30);
		sqlTextArea.setBackground(Color.WHITE);
		Font f = sqlTextArea.getFont();
		sqlTextArea.setFont(new Font(f.getName(),f.getStyle(),f.getSize()+4));
		JScrollPane scrollPane = new JScrollPane(sqlTextArea);
		sqlPanel.add(scrollPane,BorderLayout.CENTER);
		
		JButton sqlRunButton = new JButton("执行");
		sqlRunButton.setActionCommand("runSQL");
		sqlRunButton.addActionListener(this);
		JPanel subPanel = new JPanel();
		subPanel.add(sqlRunButton);
		sqlPanel.add(subPanel,BorderLayout.EAST);
		
		add(sqlPanel, BorderLayout.SOUTH);
	}

	/**
	 * 添加菜单
	 */
	private void addMenu() {
		JMenuItem m;
		/*file菜单*/
		JMenu fileMenu = new JMenu("文件(F)");
		fileMenu.setMnemonic('F');
		m = new JMenuItem("Exit");
		m.addActionListener(this);
		m.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4,
				InputEvent.ALT_MASK));
		fileMenu.add(m);
		/*查看菜单*/
		JMenu viewMenu = new JMenu("查看(V)");
		viewMenu.setMnemonic('V');
		m = new JMenuItem("更新表格数据");
		m.setActionCommand("viewTableData");
		m.addActionListener(this);
		viewMenu.add(m);
		m = new JMenuItem("B+树");
		m.setActionCommand("viewBPlusTree");
		m.addActionListener(this);
		viewMenu.add(m);
		m = new JMenuItem("数据位图");
		m.setActionCommand("viewBitMap");
		m.addActionListener(this);
		viewMenu.add(m);
		/*Style菜单*/
		JMenu styleMenu = new JMenu("风格(S)");
		styleMenu.setMnemonic('S');
		m = new JMenuItem("Windows");
		m.setActionCommand("styleWindows");
		m.addActionListener(this);
		styleMenu.add(m);
		m = new JMenuItem("Metal");
		m.setActionCommand("styleMetal");
		m.addActionListener(this);
		styleMenu.add(m);
		m = new JMenuItem("Motif");
		m.setActionCommand("styleMotif");
		m.addActionListener(this);
		styleMenu.add(m);
		/*Help菜单*/
		JMenu helpMenu = new JMenu("帮助(H)");
		helpMenu.setMnemonic('H');
		m = new JMenuItem("About");
		m.addActionListener(this);
		m.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1,
				InputEvent.ALT_MASK));
		helpMenu.add(m);

		/*设定菜单*/
		JMenuBar mBar = new JMenuBar();
		mBar.add(fileMenu);
		mBar.add(viewMenu);
		mBar.add(styleMenu);
		mBar.add(helpMenu);
		this.setJMenuBar(mBar);
	}

	/**
	 * 命令执行接口实现
	 */
	public void actionPerformed(ActionEvent e) {
		String actionCommand = e.getActionCommand();
		if (actionCommand.equals("runSQL")) {
			//执行SQL
			String sql = this.sqlTextArea.getText().trim();
			if(this.sqlTextArea.getLineCount()>1){
				sql = sql.replace('\n', ' ');
			}
			sqlRunTextArea.setText("");
			System.out.println(sql);
			sqlRunTextArea.append(sql+"\n");
			if(sql.length()==0){
				System.out.println("没有任何内容");
				sqlRunTextArea.append("没有任何内容\n");
				return;
			}
			this.parseSQL(sql);
			this.sqlTextArea.setText("");
			if(dataTable!=null){
				updateTableDataPanel();
				//updateTreePanel();
			}
		} else if (actionCommand.equals("viewTableData")) {
			//更新表格数据
			if(dataTable!=null){
				updateTableDataPanel();
			}
		} else if (actionCommand.equals("viewBPlusTree")) {
			//显示索引B+树
			if(dataTable!=null){
				BPlusTree bpt = dataTable.getIndexTree();	//得到B+树
				//bpt.print_tree_2();
				String tableName = dataTable.getTableName();//表格名字
				
				this.sqlRunTextArea.setText("");
				sqlRunTextArea.append("表格" + tableName);
				if (bpt != null) {
					String colName = dataTable.getIndexFieldName();
					sqlRunTextArea.append(",在字段" + colName + "的索引树:\n");
					sqlRunTextArea.append(bpt.toString());
					//显示B+树结构图
					BPlusTreeDialog bptf = new BPlusTreeDialog(bpt,tableName);
					bptf.setLocation(100+this.getLocation().x, 100+this.getLocation().y);
					bptf.setVisible(true);
				} else {
					sqlRunTextArea.append(",没有索引B+树!\n");
					JOptionPane.showMessageDialog(this, "表格" + tableName
							+ ",没有索引!", "提示", JOptionPane.ERROR_MESSAGE);
				}
			}
		} else if (actionCommand.equals("viewBitMap")) {
			//显示表格的位图
			if(dataTable!=null){
				BitMap bitmap = dataTable.getTableBitMap();
				BitMapDialog bmd = new BitMapDialog(bitmap,dataTable.getTableName());
				bmd.setLocation(100+this.getLocation().x, 100+this.getLocation().y);
				bmd.setVisible(true);
			}
		} else if (actionCommand.equals("Exit")) {
			//退出程序
			System.exit(0);
		} else if (actionCommand.equals("styleWindows")) {
			try {// Windows风格
				UIManager
						.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
				SwingUtilities.updateComponentTreeUI(this);
			} catch (Exception ex) {
			}
		} else if (actionCommand.equals("styleMetal")) {
			try {// Metal外观
				UIManager
						.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
				SwingUtilities.updateComponentTreeUI(this);
			} catch (Exception ex) {
			}
		} else if (actionCommand.equals("styleMotif")) {
			try {// Motif外观
				UIManager
						.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
				SwingUtilities.updateComponentTreeUI(this);
			} catch (Exception ex) {
			}
		} else if (actionCommand.equals("About")) {
			JOptionPane.showMessageDialog(this, "B+树数据库索引实现展示系统。","Info",JOptionPane.INFORMATION_MESSAGE);
		} else {
		}
	}

	/**
	 * TreeSelectionListener 实现选择表格
	 */
	public void valueChanged(TreeSelectionEvent e) {
		TreePath curTreePath = e.getPath();
		//System.out.println(curTreePath.getLastPathComponent().toString());
		int pathCount = curTreePath.getPathCount();
		//System.out.println(pathCount);
		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) curTreePath.getLastPathComponent();
		if(pathCount==2){
			String tableName = selectedNode.toString();
			if(dataTable!=null && dataTable.getTableName().equalsIgnoreCase(tableName))
				return;
			//System.out.println(tableName);
			dataTable = new TableWithIndex(tableName);
			this.setTitle("Table - "+tableName);
			updateTableDataPanel();
			//updateTreePanel();
		}
	}

	/**
	 * 更新表格数据显示Panel
	 */
	public void updateTableDataPanel(){
		String[][] data = dataTable.selectAllFromTable();
		Field[] fields = dataTable.getTableFields();
		String[] columnNames = new String[fields.length];
		for(int i=0;i<columnNames.length;i++){
			columnNames[i] = fields[i].getFieldName();
		}
		((DefaultTableModel) tableDataTable.getModel()).setDataVector(data, columnNames);
	}
	
	/**
	 *更新，重绘B+树Panel
	public void updateTreePanel(){
		treePanel.setBPlusTree(dataTable.getIndexTree());
		int width=this.getWidth(),height=this.getHeight();
		this.pack();
		this.setSize(width, height);
	}
	 */
	
	/**
	 * 解析sql语句
	 * @param sql
	 */
	void parseSQL(String sql){
		String sqlUpper = sql.toUpperCase();
		String opTitle;
		int p1;
		
		p1 = sql.indexOf(' ');//第一个空格
		if(p1==-1){
			System.out.println("格式错误");
			sqlRunTextArea.append("格式错误\n");
			return;
		}
		opTitle = sqlUpper.substring(0,p1);
		p1++;
		while(sql.charAt(p1)==' '){	p1++; }//跳过空格
		if(opTitle.equals("CREATE")){
			/*创建table或index*/
			int p2 = sql.indexOf(' ', p1);//第二个空格
			if(p2==-1){
				System.out.println("格式错误");
				sqlRunTextArea.append("格式错误\n");
				return;
			}
			String createType = sqlUpper.substring(p1, p2);
			p2++;
			while(sql.charAt(p2)==' '){	p2++; }//跳过空格
			if(createType.equals("TABLE")){
				/*创建table*/
				int p3 = sql.indexOf('(',p2);
				if(p3==-1){
					System.out.println("格式错误");
					sqlRunTextArea.append("格式错误\n");
					return;
				}
				String tableName = sqlUpper.substring(p2,p3).trim();
				if(sql.charAt(sql.length()-1)!=')'){
					System.out.println("格式错误");
					sqlRunTextArea.append("格式错误\n");
					return;
				}
				String fieldDDLStr = sql.substring(p3+1,sql.length()-1).trim();
				System.out.println("fieldDDLStr:"+fieldDDLStr);
				Field[] fields = Field.getFieldsFromDDLString(fieldDDLStr);
				System.out.println("创建表格："+tableName);
				sqlRunTextArea.append("创建表格："+tableName+"\n");
				for(int i=0;i<fields.length;i++){
					if(fields[i]==null){
						System.out.println("语句错了");
						sqlRunTextArea.append("语句错了\n");
						return;
					}
					System.out.println(fields[i].toString()+",即:"+fields[i].toDDLString());
				}
				TableWithIndex theTable = new TableWithIndex(tableName);
				boolean flag = theTable.createTable(tableName, fields);
				if(flag){
					DefaultTreeModel tm = (DefaultTreeModel)(tablesListTree.getModel());
					DefaultMutableTreeNode root = (DefaultMutableTreeNode)(tm.getRoot());
					DefaultMutableTreeNode newTableNode = new DefaultMutableTreeNode(tableName);
					root.add(newTableNode);
					tm.setRoot(root);
					dataTable = theTable;
					this.setTitle("Table - "+tableName);
					sqlRunTextArea.append("创建表格："+tableName+"成功\n");
				}else{
					sqlRunTextArea.append("创建表格："+tableName+"失败\n");
				}
				System.out.println("创建"+(flag?"成功":"失败"));
			}else if(createType.equals("INDEX")){
				/*创建index*/
				int p3 = sql.indexOf(' ',p2);
				if(p3==-1){
					System.out.println("格式错误");
					sqlRunTextArea.append("格式错误\n");
					return;
				}
				String indexName = sql.substring(p2,p3).trim();
				p3++;
				while(sql.charAt(p3)==' '){	p3++; }//跳过空格
				int p4 = sql.indexOf(' ',p3);
				if(p4==-1){
					System.out.println("格式错误");
					sqlRunTextArea.append("格式错误\n");
					return;
				}
				if(!sqlUpper.substring(p3, p4).equals("ON")){
					System.out.println("格式错误");
					sqlRunTextArea.append("格式错误\n");
					return;
				}
				p4++;
				while(sql.charAt(p4)==' '){	p4++; }//跳过空格
				int p5 = sql.indexOf('(',p4);
				if(p5==-1){
					System.out.println("格式错误");
					sqlRunTextArea.append("格式错误\n");
					return;
				}
				String tableName = sql.substring(p4,p5).trim();
				if(sql.charAt(sql.length()-1)!=')'){
					System.out.println("格式错误");
					sqlRunTextArea.append("格式错误\n");
					return;
				}
				int p6 = sql.indexOf(')',p5+1);
				if(p6==-1){
					System.out.println("格式错误");
					sqlRunTextArea.append("格式错误\n");
					return;
				}
				int p60 = sql.indexOf(',',p5+1);
				if(p60!=-1){
					System.out.println("只支持一个字段!!!");
					sqlRunTextArea.append("只支持一个字段!!!\n");
					p6 = p60;
				}
				String indexFieldName = sql.substring(p5+1, p6).trim();
				System.out.println("创建index："+indexName+",on table:"+tableName+" 的字段："+indexFieldName);
				sqlRunTextArea.append("创建index："+indexName+",on table:"+tableName+" 的字段："+indexFieldName+"\n");
				TableWithIndex theTable = new TableWithIndex(tableName);
				if(theTable.isTableExist()==false){
					sqlRunTextArea.append("表格："+tableName+"不存在!\n");
				}
				boolean flag = theTable.createIndex(indexName, indexFieldName);
				if(flag){
					dataTable = theTable;
					this.setTitle("Table - "+tableName);
					sqlRunTextArea.append("创建索引："+indexName+"成功\n");
				}else{
					sqlRunTextArea.append("创建索引："+indexName+"失败\n");
				}
			}else{
				System.out.println("格式错误");
				sqlRunTextArea.append("格式错误\n");
				return;
			}
		}else if(opTitle.equals("SELECT")){
			/*查询表格*/
			int pFrom = sqlUpper.indexOf("FROM", p1);
			if(pFrom==-1){
				System.out.println("格式错误");
				sqlRunTextArea.append("格式错误\n");
				return;
			}
			int p2 = sql.indexOf(' ',pFrom);
			if(p2==-1){
				System.out.println("格式错误");
				sqlRunTextArea.append("格式错误\n");
				return;
			}
			p2++;
			while(sql.charAt(p2)==' '){	p2++; }//跳过空格
			int p3 = sql.indexOf(' ',p2);
			String tableName;
			if(p3==-1){
				tableName = sqlUpper.substring(p2);
				TableWithIndex theTable = new TableWithIndex(tableName);
				System.out.println("select all from table:"+tableName);
				sqlRunTextArea.append("select all from table:"+tableName+"\n");
				String[][] data = theTable.selectAllFromTable();
				if(data!=null){
					System.out.println("总记录数：" + data.length);
					sqlRunTextArea.append("总记录数：" + data.length+"\n");
					for (int i = 0; i < data.length; i++) {
						for (int j = 0; j < data[i].length; j++) {
							System.out.print(data[i][j] + " ");
							sqlRunTextArea.append(data[i][j] + " ");
						}
						System.out.println();
						sqlRunTextArea.append("\n");
					}
				}else{
					System.out.println("出现错误!");
					sqlRunTextArea.append("出现错误\n");
				}
			}else{
				tableName = sqlUpper.substring(p2,p3);
				TableWithIndex theTable = new TableWithIndex(tableName);
				String wheresSql = sql.substring(p3).trim();
				ArrayList<Integer> andOrsList = new ArrayList<Integer>(5);
				WhereCondition[] wcs = WhereCondition.getWheresFromDDLString(wheresSql, andOrsList);
				int[] andOrs = WhereCondition.getAndOrs(andOrsList);
				//开始select
				System.out.println("select all from table:" + tableName	+ ", where:");
				sqlRunTextArea.append("select all from table:" + tableName + "\n");
				int i;
				// 输出表达式
				for (i = 0; i < andOrs.length; i++) {
					System.out.print(wcs[i].toString() + " "
						+ ((andOrs[i] == WhereCondition.RelationAndOp) ? "And" : "Or")
						+ " ");
				}
				System.out.println(wcs[i].toString());
				String[][] data = theTable.selectFromTable(wcs, andOrs);
				if (data != null) {
					System.out.println("总记录数：" + data.length);
					sqlRunTextArea.append("总记录数：" + data.length + "\n");
					for (i = 0; i < data.length; i++) {
						for (int j = 0; j < data[i].length; j++) {
							System.out.print(data[i][j] + " ");
							sqlRunTextArea.append(data[i][j] + " ");
						}
						System.out.println();
						sqlRunTextArea.append("\n");
					}
				} else {
					System.out.println("出现错误!");
					sqlRunTextArea.append("出现错误\n");
				}
			}
			//end select
		}else if(opTitle.equals("INSERT")){
			/*insert into table_name values(dsf,df,df)*/
			int p2 = sql.indexOf(' ',p1);
			if(p2==-1 || !sqlUpper.substring(p1,p2).equals("INTO")){
				System.out.println("格式错误");
				sqlRunTextArea.append("格式错误\n");
				return;
			}
			p2++;
			while(sql.charAt(p2)==' '){	p2++; }//跳过空格
			int p3 = sql.indexOf(' ',p2);
			if(p3==-1){
				System.out.println("格式错误");
				sqlRunTextArea.append("格式错误\n");
				return;
			}
			String tableName = sqlUpper.substring(p2, p3);
			p3++;
			while(sql.charAt(p3)==' '){	p3++; }//跳过空格
			int p4 = sql.indexOf('(',p3);
			if(p4==-1){
				System.out.println("格式错误");
				sqlRunTextArea.append("格式错误\n");
				return;
			}
			String val = sqlUpper.substring(p3, p4).trim();
			if(val.equals("VALUES")==false){
				System.out.println("格式错误");
				sqlRunTextArea.append("格式错误\n");
				return;
			}
			p4++;
			int p5 = sql.indexOf(')',p4);
			if(p5==-1){
				System.out.println("格式错误");
				sqlRunTextArea.append("格式错误\n");
				return;
			}
			String vals = sql.substring(p4, p5).trim();
			String[] oneRecord = vals.split(",");
			if(oneRecord.length==1 && oneRecord[0].length()==0){
				System.out.println("格式错误");
				sqlRunTextArea.append("格式错误\n");
				return;
			}
			System.out.println("插入表"+tableName);
			for(int i=0;i<oneRecord.length;i++){
				oneRecord[i] = oneRecord[i].trim();
				System.out.print(oneRecord[i]+" ");
			}
			System.out.println();
			TableWithIndex theTable;
			if(tableName.equals(dataTable.getTableName())){
				theTable = dataTable;
			}else{
				theTable = new TableWithIndex(tableName);
			}
				
			boolean flag = theTable.insertIntoTable(oneRecord);
			if(flag){
				sqlRunTextArea.append("插入成功\n");
			}else{
				sqlRunTextArea.append("插入失败\n");
			}
			System.out.println("插入"+(flag?"成功":"失败"));
		}else if(opTitle.equals("UPDATE")){
			/*update table_name set col=sdf where ... */
			int p2 = sql.indexOf(' ', p1);//第二个空格
			if(p2==-1){
				System.out.println("格式错误");
				sqlRunTextArea.append("格式错误\n");
				return;
			}
			String tableName = sqlUpper.substring(p1, p2);
			p2++;
			while(sql.charAt(p2)==' '){	p2++; }//跳过空格
			if(sqlUpper.substring(p2, p2+3).equals("SET")==false){
				System.out.println("格式错误");
				sqlRunTextArea.append("格式错误\n");
				return;
			}
			int pWhere = sqlUpper.indexOf("WHERE");
			TableWithIndex theTable;
			if(tableName.equals(dataTable.getTableName())){
				theTable = dataTable;
			}else{
				theTable = new TableWithIndex(tableName);
			}
			if(pWhere==-1){
				//不需要条件
				System.out.println("更新"+tableName+" 无条件");
				String setSql = sql.substring(p2);
				UpdateSetPair[] setPairs = UpdateSetPair.getPairsFromDDLString(setSql);
				int rowNum = theTable.updateTable(setPairs, null, null);
				System.out.println("影响记录数"+rowNum);
				sqlRunTextArea.append("影响记录数"+rowNum+"\n");
			}else{
				//需要条件
				String setSql = sql.substring(p2,pWhere).trim();
				UpdateSetPair[] setPairs = UpdateSetPair.getPairsFromDDLString(setSql);
				String wheresSql = sql.substring(pWhere).trim();
				ArrayList<Integer> andOrsList = new ArrayList<Integer>(5);
				WhereCondition[] wcs = WhereCondition.getWheresFromDDLString(wheresSql, andOrsList);
				System.out.print("更新"+tableName+" 条件");
				
				int[] andOrs = WhereCondition.getAndOrs(andOrsList);
				System.out.println("where:");
				int i;
				//输出表达式
				for (i = 0; i < andOrs.length; i++) {
					System.out.print(wcs[i].toString() + " "
						+ ((andOrs[i] == WhereCondition.RelationAndOp) ? "And" : "Or")
						+ " ");
				}
				System.out.println(wcs[i].toString());
				int rowNum = theTable.updateTable(setPairs, wcs, andOrs);
				System.out.println("影响记录数"+rowNum);
				sqlRunTextArea.append("影响记录数"+rowNum+"\n");
				//end update需要条件
			}
			//end update
		}else if(opTitle.equals("DELETE")){
			int p2 = sql.indexOf(' ',p1);
			if(p2==-1 || !sqlUpper.substring(p1,p2).equals("FROM")){
				System.out.println("格式错误");
				sqlRunTextArea.append("格式错误\n");
				return;
			}
			p2++;
			while(sql.charAt(p2)==' '){	p2++; }//跳过空格
			String tableName;
			int p3 = sql.indexOf(' ',p2);
			if(p3==-1){
				//无条件删除
				tableName = sqlUpper.substring(p2);
				TableWithIndex theTable;
				if(tableName.equals(dataTable.getTableName())){
					theTable = dataTable;
				}else{
					theTable = new TableWithIndex(tableName);
				}
				System.out.println("删除所有"+tableName+"记录");
				sqlRunTextArea.append("删除所有"+tableName+"记录\n");
				int rowNum = theTable.deleteAllFromTable();
				System.out.println("影响记录数"+rowNum);
				sqlRunTextArea.append("影响记录数"+rowNum+"\n");
			}else{
				//有条件删除
				tableName = sqlUpper.substring(p2,p3);
				TableWithIndex theTable;
				if(tableName.equals(dataTable.getTableName())){
					theTable = dataTable;
				}else{
					theTable = new TableWithIndex(tableName);
				}
				String wheresSql = sql.substring(p3).trim();
				ArrayList<Integer> andOrsList = new ArrayList<Integer>(5);
				WhereCondition[] wcs = WhereCondition.getWheresFromDDLString(wheresSql, andOrsList);
				System.out.print("删除"+tableName+"记录 条件");
				int[] andOrs = WhereCondition.getAndOrs(andOrsList);
				System.out.println("where:");
				int i;
				//输出表达式
				for (i = 0; i < andOrs.length; i++) {
					System.out.print(wcs[i].toString() + " "
						+ ((andOrs[i] == WhereCondition.RelationAndOp) ? "And" : "Or")
						+ " ");
				}
				System.out.println(wcs[i].toString());
				int rowNum = theTable.deleteFromTable(wcs, andOrs);
				System.out.println("影响记录数"+rowNum);
				sqlRunTextArea.append("影响记录数"+rowNum+"\n");
				//end 有条件删除
			}
			//end delete
		}else{
			System.out.println("格式错误");
			sqlRunTextArea.append("格式错误\n");
			return;
		}
	}
}
