package com.simplicite.extobjects.SimItextPdfModule;

import com.simplicite.util.AppLog;
import com.simplicite.util.ExternalObject;
import com.simplicite.util.ModuleDB;
import com.simplicite.util.Tool;
import com.simplicite.util.tools.Parameters;
import com.simplicite.util.tools.HTTPTool;

import com.simplicite.commons.SimItextPdfModule.ModuleDocGenerator;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Module PDF document external object
 */
public class SpdfModuleExport extends ExternalObject {
	private static final long serialVersionUID = 1L;

	/**
	 * Build PDF document
	 * 
	 * @param params Request parameters
	 */
	public final Object display(Parameters params) {
		String mdlName = params.getParameter("module");
		if (Tool.isEmpty(mdlName)) {
			error("No module for this name");
		}

		try {
			String path = ModuleDocGenerator.buildPdfDoc(ModuleDB.getModuleId(mdlName));
			setPDFMIMEType();
			setContentDisposition(HTTPTool.DISP_INLINE, "test.pdf");
			return Files.readAllBytes(Path.of(path));
		} catch (Exception e) {
			AppLog.error(getClass(), "display", null, e, getGrant());
			return error(e.getMessage());
		}
	}

	private String error(String e) {
		setTextMIMEType();
		setContentDisposition(HTTPTool.DISP_INLINE, null);
		return e;
	}
}
