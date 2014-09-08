package view;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import table.*;
import bplustree.*;

public class BPlusTreeDialog extends JDialog{
	private static final long serialVersionUID = 1L;
	private BPlusTree bPlusTree = null;

	/**
	 * 测试
	 * @param args
	 */
	public static void main(String[] args) {
		TableWithIndex stuTable = new TableWithIndex("student");
		/*
		// 插入一些记录
		String[] oneRecord = new String[2];
		for(int i=0;i<5;i++){
			oneRecord[0] = String.valueOf((int)(Math.random()*100.0));
			oneRecord[1] = String.valueOf((char)('a'+(Math.random()*25)));
			boolean insertFlag = stuTable.insertIntoTable(oneRecord);
			System.out.println("insert("+oneRecord[0]+","+oneRecord[1]+"):"+insertFlag);
		}
		*/
		BPlusTree bpt = stuTable.getIndexTree();
		bpt.print_tree_2();
		
		BPlusTreeDialog diag = new BPlusTreeDialog(bpt,stuTable.getTableName());
		diag.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				System.exit(0);
			}
		});
		diag.setVisible(true);
	}
	
	/**
	 * 构造方法：给定B+树
	 * @param bPlusTree
	 */
	public BPlusTreeDialog(BPlusTree bPlusTree,String tableName){
		super();
		this.setModal(true);
		this.setSize(400, 300);
		this.bPlusTree = bPlusTree;
		this.setTitle(tableName+" -- Index B+树");
		initBPlusTree();
	}
	
	/**
	 * 初始化B+树结构图
	 */
	private void initBPlusTree(){
		if(this.bPlusTree==null){
			JLabel noTree = new JLabel("没有B+树需要显示");
			noTree.setBorder(new EtchedBorder());
			this.add(noTree);
		}else{
			BPlusTreePanel treePanel = new BPlusTreePanel(this.bPlusTree);
			JScrollPane pane = new JScrollPane(treePanel);
			this.add(pane);
		}
	}

}
