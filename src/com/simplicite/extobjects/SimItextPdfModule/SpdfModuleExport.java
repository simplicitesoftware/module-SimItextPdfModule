package com.simplicite.extobjects.SimItextPdfModule;

import java.util.*;

import com.simplicite.util.*;
import com.simplicite.util.exceptions.*;
import com.simplicite.util.tools.*;

import com.simplicite.util.ExternalObject;
import com.simplicite.commons.SimItextPdfModule.ModuleDocGenerator;
import com.lowagie.text.Document;
import com.simplicite.objects.System.Module;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * PDF document external object SpdfModuleExport
 */
public class SpdfModuleExport extends com.simplicite.util.ExternalObject {
	private static final long serialVersionUID = 1L;
	String path;

	/**
	 * Build PDF document
	 * @param params Request parameters
	 */
	public final Object display(Parameters params)
	{
		String mdlName = params.getParameter("module");
		if(Tool.isEmpty(mdlName)){
			error("No module for this name");
		}
		
		try
		{
			String path = ModuleDocGenerator.buildPdfDoc(ModuleDB.getModuleId(mdlName));
			setPDFMIMEType();
			setContentDisposition(HTTPTool.DISP_INLINE, "test.pdf");
			return Files.readAllBytes(Path.of(path));
		}
		catch (Exception e)
		{
			AppLog.error(getClass(), "display", null, e, getGrant());
			return error(e.getMessage());
		}
	}
	
	private String error(String e){
		setTextMIMEType();
		setContentDisposition(HTTPTool.DISP_INLINE, null);
		return e;
	}
	
	
}
