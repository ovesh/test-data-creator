package name.ovesh;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jdom.Element;

public class TableDef implements Serializable{

	private String name = "";
	private List<FieldDef> fields;
	private int requestedRowNum = 10;
	private ArrayList<FieldDef> primaryKeys;

	public TableDef() {
		fields = new ArrayList<FieldDef>();
		primaryKeys = new ArrayList<FieldDef>();
	}

	public int getFieldNum() {
		return fields.size();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name.toUpperCase();
	}

	public void addFieldDef(FieldDef fieldDef) {
		fields.add(fieldDef);
	}

	public FieldDef getFieldDef(int n) {
		return fields.get(n);
	}

	public FieldDef getFieldDef(String fieldName) {
		for (Iterator<FieldDef> it = fields.iterator(); it.hasNext();) {
			FieldDef cur = it.next();
			if(cur.getName().equals(fieldName))
				return cur;
		}
		return null;
	}

	public String getFieldName(int n) {
		return fields.get(n).getName();
	}

	public boolean equals(Object obj) {
		return (obj instanceof TableDef) && equals((TableDef)obj);
	}

	public boolean equals(TableDef tableDef) {
		if(tableDef == null)
			return false;
		if(!name.equals(tableDef.name))
			return false;
		if(fields.size() != tableDef.fields.size())
			return false;
		for (int i = 0, size = fields.size(); i < size; i++) {
			FieldDef curField = fields.get(i);
			String fldName = curField.getName();
			FieldDef otherFldDef = tableDef.getFieldDef(fldName);
			if(!curField.equals(otherFldDef))
				return false;
		}

		return true;
	}

	public String toString() {
		return "name: " + name + ", fields: " + fields;
	}
	
	public void removeField(FieldDef field){
		fields.remove(field);
	}
	
	public FieldDef[] getFields(){
		return fields.toArray(new FieldDef[fields.size()]);
	}

	public void setRequestedRowNum(int requestedRowNum) {
		this.requestedRowNum = requestedRowNum;
	}

	public int getRequestedRowNum() {
		return requestedRowNum;
	}

	public ArrayList<FieldDef> getPrimaryKeys() {
		return primaryKeys;
	}
	
	public void setPrimaryKeys(ArrayList<FieldDef> primaryKeys) {
		this.primaryKeys = primaryKeys;
	}
	
	public Element toXml(){
		Element res = new Element("tableDef");
		res.setAttribute("name", name);
		for(int i = 0, size = fields.size(); i < size; i++){
			res.addContent(fields.get(i).toXml());
		}
		res.setAttribute("requestedRowNum", Integer.toString(requestedRowNum));
		for(int i = 0, size = primaryKeys.size(); i < size; i++){
			res.setAttribute("primaryKey" + (i + 1), primaryKeys.get(i).getName());
		}
		return res;
	}
}
