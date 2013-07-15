package name.ovesh.content;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.jdom.Element;

public class OracleSequenceContentDef implements ContentDef, Serializable {
	
	private String seqName;
	
	public OracleSequenceContentDef(String seqName){
		this.seqName = seqName;
	}
	
	public String toString() {
		return "Oracle sequence: " + seqName + ".nextval";
	}

	public String getSeqName() {
		return seqName;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeObject(seqName);
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		seqName = (String)in.readObject();
	}

	public String generateContent() {
		return seqName + ".nextval";
	}

	public ContentDefXmlElement toXml() {
		ContentDefXmlElement res = new ContentDefXmlElement(this);
		res.setAttribute("seqName", seqName);
		return res;
	}

	public static ContentDef fromXml(Element contentDefEl) {
		return new OracleSequenceContentDef(contentDefEl.getAttributeValue("seqName"));
	}
}
