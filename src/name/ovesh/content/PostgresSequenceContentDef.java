package name.ovesh.content;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.jdom.Element;

public class PostgresSequenceContentDef implements ContentDef, Serializable {
	
	private String seqName;
	
	public PostgresSequenceContentDef(String seqName){
		this.seqName = seqName;
	}
	
	public String toString() {
		return "Postgres sequence: nextval(" + seqName + ")";
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
		return "nextval('" + seqName + "')";
	}

	public ContentDefXmlElement toXml() {
		ContentDefXmlElement res = new ContentDefXmlElement(this);
		res.setAttribute("seqName", seqName);
		return res;
	}

	public static ContentDef fromXml(Element contentDefEl) {
		return new PostgresSequenceContentDef(contentDefEl.getAttributeValue("seqName"));
	}
}
