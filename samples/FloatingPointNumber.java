import java.util.Random;
import name.ovesh.content.ContentDefXmlElement;
import name.ovesh.content.CustomContentDef;

/**
 * HQ user has 9 alphanumeric characters, regular user has 4 digits only
 * */ 
public class FloatingPointNumber extends CustomContentDef {

	private static final Random random = new Random();
	
	public FloatingPointNumber(){
		random.setSeed(System.currentTimeMillis());
	}
	
	public String generateContent() {
		return Float.toString((float)random.nextInt(3000) + random.nextFloat());
	}

	@Override
	public String toString() {
		return "Floating Point Number";
	}

	public ContentDefXmlElement toXml() {
		return new ContentDefXmlElement(this);
	}
}
