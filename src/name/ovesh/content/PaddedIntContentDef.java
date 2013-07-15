package name.ovesh.content;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.jdom.Element;
import name.ovesh.TestDataCreator;

public class PaddedIntContentDef extends TextContentDef {
	
	private int minVal;
	private int maxVal;
	
	public PaddedIntContentDef(int minVal, int maxVal){
		this.minVal = minVal;
		this.maxVal = maxVal;
	}
	
	public String toString() {
		return "Padded Numeric Range: " + padNumber(minVal) + " - " + padNumber(maxVal);
	}

	public int getMinVal() {
		return minVal;
	}

	public int getMaxVal() {
		return maxVal;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(minVal);
		out.writeInt(maxVal);
	}
	
	private void readObject(ObjectInputStream in) throws IOException{
		minVal = in.readInt();
		maxVal = in.readInt();
	}

	public String generateContent() {
		return padNumber(TestDataCreator.random.nextInt(maxVal - minVal) + minVal);
	}
	
	private String padNumber(int num){
		String numStr = Integer.toString(num);
		while(numStr.length() < maxSize){
			numStr = "0" + numStr;
		}
		return numStr;
	}

	public ContentDefXmlElement toXml() {
		ContentDefXmlElement res = super.toXml();
		res.setAttribute("minVal", Integer.toString(minVal));
		res.setAttribute("maxVal", Integer.toString(maxVal));
		return res;
	}

	public static ContentDef fromXml(Element contentDefEl) {
		PaddedIntContentDef res = new PaddedIntContentDef(Integer.parseInt(contentDefEl.getAttributeValue("minVal")),
			Integer.parseInt(contentDefEl.getAttributeValue("maxVal")));
		res.setMaxSize(Integer.parseInt(contentDefEl.getParentElement().getAttributeValue("size")));
		return res;
	}
}
