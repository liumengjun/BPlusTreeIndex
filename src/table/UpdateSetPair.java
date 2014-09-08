package table;

import java.util.ArrayList;

public class UpdateSetPair {
	
	String fieldName;
	String newValue;
	
	/**
	 * 构造方法
	 * @param fieldName
	 * @param newValue
	 */
	public UpdateSetPair(String fieldName, String newValue){
		this.fieldName = fieldName.toUpperCase();
		this.newValue = newValue;
	}
	
	/**
	 * 根据fields信息，更新oneRecord中setPairs指定的内容
	 * @param oneRecord
	 * @param fields
	 * @param setPairs
	 */
	public static void updateRecordByFields(String[] oneRecord, Field[] fields, UpdateSetPair[] setPairs){
		if(setPairs==null)
			return;
		for(int i=0;i<setPairs.length;i++){
			if(setPairs[i]==null)
				continue;
			setPairs[i].updateRecordByFields(oneRecord, fields);
		}
	}
	
	/**
	 * 根据fields信息，更新oneRecord中this_UpdateSetPair_object指定的内容
	 * @param oneRecord
	 * @param fields
	 */
	public void updateRecordByFields(String[] oneRecord, Field[] fields){
		// 逐个遍历字段
		for (int i = 0; i < fields.length; i++) {
			//比较字段名
			if(this.fieldName.equalsIgnoreCase(fields[i].fieldName)){
				String newSetValue = this.newValue;
				switch (fields[i].fieldType) {
				case Field.IntType:
					try {
						Integer.parseInt(newSetValue);
					} catch (Exception e) {
						newSetValue = "0";
					}
					break;
				case Field.BigintType:
					try {
						Long.parseLong(newSetValue);
					} catch (Exception e) {
						newSetValue = "0";
					}
					break;
				default:
					if(newSetValue.length()>fields[i].fieldSize){
						newSetValue = newSetValue.substring(0, fields[i].fieldSize);
					}
					break;
				}
				oneRecord[i] = newSetValue;
			}
		}
	}
	
	/**
	 * 转化为字符串
	 */
	public String toString(){
		return "set "+this.fieldName+"="+this.newValue;
	}
	
	/**
	 * 解析DDL（Data Definition Language）语句到UpdateSetPair对象
	 * @param fieldDDLStr
	 * @return
	 */
	public static UpdateSetPair parseDDLString(String fieldDDLStr){
		String ddl = fieldDDLStr.trim();
		UpdateSetPair setPair = null;
		String name,value;
		int p;
		
		p = ddl.indexOf('=');
		if(p==-1){//语句错了
			return null;
		}
		name = ddl.substring(0,p).trim();
		value = ddl.substring(p+1).trim();
		setPair = new UpdateSetPair(name,value);
		
		return setPair;
	}
	
	/**
	 * 解析DDL（Data Definition Language）语句到一组UpdateSetPair[]对象
	 * @param setPairDDLStr
	 * @return
	 */
	public static UpdateSetPair[] getPairsFromDDLString(String setPairDDLStr){
		String ddl = setPairDDLStr.trim();
		UpdateSetPair pair;
		ArrayList<UpdateSetPair> pairs = new ArrayList<UpdateSetPair>(5);
		int posStart,posOfComma;
		String subDDL;
		
		//第一单词为 set，其后是一个空格
		if(! ddl.substring(0, 3).equalsIgnoreCase("SET") ){
			System.out.println("语句错了，应以‘set’开头");
			return null;
		}
		posStart = ddl.indexOf(' ')+1;
		posOfComma = ddl.indexOf(',',posStart);
		while(posOfComma!=-1){
			subDDL = ddl.substring(posStart,posOfComma);
			pair = UpdateSetPair.parseDDLString(subDDL);
			pairs.add(pair);
			posStart = posOfComma+1;//查找下一个
			posOfComma = ddl.indexOf(',',posStart);
		}
		//最后一个
		subDDL = ddl.substring(posStart);
		pair = UpdateSetPair.parseDDLString(subDDL);
		pairs.add(pair);
		//转换
		UpdateSetPair[] ps = new UpdateSetPair[pairs.size()];
		pairs.toArray(ps);
		return ps;
	}
	
	/**
	 * 测试
	 * @param args
	 */
	public static void main(String[] args) {
		UpdateSetPair setPairs[];
		setPairs = UpdateSetPair.getPairsFromDDLString("set id=5,sdf=435,34= 3");
		for(int i=0;i<setPairs.length;i++){
			System.out.println(setPairs[i].toString());
		}
		
		setPairs = UpdateSetPair.getPairsFromDDLString("sEt id=,sdf=435,34=0");
		for(int i=0;i<setPairs.length;i++){
			if(setPairs[i]==null){
				System.out.println("语句错了");
				continue;
			}
			System.out.println(setPairs[i].toString());
		}
		
		/*
		UpdateSetPair setPairs[] = new UpdateSetPair[2];
		
		setPairs[0] = new UpdateSetPair("id","234");
		setPairs[1] = new UpdateSetPair("name","wahaha12345678901234567890");
		for(int i=0;i<setPairs.length;i++){
			System.out.println(setPairs[i].toString());
		}
		
		Field[] fields = {new Field("ID",Field.BigintType),new Field("Name",Field.VarcharType)};
		
		String[] oneRecord = new String[2];
		oneRecord[0] = "123";oneRecord[1] = "hao";
		for(int i=0;i<oneRecord.length;i++){
			System.out.print(oneRecord[i]+" ");
		}
		System.out.println();
		
		setPairs[0].updateRecordByFields(oneRecord, fields);
		for(int i=0;i<oneRecord.length;i++){
			System.out.print(oneRecord[i]+" ");
		}
		System.out.println();
		
		setPairs[1].updateRecordByFields(oneRecord, fields);
		for(int i=0;i<oneRecord.length;i++){
			System.out.print(oneRecord[i]+" ");
		}
		System.out.println();
		
		oneRecord[0] = "123";oneRecord[1] = "hao";
		for(int i=0;i<oneRecord.length;i++){
			System.out.print(oneRecord[i]+" ");
		}
		System.out.println();
		System.out.println("updateRecordByFields(oneRecord, fields, setPairs)");
		updateRecordByFields(oneRecord, fields, setPairs);
		for(int i=0;i<oneRecord.length;i++){
			System.out.print(oneRecord[i]+" ");
		}
		System.out.println();
		*/
	}
	
}
