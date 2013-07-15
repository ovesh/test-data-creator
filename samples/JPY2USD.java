import java.util.Random;
import name.ovesh.content.ContentDefXmlElement;
import name.ovesh.content.CustomContentDef;

public class JPY2USD extends CustomContentDef {

	private static final Random random = new Random();
	
	public JPY2USD(){
		random.setSeed(System.currentTimeMillis());
	}
	
	public String generateContent() {
		return Double.toString(0.008 + (random.nextDouble() * (0.01 - 0.008)));
	}

	@Override
	public String toString() {
		return "JPY to USD conversion rate";
	}

	public ContentDefXmlElement toXml() {
		return new ContentDefXmlElement(this);
	}
}
