package uk.org.whitecottage.ea.ldm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.MultiplicityElement;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageMerge;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.resource.UMLResource;


public class LDMRenderer {
	protected Model root;
	protected Profile profile;
	protected List<String> untypedAttributes;
	protected List<Class> allClasses;

	public LDMRenderer(String path) {
		URI typesUri = URI.createFileURI(path);
		ResourceSet set = new ResourceSetImpl();
		set.getPackageRegistry().put(UMLPackage.eNS_URI, UMLPackage.eINSTANCE);
		set.getResourceFactoryRegistry().getExtensionToFactoryMap().put(UMLResource.FILE_EXTENSION, UMLResource.Factory.INSTANCE);
		set.createResource(typesUri);

		Resource r = set.getResource(typesUri, true);

        root = (Model) EcoreUtil.getObjectByType(r.getContents(), UMLPackage.Literals.MODEL);
		profile = root.getAppliedProfile("Profile", true);

		untypedAttributes = new ArrayList<String>();
		allClasses = new ArrayList<Class>();
		
		buildClassList(root);
	}
	
	protected void buildClassList(Package p) {
		for (Object o: EcoreUtil.getObjectsByType(p.getPackagedElements(), UMLPackage.Literals.CLASS)) {
			allClasses.add((Class) o);
		}

		EList<PackageMerge> merges = p.getPackageMerges();
		for (PackageMerge m: merges) {
			buildClassList(m.getMergedPackage());
		}

		for (Object o: EcoreUtil.getObjectsByType(p.getPackagedElements(), UMLPackage.Literals.PACKAGE)) {
			buildClassList((Package) o);
		}
	}

	protected void buildHierarchy(List<Class> hierarchy, Class cls) {
		if (!cls.getSuperClasses().isEmpty()) {
			for (Class s: cls.getSuperClasses()) {
				hierarchy.add(0, s);
				buildHierarchy(hierarchy, s);
			}
		}
	}
	
	protected Collection<Class> filterClasses(Stereotype stereotype, Collection<Object> classes) {
		Collection<Class> result = new ArrayList<Class>();
		for (Object o: classes) {
			Class c = (Class) o;
			if (c.isStereotypeApplied(stereotype)) {
				result.add(c);
			}
		}
		
		return result;
	}
	
	protected String getQualifiedElementName(NamedElement e) {
		String qPackageName = e.getNamespace().getQualifiedName();
		
		qPackageName = qPackageName.substring(qPackageName.indexOf("::") + 2);
		qPackageName = qPackageName.substring(qPackageName.indexOf("::") + 2);

		String separator = "";
		if (!qPackageName.isEmpty()) {
			separator = "::";
		}
		
		return qPackageName + separator + e.getName();
	}
	
	protected String formatCardinality(MultiplicityElement multiplicity, String one) {
		String cardinality = one;
		
		if (multiplicity.isMultivalued()) {
			if (multiplicity.lowerBound() == 0) {
				cardinality = "[*]";
			} else {
				cardinality = "[1..*]";
			}
		} else {
			if (multiplicity.lowerBound() == 0) {
				cardinality = "[0..1]";
			}
		}
		
		return cardinality;
	}
	
	protected String formatCardinality(MultiplicityElement multiplicity) {
		return formatCardinality(multiplicity, "[1]");
	}
	
	protected static String formatPackageName(String name) {
		String formattedName = name.substring(name.indexOf("::") + 2);
		if (formattedName.indexOf("::") != -1) {
			formattedName = formattedName.substring(formattedName.indexOf("::") + 2);
		}
		
		return formattedName;
	}
}