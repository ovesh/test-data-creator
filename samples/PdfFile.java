import java.io.File;
import java.io.FileOutputStream;
import org.apache.log4j.Logger;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import name.ovesh.content.ContentDefXmlElement;
import name.ovesh.content.CustomContentDef;
import name.ovesh.content.EnglishStringContentDef;

public class PdfFile extends CustomContentDef {
	
	private static boolean isFirst = true;
	
	public String toString() {
		return "PDF File";
	}

	public String generateContent() {
//		File dir = new File("files");
		File dir = new File("C:\\tmp\\test_data\\");
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
			File f = File.createTempFile("att.", ".pdf", dir);
			FileOutputStream fos = new FileOutputStream(f);
			EnglishStringContentDef cd = new EnglishStringContentDef();
			cd.setMaxSize(200);
			
			Document document = new Document();
			PdfWriter.getInstance(document, fos);
			document.open();
			document.add(new Paragraph(cd.generateContent()));
			document.close();
			
			return f.getName();
		} catch (Exception e) {
			Logger l = Logger.getLogger(PdfFile.class);
			l.error("caught exception", e);
			System.out.println("caught exception: " + e.getMessage());
		}
		
		return "";
	}

	public ContentDefXmlElement toXml() {
		return new ContentDefXmlElement(this);
	}
}
