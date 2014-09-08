package table;

import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WhereCondition {
	
	String fieldName;		//字段名
	int comparisonOp;			//关系操作符
	String referencValue;	//参照值
	boolean negation;
	
	public static final int EqualOp = 1;
	public static final int LessThanOp = 2;
	public static final int GreatThanOp = 3;
	public static final int LessEqualOp = 4;
	public static final int GreatEqualOp = 5;
	public static final int NotEqualOp = 6;
	public static final int LikeOp = 7;
	
	public static final int RelationAndOp = 11;
	public static final int RelationOrOp = 12;
	public static final int RelationNotOp = 13;
	
	/**
	 * 构造方法
	 * @param fieldName
	 * @param relationOp
	 * @param referencValue
	 */
	public WhereCondition(String fieldName, int comparisonOp, String referencValue){
		this.fieldName = fieldName.toUpperCase();
		this.comparisonOp = comparisonOp;
		this.referencValue = referencValue;
		this.negation = false;
	}
	
	/**
	 * 构造方法2
	 * @param fieldName
	 * @param comparisonOp
	 * @param referencValue
	 * @param negation
	 */
	public WhereCondition(String fieldName, int comparisonOp, String referencValue, boolean negation){
		this.fieldName = fieldName.toUpperCase();
		this.comparisonOp = comparisonOp;
		this.referencValue = referencValue;
		this.negation = negation;
	}
	
	/**
	 * toString()
	 */
	public String toString(){
		String temp="";
		if(this.negation)
			temp += "Not";
		temp += "("+this.fieldName+" ";
		switch (this.comparisonOp) {
		case EqualOp:
			temp += "=";
			break;
		case LessThanOp:
			temp += "<";
			break;
		case GreatThanOp:
			temp += ">";
			break;
		case LessEqualOp:
			temp += "<=";
			break;
		case GreatEqualOp:
			temp += ">=";
			break;
		case NotEqualOp:
			temp += "!=";
			break;
		case LikeOp:
			temp += "LIKE";
			break;
		}
		temp += " "+this.referencValue+")";
		return temp;
	}
	
	/**
	 * 解析DDL（Data Definition Language）语句到WhereCondition对象
	 * @param whereDDLStr
	 * @return
	 */
	public static WhereCondition parseDDLString(String whereDDLStr){
		String ddl = whereDDLStr.trim();
		//System.out.println(ddl);
		if(ddl.charAt(0)=='('){//第一个字符为'('，则最后一个字符为')',否则错误
			ddl = ddl.substring(1,ddl.length()-1).trim();//去掉括号
			//System.out.println(ddl);
		}
		//System.out.println("Str:"+ddl);
		WhereCondition wc = null;
		String name,operator,value;
		int opInt = 1;
		boolean notFlag = false;
		int p1,p2,i;
		
		//检索字段名的结束
		//p1 = ddl.indexOf(' ');
		i=0;
		while(i<ddl.length()){
			if(!Character.isLetter(ddl.charAt(i)))//不是字符(空格也不是字符)
				break;
			i++;
		}
		p1 = i;
		if(p1==ddl.length()){//格式错误
			return null;
		}
		p2 = ddl.indexOf('(');
		//System.out.println("first:"+p1+","+p2);
		if(p2!=-1 && p2<p1){//有NOT修饰，且NOT后紧跟'('
			p1=p2;
		}
		//得到字段名
		name = ddl.substring(0, p1);
		if(name.equalsIgnoreCase("NOT")){
			//判断是否有NOT修饰
			notFlag = true;
			p2 = ddl.indexOf('(', p1);
			if(p2!=-1){//NOT后有'('
				p1 = p2;
				p2 = ddl.indexOf(')', p1+1);//找到右括号')'
				if(p2==-1){//格式错误
					return null;
				}
				//System.out.println("in not:"+p1+","+p2);
				ddl = ddl.substring(p1+1, p2).trim();
				//System.out.println(ddl);
				p1 = 0;//重新定位p1
			}
			//检索字段名的结束
			while(ddl.charAt(p1)==' '){	p1++; }//跳过空格
			//p2 = ddl.indexOf(' ', p1+1);
			i=p1+1;
			while(i<ddl.length()){
				if(!Character.isLetter(ddl.charAt(i)))//不是字符
					break;
				i++;
			}
			p2 = i;
			//System.out.println("after not:"+p1+","+p2);
			if(p2==ddl.length()){//格式错误
				return null;
			}
			name = ddl.substring(p1, p2).trim();
			p1 = p2;
		}
		//System.out.println("name:"+name);
		
		//得到比较操作符,p1为比较操作符的开始
		while(ddl.charAt(p1)==' '){	p1++; }//跳过空格
		//检索比较操作符的结束
		//p2 = ddl.indexOf(' ', p1);
		String subStrUp = ddl.substring(p1).toUpperCase();
		if(subStrUp.startsWith("LIKE")){
			p2 = p1+4;
		}else{
			i=p1+1;
			while(i<ddl.length()){
				char ci = ddl.charAt(i);
				if(ci==' ' || (ci!='=' && ci!='<' && ci!='>' && ci!='!'))//是字符
					break;
				i++;
			}
			p2 = i;
		}
		//System.out.println("second:"+p1+","+p2);
		if(p2==ddl.length()){//格式错误
			return null;
		}
		operator = ddl.substring(p1, p2).trim();
		//System.out.println("operator:"+operator);
		switch (operator.length()) {
		case 1:// = < >
			switch (operator.charAt(0)) {
			case '=':
				opInt = EqualOp;
				break;
			case '<':
				opInt = LessThanOp;
				break;
			case '>':
				opInt = GreatThanOp;
				break;
			default://格式错误
				return null;
			}
			break;
		case 2:// <= >= !=
			if(operator.charAt(1)!='='){//格式错误
				return null;
			}
			switch (operator.charAt(0)) {
			case '<':
				opInt = LessEqualOp;
				break;
			case '>':
				opInt = GreatEqualOp;
				break;
			case '!':
				opInt = NotEqualOp;
				break;
			default://格式错误
				return null;
			}
			break;
		case 4://like
			if(operator.equalsIgnoreCase("LIKE")){
				opInt = LikeOp;
			}else{//格式错误
				return null;
			}
			break;
		default://格式错误
			return null;
		}
		//得到参考值
		value = ddl.substring(p2).trim();
		//System.out.println("value:"+value);
		wc = new WhereCondition(name,opInt,value,notFlag);
		
		return wc;
	}
	
	/**
	 * 解析DDL（Data Definition Language）语句到一组WhereCondition[]对象
	 * @param wheresDDLString
	 * @param andOrsList
	 * @return
	 */
	public static WhereCondition[] getWheresFromDDLString(String wheresDDLString, ArrayList<Integer> andOrsList){
		String ddl = wheresDDLString.trim();
		String ddlUpper = ddl.toUpperCase();
		WhereCondition wc;
		ArrayList<WhereCondition> wheres = new ArrayList<WhereCondition>(5);
		int posAnd,posOr,posStart,posOneWC;
		String subDDL;
		
		//第一单词为 where，其后是一个空格
		if(! ddl.substring(0, 5).equalsIgnoreCase("WHERE") ){
			System.out.println("语句错了，应以‘where’开头");
			return null;
		}
		posStart = ddl.indexOf(' ')+1;
		if(posStart == -1){
			System.out.println("格式错误，应以‘where’开头,其后是一个空格");
			return null;
		}
		//搜索"And" 或 "Or"
		posAnd = ddlUpper.indexOf("AND",posStart);
		posOr = ddlUpper.indexOf("OR",posStart);
		if(posAnd==-1 && posOr==-1){		//没有"And" 或 "Or"
			posOneWC = -1;
		}else if(posAnd==-1 && posOr!=-1){	//有"Or"
			posOneWC = posOr;
			andOrsList.add(new Integer(WhereCondition.RelationOrOp));
		}else if(posAnd!=-1 && posOr==-1){	//有"And"
			posOneWC = posAnd;
			andOrsList.add(new Integer(WhereCondition.RelationAndOp));
		}else{								//索引小的为准
			if(posAnd>posOr){	//or
				posOneWC = posOr;
				andOrsList.add(new Integer(WhereCondition.RelationOrOp));
			}else{				//and
				posOneWC = posAnd;
				andOrsList.add(new Integer(WhereCondition.RelationAndOp));
			}
		}
		while(posOneWC!=-1){
			subDDL = ddl.substring(posStart,posOneWC);
			//System.out.println(subDDL);
			wc = WhereCondition.parseDDLString(subDDL);
			wheres.add(wc);
			posStart = posOneWC+3;//查找下一个
			//搜索"And" 或 "Or"
			posAnd = ddlUpper.indexOf("AND",posStart);
			posOr = ddlUpper.indexOf("OR",posStart);
			if(posAnd==-1 && posOr==-1){		//没有"And" 或 "Or"
				posOneWC = -1;
			}else if(posAnd==-1 && posOr!=-1){	//有"Or"
				posOneWC = posOr;
				andOrsList.add(new Integer(WhereCondition.RelationOrOp));
			}else if(posAnd!=-1 && posOr==-1){	//有"And"
				posOneWC = posAnd;
				andOrsList.add(new Integer(WhereCondition.RelationAndOp));
			}else{								//索引小的为准
				if(posAnd<posOr){	//or
					posOneWC = posOr;
					andOrsList.add(new Integer(WhereCondition.RelationOrOp));
				}else{				//and
					posOneWC = posAnd;
					andOrsList.add(new Integer(WhereCondition.RelationAndOp));
				}
			}
		}
		//最后一个
		subDDL = ddl.substring(posStart);
		//System.out.println(subDDL);
		wc = WhereCondition.parseDDLString(subDDL);
		wheres.add(wc);
		
		//转换
		WhereCondition[] wcs = new WhereCondition[wheres.size()];
		wheres.toArray(wcs);
		return wcs;
	}
	
	/**
	 * 继getWheresFromDDLString之后解析andOrsList到一个int[]
	 * @param andOrsList
	 * @return
	 */
	public static int[] getAndOrs(ArrayList<Integer> andOrsList){
		int andOrNum = andOrsList.size();
		//System.out.println("andOrNum:"+andOrNum);
		int[] andOrs = new int[andOrNum];
		for(int i=0;i<andOrNum;i++){
			andOrs[i] = andOrsList.get(i).intValue();
		}
		return andOrs;
	}
	
	/**
	 * 根据fields信息，判断oneRecord记录是否符合conditions
	 * @param oneRecord
	 * @param fields
	 * @param conditions
	 * @return
	 */
	@Deprecated
	public static boolean checkRecordByFields(String[] oneRecord,Field[] fields,WhereCondition[] conditions){
		if(conditions==null || conditions.length==0)
			return true;
		boolean compareFlag = true;
		boolean curFlag;
		//逐个遍历条件
		for (int i = 0; i < conditions.length; i++) {
			if(conditions[i]==null)
				continue;
			curFlag = conditions[i].checkRecordByFields(oneRecord, fields);
			compareFlag = compareFlag && curFlag;
			//完成一个条件的判断
		}
		return compareFlag;
	}
	
	/**
	 * 根据fields信息，判断oneRecord记录是否符合(conditions AndOrs组成的条件)
	 * @param oneRecord
	 * @param fields
	 * @param conditions
	 * @param AndOrs
	 * @return
	 */
	public static boolean checkRecordByFields(String[] oneRecord,Field[] fields,WhereCondition[] conditions, int[] AndOrs){
		if(conditions==null || conditions.length==0)
			return true;
		if(AndOrs == null)// 不需要And Or操作
			return checkRecordByFields(oneRecord, fields, conditions);
		if(conditions.length != (AndOrs.length+1)){
			//操作数应该比操作符多一个
			return false;
		}
		
		boolean[] bools = new boolean[conditions.length];
		//逐个遍历条件
		for (int i = 0; i < conditions.length; i++) {
			if(conditions[i]==null){
				bools[i] = true;
				continue;
			}
			bools[i] = conditions[i].checkRecordByFields(oneRecord, fields);
		}
		return testBoolAndOrs(bools, AndOrs);
	}
	
	/**
	 * 判断一组bools[]值 AndOrs[]操作后的结果
	 * @param bools
	 * @param AndOrs
	 * @return
	 */
	static boolean testBoolAndOrs(boolean[] bools, int[] AndOrs){
		if(bools.length != (AndOrs.length+1)){
			//操作数应该比操作符多一个
			return false;
		}
		if(bools.length==1){
			return bools[0];
		}
		Stack<Boolean> booleans = new Stack<Boolean>();
		Stack<Integer> ops = new Stack<Integer>();
		
		booleans.push(new Boolean(bools[0]));
		ops.push(new Integer(AndOrs[0]));
		
		int oldOp,newOp;
		boolean flag1,flag2,flag1OpFlag2;
		int i;
		for(i=1;i<AndOrs.length;i++){
			oldOp = ops.peek().intValue();	//查看栈顶操作符
			newOp = AndOrs[i];
			flag1 = booleans.peek().booleanValue();	//查看栈顶操作数
			flag2 = bools[i];
			if(newOp>=oldOp){
				//flag1 && flag2 || flag3的情况
				//flag1 && flag2 && flag3的情况
				//flag1 || flag2 || flag3的情况
				booleans.pop();	//移除堆栈顶部的对象flag1
				ops.pop();		//移除堆栈顶部的对象oldOp
				//flag1 oldOp flag2
				if(oldOp==RelationAndOp)
					flag1OpFlag2 = flag1 && flag2;
				else//(oldOp==Or)
					flag1OpFlag2 = flag1 || flag2;
				booleans.push(new Boolean( flag1OpFlag2 ));
				ops.push(new Integer(newOp));
			}else{
				//flag1 || flag2 && flag3的情况
				booleans.push(new Boolean( flag2 ));
				ops.push(new Integer(newOp));
			}
		}
		//System.out.println("now i="+i);
		booleans.push(new Boolean(bools[i]));//将最后一个操作数压栈
		
		//System.out.println(booleans.size());
		//System.out.println(ops.size());
		while(!ops.isEmpty()){
			oldOp = ops.pop().intValue();
			flag2 = booleans.pop().booleanValue();
			flag1 = booleans.pop().booleanValue();
			//flag1 oldOp flag2
			if(oldOp==RelationAndOp)
				flag1OpFlag2 = flag1 && flag2;
			else//(oldOp==Or)
				flag1OpFlag2 = flag1 || flag2;
			booleans.push(new Boolean( flag1OpFlag2 ));
		}
		//System.out.println(booleans.size());
		//System.out.println(ops.size());
		
		return booleans.pop().booleanValue();
	}
	
	/**
	 * 根据this对象判断oneRecord是否符合记录
	 * @param oneRecord
	 * @param fields
	 * @return
	 */
	boolean checkRecordByFields(String[] oneRecord,Field[] fields){
		boolean flag = false;
		int curInt,referenceInt;
		long curLong,referenceLong;
		String curStr,referenceStr;
		
		referenceStr = this.referencValue;//参考比较值
		// 逐个遍历字段
		for (int i = 0; i < fields.length; i++) {
			//比较字段名
			if(fieldName.equalsIgnoreCase(fields[i].fieldName)){
				curStr = oneRecord[i];
				// 根据字段类型，做相应转换
				switch (fields[i].fieldType) {
				case Field.IntType:
					try {
						curInt = Integer.parseInt(curStr);
					} catch (Exception e) {
						curInt = 0;
					}
					try {
						referenceInt = Integer.parseInt(referenceStr);
					} catch (Exception e) {
						referenceInt = 0;
					}
					flag = compareByOp(this.comparisonOp, curInt, referenceInt);
					break;
				case Field.BigintType:
					try {
						curLong = Long.parseLong(curStr);
					} catch (Exception e) {
						curLong = 0;
					}
					try {
						referenceLong = Long.parseLong(referenceStr);
					} catch (Exception e) {
						referenceLong = 0;
					}
					flag = compareByOp(this.comparisonOp, curLong, referenceLong);
					break;
				default:
					flag = compareByOp(this.comparisonOp, curStr, referenceStr);
					break;
				}
			}
		}
		if(this.negation)
			return !flag;
		return flag;
	}
	
	/**
	 * current op reference比较运算(整数)
	 * @param op
	 * @param current
	 * @param reference
	 * @return
	 */
	public static boolean compareByOp(int op,long current,long reference){
		boolean flag = true;
		switch (op) {
		case EqualOp:
			flag = current==reference;
			break;
		case LessThanOp:
			flag = current<reference;
			break;
		case GreatThanOp:
			flag = current>reference;
			break;
		case LessEqualOp:
			flag = current<=reference;
			break;
		case GreatEqualOp:
			flag = current>=reference;
			break;
		case NotEqualOp:
			flag = current!=reference;
			break;
		default:
			flag = false;
			break;
		}
		return flag;
	}
	
	/**
	 * current op reference比较运算(String)
	 * @param op
	 * @param current
	 * @param reference
	 * @return
	 */
	public static boolean compareByOp(int op,String current,String reference){
		boolean flag = true;
		switch (op) {
		case EqualOp:
			flag = current.compareTo(reference)==0;
			break;
		case LessThanOp:
			flag = current.compareTo(reference)<0;
			break;
		case GreatThanOp:
			flag = current.compareTo(reference)>0;
			break;
		case LessEqualOp:
			flag = current.compareTo(reference)<=0;
			break;
		case GreatEqualOp:
			flag = current.compareTo(reference)>=0;
			break;
		case NotEqualOp:
			flag = current.compareTo(reference)!=0;
			break;
		case LikeOp:
			reference = reference.trim();
			reference = reference.replace("%", "(.)*");
			//System.out.println(reference);
			//System.out.println(current);
			Pattern p = Pattern.compile(reference);	//正则表达式    
			Matcher m = p.matcher(current);			//操作的字符串
			flag = m.matches();
			//System.out.println("匹配结果:"+flag);
			break;
		default:
			flag = false;
			break;
		}
		return flag;
	}

	/**
	 * 测试
	 * @param args
	 */
	public static void main(String[] args) {
		/*
		Field[] fields = {new Field("ID",Field.BigintType),new Field("Name",Field.VarcharType)};
		
		int n = 3;
		WhereCondition[] conditions = new WhereCondition[n];
		conditions[0] = new WhereCondition("id",WhereCondition.GreatThanOp,"3");
		conditions[1] = new WhereCondition("name",WhereCondition.LessEqualOp,"hao");
		conditions[2] = new WhereCondition("name",WhereCondition.LikeOp,"h%",true);
		int[] AndOrs = new int[n-1];
		String[] oneRecord = new String[2];
		
		for(int times=0; times<5; times++ ){
			System.out.println("第"+(times+1)+"次:");
			int i = 0;
			//对AndOrs随即赋值
			for (i = 0; i < AndOrs.length; i++) {
				AndOrs[i] = ((int)(Math.random()*2))==0?RelationAndOp:RelationOrOp;
			}
			//输出表达式
			System.out.print("(");
			for (i = 0; i < n - 1; i++) {
				System.out.print(conditions[i].toString() + " "
						+ ((AndOrs[i] == RelationAndOp) ? "And" : "Or")
						+ " ");
			}
			System.out.print(conditions[i].toString() + ")");
			System.out.println();
			
			oneRecord[0] = "123";oneRecord[1] = "hao";
			System.out.println(oneRecord[0]+" "+oneRecord[1]);
			System.out.println(conditions[0].checkRecordByFields(oneRecord, fields));
			System.out.println(conditions[1].checkRecordByFields(oneRecord, fields));
			System.out.println(conditions[2].checkRecordByFields(oneRecord, fields));
			System.out.println("If Ands:"+checkRecordByFields(oneRecord, fields, conditions));
			System.out.println(checkRecordByFields(oneRecord, fields, conditions, AndOrs));
			System.out.println();
			
			oneRecord[0] = "12";oneRecord[1] = "asdf";
			System.out.println(oneRecord[0]+" "+oneRecord[1]);
			System.out.println(conditions[0].checkRecordByFields(oneRecord, fields));
			System.out.println(conditions[1].checkRecordByFields(oneRecord, fields));
			System.out.println(conditions[2].checkRecordByFields(oneRecord, fields));
			System.out.println("If Ands:"+checkRecordByFields(oneRecord, fields, conditions));
			System.out.println(checkRecordByFields(oneRecord, fields, conditions, AndOrs));
			System.out.println();
	
			oneRecord[0] = "1";oneRecord[1] = "liu";
			System.out.println(oneRecord[0]+" "+oneRecord[1]);
			System.out.println(conditions[0].checkRecordByFields(oneRecord, fields));
			System.out.println(conditions[1].checkRecordByFields(oneRecord, fields));
			System.out.println(conditions[2].checkRecordByFields(oneRecord, fields));
			System.out.println("If Ands:"+checkRecordByFields(oneRecord, fields, conditions));
			System.out.println(checkRecordByFields(oneRecord, fields, conditions, AndOrs));
			System.out.println();
	
			oneRecord[0] = "4";oneRecord[1] = "helloworld";
			System.out.println(oneRecord[0]+" "+oneRecord[1]);
			System.out.println(conditions[0].checkRecordByFields(oneRecord, fields));
			System.out.println(conditions[1].checkRecordByFields(oneRecord, fields));
			System.out.println(conditions[2].checkRecordByFields(oneRecord, fields));
			System.out.println("If Ands:"+checkRecordByFields(oneRecord, fields, conditions));
			System.out.println(checkRecordByFields(oneRecord, fields, conditions, AndOrs));
			System.out.println();
		}
		*/
		
		/*
		WhereCondition wc = WhereCondition.parseDDLString(" not ( name < = hao )   ");
		if(wc==null){
			System.out.println("格式错误！");
		}else{
			System.out.println(wc.toString());
		}
		*/
		
		ArrayList<Integer> AndOrsList = new ArrayList<Integer>(5);
		//WhereCondition[] wcs = WhereCondition.getWheresFromDDLString("where not ( name < = hao )  and id > 4 or sdf = 9 ", AndOrsList);
		WhereCondition[] wcs = WhereCondition.getWheresFromDDLString("where not id!=4 ", AndOrsList);
		if(wcs.length==1){
			System.out.println(wcs[0].toString());
		}else{
			int[] andOrs = getAndOrs(AndOrsList);
			
			int i;
			//输出表达式
			for (i = 0; i < andOrs.length; i++) {
				System.out.print(wcs[i].toString() + " "
						+ ((andOrs[i] == RelationAndOp) ? "And" : "Or")
						+ " ");
			}
			System.out.println(wcs[i].toString());
		}
	}
}
