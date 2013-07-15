package name.ovesh.content;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import name.ovesh.FieldDef;
import name.ovesh.TableDef;

public class ReferenceContentDef implements ContentDef, Serializable {
	
	protected TableDef tableDef;
	protected FieldDef fieldDef;
	
	public ReferenceContentDef(TableDef tableDef, FieldDef fieldDef){
		this.tableDef = tableDef;
		this.fieldDef = fieldDef;
	}
	
	public String toString() {
		return "Reference: " + tableDef.getName() + "." + fieldDef.getName();
	}

	public TableDef getTableDef() {
		return tableDef;
	}

	public FieldDef getFieldDef() {
		return fieldDef;
	}

	// TODO this is not tested yet, does this create an infinite loop?
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeObject(tableDef);
		out.writeObject(fieldDef);
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		tableDef = (TableDef)in.readObject();
		fieldDef = (FieldDef)in.readObject();
	}

	/**
	 * due to implementation difficulties, this class is an exception, in that
	 * this method returns null. The content is generated in the application's
	 * main class.
	 * */
	public String generateContent() {
		return null;
	}

	public ContentDefXmlElement toXml() {
		ContentDefXmlElement res = new ContentDefXmlElement(this);
		res.setAttribute("table", tableDef.getName());
		res.setAttribute("field", fieldDef.getName());
		return res;
	}
}
