package name.ovesh.content;

import java.util.Hashtable;

import name.ovesh.FieldDef;
import name.ovesh.TableDef;

public class DupReferenceContentDef extends ReferenceContentDef {
	private TableDef destTableDef = null;
	private FieldDef destFieldDef = null;
	
	public static Hashtable<String, Integer> copyIndex = new Hashtable<String, Integer>();
	
	public DupReferenceContentDef(TableDef destTableDef, FieldDef destFieldDef, TableDef srcTableDef, FieldDef srcFieldDef) {
		super(srcTableDef, srcFieldDef);
		this.destTableDef = destTableDef;
		this.destFieldDef = destFieldDef;
		copyIndex.put(getDestTableDef().getName() + "." + getDestFieldDef().getName() + "." + getTableDef().getName() + "." + getFieldDef().getName(), 0);
	}

	public String toString() {
		return "Duplicate Reference: " + tableDef.getName() + "." + fieldDef.getName();
	}

	public TableDef getDestTableDef() {
		return destTableDef;
	}

	public FieldDef getDestFieldDef() {
		return destFieldDef;
	}
}
