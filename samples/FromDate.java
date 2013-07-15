import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import name.ovesh.TestDataCreator;
import name.ovesh.content.ContentDefXmlElement;
import name.ovesh.content.CustomContentDef;

public class FromDate extends CustomContentDef {
	
	public static Date lastFromDate = null;
	
	public String toString() {
		return "From Date";
	}

	public String generateContent() {
		String format = "yyyy/MM/dd";
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		StringBuffer res = new StringBuffer();
		Date minVal = null;
		Date maxVal = null;
		try {
			minVal = sdf.parse("2008/04/10");
			maxVal = sdf.parse("2010/04/10");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Date date = getRandomDate(minVal, maxVal);
		lastFromDate = date;
		res.append(sdf.format(date));
		
		return res.toString();
	}

	private Date getRandomDate(Date from, Date to){
		Date res = new Date();
		long fromTime = from.getTime();
		long toTime = to.getTime();
		res.setTime(fromTime + (TestDataCreator.random.nextLong() % (toTime - fromTime)));
		return res;
	}

	public ContentDefXmlElement toXml() {
		return new ContentDefXmlElement(this);
	}
}
