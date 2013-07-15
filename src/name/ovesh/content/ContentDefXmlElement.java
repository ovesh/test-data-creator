package name.ovesh.content;

import org.jdom.Element;

public class ContentDefXmlElement extends Element {
	
	public ContentDefXmlElement(Object creator) {
		super("contentDef");
		setAttribute("class", creator.getClass().getName());
	}
}
