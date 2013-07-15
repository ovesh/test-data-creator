package name.ovesh.content;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.jdom.Element;

/**
 * This kind of data type is only supported for CTL files, not for sql insert statements files
 * */
public class Blob implements ContentDef, Serializable {
	
	private String fileNameField;
	
	public void setFileNameField(String name){
		this.fileNameField = name;
	}

	public String getFileNameField(){
		return fileNameField;
	}

	public String toString() {
		return "BLOB (named by " + fileNameField + ")";
	}

	/**
	 * this method returns a blank string. The content is generated in the application's
	 * main class.
	 * */
	public String generateContent() {
		return "";
	}

	public static ContentDef fromXml(Element contentDefEl) {
		Blob res = new Blob();
		res.setFileNameField(contentDefEl.getAttributeValue("fileNameField"));
		return res;
	}

	public ContentDefXmlElement toXml() {
		ContentDefXmlElement res = new ContentDefXmlElement(this);
		res.setAttribute("fileNameField", fileNameField);
		return res;
	}

	private void writeObject(@SuppressWarnings("unused") ObjectOutputStream out) {}
	
	private void readObject(@SuppressWarnings("unused") ObjectInputStream in) {}
}
