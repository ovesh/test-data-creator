import java.util.Random;
import name.ovesh.content.ContentDefXmlElement;
import name.ovesh.content.CustomContentDef;

/**
 * HQ user has 9 alphanumeric characters, regular user has 4 digits only
 * */ 
public class UserIdContentDef extends CustomContentDef {
	
	private static final Random random = new Random();
	private static final String alphanumericChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	
	public UserIdContentDef(){
		random.setSeed(System.currentTimeMillis());
	}

	public String generateContent() {
		String res = "";
		for(int i = 0; i < size; i++){
			res += alphanumericChars.charAt(random.nextInt(alphanumericChars.length()));
		}
		return res;
	}

	@Override
	public String toString() {
		return "User Id";
	}

	public ContentDefXmlElement toXml() {
		return new ContentDefXmlElement(this);
	}
}
