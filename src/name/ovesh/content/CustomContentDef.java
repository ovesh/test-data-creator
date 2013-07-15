package name.ovesh.content;

import java.io.Serializable;

/**
 * This application allows to create custom content. This is done
 * by implementing a class that implements <code>CustomContentDef</code>.
 * 
 * The implemented class must fulfil the following conditions:
 * <ul>
 * <li>have a constructor with no parameters</li>
 * <li>must be serializeable (this is usually the default, and doesn't require extra effort)</li>
 * <li>must be in the default package (i.e., blank package name)</li>
 * <li>the compiled class file must be placed in the "plugins" directory in the application directory</li>
 * </ul> 
 * */
public abstract class CustomContentDef implements ContentDef, Serializable {
	protected int size = -1;
	
	public void setSize(int size){
		this.size = size;
	}
}
