package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import bplustree.BPlusTree;
import bplustree.BPlusTreeNode;

public class BPlusTreePanel extends JPanel{
	private static final long serialVersionUID = 1L;
	
	private int elementWidth = 30;
	private int elementHeight = 40;
	private BPlusTree bPlusTree;
	private HashMap<BPlusTreeNode, RectPosition> nodePositionsMap;
	private class RectPosition {
		int x,y,w,h;
		RectPosition(int x, int y, int w, int h) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
		}
	}
	
	/**
	 * 构造方法：初始化BPlusTree
	 * @param bPlusTree
	 */
	public BPlusTreePanel(BPlusTree bPlusTree){
		super();
		this.setLayout(null);
		this.nodePositionsMap = new HashMap<BPlusTreeNode, RectPosition>();
		setBPlusTree(bPlusTree);
	}
	
	/**
	 * 设定BPlusTree
	 * @param bPlusTree
	 */
	public void setBPlusTree(BPlusTree bPlusTree){
		if (bPlusTree==null) {
			return;
		}
		this.bPlusTree = bPlusTree;
		this.nodePositionsMap.clear();
		this.repaint();
	}
	
	/**
	 * 继承的paintComponent
	 */
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;
		if(this.bPlusTree==null){
			g2.drawString("没有B+树！", 20, 20);
			setPreferredSize(new Dimension(100, 50));
			return;
		}
		BPlusTreeNode root = this.bPlusTree.getRoot();
		if(root==null){
			g2.drawString("没有任何数据结点！", 20, 20);
			setPreferredSize(new Dimension(150, 50));
			return;
		}
		int treeHeight = this.bPlusTree.getHeight();
		int leafNodeCount = this.bPlusTree.getLeafNodeCount();
		int maxKeyCount = this.bPlusTree.getMaxKeyCount();
		int width = leafNodeCount*(maxKeyCount*elementWidth*2)+30;
		int height = (2*treeHeight-1)*elementHeight+50;
		//System.out.println("width:"+width+",height:"+height);
		placeNode(root,width/2,50,treeHeight-1);
		setPreferredSize(new Dimension(width, height));
	}
	
	/**
	 * 在JPanel中划出BPlusTreeNode图形
	 */
	private void placeNode(BPlusTreeNode node, int x_pos, int y_pos, int level) {
		if(node!=null){
			int n = node.getNum();
			int w = elementWidth*n;
			int h = elementHeight;
			int x = x_pos-w/2;
			int y = y_pos;
			//System.out.println("position:("+x_pos+","+y_pos+")");
			//System.out.println("num:"+n+":("+x+","+y+")|("+w+","+h+")");
			this.drawNode(node, x, y, w, h);
			nodePositionsMap.put(node, new RectPosition(x, y, w, h));
			
			if(!node.isLeaf()){//有子结点
				int interval = this.bPlusTree.getMaxKeyCount()*level;
				//System.out.println("level:"+level+",interval:"+elementWidth*level);
				for(int i=0;i<n;i++){
					int deltaWidth = (2*i+1-n)*elementWidth;
					int new_x_pos = x_pos + deltaWidth*interval;
					int new_y_pos = y_pos + elementHeight*2;
					drawArrow(x_pos+deltaWidth/2, y_pos+elementHeight,
							new_x_pos, new_y_pos);
					this.placeNode(node.getChildAt(i), new_x_pos, new_y_pos, level-1);
				}
			}

			BPlusTreeNode nextNode = node.getNext();
			if (nextNode != null) { // 有下一节点
				RectPosition rectPos = nodePositionsMap.get(nextNode);
				if (rectPos != null) {
					drawArrow(x+w+2, y+elementHeight/2, rectPos.x-2, rectPos.y+elementHeight/2);
				}
			}
		}
	}
	
	private void drawNode(BPlusTreeNode node,int x,int y,int w,int h){
		int n=node.getNum();
		/*
		//以标签形式展现结点
		JPanel nodep = new JPanel(new GridLayout(2,n));
		for(int i=0;i<n;i++){
			JLabel keyl = new JLabel(String.valueOf(node.getKeyAt(i)));
			keyl.setBorder(new EtchedBorder());
			nodep.add(keyl);
		}
		if(node.isLeaf()){
			for(int i=0;i<n;i++){
				JLabel addrl = new JLabel(String.valueOf(node.getAddrAt(i)));
				addrl.setBorder(new EtchedBorder());
				nodep.add(addrl);
			}
		}else{
			for(int i=0;i<n;i++){
				JLabel l = new JLabel();
				l.setBorder(new EtchedBorder());
				nodep.add(l);
			}
		}
		nodep.setBounds(x, y, w, h);
		this.add(nodep);
		*/
		//以图形形式展现结点
		Graphics2D g2 = (Graphics2D)(this.getGraphics());
		g2.drawRect(x, y, w, h);
		g2.drawLine(x, y+h/2, x+w, y+h/2);
		for(int i=1;i<n;i++){
			g2.drawLine(x+i*elementWidth, y, x+i*elementWidth, y+h);
		}
		for(int i=0;i<n;i++){
			g2.drawString(String.valueOf(node.getKeyAt(i)), x+i*elementWidth, y+elementHeight/2);
		}
		if(node.isLeaf()){
			for(int i=0;i<n;i++){
				g2.drawString(String.valueOf(node.getAddrAt(i)), x+i*elementWidth, y+elementHeight);
			}
		}
	}
	
	/**
	 * 画箭头
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	private void drawArrow(int x1, int y1, int x2, int y2){
		Graphics2D g2 = (Graphics2D)(this.getGraphics());
		g2.setColor(Color.BLUE);
		g2.drawLine(x1, y1, x2, y2);
		//System.out.println("P1:("+x1+","+y1+")");
		//System.out.println("P2:("+x2+","+y2+")");
		int deltaX=x1-x2,deltaY=y1-y2;
		//P1P2的长度
		int len = (int)(Math.sqrt(deltaX*deltaX+deltaY*deltaY));
		//System.out.println("P1P2:"+len);
		double L = 10;					//箭头部分的长度，|P3P2|为箭头的长度
		//点P3(线段PAPB与P1P2的焦点，即垂足)
		int x3=(int)(L/len*deltaX)+x2;
		int y3=(int)(L/len*deltaY)+y2;
		//System.out.println("P3:("+x3+","+y3+")");
		double theta = Math.PI*30/180;	//一半的角度为30°，角度PAP2P3
		double W = L * Math.tan(theta);	//箭头宽度的一半,|PAPB|为箭头的总宽度
		//System.out.println("L:"+L+",W"+W);
		//箭头的一条边PAP2
		int xA = x3-(int)(W/len*deltaY);
		int yA = y3+(int)(W/len*deltaX);
		//System.out.println("A:("+xA+","+yA+")");
		g2.drawLine(xA, yA, x2, y2);
		//箭头的另一条边PBP2
		int xB = x3+(int)(W/len*deltaY);
		int yB = y3-(int)(W/len*deltaX);
		//System.out.println("B:("+xB+","+yB+")");
		g2.drawLine(xB, yB, x2, y2);
	}
	
	/**
	 * 测试
	 * @param args
	 */
	public static void main(String[] args) {
		BPlusTree bpt = new BPlusTree(4);
		//int list[]={52,187,98,180,148,88,122,8,58,129,184,87,74,71,111,198,74,181,185,86};
		int list[] = { 52, 187, 98, 180, 148, 88, 122, 8, 58, 129, 184 };
		int n = list.length;
		//往B+树中插入关键字
		for (int i = 0; i < n; i++) {
			bpt.insert(list[i], i);
		}
		
		BPlusTreePanel p = new BPlusTreePanel(null);
		p.setBPlusTree(bpt);
		
		JFrame f = new JFrame();
		f.add(new JScrollPane(p));
		f.setTitle("B+树显示测试");
		f.setSize(800, 500);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
		/*
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		p.setBPlusTree(null);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		p.setBPlusTree(bpt);
		*/
	}
}
