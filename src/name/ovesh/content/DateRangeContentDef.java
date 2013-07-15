package name.ovesh.content;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.jdom.Element;
import name.ovesh.TestDataCreator;
import name.ovesh.TestDataCreator.ExportFormat;

public class DateRangeContentDef implements ContentDef, Serializable {
	private Date minVal;
	private Date maxVal;
	private String format;
	private int type;
	
	public DateRangeContentDef(Date minVal, Date maxVal, String format, int type){
		if(maxVal.getTime() < minVal.getTime())
			throw new IllegalArgumentException("max value must be larger than the min value");
		this.minVal = minVal;
		this.maxVal = maxVal;
		if(format == null || format.trim().length() == 0)
			format = "yyyy/MM/dd";
		this.format = format;
		this.type = type;
	}

	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		StringBuffer res = new StringBuffer();
		if(minVal != null) res.append(sdf.format(minVal)).append(" ");
		res.append("-");
		if(maxVal != null) res.append(" ").append(sdf.format(maxVal));
		return "Date Range: " + res.toString();
	}

	public Date getMinVal() {
		return minVal;
	}

	public Date getMaxVal() {
		return maxVal;
	}

	public String getFormat() {
		return format;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeObject(minVal);
		out.writeObject(maxVal);
		out.writeObject(format);
		out.writeInt(type);
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		minVal = (Date)in.readObject();
		maxVal = (Date)in.readObject();
		format = (String)in.readObject();
		type = in.readInt();
	}

	public String generateContent() {
		if(type == Types.DATE || type == Types.TIME || type == Types.TIMESTAMP){
			Date date = getRandomDate(minVal, maxVal);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
			if(ExportFormat.SQL.equals(TestDataCreator.getExportFormat())){
				StringBuffer res = new StringBuffer();
				res.append("to_date('").append(sdf.format(date)).append("','YYYY/MM/DD')");
				return res.toString();
			}
			// ExportFormat.CTL.equals(TestDataCreator.getExportFormat())
			return sdf.format(date);
		}
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(getRandomDate(minVal, maxVal));
	}

	private Date getRandomDate(Date from, Date to){
		Date res = new Date();
		long fromTime = from.getTime();
		long toTime = to.getTime();
		res.setTime(fromTime + (TestDataCreator.random.nextLong() % (toTime - fromTime)));
		return res;
	}

	public ContentDefXmlElement toXml() {
		ContentDefXmlElement res = new ContentDefXmlElement(this);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
		res.setAttribute("minVal", sdf.format(minVal));
		res.setAttribute("maxVal", sdf.format(maxVal));
		res.setAttribute("format", format);
		res.setAttribute("type", Integer.toString(type));
		return res;
	}

	public static ContentDef fromXml(Element contentDefEl) throws NumberFormatException, ParseException{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
		return new DateRangeContentDef(sdf.parse(contentDefEl.getAttributeValue("minVal")),
			sdf.parse(contentDefEl.getAttributeValue("maxVal")),
			contentDefEl.getAttributeValue("format"),
			Integer.parseInt(contentDefEl.getAttributeValue("type")));
	}
}
