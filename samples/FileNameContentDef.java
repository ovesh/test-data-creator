import name.ovesh.TestDataCreator;
import name.ovesh.content.ContentDefXmlElement;
import name.ovesh.content.CustomContentDef;

public class FileNameContentDef extends CustomContentDef {
	
	private static final String alphaNumericChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_-";
	private int maxSize = 30;
	
	public String toString() {
		return "File Name";
	}

	public String generateContent() {
		StringBuffer res = new StringBuffer();
		int len = (maxSize <= 1)? 1: TestDataCreator.random.nextInt(maxSize - 4) + 4;
		for(int i = 0; i < len - 4; i++){
			if(TestDataCreator.random.nextInt(4) == 0){
				// hiragana range is \u3042 (12354) to \u3093 (12435)
				res.append((char)(TestDataCreator.random.nextInt(12435 - 12354) + 12354));
			}
			else{
				res.append(alphaNumericChars.charAt(TestDataCreator.random.nextInt(alphaNumericChars.length())));
			}
		}
		res.append(".");
		for(int i = 0; i < 3; i++){
			res.append(alphaNumericChars.charAt(TestDataCreator.random.nextInt(alphaNumericChars.length())));
		}
		
		return res.toString();
	}

	public ContentDefXmlElement toXml() {
		return new ContentDefXmlElement(this);
	}
}
