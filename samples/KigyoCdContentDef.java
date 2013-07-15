import java.util.Random;
import name.ovesh.content.ContentDefXmlElement;
import name.ovesh.content.CustomContentDef;

public class KigyoCdContentDef extends CustomContentDef {
	
	private static final Random random = new Random();
	public KigyoCdContentDef(){
		random.setSeed(System.currentTimeMillis());
	}

	public String generateContent() {
		if(random.nextInt(10) < 3){
			return "0000"; // shared code
		}
		int ptNo = random.nextInt(50) + (random.nextBoolean()? 5001: 1);
		String res = Integer.toString(ptNo);
		while(res.length() < 4)
			res = "0" + res;
		return res;
	}

	@Override
	public String toString() {
		return "Kigyo Code";
	}

	public ContentDefXmlElement toXml() {
		return new ContentDefXmlElement(this);
	}
}
