package uk.org.whitecottage.ea.ldm;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.UMLPackage;

public abstract class LDMProcessor {
	protected Model root;
	protected Profile profile;

	public LDMProcessor(Model root) {
		super();
		this.root = root;
		profile = getLDM().getAppliedProfile("Profile", true);
	}
	
	protected Package getLDM() {
		for (Object o: EcoreUtil.getObjectsByType(root.getPackagedElements(), UMLPackage.Literals.PACKAGE)) {
			Package p = (Package) o;
			if (p.getName().equals("LDM")) {
				return p;
			}
		}
		
		return null;
	}
	
	protected String upperCamelCase(String s) {
		String parts[] = s.split(" ");
		String result = "";
		
		for (int i=0; i<parts.length; i++) {
			result += parts[i].substring(0, 1).toUpperCase() + parts[i].substring(1);
		}
		
		return result;
	}
	
	protected String lowerCamelCase(String s) {
		s = s.trim();
		if (s.isEmpty()) {
			return s;
		} else if (s.length() == 1) {
			return s.toLowerCase();
		}
		
		String parts[] = s.split("\\s");
		String result = parts[0].substring(0, 1).toLowerCase() + parts[0].substring(1);
		
		for (int i=1; i<parts.length; i++) {
			result += parts[i].substring(0, 1).toUpperCase() + parts[i].substring(1);
		}
		
		return result;
	}
}
