package name.ovesh.content;

import org.jdom.Element;
import name.ovesh.TestDataCreator;

public class EnglishStringContentDef extends TextContentDef {
	
	private static final String alphaNumericChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_-=., !";
	
	public String toString() {
		return "English String";
	}

	public String generateContent() {
		StringBuffer res = new StringBuffer();
		int len = (maxSize <= 1)? 1: TestDataCreator.random.nextInt(maxSize - 1) + 1;
		for(int i = 0; i < len; i++){
			res.append(alphaNumericChars.charAt(TestDataCreator.random.nextInt(alphaNumericChars.length())));
		}
		return res.toString();
	}

	public static ContentDef fromXml(Element contentDefEl) {
		EnglishStringContentDef res = new EnglishStringContentDef();
		res.setMaxSize(Integer.parseInt(contentDefEl.getParentElement().getAttributeValue("size")));
		return res;
	}
}
