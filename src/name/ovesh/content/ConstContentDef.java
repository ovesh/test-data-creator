package name.ovesh.content;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Vector;
import org.jdom.Element;
import name.ovesh.TestDataCreator;

public class ConstContentDef implements ContentDef, Serializable {
	private Vector<String> allowedVals = new Vector<String>();
	
	public Vector<String> getAllowedVals() {
		return allowedVals;
	}

	public ConstContentDef(Vector<String> allowedVals){
		this.allowedVals = allowedVals;
	}

	public String toString() {
		return "Constant values: " + allowedVals.toString();
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeObject(allowedVals);
	}
	
	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		allowedVals = (Vector<String>)in.readObject();
	}

	public String generateContent() {
		return allowedVals.elementAt(TestDataCreator.random.nextInt(allowedVals.size()));
	}

	public ContentDefXmlElement toXml() {
		ContentDefXmlElement res = new ContentDefXmlElement(this);
		StringBuffer allowedValsStr = new StringBuffer();
		for(int i = 0, size = allowedVals.size() - 1; i < size; i++){
			allowedValsStr.append(allowedVals.get(i)).append(",");
		}
		allowedValsStr.append(allowedVals.get(allowedVals.size() - 1));
		res.setAttribute("allowedVals", allowedValsStr.toString());
		return res;
	}

	public static ContentDef fromXml(Element contentDefEl){
		String allowedValsStr = contentDefEl.getAttributeValue("allowedVals");
		return new ConstContentDef
			(new Vector<String>(Arrays.asList(allowedValsStr.split(","))));
	}
}
