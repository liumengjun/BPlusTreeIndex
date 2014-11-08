package table;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import bplustree.BPlusTree;
import fileio.BitMap;

public class TableWithIndex extends Table {
	public static final String IndexDir = "index";
	
	public static final String CfgFileExt = ".indexcfg";
	public static final String TreeFileExt = ".indextree";
	public static int indexTreeMaxKeyCount = 4;
	
	String indexName;
	String indexFieldName;	//只支持单个字段的索引
	BPlusTree indexTree;	//只支持整形字段的索引
	
	/**
	 * 空构造方法
	 */
	public TableWithIndex(){
		super();
		indexName = null;
		indexFieldName = null;
		indexTree = null;
	}
	
	/**
	 * 指定表名的构造方法
	 * @param tableName
	 */
	public TableWithIndex(String tableName){
		super(tableName);
		indexName = null;
		indexFieldName = null;
		indexTree = null;
		checkIndex();
	}
	
	/**
	 * 指定表名和索引名的构造方法
	 * @param tableName
	 * @param indexName
	 */
	public TableWithIndex(String tableName, String indexName){
		super(tableName);
		this.indexName= indexName.toUpperCase();
		indexFieldName = null;
		indexTree = null;
		updateIndexInfo();
	}
	
	/**
	 * 检查表格是否有索引已经创建
	 */
	private void checkIndex(){
		if(!this.isTableExist()){
			return;
		}
		String indexRootDirName = this.getTablePath()+File.separator+IndexDir;
		File indexRootDir = new File(indexRootDirName);
		if(indexRootDir.isDirectory()==false){
			//System.out.println("没有创建索引目录");
			return;
		}
		String[] indexNames = indexRootDir.list();
		if(indexNames.length==0){
			//System.out.println("没有创建任何索引");
			return;
		}
		this.indexName = indexNames[0];
		//System.out.println("已经创建索引："+this.indexName);
		updateIndexInfo();
	}
	
	/**
	 * 得到索引名字
	 * @return
	 */
	public String getIndexName(){
		return this.indexName;
	}
	
	/**
	 * 设定索引名字
	 * @param indexName
	 */
	public void setIndexName(String indexName){
		this.indexName = indexName.toUpperCase();
		this.updateIndexInfo();
	}
	
	/**
	 * 根据this.indexName更新索引信息（索引字段名，索引B+树）
	 */
	private void updateIndexInfo(){
		if (this.indexName == null) {
			System.out.println("还没有指定索引的名字");
			return;
		}
		// 为每个表创建以表名命名的目录directory
		File indexDir = new File(this.getIndexPath());
		boolean isDirectory = indexDir.isDirectory();
		if (isDirectory) {
			//System.out.println("索引：" + tableName+"."+indexName+"已存在");
			this.indexFieldName = this.getIndexFieldName();
			this.indexTree = this.getIndexTree();
			if(this.indexTree==null || this.indexFieldName==null){
				this.indexName = null;
				this.indexFieldName = null;
				this.indexTree = null;
				indexDir.delete();
			}
		}else{
			this.indexName = null;
			this.indexFieldName = null;
			this.indexTree = null;
			indexDir.delete();
		}
	}
	
	/**
	 * 得到索引所在路径
	 * @return
	 */
	public String getIndexPath(){
		String path = super.getTablePath()+File.separator;
		path += IndexDir+File.separator+this.indexName;
		return path;
	}
	
	/**
	 * 创建索引 -- 简单索引(单一字段索引)
	 * 1,若此表已经有一个索引，则不在创建
	 * 2,并且只支持int，bigInt字段
	 * @param indexName
	 * @param indexFieldName
	 * @return
	 */
	public boolean createIndex(String indexName,String indexFieldName){
		if (isTableExist()==false){//表格不存在
			return false;
		}
		if(this.indexName!=null){
			System.out.println("已经存在一个索引,不能创建第二个，抱歉！");
			return false;
		}
		Field[] fields = getTableFields();
		if (fields == null) {
			return false;
		}
		/*检测字段类型*/
		int indexFiledType = Field.getTypeByNameFromFields(fields, indexFieldName);
		if(indexFiledType == Field.TypeError){
			System.out.println("不存在的字段名，请检测！");
			return false;
		}
		if(indexFiledType!=Field.IntType && indexFiledType!=Field.BigintType){
			System.out.println("不支持的索引字段类型，抱歉！");
			return false;
		}
		//开始创建索引
		this.indexName = indexName.toUpperCase();
		// 为每个索引创建以"表名/index/indexName"命名的目录
		File indexDir = new File(getIndexPath());
		boolean isExists = indexDir.exists();
		if (isExists) {
			if(indexDir.isDirectory()){
				System.out.println("索引：" + indexName + "已存在");
				updateIndexInfo();
			}else{
				System.out.println("系统错误！已有文件：\""+getIndexPath()+"\"存在");
				System.out.println("无法创建索引" + indexName);
				this.indexName = null;
			}
			return false;
		}
		// 创建index所在的目录
		if(!indexDir.mkdirs()){
			this.indexName = null;
		}
		this.indexFieldName = indexFieldName.toUpperCase();
		
		/*写indexFieldName到配置文件*/
		String cfgFileName = this.getIndexPath() + File.separator
				+ this.indexName + CfgFileExt;
		File cfgFile = new File(cfgFileName);
		PrintWriter outputStream = null;
		try {
			outputStream = new PrintWriter(new FileOutputStream(cfgFile));
			outputStream.println(this.indexFieldName);
			outputStream.close();
			//System.out.println("创建配置文件" + cfgFileName + "结束");
		} catch (IOException e) {
			System.out.println("创建配置文件" + cfgFileName + "失败");
			e.printStackTrace();
			cfgFile.delete();
			this.indexName = null;
			this.indexFieldName = null;
			return false;
		}
		cfgFile.setReadOnly();// 设定配置文件为 “只读”
		
		/* 读取位图文件 */
		BitMap map = getTableBitMap();
		if (map == null) {
			cfgFile.delete();
			this.indexName = null;
			this.indexFieldName = null;
			System.out.println("读取位图文件失败");
			return false;
		}
		
		/*从数据文件初始化this.indexTree*/
		this.initIndexTree(map,fields);
		updateIndexTreeToFile();//更新索引B+树to文件
		
		return true;
	}
	
	/**
	 * 从数据文件初始化this.indexTree
	 * @return
	 */
	private void initIndexTree(BitMap map, Field[] fields){
		this.indexTree = new BPlusTree(indexTreeMaxKeyCount);
		int totalRecord = map.getSetNum();// 得到总记录数
		if (totalRecord > 0) {
			int fieldCount = fields.length;
			int recordSize = 0;
			for (int i = 0; i < fieldCount; i++) {
				recordSize += fields[i].fieldSize;
			}
			/* 读取数据文件 */
			String dataFileName = getTablePath() + File.separator + tableName + DataFileExt;
			File dataFile = new File(dataFileName);
			RandomAccessFile dataStream = null;
			try {
				dataStream = new RandomAccessFile(dataFile, "r");
				boolean[] bits = map.getMap();
				byte[] buf = new byte[recordSize];
				String[] oneRecord;
				for (int i = 0; i < bits.length; i++) {
					if (bits[i]) {
						// 读取一条记录，读到buf中
						dataStream.seek(i * recordSize);
						dataStream.readFully(buf);
						// 将buf数据转化为String[]
						oneRecord = Field.parseBytesToStrings(buf, fields);
						String keyValue = Field.getValueByFieldName(oneRecord, fields, this.indexFieldName);
						//插入索引B+树（keyValue作为关键字，i作为数据指针）
						this.indexTree.insert(Integer.parseInt(keyValue), i);
					}
				}
				dataStream.close();
			} catch (IOException e) {
				System.out.println("读取数据文件失败");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 从数据库表中重新初始化B+树
	 */
	public void updateIndexTree(){
		if(!isIndexExist()){//表格,索引都不存在
			return;
		}
		/* 读取位图文件 */
		BitMap map = getTableBitMap();
		if (map == null) {
			return;
		}
		Field[] fields = getTableFields();
		if (fields == null) {
			return;
		}
		this.initIndexTree(map,fields);
		updateIndexTreeToFile();//更新索引B+树to文件
	}
	
	/**
	 * 判断索引是否存在
	 * @return
	 */
	public boolean isIndexExist(){
		if (isTableExist()==false){
			//表格不存在
			return false;
		}
		if (this.indexName == null) {
			System.out.println("还没有指定索引的名字");
			return false;
		}
		// 为每个表创建以表名命名的目录directory
		File indexDir = new File(this.getIndexPath());
		boolean isDirectory = indexDir.isDirectory();
		if (isDirectory) {
			//System.out.println("索引：" + tableName+"."+indexName+"已存在");
			this.indexFieldName = this.getIndexFieldName();
			this.indexTree = this.getIndexTree();
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 得到索引字段名
	 * @return
	 */
	public String getIndexFieldName(){
		if (indexFieldName == null) {
			String cfgFileName = this.getIndexPath() + File.separator
					+ this.indexName + CfgFileExt;
			File cfgFile = new File(cfgFileName);
			if (!cfgFile.exists()) {
				//System.out.println("配置文件" + cfgFileName + "丢失");
				return null;
			}
			BufferedReader stream = null;
			String line;
			try {
				stream = new BufferedReader(new FileReader(cfgFileName));
				// 读取indexFieldName
				line = stream.readLine();
				stream.close();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			indexFieldName = line.toUpperCase();
		}
		return indexFieldName;
	}
	
	/**
	 * 得到索引B+树
	 * @return
	 */
	public BPlusTree getIndexTree() {
		if (indexTree == null) {
			String treeFileName = this.getIndexPath() + File.separator
					+ this.indexName + TreeFileExt;
			File treeFile = new File(treeFileName);
			if (!treeFile.exists()) {
				//System.out.println("索引B+树文件" + treeFileName + "丢失");
				return null;
			}
			indexTree = BPlusTree.getFormFile(treeFileName);
		}
		return indexTree;
	}
	
	/**
	 * 更新索引B+树to文件
	 */
	private void updateIndexTreeToFile(){
		if (indexTree != null) {
			String treeFileName = this.getIndexPath() + File.separator
					+ this.indexName + TreeFileExt;
			indexTree.toFile(treeFileName);
		}
	}

	/**
	 * 查询表格tableName符合条件conditions AndOrs组成的条件的内容
	 * 
	 * @param conditions
	 * @param AndOrs
	 * @return
	 */
	public String[][] selectFromTable(WhereCondition[] conditions, int[] AndOrs) {
		int condFiledIsIndex = -1; // 暂时只支持一个条件表达式用于索引
		if (conditions != null && conditions.length > 0) {
			// 如果条件表达式字段为索引字段，则从索引读取，否则不用索引
			for (int i=0; i<conditions.length; i++) {
				if (conditions[i].fieldName.equalsIgnoreCase(this.indexFieldName)) {
					if (AndOrs==null || AndOrs.length==0
							|| (i==0 && AndOrs[0]==WhereCondition.RelationAndOp)
							|| (AndOrs[i-1]==WhereCondition.RelationAndOp && (AndOrs.length<=i || (AndOrs[i]==WhereCondition.RelationAndOp)))
							) { // 前后必须是and条件
						condFiledIsIndex = i;
						break; // 暂时只支持一个条件表达式用于索引
					}
				}
			}
		}
		if (condFiledIsIndex == -1 || indexTree==null) {
			return super.selectFromTable(conditions, AndOrs);
		}
		resetIOCount();
		// 表格所在目录
		File tableDir = new File(rootDir + tableName);
		boolean isDirectory = tableDir.isDirectory();
		if (!isDirectory) {
			System.out.println("表格：" + tableName + "不存在");
			return null;
		}

		/* 表文件通用前缀 */
		String tableFilesPrefix = tableDir.getPath() + File.separator
				+ tableName;

		/* 读取配置文件 */
		String cfgFileName = tableFilesPrefix + CfgFileExt;
		Field[] fields;
		if (tableFields != null)
			fields = tableFields;
		else {
			fields = getFieldCfg(cfgFileName);
			tableFields = fields;
		}
		if (fields == null) {
			System.out.println("读取配置文件" + cfgFileName + "失败");
			return null;
		}
		int fieldCount = fields.length;
		int recordSize = 0;
		for (int i = 0; i < fieldCount; i++) {
			recordSize += fields[i].fieldSize;
		}

		/* 读取位图文件 */
		String mapFileName = tableFilesPrefix + MapFileExt;
		BitMap map = BitMap.getFormFile(mapFileName);
		if (map == null) {
			System.out.println("读取位图文件" + mapFileName + "失败");
			return null;
		}
		int totalRecord = map.getSetNum();
		if (totalRecord == 0) {
			// 总记录数为0，则直接返回
			return new String[0][fieldCount];
		}
		
		/* 读取索引树*/
		WhereCondition wc = conditions[condFiledIsIndex];
		List<Long> addrList = indexTree.search(Integer.valueOf(wc.referencValue), wc.comparisonOp);
		if (addrList == null) {
			return null;
		}
		
		/* 读取数据文件 */
		String dataFileName = tableFilesPrefix + DataFileExt;
		File dataFile = new File(dataFileName);
		RandomAccessFile dataStream = null;
		ArrayList<String[]> resultList = new ArrayList<String[]>(totalRecord);
		try {
			dataStream = new RandomAccessFile(dataFile, "r");
			//boolean[] bits = map.getMap();
			byte[] buf = new byte[recordSize];
			String[] oneRecord;
			Collections.sort(addrList);
			for (int i = 0; i < addrList.size(); i++) {
				// 读取一条记录，读到buf中
				dataStream.seek(addrList.get(i) * recordSize);
				dataStream.readFully(buf);
				incIOCount();
				// 将buf数据转化为String[]
				oneRecord = Field.parseBytesToStrings(buf, fields);
				if (WhereCondition.checkRecordByFields(oneRecord, fields,
						conditions, AndOrs))
					resultList.add(oneRecord);
				// System.out.println(resultList.size());
			}
			dataStream.close();
		} catch (IOException e) {
			System.out.println("读取数据文件失败");
			e.printStackTrace();
			return null;
		}
		/*转换*/
		String[][] resultSet = new String[resultList.size()][];
		resultList.toArray(resultSet);

		return resultSet;
	}
	
	/**
	 * 向表中插入一条数据:
	 * 
	 * @param oneRecord
	 * @return boolean success or not
	 * @see table.Table#insertIntoTable(java.lang.String[])
	 */
	public boolean insertIntoTable(String[] oneRecord){
		boolean flag = super.insertIntoTable(oneRecord);
		if(flag){
			//System.out.println("插入一条新数据，updateIndexTree");
			this.updateIndexTree();
		}
		return flag;
	}
	
	/**
	 * 删除表格所有内容
	 * 
	 * @return 删除记录个数
	 * @see table.Table#deleteAllFromTable()
	 */
	public int deleteAllFromTable(){
		int num = super.deleteAllFromTable();
		if(num>0){
			this.updateIndexTree();
		}
		return num;
	}
	
	/**
	 * 删除表格tableName符合条件(conditions AndOrs组成的条件)的内容
	 * 
	 * @param conditions
	 * @param AndOrs
	 * @return 删除记录个数
	 * @see table.Table#deleteFromTable(table.WhereCondition[], int[])
	 */
	public int deleteFromTable(WhereCondition[] conditions, int[] AndOrs){
		int num = super.deleteFromTable(conditions,AndOrs);
		if(num>0){
			this.updateIndexTree();
		}
		return num;
	}
	
	/**
	 * update表格tableName符合条件(conditions AndOrs组成的条件)的记录
	 * 
	 * @param setPairs
	 * @param conditions
	 * @param AndOrs
	 * @return 影响记录条数
	 * @see table.Table#updateTable(table.UpdateSetPair[], table.WhereCondition[], int[])
	 */
	public int updateTable(UpdateSetPair[] setPairs,
			WhereCondition[] conditions, int[] AndOrs){
		int num = super.updateTable(setPairs,conditions,AndOrs);
		if(num>0){
			this.updateIndexTree();
		}
		return num;
	}
	
	/**
	 * 转化为DDL（Data Definition Language数据库模式定义语言）
	 * @return create_table_sql 和 create_index_sql
	 * @see table.Table#toDDLString()
	 */
	public String toDDLString() {
		if (this.tableName == null) {
			return "还没有指定表的名字";
		}
		StringBuffer ddl = new StringBuffer("Create Table " + this.tableName + "( ");
		if (this.tableFields != null) {
			int i = 0;
			for (i = 0; i < this.tableFields.length - 1; i++)
				ddl.append(this.tableFields[i].toDDLString() + ", ");
			ddl.append(this.tableFields[i].toDDLString());
		}
		ddl.append(" );\n");
		
		if(this.indexName == null){
			ddl.append("还没有创建索引");
			return ddl.toString();
		}
		ddl.append("Create index "+this.indexName+" ON "+
				this.tableName+"("+this.indexFieldName+");");
		
		return ddl.toString();
	}
	
	/**
	 * 测试
	 * @param args
	 */
	public static void main(String[] args) {
		TableWithIndex test = new TableWithIndex("test");
		boolean flag = test.createIndex("asf", "id");
		System.out.println("创建索引"+flag);
		//test.setIndexName("idindex");
		String tablePath = test.getTablePath();
		String indexPath = test.getIndexPath();
		System.out.println(tablePath);
		System.out.println(indexPath);
		boolean isTableExist = test.isTableExist();
		System.out.println("isTableExist:"+isTableExist);
		boolean isIndexExist = test.isIndexExist();
		System.out.println("isIndexExist:"+isIndexExist);
		String indexFieldName = test.getIndexFieldName();
		System.out.println("indexFieldName:"+indexFieldName+",");
		BPlusTree bplustree = test.getIndexTree();
		if(bplustree!=null){
			bplustree.print_tree_2();
		}else{
			System.out.println("IndexTree丢失");
		}
		System.out.println(test.toDDLString());
		
		TableWithIndex studentIndexTable = new TableWithIndex("student");
		System.out.println(studentIndexTable.toDDLString());
		System.out.println(studentIndexTable.toString());
	}
}
