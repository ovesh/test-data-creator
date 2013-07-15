package name.ovesh.content;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public abstract class TextContentDef implements ContentDef, Serializable {
	
	protected int maxSize = -1;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(maxSize);
	}
	
	private void readObject(ObjectInputStream in) throws IOException{
		maxSize = in.readInt();
	}
	
	public void setMaxSize(int maxSize){
		this.maxSize = maxSize;
	}

	public ContentDefXmlElement toXml() {
		ContentDefXmlElement res = new ContentDefXmlElement(this);
		res.setAttribute("maxSize", Integer.toString(maxSize));
		return res;
	}
}
