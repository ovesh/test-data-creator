package name.ovesh.content;

import org.jdom.Element;
import name.ovesh.TestDataCreator;

public class EmailContentDef extends TextContentDef {

	private static final String[] domains = new String[]{".com", ".co.jp", ".ne.jp", ".jp", "co.il"};
	private static final String emailChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_-.";
	
	public String toString() {
		return "Email Address";
	}

	public String generateContent() {
		String ending = domains[TestDataCreator.random.nextInt(domains.length)];
		int domainLen = ending.length();
		int remainingMaxLen = maxSize - (domainLen + 1);
		if(remainingMaxLen <= 0) // normally shouldn't happen
			return ending;
		StringBuffer res = new StringBuffer();
		int emailCharsLen = emailChars.length();
		int remainingLen = TestDataCreator.random.nextInt(remainingMaxLen) + 1;
		for(int i = 0; i < remainingLen - 1; i++){
			res.append(emailChars.charAt(TestDataCreator.random.nextInt(emailCharsLen)));
		}
		res.insert(TestDataCreator.random.nextInt(remainingLen), '@');
		res.append(ending);
		
		return res.toString();
	}

	public static ContentDef fromXml(Element contentDefEl) {
		EmailContentDef res = new EmailContentDef();
		res.setMaxSize(Integer.parseInt(contentDefEl.getParentElement().getAttributeValue("size")));
		return res;
	}
}
