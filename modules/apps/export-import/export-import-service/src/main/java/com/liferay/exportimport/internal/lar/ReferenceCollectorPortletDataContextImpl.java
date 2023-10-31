package com.liferay.exportimport.internal.lar;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.lock.LockManager;
import com.liferay.portal.kernel.model.ClassedModel;
import com.liferay.portal.kernel.xml.Element;

import java.util.ArrayList;
import java.util.List;

public class ReferenceCollectorPortletDataContextImpl
	extends PortletDataContextImpl {

	public ReferenceCollectorPortletDataContextImpl(LockManager lockManager) {
		super(lockManager);
	}

	@Override
	public Element addReferenceElement(
		ClassedModel referrerClassedModel, Element element,
		ClassedModel classedModel, String className, String binPath,
		String referenceType, boolean missing) {

		if (missing && !isGlobalBasicWebContentStructure(classedModel)) {
			_references.add(
				"[addReferenceElement] " +
				referrerClassedModel.getModelClassName() + "(" +
				referrerClassedModel.getPrimaryKeyObj() + ")"
				+ " ---> " + classedModel.getModelClassName() + "(" +
				classedModel.getPrimaryKeyObj() + ")"
				+ ", classedModel=" + classedModel
				+ ", className=" + className
				+ ", binPath=" + binPath
				+ ", type=" + referenceType
				+ ", missing=" + missing);
		}

		return super.addReferenceElement(referrerClassedModel, element, classedModel, className, binPath, referenceType, missing);
	}

	private static boolean isGlobalBasicWebContentStructure(ClassedModel classedModel) {
		String text = classedModel.toString();
		String className = classedModel.getModelClassName();

		return (
			className.equals("com.liferay.dynamic.data.mapping.model.DDMTemplate") ||
			className.equals("com.liferay.dynamic.data.mapping.model.DDMStructure")
		) && text.contains("\"BASIC-WEB-CONTENT\"");
	}

	@Override
	public void addClassedModel(
		Element element, String path, ClassedModel classedModel,
		Class<?> clazz)
		throws PortalException {

		/*_references.add(
			"[addClassedModel] path=" + path
			+ ", classedModel=" + classedModel.getModelClassName() + "("+classedModel.getPrimaryKeyObj()+")"
		);*/

		super.addClassedModel(element, path, classedModel, clazz);
	}
	public List<String> getReferences() {
		return _references;
	}

	private final List<String> _references = new ArrayList<>();

}