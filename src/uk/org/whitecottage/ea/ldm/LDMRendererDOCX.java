package uk.org.whitecottage.ea.ldm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.IRunBody;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Comment;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageMerge;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.UMLPackage;


public class LDMRendererDOCX extends LDMRenderer {
	protected Model root;
	protected Profile profile;
	protected List<String> untypedAttributes;
	protected List<Class> allClasses;

	public LDMRendererDOCX(String path) {
		super(path);
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
	
	public void renderCatalogue(XWPFDocument docx) {
		XWPFRun r = createParagraph(docx, "Heading1", "");
		r.addBreak(BreakType.PAGE);
		addRun(r.getParent(), "Entities");
		renderPackageEntities(docx, root, true);
	
		createParagraph(docx, "Heading1", "Reference Data");
		renderPackageReferenceData(docx, root, true);
	
		createParagraph(docx, "Heading1", "Structures");
		renderPackageDataTypes(docx, root, true);
	
		renderModelValidation(docx);
	}

		
	protected void renderPackageEntities(XWPFDocument docx, Package pkg, boolean topLevel) {
		Collection<Class> classes = EcoreUtil.getObjectsByType(pkg.getPackagedElements(), UMLPackage.Literals.CLASS);
		Collection<Class> entities = new ArrayList<Class>();

		for (Class c: classes) {
			if (!c.isStereotypeApplied(profile.getOwnedStereotype("ReferenceEntity"))) {
				entities.add(c);
			}
		}
		
		if (!topLevel) {
			if (entities.size() > 0) {
				createParagraph(docx, "Heading2", formatPackageName(pkg.getQualifiedName()));
				renderComments(docx, pkg.getOwnedComments());
			}
		}
		
		for (Class c: entities) {
			renderClass(docx, c);
		}
		
		for (Object o: EcoreUtil.getObjectsByType(pkg.getPackagedElements(), UMLPackage.Literals.PACKAGE)) {
			renderPackageEntities(docx, (Package) o, false);
		}
	}
	
	protected void renderPackageReferenceData(XWPFDocument docx, Package pkg, boolean topLevel) {
		Collection<Class> classes = EcoreUtil.getObjectsByType(pkg.getPackagedElements(), UMLPackage.Literals.CLASS);
		Collection<Class> entities = new ArrayList<Class>();

		for (Class c: classes) {
			if (c.isStereotypeApplied(profile.getOwnedStereotype("ReferenceEntity"))) {
				entities.add(c);
			}
		}
		
		if (!topLevel) {
			if (entities.size() > 0) {
				createParagraph(docx, "Heading2", formatPackageName(pkg.getQualifiedName()));
				renderComments(docx, pkg.getOwnedComments());
			}
		}
		
		for (Object o: entities) {
			renderClass(docx, (Class) o);
		}
		
		for (Object o: EcoreUtil.getObjectsByType(pkg.getPackagedElements(), UMLPackage.Literals.PACKAGE)) {
			renderPackageReferenceData(docx, (Package) o, false);
		}
	}
	
	protected void renderPackageDataTypes(XWPFDocument docx, Package pkg, boolean topLevel) {
		Collection<DataType> datatypes = EcoreUtil.getObjectsByType(pkg.getPackagedElements(), UMLPackage.Literals.DATA_TYPE);
		if (!topLevel) {
			if (datatypes.size() > 0) {
				createParagraph(docx, "Heading2", formatPackageName(pkg.getQualifiedName()));
				renderComments(docx, pkg.getOwnedComments());
			}
		}
		
		for (DataType d: datatypes) {
			renderDataType(docx, d);
		}
		
		for (Object o: EcoreUtil.getObjectsByType(pkg.getPackagedElements(), UMLPackage.Literals.PACKAGE)) {
			renderPackageDataTypes(docx, (Package) o, false);
		}
	}
	
	protected void renderPackage(XWPFDocument docx, Package pkg, Stereotype stereotype, boolean topLevel) {
		Collection<Class> classes = filterClasses(stereotype, EcoreUtil.getObjectsByType(pkg.getPackagedElements(), UMLPackage.Literals.CLASS));
		if (topLevel) {
			//createParagraph(docx, "Heading1", formatPackageName(pkg.getQualifiedName()));
		} else {
			if (classes.size() > 0) {
				createParagraph(docx, "Heading2", formatPackageName(pkg.getQualifiedName()));
				renderComments(docx, pkg.getOwnedComments());
			}
		}
		for (Object o: classes) {
			renderClass(docx, (Class) o);
		}
		for (Object o: EcoreUtil.getObjectsByType(pkg.getPackagedElements(), UMLPackage.Literals.PACKAGE)) {
			renderPackage(docx, (Package) o, stereotype, false);
		}
	}
	
	protected void renderClass(XWPFDocument docx, Class cls) {
		XWPFRun r = createParagraph(docx, "Heading3", cls.getName());
		
		if (cls.eClass().equals(UMLPackage.Literals.ASSOCIATION_CLASS)) {
			addRun(r.getParent(), " (Association Entity)");
		}

		renderDescription(docx, cls);
		
		renderSuperclasses(docx, cls);
		
		renderSubclasses(docx, cls);

		renderProperties(docx, cls.getAllAttributes());

		renderAssociations(docx, cls.getAssociations(), cls);
	}
	
	protected void renderDataType(XWPFDocument docx, DataType datatype) {
		XWPFRun r = createParagraph(docx, "Heading3", datatype.getName());
		
		if (datatype.eClass().equals(UMLPackage.Literals.ASSOCIATION_CLASS)) {
			addRun(r.getParent(), " (Association Entity)");
		}

		renderDescription(docx, datatype);
		
		//renderSuperclasses(docx, datatype);
		
		//renderSubclasses(docx, datatype);

		renderProperties(docx, datatype.getAllAttributes());

		//renderAssociations(docx, datatype.getAssociations(), datatype);
	}
	
	protected void renderComments(XWPFDocument docx, EList<Comment> comments) {
		for (Comment comment: comments) {
			createParagraph(docx, "Normal", comment.getBody());
		}
	}
	
	protected void renderDescription(XWPFDocument docx, Classifier classifier) {
		// createParagraph(docx, "Normal", "DESCRIPTION").setBold(true);
		renderComments(docx, classifier.getOwnedComments());
	}
	
	protected void renderSubclasses(XWPFDocument docx, Class cls) {
		List<Class> subclasses = findSubclasses(cls);
		
		XWPFRun r = null;
		if (!subclasses.isEmpty()) {
			createParagraph(docx, "Normal", "SUBCLASSES").setBold(true);
			
			for (Class c: subclasses) {
				r = createParagraph(docx, "Normal", getQualifiedElementName(c));
				((XWPFParagraph) r.getParent()).setSpacingAfter(0);
			}
		}
		
		if (r != null) {
			((XWPFParagraph) r.getParent()).setSpacingAfter(200); // Should be able to read default value (this is 10pt)
		}
	}
	
	protected void renderSuperclasses(XWPFDocument docx, Class cls) {
		List<Class> hierarchy = new ArrayList<Class>();
		buildHierarchy(hierarchy, cls);
		
		XWPFRun r = null;
		if (!hierarchy.isEmpty()) {
			createParagraph(docx, "Normal", "SUPERCLASSES").setBold(true);
			
			for (Class h: hierarchy) {
				r = createParagraph(docx, "Normal", getQualifiedElementName(h));
				((XWPFParagraph) r.getParent()).setSpacingAfter(0);
			}
		}
		
		if (r != null) {
			((XWPFParagraph) r.getParent()).setSpacingAfter(200); // Should be able to read default value (this is 10pt)
		}
	}
	
	protected void renderProperties(XWPFDocument docx, EList<Property> properties) {
		if (!properties.isEmpty()) {
			createParagraph(docx, "Normal", "ATTRIBUTES").setBold(true);
		}
		for (Property property: properties) {
			if (property.getAssociation() == null) {
				String type;
				if (property.getType() == null) {
					type = "!!!UNTYPED!!!";
					untypedAttributes.add(property.getClass_().getName() + ": " + property.getName());
				} else {
					type = property.getType().getName();
				}
				String cardinality = formatCardinality(property);
				XWPFRun r = createParagraph(docx, "Normal", type + cardinality + " ");
				r.setFontFamily("Courier New");
				r = addRun(r.getParent(), property.getName());
				r.setBold(true);
				
				renderComments(docx, property.getOwnedComments());
			}
		}
	}

	protected void renderAssociations(XWPFDocument docx, EList<Association> associations, Class cls) {
		if (!associations.isEmpty()) {
			createParagraph(docx, "Normal", "ASSOCIATIONS").setBold(true);
		}
		for (Association association: associations) {
			EList<Property> ends = association.getMemberEnds();
			Property from;
			Property to;
			if (ends.get(0).getType().equals(cls)) {
				from = ends.get(0);
				to = ends.get(1);
			} else {
				from = ends.get(1);
				to = ends.get(0);
			}
			XWPFRun r = createParagraph(docx, "Normal", from.getName() + " ");
			r.setItalic(true);
			r = addRun(r.getParent(), to.getType().getName() + " ");
			r.setBold(true);
			String cardinality = formatCardinality(from) + " to " + formatCardinality(to);
			r = addRun(r.getParent(), cardinality);
		}
	}

	protected List<Class> findSubclasses(Class superclass) {
		List<Class> subclasses = new ArrayList<Class>();
		
		for (Class c: allClasses) {
			EList<Class> superclasses = c.getSuperClasses();
			if (!superclasses.isEmpty()) {
				for (Class sc: superclasses) {
					if (sc.equals(superclass)) {
						subclasses.add(c);
					}
				}
			}
		}
		
		return subclasses;
	}
	
	protected void renderModelValidation(XWPFDocument docx) {
		XWPFRun r = createParagraph(docx, "Heading1", "");
		r.addBreak(BreakType.PAGE);
		addRun(r.getParent(), "Model validation report");
		String successStatus = "No model problems found";
		
		if (untypedAttributes.size() > 0) {
			successStatus = "";
			createParagraph(docx, "Heading2", "Untyped attributes");
			for (String s: untypedAttributes) {
				createParagraph(docx, "Normal", s);
			}
		}
		
		createParagraph(docx, "Normal", successStatus).setBold(true);
	}
	
	protected XWPFRun addRun(IRunBody para, String text) {
		XWPFRun r = ((XWPFParagraph) para).createRun();
		r.setText(text);

		return r;
	}
	
	protected XWPFRun createParagraph(XWPFDocument docx, String style, String text) {
		XWPFParagraph para = docx.createParagraph();
		XWPFRun r = addRun(para, text);
		para.setStyle(style);

		return r;
	}
}