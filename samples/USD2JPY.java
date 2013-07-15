import java.util.Random;
import name.ovesh.content.ContentDefXmlElement;
import name.ovesh.content.CustomContentDef;

public class USD2JPY extends CustomContentDef {

	private static final Random random = new Random();
	
	public USD2JPY(){
		random.setSeed(System.currentTimeMillis());
	}
	
	public String generateContent() {
		return Double.toString((double)random.nextInt(25) + (double)100 + random.nextDouble());
	}

	@Override
	public String toString() {
		return "USD to JPY conversion rate";
	}

	public ContentDefXmlElement toXml() {
		return new ContentDefXmlElement(this);
	}
}
