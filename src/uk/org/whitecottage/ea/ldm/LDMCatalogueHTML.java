package uk.org.whitecottage.ea.ldm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.resource.UMLResource;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;

public class LDMCatalogueHTML {

	private URI modelUri = null;
	
	public static void main(String[] args) {
		if (args.length == 1) {
			File input = new File(args[0]);
			File templateDir = new File("templates/ftl");
			File outputDir = new File("html");

			Configuration cfg = new Configuration();

			try {
				// Specify the data source where the template files come from
				cfg.setDirectoryForTemplateLoading(templateDir);
	
				// Specify how templates will see the data-model
				cfg.setObjectWrapper(new DefaultObjectWrapper());
	
				// Set your preferred charset template files are stored in
				cfg.setDefaultEncoding("UTF-8");
	
				// Sets how errors will appear. Here we assume we are developing HTML pages.
				// For production systems TemplateExceptionHandler.RETHROW_HANDLER is better.
				cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
	
				// At least in new projects, specify that you want the fixes that aren't
				// 100% backward compatible too (these are very low-risk changes as far as the
				// 1st and 2nd version number remains):
				cfg.setIncompatibleImprovements(new Version(2, 3, 20));

				Model root = new LDMCatalogueHTML().getModel(input.getAbsolutePath());
				if (root != null) {
					LDMRenderer cldm = new LDMRenderer(root);
					cldm.renderCatalogue(cfg, outputDir);
				} else {
					System.out.println("Failed to load model");
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
		
		modelUri = URI.createFileURI(pathToModel);
		ResourceSet set = new ResourceSetImpl();
		set.getPackageRegistry().put(UMLPackage.eNS_URI, UMLPackage.eINSTANCE);		
		set.getResourceFactoryRegistry().getExtensionToFactoryMap().put(UMLResource.FILE_EXTENSION, UMLResource.Factory.INSTANCE);

		set.createResource(modelUri);
		System.out.println("Fetching the resource: " + modelUri.path());
		Resource r = set.getResource(modelUri, true);

		
		
        return (Model) EcoreUtil.getObjectByType(r.getContents(), UMLPackage.Literals.MODEL);
	}	
}
