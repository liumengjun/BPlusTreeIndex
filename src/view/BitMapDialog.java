package view;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import table.*;
import fileio.*;

public class BitMapDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private BitMap bitmap;
	private final int blockSize = 15;
	
	public BitMapDialog(BitMap bitmap, String tableName){
		super();
		this.setModal(true);
		this.bitmap = bitmap;
		this.setTitle(tableName+"--BitMap");
		init();
	}
	
	private void init(){
		int size = bitmap.getSize();
		int row = size/16 + 1;
		int total = row*16;
		//System.out.println("size:"+size);
		//System.out.println("row:"+row);
		boolean[] bools = bitmap.getMap();
		JPanel centerPanel = new JPanel(new GridLayout(row,16));
		JLabel l;
		for(int i=0;i<size;i++){
			if(bools[i]){
				l = new JLabel("¡ö");
			}else{
				l = new JLabel("¡õ");
			}
			centerPanel.add(l);
		}
		for(int i=size;i<total;i++){
			l = new JLabel();
			centerPanel.add(l);
		}
		//centerPanel.setBorder(new EtchedBorder());
		centerPanel.setPreferredSize(new Dimension(16*blockSize, row*blockSize));
		this.setSize(16*blockSize+10, row*blockSize+50);
		this.setResizable(false);
		JScrollPane pane = new JScrollPane(centerPanel);
		this.add(pane);
	}
	
	public static void main(String[] args) {
		Table dataTable = new Table("student");
		BitMap bitmap = dataTable.getTableBitMap();
		bitmap.showMap();
		
		BitMapDialog test = new BitMapDialog(bitmap,"student");
		test.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				System.exit(0);
			}
		});
		test.setVisible(true);
	}
}
