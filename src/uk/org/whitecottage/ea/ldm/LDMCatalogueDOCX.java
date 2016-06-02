package uk.org.whitecottage.ea.ldm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.resource.UMLResource;

public class LDMCatalogueDOCX {

	private URI typesUri = null;
	
	public static void main(String[] args) {
		if (args.length == 1) {
			File input = new File(args[0]);
			File template = new File("templates/catalogue.docx");
			try {
				//XWPFDocument docx = new XWPFDocument();
				XWPFDocument docx = new XWPFDocument(new FileInputStream(template));
				Model root = new LDMCatalogueDOCX().getModel(input.getAbsolutePath());
				if (root != null) {
					LDMRenderer cldm = new LDMRenderer(root);
					cldm.renderCatalogue(docx);
					docx.write(new FileOutputStream(input.getName() + ".docx"));
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public Model getModel(String pathToModel) {
		
		typesUri = URI.createFileURI(pathToModel);
		ResourceSet set = new ResourceSetImpl();
		set.getPackageRegistry().put(UMLPackage.eNS_URI, UMLPackage.eINSTANCE);
		set.getResourceFactoryRegistry().getExtensionToFactoryMap().put(UMLResource.FILE_EXTENSION, UMLResource.Factory.INSTANCE);
		set.createResource(typesUri);
		System.out.println("Fetching the resource: " + typesUri.path());
		Resource r = set.getResource(typesUri, true);

        return (Model) EcoreUtil.getObjectByType(r.getContents(), UMLPackage.Literals.MODEL);
	}	
}
