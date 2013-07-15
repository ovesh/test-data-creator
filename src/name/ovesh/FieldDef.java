package name.ovesh;

import java.io.Serializable;
import java.sql.Types;
import org.jdom.Element;
import name.ovesh.content.ContentDef;

public class FieldDef implements Serializable{
	private String name = null;
	private int size = -1;
	private int precision = -1;
	private int type;
	private ContentDef contentDef;
	private boolean referenced = false;
	private boolean nullOk = false;
	private int nullRatio = 0;

	public FieldDef(){}

	public String getName() {
		return name;
	}

	public int getPrecision() {
		return precision;
	}

	public int getSize() {
		return size;
	}

	public int getType() {
		return type;
	}

	public void setName(String name) {
		this.name = name.toUpperCase();
	}

	public void setPrecision(int i) {
		precision = i;
	}

	public void setSize(int i) {
		size = i;
	}

	public void setType(int i) {
		type = i;
	}
	
	public static final int typeNameToInt(String str){
		str = str.toUpperCase();
		
		if("ARRAY".equals(str)) return Types.ARRAY;
		else if("BIGINT".equals(str)) return Types.BIGINT;
		else if("BINARY".equals(str)) return Types.BINARY;
		else if("BIT".equals(str)) return Types.BIT;
		else if("BLOB".equals(str)) return Types.BLOB;
		else if("BOOLEAN".equals(str)) return Types.BOOLEAN;
		else if("CHAR".equals(str)) return Types.CHAR;
		else if("CLOB".equals(str)) return Types.CLOB;
		else if("DATALINK".equals(str)) return Types.DATALINK;
		else if("DATE".equals(str)) return Types.DATE;
		else if("DECIMAL".equals(str)) return Types.DECIMAL;
		else if("DISTINCT".equals(str)) return Types.DISTINCT;
		else if("DOUBLE".equals(str)) return Types.DOUBLE;
		else if("DECIMAL".equals(str)) return Types.DECIMAL;
		else if("FLOAT".equals(str)) return Types.FLOAT;
		else if("INTEGER".equals(str)) return Types.INTEGER;
		else if("JAVA_OBJECT".equals(str)) return Types.JAVA_OBJECT;
		else if("LONGVARBINARY".equals(str)) return Types.LONGVARBINARY;
		else if("LONGVARCHAR".equals(str)) return Types.LONGVARCHAR;
		else if("NULL".equals(str)) return Types.NULL;
		else if("NUMERIC".equals(str)) return Types.NUMERIC;
		else if("OTHER".equals(str)) return Types.OTHER;
		else if("REAL".equals(str)) return Types.REAL;
		else if("REF".equals(str)) return Types.REF;
		else if("SMALLINT".equals(str)) return Types.SMALLINT;
		else if("STRUCT".equals(str)) return Types.STRUCT;
		else if("TIME".equals(str)) return Types.TIME;
		else if("TIMESTAMP".equals(str)) return Types.TIMESTAMP;
		else if("TINYINT".equals(str)) return Types.TINYINT;
		else if("VARBINARY".equals(str)) return Types.VARBINARY;
		else if("VARCHAR".equals(str)) return Types.VARCHAR;
		else if("TEXT".equals(str)) return Types.VARCHAR;
		else if("NUMBER".equals(str)) return Types.NUMERIC;
		else if("VARCHAR2".equals(str)) return Types.VARCHAR;
		
		throw new IllegalArgumentException("argument " + str + " does not map to any known value");
	}

	public void setType(String str) {
		type = typeNameToInt(str);
	}

	public boolean equals(Object obj) {
		return (obj instanceof FieldDef) && equals((FieldDef)obj);
	}
	
	public boolean equals(FieldDef fieldDef) {
		if(fieldDef == null)
			return false;
		if(name == null){
			if(fieldDef.name != null)
				return false;
		}
		else if(!name.equals(fieldDef.name))
			return false;
		if(size != fieldDef.size)
			return false;
		if(precision != fieldDef.precision)
			return false;
		if(type != fieldDef.type)
			return false;
//		if(!contentDef.equals(fieldDef.contentDef))
//			return false;
		if(referenced != fieldDef.referenced)
			return false;
		if(nullOk != fieldDef.nullOk)
			return false;
		if(nullRatio != fieldDef.nullRatio)
			return false;
		return true;
	}

	public String toString() {
		return name + "|" + sqlTypeToString(type) + "|" + size + "|" + precision;
	}
	
	public static final String sqlTypeToString(int type){
		switch(type){
		case(Types.ARRAY): return "ARRAY";
		case(Types.BIGINT): return "BIGINT";
		case(Types.BINARY): return "BINARY";
		case(Types.BIT): return "BIT";
		case(Types.BLOB): return "BLOB";
		case(Types.BOOLEAN): return "BOOLEAN";
		case(Types.CHAR): return "CHAR";
		case(Types.CLOB): return "CLOB";
		case(Types.DATALINK): return "DATALINK";
		case(Types.DATE): return "DATE";
		case(Types.DECIMAL): return "DECIMAL";
		case(Types.DISTINCT): return "DISTINCT";
		case(Types.DOUBLE): return "DOUBLE";
		case(Types.FLOAT): return "FLOAT";
		case(Types.INTEGER): return "INTEGER";
		case(Types.JAVA_OBJECT): return "JAVA_OBJECT";
		case(Types.LONGVARBINARY): return "LONGVARBINARY";
		case(Types.LONGVARCHAR): return "LONGVARCHAR";
		case(Types.NULL): return "NULL";
		case(Types.NUMERIC): return "NUMERIC";
		case(Types.OTHER): return "OTHER";
		case(Types.REAL): return "REAL";
		case(Types.REF): return "REF";
		case(Types.SMALLINT): return "SMALLINT";
		case(Types.STRUCT): return "STRUCT";
		case(Types.TIME): return "TIME";
		case(Types.TIMESTAMP): return "TIMESTAMP";
		case(Types.TINYINT): return "TINYINT";
		case(Types.VARBINARY): return "VARBINARY";
		case(Types.VARCHAR): return "VARCHAR";
		default: return null;
		}
	}

	public ContentDef getContentDef() {
		return contentDef;
	}

	public void setContentDef(ContentDef contentDef) {
		this.contentDef = contentDef;
	}

	public boolean isReferenced() {
		return referenced;
	}

	public void setReferenced(boolean referenced) {
		this.referenced = referenced;
	}

	public boolean isNullOk() {
		return nullOk;
	}

	public void setNullOk(boolean nullOk) {
		this.nullOk = nullOk;
	}

	public void setNullRatio(int nullRatio) {
		this.nullRatio = nullRatio;
	}

	public int getNullRatio() {
		return nullRatio;
	}
	
	public Element toXml(){
		Element res = new Element("fieldDef");
		res.setAttribute("name", name);
		res.setAttribute("size", Integer.toString(size));
		res.setAttribute("precision", Integer.toString(precision));
		res.setAttribute("type", Integer.toString(type));
		if(contentDef != null)
			res.addContent(contentDef.toXml());
		res.setAttribute("referenced", Boolean.toString(referenced));
		res.setAttribute("nullOk", Boolean.toString(nullOk));
		res.setAttribute("nullRatio", Integer.toString(nullRatio));
		return res;
	}
}
