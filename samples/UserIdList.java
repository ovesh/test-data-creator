import java.util.Random;
import name.ovesh.content.ContentDefXmlElement;
import name.ovesh.content.CustomContentDef;

public class UserIdList extends CustomContentDef {
	
	private static final Random random = new Random();
	
	public UserIdList(){
		random.setSeed(System.currentTimeMillis());
	}

	public String generateContent() {
		int len = random.nextInt(500) + 4;
		// regular user
		StringBuffer res = new StringBuffer();
		while(res.length() < len){
			for(int i = 0; i < 4; i++){
				res.append(random.nextInt(10));
			}
			if(res.length() < len - 1)
				res.append(",");
		}
		return res.toString();
	}

	@Override
	public String toString() {
		return "User Id List";
	}

	public ContentDefXmlElement toXml() {
		return new ContentDefXmlElement(this);
	}
}
