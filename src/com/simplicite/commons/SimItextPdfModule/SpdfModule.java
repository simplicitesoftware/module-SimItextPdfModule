package com.simplicite.commons.SimItextPdfModule;

import com.simplicite.util.AppLog;
import com.simplicite.util.Grant;
import com.simplicite.util.ObjectDB;
import com.simplicite.util.PrintTemplate;
import com.simplicite.util.annotations.BusinessObjectPublication;
import com.simplicite.util.tools.FileTool;
import com.simplicite.util.tools.HTTPTool;

/**
 * Module legacy PDF publication
 */
public class SpdfModule implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	@BusinessObjectPublication
	public static Object legacyPDF(PrintTemplate pt) { // ZZZ publication method must be static
		ObjectDB mdl = pt.getObject();
		String name = mdl.getFieldValue("mdl_name");
		Grant g = mdl.getGrant();
		AppLog.info("Publishing legacy PDF for module " + name, g);
		try {
			pt.setMIMEType(HTTPTool.MIME_TYPE_PDF);
			pt.setFilename(name + ".pdf");
			String path = SpdfModuleDocGenerator.buildPdfDoc(mdl.getRowId());
			return FileTool.readFileAsBytes(path);
		} catch (Exception e) { // Unexpected error => text file with error message
			AppLog.error("Unable to publish legacy PDF for module " + name, e, g);
			pt.setMIMEType(HTTPTool.MIME_TYPE_TXT);
			pt.setFilename("error.txt");
			return e.getMessage();
		}
	}
}
