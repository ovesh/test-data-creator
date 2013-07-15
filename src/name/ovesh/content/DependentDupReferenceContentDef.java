package name.ovesh.content;

import name.ovesh.FieldDef;
import name.ovesh.TableDef;

public class DependentDupReferenceContentDef extends DupReferenceContentDef {
	private FieldDef dependFieldDef = null;
	private FieldDef dependReferenceFieldDef = null;
	
	public DependentDupReferenceContentDef(FieldDef dependFieldDef, FieldDef dependReferenceFieldDef, TableDef destTableDef, FieldDef destFieldDef, TableDef srcTableDef, FieldDef srcFieldDef) {
		super(destTableDef, destFieldDef, srcTableDef, srcFieldDef);
		this.dependFieldDef = dependFieldDef;
		this.dependReferenceFieldDef = dependReferenceFieldDef;
	}

	public String toString() {
		return "Dependent Duplicate Reference: " + tableDef.getName() + "." + fieldDef.getName() + " by " + dependFieldDef.getName();
	}
	
	public FieldDef getDependFieldDef() {
		return dependFieldDef;
	}
	
	public FieldDef getDependReferenceFieldDef() {
		return dependReferenceFieldDef;
	}
}
