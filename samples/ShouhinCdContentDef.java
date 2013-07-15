import java.util.Random;
import name.ovesh.content.ContentDefXmlElement;
import name.ovesh.content.CustomContentDef;

/**
 * HQ user has 9 alphanumeric characters, regular user has 4 digits only
 * */ 
public class ShouhinCdContentDef extends CustomContentDef {
	
	private static final Random random = new Random();
	public ShouhinCdContentDef(){
		random.setSeed(System.currentTimeMillis());
	}

	public String generateContent() {
		int shouhinCd = random.nextInt(99) + 1;
		return "X0000000000" + (shouhinCd > 9? "": "0") + shouhinCd;
	}

	@Override
	public String toString() {
		return "Shouhin Code";
	}

	public ContentDefXmlElement toXml() {
		return new ContentDefXmlElement(this);
	}
}
