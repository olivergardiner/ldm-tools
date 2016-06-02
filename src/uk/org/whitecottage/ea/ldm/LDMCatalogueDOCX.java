package uk.org.whitecottage.ea.ldm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.xwpf.usermodel.XWPFDocument;

public class LDMCatalogueDOCX {

	public static void main(String[] args) {
		if (args.length == 1) {
			File input = new File(args[0]);
			File template = new File("templates/catalogue.docx");
			try {
				//XWPFDocument docx = new XWPFDocument();
				XWPFDocument docx = new XWPFDocument(new FileInputStream(template));
				LDMRendererDOCX ldm = new LDMRendererDOCX(input.getAbsolutePath());
				ldm.renderCatalogue(docx);
				docx.write(new FileOutputStream(input.getName() + ".docx"));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
