import java.util.Random;
import name.ovesh.content.ContentDefXmlElement;
import name.ovesh.content.CustomContentDef;

/**
 * HQ user has 9 alphanumeric characters, regular user has 4 digits only
 * */ 
public class TelephoneNumber extends CustomContentDef {

	private static final Random random = new Random();
	
	public TelephoneNumber(){
		random.setSeed(System.currentTimeMillis());
	}
	
	public String generateContent() {
		String res = "0";
		for(int i = 0; i < 2; i++){
			res += random.nextInt(10);
		}
		res += "-";
		for(int i = 0; i < 7; i++){
			res += random.nextInt(10);
		}
		
		return res;
	}

	@Override
	public String toString() {
		return "Telephone Number";
	}

	public ContentDefXmlElement toXml() {
		return new ContentDefXmlElement(this);
	}
}
