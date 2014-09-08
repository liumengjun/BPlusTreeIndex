package table;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class Field {
	String fieldName;
	int fieldType;
	int fieldSize;
	
	public static final int TypeError = -1;
	public static final int IntType = 1;
	public static final int BigintType = 2;
	public static final int VarcharType = 3;
	public static final int CharType = 4;
	
	public static final int DefaultCharSize = 20;
	
	/**
	 * 
	 * @param name
	 * @param type
	 */
	public Field(String name,int type){
		fieldName = name.toUpperCase();
		fieldType = type;
		switch (fieldType) {
		case IntType:
			fieldSize = 4;
			break;
		case BigintType:
			fieldSize = 8;
			break;
		default:
			fieldSize = DefaultCharSize;
			break;
		}
	}
	
	/**
	 * 完整构造方法
	 * @param name
	 * @param type
	 * @param size
	 */
	public Field(String name,int type,int size){
		fieldName = name.toUpperCase();
		fieldType = type;
		if(size<=0)
			size = DefaultCharSize;
		fieldSize = size;
		switch (fieldType) {
		case IntType:
			fieldSize = 4;
			break;
		case BigintType:
			fieldSize = 8;
			break;
		}
	}
	
	/**
	 * 得到fieldName
	 * @return
	 */
	public String getFieldName(){
		return fieldName;
	}
	
	/**
	 * 转化成字符串fieldName-fieldType-fieldSize
	 */
	public String toString(){
		String temp="";
		temp += fieldName;
		temp += "-";
		temp += fieldType;
		temp += "-";
		temp += fieldSize;
		return temp;
	}
	
	/**
	 * 转化为DDL（Data Definition Language数据库模式定义语言）
	 * @return
	 */
	public String toDDLString(){
		String ddl = fieldName + " ";
		switch (fieldType) {
		case IntType:
			ddl += "Int";
			break;
		case BigintType:
			ddl += "BigInt";
			break;
		case VarcharType:
			ddl += "Varchar";
			break;
		case CharType:
			ddl += "Char";
			break;
		}
		ddl += "("+fieldSize+")";
		return ddl;
	}
	
	/**
	 * 将byte[]数据转化为一条String[]记录
	 * @param buf
	 * @return
	 */
	public static String[] parseBytesToStrings(byte[] buf, Field[] fields){
		int fieldCount = fields.length;
		int recordSize = 0;
		for(int i=0;i<fieldCount;i++){
			recordSize+=fields[i].fieldSize;
		}
		String[] oneRecord = new String[fieldCount];
		int offset = 0;
		for(int i=0;i<fieldCount;i++){
			int value = 0;
			int tempInt = 0;
			long bigValue = 0;
			long tempLong = 0;
			int charNum = 0;
			switch (fields[i].fieldType) {
			case IntType:
				//System.out.println("parseBytesToStrings:IntType");
				value = 0;
				tempInt = 0;
				for(int j=0;j<4;j++){
					tempInt = 0xff & buf[offset+j];
					value |= tempInt << j*8;
					//value |= (buf[offset+j])<<(j*8);//小端：低位在前，高位在后
				}
				//for(int j=0;j<4;j++)
				//	System.out.print(Integer.toHexString(0xff&buf[offset+j])+" ");
				//System.out.println();
				oneRecord[i] = String.valueOf(value);
				//System.out.println(oneRecord[i]);
				//System.out.println(Integer.toHexString(value));
				break;
			case BigintType:
				bigValue = 0;
				tempLong = 0;
				for(int j=0;j<8;j++){
					tempLong = 0xff & buf[offset+j];
					bigValue |= tempLong << j*8;
					//bigValue |= (buf[offset+j])<<(j*8);//小端：低位在前，高位在后
				}
				oneRecord[i] = String.valueOf(bigValue);
				break;
			case VarcharType:
				byte[] varStr = new byte[fields[i].fieldSize];
				for(int j=0;j<varStr.length;j++)
					varStr[j] = buf[offset+j];
				charNum = 0;
				for(int j=0;j<varStr.length;j++){
					if(varStr[j]==0)
						break;
					charNum++;
				}
				oneRecord[i] = new String(varStr,0,charNum);
				break;
			case CharType:
				byte[] charStr = new byte[fields[i].fieldSize];
				for(int j=0;j<charStr.length;j++)
					charStr[j] = buf[offset+j];
				charNum = 0;
				for(int j=0;j<charStr.length;j++){
					if(charStr[j]==0)
						break;
					charNum++;
				}
				oneRecord[i] = new String(charStr,0,charNum);
				break;
			}
			offset+=fields[i].fieldSize;
		}
		return oneRecord;
	}
	
	/**
	 * 将一条String[]记录转化为byte[]
	 * @param oneRecord
	 * @return
	 */
	public static byte[] parseStringsToBytes(String[] oneRecord, Field[] fields){
		int fieldCount = fields.length;
		int recordSize = 0;
		for(int i=0;i<fieldCount;i++){
			recordSize+=fields[i].fieldSize;
		}
		byte[] buf = new byte[recordSize];
		int offset = 0;
		for(int i=0;i<fieldCount;i++){
			switch (fields[i].fieldType) {
			case IntType:
				//System.out.println("parseStringsToBytes:IntType");
				int value = Integer.parseInt(oneRecord[i]);
				//System.out.println(oneRecord[i]);
				//System.out.println(Integer.toHexString(value));
				for(int j=0;j<4;j++)
					buf[offset+j] = (byte)(value>>(j*8));//小端：低位在前，高位在后
				//for(int j=0;j<4;j++)
				//	System.out.print(Integer.toHexString(0xff&buf[offset+j])+" ");
				//System.out.println();
				break;
			case BigintType:
				long bigValue = Long.parseLong(oneRecord[i]);
				for(int j=0;j<8;j++)
					buf[offset+j] = (byte)(bigValue>>(j*8));//小端：低位在前，高位在后
				break;
			case VarcharType:
				byte[] varStr = oneRecord[i].getBytes();
				for(int j=0;j<varStr.length;j++)
					buf[offset+j] = varStr[j];
				for(int j=varStr.length;j<fields[i].fieldSize;j++)
					buf[offset+j] = 0;
				break;
			case CharType:
				byte[] charStr = oneRecord[i].getBytes();
				for(int j=0;j<charStr.length;j++)
					buf[offset+j] = charStr[j];
				for(int j=charStr.length;j<fields[i].fieldSize;j++)
					buf[offset+j] = 0;
				break;
			}
			offset+=fields[i].fieldSize;
		}
		return buf;
	}
	
	/**
	 * 检验oneRecord的String数组，并返回合法的数据
	 * @param oneRecord
	 * @param fields
	 * @return
	 */
	public static String[] checkRecordByFields(String[] oneRecord, Field[] fields){
		String[] legalRecord=null;
		int fieldCount = fields.length;
		if(fieldCount!=oneRecord.length){
			legalRecord = new String[fieldCount];
		}else{
			legalRecord = oneRecord;
		}
		for(int i=0;i<fieldCount;i++){
			switch (fields[i].fieldType) {
			case IntType:
				try{
					Integer.parseInt(legalRecord[i]);
				}catch(Exception e){
					legalRecord[i]="0";
				}
				break;
			case BigintType:
				try{
					Long.parseLong(legalRecord[i]);
				}catch(Exception e){
					legalRecord[i]="0";
				}
				break;
			case VarcharType:
				if(legalRecord[i]==null){
					legalRecord[i]="";
				}
				if(legalRecord[i].length()>fields[i].fieldSize){
					legalRecord[i] = legalRecord[i].substring(0, fields[i].fieldSize);
				}
				break;
			case CharType:
				if(legalRecord[i]==null){
					legalRecord[i]="";
				}
				if(legalRecord[i].length()>fields[i].fieldSize){
					legalRecord[i] = legalRecord[i].substring(0, fields[i].fieldSize);
				}
				break;
			default:
				legalRecord[i]="";
				break;
			}
		}
		
		return legalRecord;
	}
	
	/**
	 * 显示这个对象的详细信息
	 */
	public void showDetail(){
		System.out.print("Name:"+fieldName+"; Type:");
		switch (fieldType) {
		case IntType:
			System.out.print("Int");
			break;
		case BigintType:
			System.out.print("BigInt");
			break;
		case VarcharType:
			System.out.print("Varchar");
			break;
		case CharType:
			System.out.print("Char");
			break;
		}
		System.out.println("; Size:"+fieldSize+"B");
	}
	
	/**
	 * 解析fieldName-fieldType-fieldSize到Field对象
	 * @param filedString
	 * @return
	 */
	public static Field parseString(String fieldString){
		//String[] s = fieldString.split("-");
		//for(int i=0;i<s.length;i++)
		//	System.out.println(s[i]);
		StringTokenizer st = new StringTokenizer(fieldString,"-");
		String name = st.nextToken();
		int type = Integer.parseInt(st.nextToken());
		int size = Integer.parseInt(st.nextToken());
		return new Field(name,type,size);
	}
	
	/**
	 * 解析DDL（Data Definition Language）语句到Field对象
	 * @param fieldDDLStr
	 * @return
	 */
	public static Field parseDDLString(String fieldDDLStr){
		String ddl = fieldDDLStr.trim();
		Field f = null;
		String name,type,size;
		int p1,p2,p3,sizeInt;

		//得到字段名
		p1 = ddl.indexOf(' ');
		if(p1==-1){//格式错误
			return null;
		}
		name = ddl.substring(0, p1);
		
		//得到字段类型
		p2 = ddl.indexOf('(', p1+1);
		if(p2 == -1){//没有括号，int或bigint,或默认大小的VARCHAR，CHAR
			type = ddl.substring(p1+1).trim();
			if(type.equalsIgnoreCase("INT"))
				f = new Field(name,IntType);
			else if(type.equalsIgnoreCase("BIGINT"))
				f = new Field(name,BigintType);
			else{//默认大小的VARCHAR，CHAR
				if(type.equalsIgnoreCase("VARCHAR"))
					f = new Field(name,VarcharType);
				else if(type.equalsIgnoreCase("CHAR"))
					f = new Field(name,CharType);
				else{//不支持的字段类型
					return null;
				}
			}
		}else{//varchar或char
			type = ddl.substring(p1+1,p2).trim();
			p3 = ddl.indexOf(')', p2+1);
			if(p3==-1){//格式错误
				return null;
			}
			size = ddl.substring(p2+1, p3).trim();
			try{
				sizeInt = Integer.parseInt(size);
			}catch(Exception e){
				sizeInt = 0;
			}
			if(type.equalsIgnoreCase("VARCHAR"))
				f = new Field(name,VarcharType,sizeInt);
			else if(type.equalsIgnoreCase("CHAR"))
				f = new Field(name,CharType,sizeInt);
			else{//不支持的字段类型,或格式错误
				return null;
			}
		}
		
		return f;
	}
	
	/**
	 * 解析DDL（Data Definition Language）语句到一组Field[]对象
	 * @param fieldDDLStr
	 * @return
	 */
	public static Field[] getFieldsFromDDLString(String fieldDDLStr){
		String ddl = fieldDDLStr.trim();
		Field f;
		ArrayList<Field> fields = new ArrayList<Field>(5);
		int posStart,posOfComma;
		String subDDL;
		
		posStart = 0;
		posOfComma = ddl.indexOf(',',posStart);
		while(posOfComma!=-1){
			subDDL = ddl.substring(posStart,posOfComma);
			f = Field.parseDDLString(subDDL);
			fields.add(f);
			posStart = posOfComma+1;//查找下一个
			posOfComma = ddl.indexOf(',',posStart);
		}
		//最后一个
		subDDL = ddl.substring(posStart);
		f = Field.parseDDLString(subDDL);
		fields.add(f);
		//转换
		Field[] fs = new Field[fields.size()];
		fields.toArray(fs);
		return fs;
	}
	
	/**
	 * 检测fields中字段名是否唯一
	 * @param fields
	 * @return
	 */
	public static boolean checkFieldsNamesUnique(Field[] fields){
		for(int i=0;i<fields.length;i++){
			String curName = fields[i].fieldName;
			for(int j=i+1;j<fields.length;j++){
				String chechName = fields[j].fieldName;
				if(curName.equals(chechName))
					return false;
			}
		}
		return true;
	}
	
	/**
	 * 从fileds得到字段名colName的字段类型
	 * @param fileds
	 * @param colName
	 * @return
	 */
	public static int getTypeByNameFromFields(Field[] fields,String colName){
		String colNameUpper = colName.toUpperCase();
		for(int i=0;i<fields.length;i++){
			String curName = fields[i].fieldName;
			if(curName.equals(colNameUpper))
				return fields[i].fieldType;
		}
		return TypeError;
	}
	
	/**
	 * 从oneRecord中得到自定字段名的值
	 * @return
	 */
	public static String getValueByFieldName(String[] oneRecord, Field[] fields,String colName){
		int fieldCount = fields.length;
		if(fieldCount!=oneRecord.length){
			return "";
		}
		String colNameUpper = colName.toUpperCase();
		int i;
		for(i=0;i<fieldCount;i++){
			String curFiledName = fields[i].fieldName;
			if(colNameUpper.equals(curFiledName))
				break;
		}
		if(i<fieldCount){
			return oneRecord[i];
		}else{
			return "";
		}
	}
	
	/**
	 * 测试
	 * @param args
	 */
	public static void main(String[] args) {
		/*
		Field f1 = new Field("ID",Field.IntType);
		f1.showDetail();
		System.out.println(f1.toString());
		Field f2 = Field.parseString("sno-2-34");
		f2.showDetail();
		System.out.println(f2.toString());
		StringTokenizer o = new StringTokenizer(f2.toString(),"-");
		System.out.println(o.countTokens());
		while (o.hasMoreTokens()) {
	         System.out.println(o.nextToken());
	     }

		System.out.println(o.toString());
		*/
		
		/*
		int fieldCount = 3;
		Field[] fields=new Field[fieldCount];
		fields[0] = new Field("ID",Field.IntType);
		fields[1] = new Field("Name",Field.CharType);
		fields[2] = new Field("SNO",Field.BigintType);
		int recordSize = 0;
		for(int i=0;i<fieldCount;i++){
			recordSize+=fields[i].fieldSize;
			fields[i].showDetail();
			System.out.println(fields[i].toDDLString());
		}
		System.out.println("recordSize:"+recordSize);
		String[] oneRecord = {"-12332","姓名dfsd","-12345"};
		for(int i=0;i<fieldCount;i++){
			System.out.println(oneRecord[i]);
		}
		
		byte[] buf = Field.parseStringsToBytes(oneRecord, fields);
		for(int i=0;i<buf.length;i++){
			System.out.print(Integer.toHexString(0xff&buf[i])+" ");
		}
		System.out.println();
		System.out.println("buf.length:"+buf.length+","+new String(buf));
		
		String[] record2 = Field.parseBytesToStrings(buf, fields);
		for(int i=0;i<fieldCount;i++){
			System.out.println(record2[i]);
		}
		
		byte[] buf2 = Field.parseStringsToBytes(record2, fields);
		for(int i=0;i<buf2.length;i++){
			System.out.print(Integer.toHexString(0xff&buf[i])+" ");
		}
		System.out.println();
		System.out.println("buf2.length:"+buf2.length+","+new String(buf2));
		*/
		
		/*
		Field f1;
		f1 = parseDDLString("id  int");
		if(f1==null){
			System.out.println("语句错了");
		}else{
			System.out.println(f1.toString()+",即:"+f1.toDDLString());
		}
		
		f1 = parseDDLString("id  int()");
		if(f1==null){
			System.out.println("语句错了");
		}else{
			System.out.println(f1.toString()+",即:"+f1.toDDLString());
		}
		
		f1 = parseDDLString("id  bigint ");
		if(f1==null){
			System.out.println("语句错了");
		}else{
			System.out.println(f1.toString()+",即:"+f1.toDDLString());
		}
		
		f1 = parseDDLString("name  char()");
		if(f1==null){
			System.out.println("语句错了");
		}else{
			System.out.println(f1.toString()+",即:"+f1.toDDLString());
		}
		
		f1 = parseDDLString("name  varchar");
		if(f1==null){
			System.out.println("语句错了");
		}else{
			System.out.println(f1.toString()+",即:"+f1.toDDLString());
		}
		
		f1 = parseDDLString("sir  varchar(1)");
		if(f1==null){
			System.out.println("语句错了");
		}else{
			System.out.println(f1.toString()+",即:"+f1.toDDLString());
		}
		
		f1 = parseDDLString("sir  varchar(");
		if(f1==null){
			System.out.println("语句错了");
		}else{
			System.out.println(f1.toString()+",即:"+f1.toDDLString());
		}
		
		f1 = parseDDLString("sir  varchar)");
		if(f1==null){
			System.out.println("语句错了");
		}else{
			System.out.println(f1.toString()+",即:"+f1.toDDLString());
		}
		*/
		
		
		Field[] fields = Field.getFieldsFromDDLString("id int,name char,class varchar(10)");
		for(int i=0;i<fields.length;i++){
			if(fields[i]==null){
				System.out.println("语句错了");
				continue;
			}
			System.out.println(fields[i].toString()+",即:"+fields[i].toDDLString());
		}
		
		System.out.println("checkFieldsNamesUnique:"+checkFieldsNamesUnique(fields));
		
		
		/*
		byte[] buf=new byte[4];
		int value = -123;
		System.out.println(String.valueOf(value));
		for(int j=0;j<4;j++)
			buf[j] = (byte)(value>>(j*8));//小端：低位在前，高位在后
		System.out.println(Integer.toHexString(value));
		for(int i=0;i<buf.length;i++){
			System.out.print(Integer.toHexString(0xff&buf[i])+" ");
		}
		System.out.println();
		
		int value2 = 0;
		for(int j=0;j<4;j++)
			value2 |= (buf[j])<<(j*8);//小端：低位在前，高位在后
		System.out.println(Integer.toHexString(value2));
		System.out.println(String.valueOf(value2));
		*/
	}
}
