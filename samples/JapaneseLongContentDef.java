import name.ovesh.TestDataCreator;
import name.ovesh.content.ContentDefXmlElement;
import name.ovesh.content.CustomContentDef;

public class JapaneseLongContentDef extends CustomContentDef {
	
	private int maxSize = 900;
	
	public String toString() {
		return "Japanese Long Content";
	}

	public String generateContent() {
		StringBuffer res = new StringBuffer();
		int len = (maxSize <= 1)? 1: TestDataCreator.random.nextInt(maxSize - 1) + 1;
		for(int i = 0; i < len; i++){
			if(i > 0 && TestDataCreator.random.nextInt(60) == 0 && i < len - 3){
				res.append("\r\n");
//				res.append("'  || CHR(13) || CHR(10) || '");
				i++;
				continue;
			}
			if(TestDataCreator.random.nextBoolean()){
				// hiragana range is \u3042 (12354) to \u3093 (12435)
				res.append((char)(TestDataCreator.random.nextInt(12435 - 12354) + 12354));
			} else{
				// katakana range is \u30A0 (12448) to \u30F4 (12532)
				res.append((char)(TestDataCreator.random.nextInt(12532 - 12448) + 12448));
			}
		}
		return res.toString();
	}

	public ContentDefXmlElement toXml() {
		return new ContentDefXmlElement(this);
	}
}
