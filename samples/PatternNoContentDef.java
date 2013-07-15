import java.util.Random;
import name.ovesh.content.ContentDefXmlElement;
import name.ovesh.content.CustomContentDef;

public class PatternNoContentDef extends CustomContentDef {
	
	private static final Random random = new Random();
	public PatternNoContentDef(){
		random.setSeed(System.currentTimeMillis());
	}

	public String generateContent() {
		int ptNo = random.nextInt(500) + 1;
		String res = Integer.toString(ptNo);
		while(res.length() < 10)
			res = "0" + res;
		return res;
	}

	@Override
	public String toString() {
		return "Pattern Number";
	}

	public ContentDefXmlElement toXml() {
		return new ContentDefXmlElement(this);
	}
}
