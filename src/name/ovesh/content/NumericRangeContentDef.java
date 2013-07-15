package name.ovesh.content;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import org.jdom.Element;
import name.ovesh.TestDataCreator;

public class NumericRangeContentDef implements ContentDef, Serializable {
	private double minVal;
	private double maxVal;
	private int precision = -1;
	
	public NumericRangeContentDef(double minVal, double maxVal, int precision){
		this.minVal = minVal;
		this.maxVal = maxVal;
		this.precision = precision;
	}

	public String toString() {
		return "Numeric Range: " + (int)minVal + " - " + (int)maxVal;
	}

	public double getMinVal() {
		return minVal;
	}

	public double getMaxVal() {
		return maxVal;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeDouble(minVal);
		out.writeDouble(maxVal);
		out.writeInt(precision);
	}
	
	private void readObject(ObjectInputStream in) throws IOException{
		minVal = in.readDouble();
		maxVal = in.readDouble();
		precision = in.readInt();
	}

	public String generateContent() {
		int res = TestDataCreator.random.nextInt((int)(maxVal - minVal)) + (int)minVal;
		if(precision > 0){
		    double d = TestDataCreator.random.nextDouble() + (double)res;
			BigDecimal bd = new BigDecimal(d);
			bd = bd.setScale(precision, BigDecimal.ROUND_HALF_UP);
			return Double.toString(bd.doubleValue());
		}
		return Integer.toString(res);
	}
	  
	public ContentDefXmlElement toXml() {
		ContentDefXmlElement res = new ContentDefXmlElement(this);
		res.setAttribute("minVal", Double.toString(minVal));
		res.setAttribute("maxVal", Double.toString(maxVal));
		res.setAttribute("precision", Integer.toString(precision));
		return res;
	}

	public static ContentDef fromXml(Element contentDefEl) {
		return new NumericRangeContentDef(Double.parseDouble(contentDefEl.getAttributeValue("minVal")),
			Double.parseDouble(contentDefEl.getAttributeValue("maxVal")),
			Integer.parseInt(contentDefEl.getAttributeValue("precision")));
	}
}
