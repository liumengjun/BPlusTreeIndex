package table;

import java.io.*;
import java.util.*;

import fileio.*;

public class Table {
	public static final int initialRecordNum = 45;
	public static final int recordNumIncrement = 10;
	public static final String rootDir = "./table/";

	public static final String CfgFileExt = ".cfg";
	public static final String DataFileExt = ".dat";
	public static final String MapFileExt = ".map";

	String tableName;
	Field[] tableFields;

	/**
	 * 空的构造方法
	 */
	public Table() {
		this.tableName = null;
		this.tableFields = null;
	}

	/**
	 * 构造方法
	 * 
	 * @param tableName
	 */
	public Table(String tableName) {
		this.tableName = tableName.toUpperCase();
		this.tableFields = null;
	}

	/**
	 * 得到表的名字
	 * 
	 * @return
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * 得到字段定义信息
	 * 
	 * @return
	 */
	public Field[] getTableFields() {
		if (tableFields == null) {
			String tableFilesPrefix = rootDir + tableName + File.separator
					+ tableName;
			String cfgFileName = tableFilesPrefix + CfgFileExt;
			tableFields = getFieldCfg(cfgFileName);
		}
		return tableFields;
	}

	/**
	 * 得到数据位图
	 * 
	 * @return
	 */
	public BitMap getTableBitMap() {
		/* 读取位图文件 */
		String tableFilesPrefix = rootDir + tableName + File.separator
				+ tableName;
		String mapFileName = tableFilesPrefix + MapFileExt;
		BitMap map = BitMap.getFormFile(mapFileName);
		return map;
	}

	/**
	 * 更新数据位图
	 * 
	 * @param map
	 * @return
	 */
	public boolean updateBitMap(BitMap map) {
		String tableFilesPrefix = rootDir + tableName + File.separator
				+ tableName;
		String mapFileName = tableFilesPrefix + MapFileExt;
		return map.toFile(mapFileName);
	}

	/**
	 * 从配置文件中得到字段信息
	 * 
	 * @param cfgFileName
	 * @return
	 */
	Field[] getFieldCfg(String cfgFileName) {
		File cfgFile = new File(cfgFileName);
		if (!cfgFile.exists()) {
			System.out.println("配置文件" + cfgFileName + "丢失");
			return null;
		}
		BufferedReader stream = null;
		String line;
		int fieldCount;
		Field[] fields = null;
		try {
			stream = new BufferedReader(new FileReader(cfgFileName));
			// 读取字段数目
			line = stream.readLine();
			fieldCount = Integer.parseInt(line);
			// 逐个读取字段信息
			fields = new Field[fieldCount];
			for (int i = 0; i < fieldCount; i++) {
				line = stream.readLine();
				fields[i] = Field.parseString(line);
			}
			stream.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return fields;
	}
	
	/**
	 * 得到表格所在路径
	 * @return
	 */
	public String getTablePath(){
		return (rootDir + tableName);
	}

	/**
	 * 创建表格this.tableName，字段在fields中定义
	 * 
	 * @param fields
	 * @return
	 */
	public boolean createTable(Field[] fields) {
		if (this.tableName == null) {
			System.out.println("还没有指定表的名字");
			return false;
		}
		// 为每个表创建以表名命名的目录directory
		File tableDir = new File(rootDir + tableName);
		boolean isDirectory = tableDir.isDirectory();
		if (isDirectory) {
			System.out.println("表格：" + tableName + "已存在");
			this.tableFields = this.getTableFields();
			return false;
		}
		boolean isExists = tableDir.exists();
		if (isExists) {
			System.out.println("系统错误！有个文件"+getTablePath()+"存在");
			System.out.println("无法创建表格" + tableName);
			return false;
		}
		if(Field.checkFieldsNamesUnique(fields)==false){
			System.out.println("字段名有重复");
			return false;
		}
		// 创建table所在的目录
		if(!tableDir.mkdirs()){
			this.tableName = null;
		}
		this.tableFields = fields; // 设定表格字段定义

		/* 表文件通用前缀 */
		String tableFilesPrefix = tableDir.getPath() + File.separator
				+ tableName;

		int fieldCount = fields.length;
		/* 创建配置文件 */
		String cfgFileName = tableFilesPrefix + CfgFileExt;
		File cfgFile = new File(cfgFileName);
		PrintWriter outputStream = null;
		try {
			outputStream = new PrintWriter(new FileOutputStream(cfgFile));
			outputStream.println(fieldCount);
			for (int i = 0; i < fieldCount; i++) {
				outputStream.println(fields[i]);
			}
			outputStream.close();
			//System.out.println("创建配置文件" + cfgFileName + "结束");
		} catch (IOException e) {
			e.printStackTrace();
			cfgFile.delete();
			this.tableFields = null;
			System.out.println("创建配置文件" + cfgFileName + "失败");
			return false;
		}
		cfgFile.setReadOnly();// 设定配置文件为 “只读”

		/* 创建位图文件 */
		String mapFileName = tableFilesPrefix + MapFileExt;
		File mapFile = new File(mapFileName);
		BitMap map = new BitMap(initialRecordNum);
		if (map.toFile(mapFile)) {
			//System.out.println("创建位图文件" + mapFileName + "结束");
		} else {
			cfgFile.delete();
			mapFile.delete();
			System.out.println("创建位图文件" + mapFileName + "失败");
			return false;
		}

		/* 创建数据文件 */
		String dataFileName = tableFilesPrefix + DataFileExt;
		File dataFile = new File(dataFileName);
		try {
			dataFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			cfgFile.delete();
			mapFile.delete();
			dataFile.delete();
			System.out.println("创建数据文件" + dataFileName + "失败");
			return false;
		}
		//System.out.println("创建数据文件" + dataFileName + "结束");

		//System.out.println("创建表格" + tableName + "结束");
		return true;
	}

	/**
	 * 创建表格tableName，字段在fields中定义
	 * 
	 * @param tableName
	 * @param fields
	 */
	public boolean createTable(String tableName, Field[] fields) {
		this.tableName = tableName.toUpperCase();
		return createTable(fields);
	}
	
	/**
	 * 判断表格是否存在
	 * @return
	 */
	public boolean isTableExist(){
		if (this.tableName == null) {
			System.out.println("还没有指定表的名字");
			return false;
		}
		// 为每个表创建以表名命名的目录directory
		File tableDir = new File(rootDir + tableName);
		boolean isDirectory = tableDir.isDirectory();
		if (isDirectory) {
			//System.out.println("表格：" + tableName + "已存在");
			this.tableFields = this.getTableFields();
			return true;
		}else{
			return false;
		}
	}

	/**
	 * 查询表格tableName所有内容
	 * 
	 * @return
	 */
	public String[][] selectAllFromTable() {
		if (this.tableName == null) {
			System.out.println("还没有指定表的名字");
			return null;
		}
		String[][] resultSet = null;
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
		int totalRecord = map.getSetNum();// 得到总记录数
		resultSet = new String[totalRecord][fieldCount];
		if (totalRecord == 0) {
			// 总记录数为0，则直接返回
			return resultSet;
		}

		/* 读取数据文件 */
		String dataFileName = tableFilesPrefix + DataFileExt;
		File dataFile = new File(dataFileName);
		RandomAccessFile dataStream = null;
		try {
			dataStream = new RandomAccessFile(dataFile, "r");
			boolean[] bits = map.getMap();
			int sum = 0;
			byte[] buf = new byte[recordSize];
			String[] oneRecord;
			for (int i = 0; i < bits.length; i++) {
				if (bits[i]) {
					// 读取一条记录，读到buf中
					dataStream.seek(i * recordSize);
					dataStream.readFully(buf);
					// 将buf数据转化为String[]
					oneRecord = Field.parseBytesToStrings(buf, fields);
					for (int j = 0; j < fieldCount; j++) {
						resultSet[sum][j] = oneRecord[j];
					}
					sum++;// 数据记录数目
				}
			}
			dataStream.close();
		} catch (IOException e) {
			System.out.println("读取数据文件失败");
			e.printStackTrace();
			return null;
		}

		return resultSet;
	}

	/**
	 * 查询表格tableName符合条件conditions的内容
	 * 
	 * @param conditions
	 *            WhereCondition[]
	 * @return
	 */
	@Deprecated
	public String[][] selectFromTable(WhereCondition[] conditions) {
		if (conditions == null || conditions.length == 0)
			return selectAllFromTable();
		if (this.tableName == null) {
			System.out.println("还没有指定表的名字");
			return null;
		}

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

		/* 读取数据文件 */
		String dataFileName = tableFilesPrefix + DataFileExt;
		File dataFile = new File(dataFileName);
		RandomAccessFile dataStream = null;
		ArrayList<String[]> resultList = new ArrayList<String[]>(totalRecord);
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
					if (WhereCondition.checkRecordByFields(oneRecord, fields,
							conditions))
						resultList.add(oneRecord);
					// System.out.println(resultList.size());
				}
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
	 * 查询表格tableName符合条件conditions AndOrs组成的条件的内容
	 * 
	 * @param conditions
	 * @param AndOrs
	 * @return
	 */
	public String[][] selectFromTable(WhereCondition[] conditions, int[] AndOrs) {
		if (conditions == null || conditions.length == 0)
			return selectAllFromTable();
		if (AndOrs == null)// 不需要And Or操作
			return selectFromTable(conditions);
		if (this.tableName == null) {
			System.out.println("还没有指定表的名字");
			return null;
		}

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

		/* 读取数据文件 */
		String dataFileName = tableFilesPrefix + DataFileExt;
		File dataFile = new File(dataFileName);
		RandomAccessFile dataStream = null;
		ArrayList<String[]> resultList = new ArrayList<String[]>(totalRecord);
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
					if (WhereCondition.checkRecordByFields(oneRecord, fields,
							conditions, AndOrs))
						resultList.add(oneRecord);
					// System.out.println(resultList.size());
				}
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
	 * 向表中插入一条数据
	 * 
	 * @param oneRecord
	 * @return boolean success or not
	 */
	public boolean insertIntoTable(String[] oneRecord) {
		// 表格所在目录
		File tableDir = new File(rootDir + tableName);
		boolean isDirectory = tableDir.isDirectory();
		if (!isDirectory) {
			System.out.println("表格：" + tableName + "不存在");
			return false;
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
			return false;
		}
		String[] legalRecord = Field.checkRecordByFields(oneRecord, fields);
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
			return false;
		}
		// 得到第一个可用存储的新位置
		int newDataLocation = map.getFirstAvailable() - 1;
		// System.out.println("newDataLocation:"+newDataLocation);
		if (newDataLocation < 0) {
			newDataLocation = map.getSize();
			map.setSize(newDataLocation + recordNumIncrement);
			map.toFile(mapFileName);
		}

		/* 写数据文件 */
		String dataFileName = tableFilesPrefix + DataFileExt;
		// System.out.println("dataFileName:"+dataFileName);
		File dataFile = new File(dataFileName);
		RandomAccessFile dataStream = null;
		try {
			dataStream = new RandomAccessFile(dataFile, "rw");
			dataStream.seek(newDataLocation * recordSize);
			byte[] buf = Field.parseStringsToBytes(legalRecord, fields);
			dataStream.write(buf);
			dataStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		map.set(newDataLocation + 1);
		map.toFile(mapFileName);
		return true;
	}

	/**
	 * 删除表格所有内容
	 * 
	 * @return 删除记录个数
	 */
	public int deleteAllFromTable() {
		// 表格所在目录
		File tableDir = new File(rootDir + tableName);
		boolean isDirectory = tableDir.isDirectory();
		if (!isDirectory) {
			System.out.println("表格：" + tableName + "不存在");
			return 0;
		}

		/* 表文件通用前缀 */
		String tableFilesPrefix = tableDir.getPath() + File.separator
				+ tableName;
		/* 读取位图文件 */
		String mapFileName = tableFilesPrefix + MapFileExt;
		BitMap map = BitMap.getFormFile(mapFileName);
		if (map == null) {
			System.out.println("读取位图文件" + mapFileName + "失败");
			return 0;
		}
		int sum = map.getSetNum();// 得到总记录数
		map = null;
		map = new BitMap(initialRecordNum);
		if (map.toFile(mapFileName)) {
			return sum;
		}
		return 0;
	}

	/**
	 * 删除表格tableName符合条件conditions的内容
	 * 
	 * @param conditions
	 * @return
	 */
	@Deprecated
	public int deleteFromTable(WhereCondition[] conditions) {
		if (conditions == null || conditions.length == 0)
			return deleteAllFromTable();
		// 表格所在目录
		File tableDir = new File(rootDir + tableName);
		boolean isDirectory = tableDir.isDirectory();
		if (!isDirectory) {
			System.out.println("表格：" + tableName + "不存在");
			return 0;
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
			return 0;
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
			return 0;
		}
		int totalRecord = map.getSetNum();
		if (totalRecord == 0) {
			// 总记录数为0，则直接返回
			return 0;
		}

		int sum = 0;// 影响记录数
		/* 读取数据文件 */
		String dataFileName = tableFilesPrefix + DataFileExt;
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
					if (WhereCondition.checkRecordByFields(oneRecord, fields,
							conditions)) {
						map.clear(i + 1);
						sum++;
					}
				}
			}
			dataStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}

		if (map.toFile(mapFileName)) {
			return sum;
		}
		return 0;
	}

	/**
	 * 删除表格tableName符合条件(conditions AndOrs组成的条件)的内容
	 * 
	 * @param conditions
	 * @param AndOrs
	 * @return 删除记录个数
	 */
	public int deleteFromTable(WhereCondition[] conditions, int[] AndOrs) {
		if (AndOrs == null)// 不需要And Or操作
			return deleteFromTable(conditions);
		// 表格所在目录
		File tableDir = new File(rootDir + tableName);
		boolean isDirectory = tableDir.isDirectory();
		if (!isDirectory) {
			System.out.println("表格：" + tableName + "不存在");
			return 0;
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
			return 0;
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
			return 0;
		}
		int totalRecord = map.getSetNum();
		if (totalRecord == 0) {
			// 总记录数为0，则直接返回
			return 0;
		}

		int sum = 0;// 影响记录数
		/* 读取数据文件 */
		String dataFileName = tableFilesPrefix + DataFileExt;
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
					if (WhereCondition.checkRecordByFields(oneRecord, fields,
							conditions,AndOrs)) {
						map.clear(i + 1);
						sum++;
					}
				}
			}
			dataStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}

		if (map.toFile(mapFileName)) {
			return sum;
		}
		return 0;
	}

	
	/**
	 * update表格tableName符合条件conditions的记录
	 * 
	 * @param setPairs
	 * @param conditions
	 * @return
	 */
	@Deprecated
	public int updateTable(UpdateSetPair[] setPairs, WhereCondition[] conditions) {
		// 表格所在目录
		File tableDir = new File(rootDir + tableName);
		boolean isDirectory = tableDir.isDirectory();
		if (!isDirectory) {
			System.out.println("表格：" + tableName + "不存在");
			return 0;
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
			return 0;
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
			return 0;
		}
		int totalRecord = map.getSetNum();
		if (totalRecord == 0) {
			// 总记录数为0，则直接返回
			return 0;
		}

		int sum = 0;// 影响的记录数
		/* 读取数据文件 */
		String dataFileName = tableFilesPrefix + DataFileExt;
		File dataFile = new File(dataFileName);
		RandomAccessFile dataStream = null;
		try {
			dataStream = new RandomAccessFile(dataFile, "rw");
			boolean[] bits = map.getMap();
			byte[] buf = new byte[recordSize];
			String[] oneRecord;
			byte[] newRecordBuf;
			for (int i = 0; i < bits.length; i++) {
				if (bits[i]) {
					// 读取一条记录，读到buf中
					dataStream.seek(i * recordSize);
					dataStream.readFully(buf);
					// 将buf数据转化为String[]
					oneRecord = Field.parseBytesToStrings(buf, fields);
					if (WhereCondition.checkRecordByFields(oneRecord, fields,
							conditions)) {
						UpdateSetPair.updateRecordByFields(oneRecord, fields,
								setPairs);
						newRecordBuf = Field.parseStringsToBytes(oneRecord,
								fields);
						dataStream.seek(i * recordSize);
						dataStream.write(newRecordBuf);
						sum++;
					}
				}
			}
			dataStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
		return sum;
	}

	/**
	 * update表格tableName符合条件(conditions AndOrs组成的条件)的记录
	 * 
	 * @param setPairs
	 * @param conditions
	 * @param AndOrs
	 * @return 影响记录条数
	 */
	public int updateTable(UpdateSetPair[] setPairs,
			WhereCondition[] conditions, int[] AndOrs) {
		if (AndOrs == null)// 不需要And Or操作
			return updateTable(setPairs, conditions);
		// 表格所在目录
		File tableDir = new File(rootDir + tableName);
		boolean isDirectory = tableDir.isDirectory();
		if (!isDirectory) {
			System.out.println("表格：" + tableName + "不存在");
			return 0;
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
			return 0;
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
			return 0;
		}
		int totalRecord = map.getSetNum();
		if (totalRecord == 0) {
			// 总记录数为0，则直接返回
			return 0;
		}

		int sum = 0;// 影响的记录数
		/* 读取数据文件 */

		String dataFileName = tableFilesPrefix + DataFileExt;
		File dataFile = new File(dataFileName);
		RandomAccessFile dataStream = null;
		try {
			dataStream = new RandomAccessFile(dataFile, "rw");
			boolean[] bits = map.getMap();
			byte[] buf = new byte[recordSize];
			String[] oneRecord;
			byte[] newRecordBuf;
			for (int i = 0; i < bits.length; i++) {
				if (bits[i]) {
					// 读取一条记录，读到buf中
					dataStream.seek(i * recordSize);
					dataStream.readFully(buf);
					// 将buf数据转化为String[]
					oneRecord = Field.parseBytesToStrings(buf, fields);
					if (WhereCondition.checkRecordByFields(oneRecord, fields,
							conditions,AndOrs)) {
						UpdateSetPair.updateRecordByFields(oneRecord, fields,
								setPairs);
						newRecordBuf = Field.parseStringsToBytes(oneRecord,
								fields);
						dataStream.seek(i * recordSize);
						dataStream.write(newRecordBuf);
						sum++;
					}
				}
			}
			dataStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
		return sum;
	}

	/**
	 * 转化为DDL（Data Definition Language数据库模式定义语言）
	 * 
	 * @return create_table_sql
	 */
	public String toDDLString() {
		if (this.tableName == null) {
			return "还没有指定表的名字";
		}
		String ddl = "Create Table " + this.tableName + "( ";
		if (this.tableFields != null) {
			int i = 0;
			for (i = 0; i < this.tableFields.length - 1; i++)
				ddl += this.tableFields[i].toDDLString() + ", ";
			ddl += this.tableFields[i].toDDLString();
		}
		ddl += " );";
		return ddl;
	}

	/**
	 * 测试Table的例子
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String stuTableName = "student";
		Field[] fields = { new Field("ID", Field.BigintType),
				new Field("Name", Field.VarcharType) };
		Table stuTable = new Table();

		boolean createFlag;
		System.out.println(stuTable.toDDLString());
		createFlag = stuTable.createTable(stuTableName, fields);
		System.out.println(stuTable.toDDLString());
		System.out.println("createFlag:" + createFlag);

		BitMap map;
		// System.out.println("现在bitmap位图：");
		// map = stuTable.getTableBitMap();
		// map.showMap();

		boolean insertFlag;
		String[] oneRecord = new String[2];
		// 插入一些记录
		oneRecord[0] = "123";
		oneRecord[1] = "hao";
		insertFlag = stuTable.insertIntoTable(oneRecord);
		System.out.println("insertFlag:" + insertFlag);
		oneRecord[0] = "12";
		oneRecord[1] = "asdf";
		insertFlag = stuTable.insertIntoTable(oneRecord);
		System.out.println("insertFlag:" + insertFlag);
		oneRecord[0] = "1";
		oneRecord[1] = "liu";
		insertFlag = stuTable.insertIntoTable(oneRecord);
		System.out.println("insertFlag:" + insertFlag);
		oneRecord[0] = "4";
		oneRecord[1] = "helloworld";
		insertFlag = stuTable.insertIntoTable(oneRecord);
		System.out.println("insertFlag:" + insertFlag);

		System.out.println("现在bitmap位图：");
		map = stuTable.getTableBitMap();
		map.showMap();

		String[][] students;

		System.out.println("selectAllFromTable");
		students = stuTable.selectAllFromTable();
		System.out.println("总记录数：" + students.length);
		for (int i = 0; i < students.length; i++) {
			for (int j = 0; j < students[i].length; j++) {
				System.out.print(students[i][j] + " ");
			}
			System.out.println();
		}

		WhereCondition[] conditions = new WhereCondition[3];
		conditions[0] = new WhereCondition("id", WhereCondition.GreatThanOp,
				"3");
		conditions[1] = new WhereCondition("name", WhereCondition.LessEqualOp,
				"liu");
		conditions[2] = new WhereCondition("name", WhereCondition.LikeOp,
				"hao", true);
		System.out.println("Where Conditions:");
		for (int i = 0; i < conditions.length; i++) {
			System.out.println("    condition[" + i + "]"
					+ conditions[i].toString());
		}

		System.out.println("selectFromTable Where Conditions:");
		students = stuTable.selectFromTable(conditions);
		System.out.println("记录数：" + students.length);
		for (int i = 0; i < students.length; i++) {
			for (int j = 0; j < students[i].length; j++) {
				System.out.print(students[i][j] + " ");
			}
			System.out.println();
		}

		int num = 0;
		UpdateSetPair setPairs[] = new UpdateSetPair[2];
		setPairs[0] = new UpdateSetPair("id", "234");
		setPairs[1] = new UpdateSetPair("name", "ahaha12345678901234567890");
		for (int i = 0; i < setPairs.length; i++) {
			System.out.println(setPairs[i].toString());
		}
		System.out.println("updateTable Where Conditions:");
		num = stuTable.updateTable(setPairs, conditions);
		System.out.println("更新记录数:" + num);

		System.out.println("再次selectAllFromTable");
		students = stuTable.selectAllFromTable();
		System.out.println("总记录数：" + students.length);
		for (int i = 0; i < students.length; i++) {
			for (int j = 0; j < students[i].length; j++) {
				System.out.print(students[i][j] + " ");
			}
			System.out.println();
		}

		System.out.println("再次selectFromTable Where Conditions:");
		students = stuTable.selectFromTable(conditions);
		System.out.println("记录数：" + students.length);
		for (int i = 0; i < students.length; i++) {
			for (int j = 0; j < students[i].length; j++) {
				System.out.print(students[i][j] + " ");
			}
			System.out.println();
		}

		System.out.println("deleteFromTable Where Conditions:");
		num = stuTable.deleteFromTable(conditions);
		System.out.println("删除记录数:" + num);

		System.out.println("现在bitmap位图：");
		map = stuTable.getTableBitMap();
		map.showMap();

		students = stuTable.selectAllFromTable();
		System.out.println("now总记录数：" + students.length);
		for (int i = 0; i < students.length; i++) {
			for (int j = 0; j < students[i].length; j++) {
				System.out.print(students[i][j] + " ");
			}
			System.out.println();
		}

		/*
		System.out.println("deleteAllFromTable");
		num = stuTable.deleteAllFromTable();
		System.out.println("删除记录数:" + num);
		*/
		
		System.out.println("现在bitmap位图：");
		map = stuTable.getTableBitMap();
		map.showMap();

		students = stuTable.selectAllFromTable();
		System.out.println("now总记录数：" + students.length);
		for (int i = 0; i < students.length; i++) {
			for (int j = 0; j < students[i].length; j++) {
				System.out.print(students[i][j] + " ");
			}
			System.out.println();
		}
	}
}
