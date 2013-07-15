import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.log4j.Logger;
import name.ovesh.content.ContentDefXmlElement;
import name.ovesh.content.CustomContentDef;

public class TempFileNameContentDef extends CustomContentDef {
	
	private static boolean isFirst = true;
	
	public String toString() {
		return "Repository File Name";
	}

	public String generateContent() {
//		File dir = new File("files");
		File dir = new File("\\\\oliver\\shared\\files");
		if(!dir.exists())
			dir.mkdir();
		else{
			if(isFirst){
				File[] files = dir.listFiles();
				for(int i = 0; i < files.length; i++){
					files[i].delete();
				}
				dir.delete();
				dir.mkdir();
				isFirst = false;
			}
		}
		try {
			File f = File.createTempFile("att.", ".tmp", dir);
			FileWriter fw = new FileWriter(f);
			fw.write("text " + f.getName() + " the end");
			fw.close();
			return f.getName();
		} catch (IOException e) {
			Logger l = Logger.getLogger(TempFileNameContentDef.class);
			l.error("caught exception", e);
			System.out.println("caught exception: " + e.getMessage());
		}
		
		return "";
	}

	public ContentDefXmlElement toXml() {
		return new ContentDefXmlElement(this);
	}
}
