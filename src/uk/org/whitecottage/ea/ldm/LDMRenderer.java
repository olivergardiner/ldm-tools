package uk.org.whitecottage.ea.ldm;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.IRunBody;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Comment;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.MultiplicityElement;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageMerge;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLPackage;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;


public class LDMRenderer extends LDMProcessor {
	protected List<String> untypedAttributes;
	protected List<Class> classes;

	public LDMRenderer(Model root) {
		super(root);
		untypedAttributes = new ArrayList<String>();
		classes = new ArrayList<Class>();
		
		buildClassList(getLDM());
	}
	
	public void buildClassList(Package p) {
		for (Object o: EcoreUtil.getObjectsByType(p.getPackagedElements(), UMLPackage.Literals.CLASS)) {
			classes.add((Class) o);
		}

		EList<PackageMerge> merges = p.getPackageMerges();
		for (PackageMerge m: merges) {
			buildClassList(m.getMergedPackage());
		}

		for (Object o: EcoreUtil.getObjectsByType(p.getPackagedElements(), UMLPackage.Literals.PACKAGE)) {
			buildClassList((Package) o);
		}
	}
	
	public void renderCatalogue(Configuration cfg, File outputDir) {
		Package ldm = getLDM();
		String basePath = outputDir.getPath();
		
		Map<String, Object> packageListData = buildPackageListData(ldm);

		Map<String, Object> allentityData = buildAllentityData(ldm);
		
		List<Map<String, Object>> packageData = buildPackageData(ldm);
		
		List<Map<String, Object>> entityData = buildEntityData(ldm, profile.getOwnedStereotype("Entity"));
		
		List<Map<String, Object>> referenceEntityData = buildEntityData(ldm, profile.getOwnedStereotype("ReferenceEntity"));
		
		List<Map<String, Object>> complexEntityData = buildEntityData(ldm, profile.getOwnedStereotype("ComplexEntity"));
		
		System.out.println("Writing files");
		
		try {
			Template packageListTmpl = cfg.getTemplate("overview-frame.ftl");
			packageListTmpl.process(packageListData, new FileWriter(new File(basePath + "/overview-frame.html")));
			
			Template packageTmpl = cfg.getTemplate("package-frame.ftl");

			packageTmpl.process(allentityData, new FileWriter(new File(basePath + "/allentities-frame.html")));
			
			for (Map<String, Object> pkg: packageData) {
				String packagePath = (String) pkg.get("packagePath");
				packageTmpl.process(pkg, new FileWriter(new File(basePath + packagePath + "/package-frame.html")));
			}

			Template entityTmpl = cfg.getTemplate("entity.ftl");

			for (Map<String, Object> entity: entityData) {
				String packagePath = (String) entity.get("packagePath");
				String entityName = (String) entity.get("entityName");
				entityTmpl.process(entity, new FileWriter(new File(basePath + packagePath + "/" + entityName + ".html")));
			}

			for (Map<String, Object> entity: referenceEntityData) {
				String packagePath = (String) entity.get("packagePath");
				String entityName = (String) entity.get("entityName");
				entityTmpl.process(entity, new FileWriter(new File(basePath + packagePath + "/" + entityName + ".html")));
			}

			for (Map<String, Object> entity: complexEntityData) {
				String packagePath = (String) entity.get("packagePath");
				String entityName = (String) entity.get("entityName");
				entityTmpl.process(entity, new FileWriter(new File(basePath + packagePath + "/" + entityName + ".html")));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TemplateException e) {
			e.printStackTrace();
		}
	}
	
	protected Map<String, Object> buildAllentityData(Package pkg) {
		Map<String, Object> packageListData = new HashMap<String, Object>();
		
		packageListData.put("rootPath", ".");
		
		packageListData.put("entities", buildAllentityList(pkg, profile.getOwnedStereotype("Entity")));
		
		packageListData.put("referenceEntities", buildAllentityList(pkg, profile.getOwnedStereotype("ReferenceEntity")));
		
		packageListData.put("complexEntities", buildAllentityList(pkg, profile.getOwnedStereotype("ComplexEntity")));
		
		return packageListData;
	}
	
	protected List<Map<String, Object>> buildPackageData(Package pkg) {
		List<Map<String, Object>> packageData = new ArrayList<Map<String, Object>>();
		Map<String, Object> packageListData = new HashMap<String, Object>();
		
		String qualifiedPackageName = formatPackageName(pkg.getQualifiedName());
		String packagePath = "/" + qualifiedPackageName.replace("::", "/");

		int depth = packagePath.split("/").length;
		String rootPath="";
		while (--depth > 0) {
			rootPath += "../";
		}
		
		packageListData.put("packagePath", packagePath);
		packageListData.put("rootPath", rootPath);
		
		packageListData.put("entities", buildEntityList(pkg, profile.getOwnedStereotype("Entity")));
		
		packageListData.put("referenceEntities", buildEntityList(pkg, profile.getOwnedStereotype("ReferenceEntity")));
		
		packageListData.put("complexEntities", buildEntityList(pkg, profile.getOwnedStereotype("ComplexEntity")));
		
		for (Object o: EcoreUtil.getObjectsByType(pkg.getPackagedElements(), UMLPackage.Literals.PACKAGE)) {
			packageData.addAll(buildPackageData((Package) o));
		}
		
		packageData.add(packageListData);
				
		return packageData;
	}
	
	protected List<Map<String, Object>> buildAllentityList(Package pkg, Stereotype s) {
		List<Map<String, Object>> entityList = new ArrayList<Map<String, Object>>();

		entityList.addAll(buildEntityList(pkg, s));
		
		for (Object o: EcoreUtil.getObjectsByType(pkg.getPackagedElements(), UMLPackage.Literals.PACKAGE)) {
			entityList.addAll(buildAllentityList((Package) o, s));
		}

		return entityList;
	}
	
	protected List<Map<String, Object>> buildEntityList(Package pkg, Stereotype s) {
		List<Map<String, Object>> entityList = new ArrayList<Map<String, Object>>();

		Collection<Class> classes = filterClasses(s, EcoreUtil.getObjectsByType(pkg.getPackagedElements(),UMLPackage.Literals.CLASS));

		String qualifiedPackageName = formatPackageName(pkg.getQualifiedName());
		String packagePath = "/" + qualifiedPackageName.replace("::", "/");
		
		for (Class c: classes) {
			Map<String, Object> entity = new HashMap<String, Object>();
			entity.put("entityName", c.getName());
			entity.put("packagePath", packagePath);
			entity.put("isAbstract", new Boolean(c.isAbstract()));
			entityList.add(entity);
		}

		return entityList;
	}
	
	protected Map<String, Object> buildPackageListData(Package pkg) {
		Map<String, Object> packageListData = new HashMap<String, Object>();
		
		packageListData.put("packages", buildPackageList(pkg));
		
		return packageListData;
	}
	
	protected List<Map<String, Object>> buildPackageList(Package pkg) {
		List<Map<String, Object>> packageList = new ArrayList<Map<String, Object>>();

		Collection<Class> classes = EcoreUtil.getObjectsByType(pkg.getPackagedElements(), UMLPackage.Literals.CLASS);

		String qualifiedPackageName = formatPackageName(pkg.getQualifiedName());
		String packagePath = "/" + qualifiedPackageName.replace("::", "/");
		
		if (classes.size() > 0) {
			Map<String, Object> packageEntry = new HashMap<String, Object>();
			packageEntry.put("qualifiedPackageName", qualifiedPackageName);
			packageEntry.put("packagePath", packagePath);
			packageList.add(packageEntry);
		}
		for (Object o: EcoreUtil.getObjectsByType(pkg.getPackagedElements(), UMLPackage.Literals.PACKAGE)) {
			packageList.addAll(buildPackageList((Package) o));
		}

		return packageList;
	}
	
	protected List<Map<String, Object>> buildEntityData(Package pkg, Stereotype s) {
		List<Map<String, Object>> entityData = new ArrayList<Map<String, Object>>();
		Collection<Class> classes = filterClasses(s, EcoreUtil.getObjectsByType(pkg.getPackagedElements(), UMLPackage.Literals.CLASS));

		String qualifiedPackageName = formatPackageName(pkg.getQualifiedName());
		String packagePath = "/" + qualifiedPackageName.replace("::", "/");

		int depth = packagePath.split("/").length;
		String rootPath="";
		while (--depth > 0) {
			rootPath += "../";
		}
		
		File packageDir = new File("html" + packagePath);
		
		if (!packageDir.exists()) {
			packageDir.mkdir();
		}
		
		if (classes.size() > 0) {
			// Build the package summary page
			//renderComments(docx, pkg.getOwnedComments());
		}
		for (Object o: classes) {
			Map<String, Object> entity = new HashMap<String, Object>();
			entity.put("qualifiedPackageName", qualifiedPackageName);
			entity.put("packagePath", packagePath);
			entity.put("rootPath", rootPath);
			entity.put("stereotype", s.getQualifiedName());
			entity.put("class", s.getName());
			buildEntity(entity, (Class) o);
			entityData.add(entity);
		}
		for (Object o: EcoreUtil.getObjectsByType(pkg.getPackagedElements(), UMLPackage.Literals.PACKAGE)) {
			entityData.addAll(buildEntityData((Package) o, s));
		}
		
		return entityData;
	}
	
	protected void buildEntity(Map<String, Object> entity, Class c) {
		entity.put("entityName", c.getName());

		String qualifiedEntityName = formatPackageName(c.getQualifiedName());
		String entityPath = qualifiedEntityName.replace("::", "/");

		int depth = entityPath.split("/").length;
		String rootPath="";
		while (--depth > 0) {
			rootPath += "../";
		}
		entity.put("rootPath", rootPath);

		List<Class> hierarchy = new ArrayList<Class>();
		buildHierarchy(hierarchy, c);
		
		List<Map<String, String>> entityHierarchy = new ArrayList<Map<String, String>>();
		for (Class cls: hierarchy) {
			entityHierarchy.add(createEntityReference(cls));
		}

		String parent = "";
		if (!hierarchy.isEmpty()) {
			parent = hierarchy.get(hierarchy.size() - 1).getName();
		}
		entity.put("parent", parent);
		
		Map<String, String> entityReference = new HashMap<String, String>();
		entityReference.put("qualifiedEntityName", qualifiedEntityName);
		entityReference.put("entityPath", "");
		entityHierarchy.add(entityReference);

		entity.put("entityHierarchy", entityHierarchy);
		
		List<String> comments = new ArrayList<String>();
		for (Comment comment: c.getOwnedComments()) {
			comments.add(comment.getBody());
		}
		entity.put("comments", comments);
		
		entity.put("isAbstract", new Boolean(c.isAbstract()));
		
		entity.put("attributes", buildAttributeListData(c));
		
		entity.put("associations", buildAssociationListData(c));
		
		entity.put("inheritedAttributes", buildInheritedAttributesListData(hierarchy));
		
		entity.put("inheritedAssociations", buildInheritedAssociationsListData(hierarchy));
	}
	
	protected List<Map<String, Object>> buildInheritedAssociationsListData(List<Class> hierarchy) {
		List<Map<String, Object>> inheritedAssociationsListData = new ArrayList<Map<String, Object>>();
		
		for (Class c: hierarchy) {
			Map<String, Object> inheritedAssociations = new HashMap<String, Object>();
			
			String qualifiedEntityName = getQualifiedElementName(c);
			String entityPath = qualifiedEntityName.replace("::", "/");
			
			inheritedAssociations.put("entityName", qualifiedEntityName);
			inheritedAssociations.put("entityPath", entityPath);
			
			List<String> associations = new ArrayList<String>();
			for (Association association: c.getAssociations()) {
				EList<Property> ends = association.getMemberEnds();
				Property from;
				Property to;
				if (ends.get(0).getType().equals(c)) {
					from = ends.get(0);
					to = ends.get(1);
				} else {
					from = ends.get(1);
					to = ends.get(0);
				}
				
				String a = from.getName() + " " + to.getType().getName() + " " + formatCardinality(from) + " to " + formatCardinality(to);
				
				associations.add(a);
			}

			inheritedAssociations.put("associations", associations);
			
			inheritedAssociationsListData.add(inheritedAssociations);
		}

		return inheritedAssociationsListData;
	}
	
	protected List<Map<String, Object>> buildInheritedAttributesListData(List<Class> hierarchy) {
		List<Map<String, Object>> inheritedAttributeListData = new ArrayList<Map<String, Object>>();
		
		for (Class c: hierarchy) {
			Map<String, Object> inheritedAttributes = new HashMap<String, Object>();
			
			String qualifiedEntityName = getQualifiedElementName(c);
			String entityPath = qualifiedEntityName.replace("::", "/");
			
			inheritedAttributes.put("entityName", qualifiedEntityName);
			inheritedAttributes.put("entityPath", entityPath);
			
			List<String> attributes = new ArrayList<String>();
			for (Property p: c.getOwnedAttributes()) {
				attributes.add(p.getName());
			}
			inheritedAttributes.put("attributes", attributes);
			
			inheritedAttributeListData.add(inheritedAttributes);
		}

		return inheritedAttributeListData;
	}
	
	protected List<Map<String, Object>> buildAssociationListData(Class c) {
		List<Map<String, Object>> associationListData = new ArrayList<Map<String, Object>>();
		
		for (Association association: c.getAssociations()) {
			Map<String, Object> associationData = new HashMap<String, Object>();
			
			EList<Property> ends = association.getMemberEnds();
			Property from;
			Property to;
			if (ends.get(0).getType().equals(c)) {
				from = ends.get(0);
				to = ends.get(1);
			} else {
				from = ends.get(1);
				to = ends.get(0);
			}
			
			associationData.put("name", from.getName());
			Type toType = to.getType();
			associationData.put("type", to.getType().getName());
			String typePath = getQualifiedElementName(toType).replace("::", "/");
			associationData.put("typePath", typePath);
			String cardinality = formatCardinality(from) + " to " + formatCardinality(to);
			associationData.put("cardinality", cardinality);

			List<String> comments = new ArrayList<String>();
			for (Comment comment: association.getOwnedComments()) {
				comments.add(comment.getBody());
			}
			associationData.put("comments", comments);
			
			associationListData.add(associationData);
		}
		
		return associationListData;
	}
	
	protected List<Map<String, Object>> buildAttributeListData(Class c) {
		List<Map<String, Object>> attributeListData = new ArrayList<Map<String, Object>>();
		
		for (Property attribute: c.getOwnedAttributes()) {
			Map<String, Object> attributeData = new HashMap<String, Object>();
			attributeData.put("name", attribute.getName());
			String stereotypes = "";
			for (Stereotype s: attribute.getAppliedStereotypes()) {
				stereotypes += "&lt;&lt;" + s.getName() + "&gt;&gt;";
			}
			attributeData.put("stereotypes", stereotypes);
			String type = "NoType";
			String typePath = "";
			Type attributeType = attribute.getType();
			if (attributeType != null) {
				if (attributeType.getName() != null) {
					type = attributeType.getName();
					
					if (attributeType.isStereotypeApplied(profile.getOwnedStereotype("ReferenceEntity")) || attributeType.isStereotypeApplied(profile.getOwnedStereotype("ComplexEntity"))) {
						typePath = getQualifiedElementName(attributeType).replace("::", "/");
					}
				}
			}
			
			attributeData.put("type", type);
			attributeData.put("typePath", typePath);
			attributeData.put("cardinality", formatCardinality(attribute, ""));
			List<String> comments = new ArrayList<String>();
			for (Comment comment: attribute.getOwnedComments()) {
				comments.add(comment.getBody());
			}
			attributeData.put("comments", comments);
			
			attributeListData.add(attributeData);
		}
		
		return attributeListData;
	}
	
	protected Map<String, String> createEntityReference(Class c) {
		Map<String, String> entityReference = new HashMap<String, String>();
		String qualifiedEntityName = formatPackageName(c.getQualifiedName());
		entityReference.put("qualifiedEntityName", qualifiedEntityName);
		String entityPath = qualifiedEntityName.replace("::", "/");
		entityReference.put("entityPath", entityPath);
		
		return entityReference;
	}
		
	public void renderCatalogue(XWPFDocument docx) {
		Package ldm = getLDM();
		XWPFRun r = createParagraph(docx, "Heading1", "");
		r.addBreak(BreakType.PAGE);
		addRun(r.getParent(), "Entities");
		renderPackage(docx, ldm, profile.getOwnedStereotype("Entity"), true);

		createParagraph(docx, "Heading1", "Structures");
		renderPackage(docx, ldm, profile.getOwnedStereotype("ComplexEntity"), true);

		createParagraph(docx, "Heading1", "Reference Data");
		renderPackage(docx, ldm, profile.getOwnedStereotype("ReferenceEntity"), true);

		renderModelValidation(docx);
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
	
	protected void renderComments(XWPFDocument docx, EList<Comment> comments) {
		for (Comment comment: comments) {
			createParagraph(docx, "Normal", comment.getBody());
		}
	}
	
	protected void renderDescription(XWPFDocument docx, Class cls) {
		// createParagraph(docx, "Normal", "DESCRIPTION").setBold(true);
		renderComments(docx, cls.getOwnedComments());
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
	
	protected void buildHierarchy(List<Class> hierarchy, Class cls) {
		if (!cls.getSuperClasses().isEmpty()) {
			for (Class s: cls.getSuperClasses()) {
				hierarchy.add(0, s);
				buildHierarchy(hierarchy, s);
			}
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
	
	protected List<Class> findSubclasses(Class superclass) {
		List<Class> subclasses = new ArrayList<Class>();
		
		for (Class c: classes) {
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