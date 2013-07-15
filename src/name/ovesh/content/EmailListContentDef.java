package name.ovesh.content;

import org.jdom.Element;
import name.ovesh.TestDataCreator;

public class EmailListContentDef extends TextContentDef {
	
	public String toString() {
		return "Email Address List";
	}

	public String generateContent() {
		EmailContentDef cd = new EmailContentDef();
		cd.setMaxSize(20);
		int len = TestDataCreator.random.nextInt(maxSize);
		StringBuffer res = new StringBuffer();
		String nextAdrs = null;
		while((nextAdrs = cd.generateContent()).length() + res.length() < len){
			res.append(nextAdrs).append(",");
		}
		// chop off the last comma
		return res.toString().substring(0, res.length() - 1);
	}

	public static ContentDef fromXml(Element contentDefEl) {
		EmailListContentDef res = new EmailListContentDef();
		res.setMaxSize(Integer.parseInt(contentDefEl.getParentElement().getAttributeValue("size")));
		return res;
	}
}
