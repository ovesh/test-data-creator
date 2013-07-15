package name.ovesh.content;

import org.jdom.Element;
import name.ovesh.TestDataCreator;

public class JapaneseStringContentDef extends TextContentDef {
	
	public String toString() {
		return "Japanese String";
	}

	public String generateContent() {
		StringBuffer res = new StringBuffer();
		int len = (maxSize <= 1)? 1: TestDataCreator.random.nextInt(maxSize - 1) + 1;
		for(int i = 0; i < len; i++){
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

	public static ContentDef fromXml(Element contentDefEl) {
		JapaneseStringContentDef res = new JapaneseStringContentDef();
		res.setMaxSize(Integer.parseInt(contentDefEl.getParentElement().getAttributeValue("size")));
		return res;
	}
}
