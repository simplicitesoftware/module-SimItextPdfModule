package com.simplicite.commons.SimItextPdfModule;

import java.awt.Color;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lowagie.text.Cell;
import com.lowagie.text.Chapter;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Section;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.simplicite.util.AppLog;
import com.simplicite.util.ExternalObject;
import com.simplicite.util.Grant;
import com.simplicite.util.ObjectDB;
import com.simplicite.util.ObjectField;
import com.simplicite.util.Resource;
import com.simplicite.util.Tool;
import com.simplicite.util.exceptions.GetException;
import com.simplicite.util.exceptions.MethodException;

import com.simplicite.objects.System.Module;

/**
 * Module documentation generator
 */
@SuppressWarnings("unused")
public class SpdfModuleDocGenerator implements SpdfTool.PDFInterface {
	/** Blue of Simplicite */
	private static final Color COLOR_HEAD_BKG = new Color(0, 189, 242);

	/**
	 * List of domain/object to export in Doc grouped by domain in chapter
	 * with 4 params : split option, split bool, split large, show lov.
	 * Not final to be changed if necessary
	 */
	private static final String[][] OBJECTS = {
			{ "DomainGrant", "Group", "0", "0", "1", "1" },
			{ "DomainGrant", "Profile", "0", "0", "1", "1" },
			{ "DomainGrant", "Responsability", "0", "0", "1", "1" },
			{ "DomainGrant", "User", "1", "1", "1", "1" },
			{ "DomainGrant", "Grant", "0", "0", "1", "1" },
			{ "DomainGrant", "Function", "0", "0", "1", "1" },
			{ "DomainGrant", "Permission", "0", "0", "1", "1" },
			{ "DomainGrant", "PermGroup", "0", "0", "1", "1" },
			{ "DomainAdmin", "SystemParam", "1", "0", "1", "1" },
			{ "DomainAdmin", "Domain", "1", "0", "1", "1" },
			{ "DomainAdmin", "TranslateDomain", "1", "0", "1", "0" },
			// { "DomainAdmin", "ObjectInternal", "1", "1", "1", "1" }, // Specific sections
			// { "DomainAdmin", "TranslateObject", "1", "0", "1", "0" },
			// { "DomainAdmin", "ObjectFieldSystem", "0", "0", "1", "1" },
			// { "DomainAdmin", "TranslateObjectField", "0", "0", "1", "0" },
			// { "DomainAdmin", "Field", "1", "0", "1", "1" },
			// { "DomainAdmin", "TranslateField", "1", "0", "1", "0" },
			{ "DomainAdmin", "Action", "1", "0", "1", "1" },
			{ "DomainAdmin", "TranslateAction", "1", "0", "1", "0" },
			{ "DomainAdmin", "Link", "1", "0", "1", "1" },
			{ "DomainAdmin", "DataMap", "1", "0", "1", "1" },
			{ "DomainAdmin", "ListOfValue", "1", "0", "1", "1" },
			// { "DomainAdmin", "FieldList", "0", "0", "0", "0" },
			// { "DomainAdmin", "FieldListCode", "0", "0", "0", "0" },
			{ "DomainAdmin", "FieldListValue", "0", "0", "0", "0" },
			{ "DomainAdmin", "FieldListLink", "0", "0", "0", "0" },
			{ "DomainAdmin", "FieldType", "0", "0", "1", "1" },
			{ "DomainAdmin", "TranslateFieldType", "1", "0", "1", "0" },
			{ "DomainAdmin", "ConstraintObject", "1", "0", "1", "1" },
			{ "DomainAdmin", "Script", "1", "0", "1", "1" },
			{ "DomainInterface", "Disposition", "1", "0", "1", "0" },
			{ "DomainInterface", "DispSysParam", "0", "0", "0", "0" },
			{ "DomainInterface", "Resource", "0", "0", "1", "1" },
			{ "DomainInterface", "Map", "0", "0", "1", "1" },
			{ "DomainInterface", "ShortCut", "0", "0", "1", "1" },
			{ "DomainInterface", "TranslateShortcut", "0", "0", "1", "0" },
			{ "DomainInterface", "Research", "0", "0", "1", "1" },
			{ "DomainInterface", "TranslateResearch", "0", "0", "1", "0" },
			// { "DomainInterface", "ObjectExternal", "0", "0", "1", "1" },
			// { "DomainInterface", "TranslateExternal", "0", "0", "1", "0" },
			{ "DomainInterface", "View", "1", "0", "1", "1" },
			{ "DomainInterface", "TranslateView", "0", "0", "1", "0" },
			{ "DomainInterface", "Template", "1", "1", "1", "1" },
			{ "DomainInterface", "ObjectFieldArea", "0", "1", "1", "0" },
			{ "DomainInterface", "TranslateFieldArea", "0", "0", "1", "0" },
			{ "DomainInterface", "FieldStyle", "0", "0", "1", "1" },
			{ "DomainInterface", "PrintTemplate", "1", "0", "1", "1" },
			{ "DomainInterface", "TreeView", "1", "0", "1", "1" },
			{ "DomainInterface", "TreeViewObject", "1", "0", "1", "1" },
			{ "DomainInterface", "TranslateTreeView", "0", "0", "1", "0" },
			{ "DomainInterface", "Crosstab", "1", "0", "1", "1" },
			{ "DomainInterface", "TranslateCrosstab", "0", "0", "1", "0" },
			{ "DomainInterface", "Agenda", "1", "0", "1", "1" },
			{ "DomainInterface", "PlaceMap", "1", "0", "1", "1" },
			{ "DomainInterface", "Timesheet", "1", "0", "1", "1" },
			{ "DomainWorkflow", "BPMProcess", "1", "0", "1", "1" },
			{ "DomainWorkflow", "TranslateProcess", "0", "0", "1", "0" },
			{ "DomainWorkflow", "BPMActivity", "1", "0", "1", "1" },
			{ "DomainWorkflow", "TranslateActivity", "0", "0", "1", "0" },
			{ "DomainWorkflow", "BPMTransition", "1", "0", "1", "1" },
			{ "DomainWorkflow", "BPMData", "1", "0", "1", "1" },
			{ "DomainWorkflow", "BPMState", "1", "0", "1", "1" },
			{ "DomainWorkflow", "BPMStateTransition", "1", "1", "1", "1" },
			{ "DomainWorkflow", "BPMHelp", "1", "0", "1", "1" },
			{ "DomainWorkflow", "BPMGrantProcess", "1", "0", "1", "1" },
			{ "DomainWorkflow", "BPMGrantActivity", "1", "0", "1", "1" },
			{ "DomainWorkflow", "BPMAlert", "1", "0", "1", "1" },
			{ "DomainDoc", "DocIndex", "1", "0", "1", "1" },
			{ "DomainDoc", "TranslateDocIndex", "0", "0", "1", "0" },
			{ "DomainDoc", "DocIndexField", "1", "0", "1", "1" },
			{ "DomainDoc", "DocMIME", "1", "0", "1", "1" },
			{ "DomainDoc", "DocIndexMIME", "1", "0", "1", "1" },
			// { "DomainWebContent", "WebZone", "1", "0", "1", "1" },
			{ "DomainOperation", "LogEvent", "1", "0", "1", "1" },
			{ "DomainOperation", "CronTable", "1", "1", "1", "1" },
			{ "DomainOperation", "Adapter", "1", "0", "1", "1" }
	};

	private Module m_module = null;
	private SpdfTool.PDFEvent m_event = null;

	private static String m_yes = "Yes";
	private static String m_no = "No";

	public static String buildPdfDoc(String rowId) throws MethodException {
		if (Tool.isEmpty(rowId))
			throw new MethodException("Module row ID is empty!");

		Module mdl = (Module) Grant.getSystemAdmin().getTmpObject("Module");
		try {
			mdl.getTool().get(rowId);

			String pdf = mdl.getFieldValue("mdl_name") + "-" + mdl.getFieldValue("mdl_version") + ".pdf";
			String path = com.simplicite.util.engine.Platform.getExportDir() + "/" + pdf;
			SpdfModuleDocGenerator dg = new SpdfModuleDocGenerator();
			Document d = dg.process(path, mdl);

			if (d == null)
				throw new MethodException("Error generating module PDF documentation");
			return path;
		} catch (GetException e) {
			throw new MethodException("Module row ID unknown!");
		}
	}

	/**
	 * Generates PDF documentation for a given module
	 * 
	 * @param pdfPath Path for generated PDF
	 * @param module  Module object
	 * @return PDF document
	 */
	public synchronized Document process(String pdfPath, Module module) {
		m_module = module;
		Document document = null;
		Grant g = Grant.getSystemAdmin();
		m_yes = g.T("YES");
		m_no = g.T("NO");

		try {
			byte[] header = null;
			byte[] footer = null;
			Resource rs = g.getResource(Resource.TYPE_IMAGE, "MODULE_HEADER", null, null);
			if (rs != null)
				header = rs.getDocumentContent(g);
			else
				header = Tool.readStaticResource("images/module_header.jpg");
			rs = g.getResource(Resource.TYPE_IMAGE, "MODULE_FOOTER", null, null);
			if (rs != null)
				footer = rs.getDocumentContent(g);
			else
				footer = Tool.readStaticResource("images/module_footer.jpg");

			m_event = SpdfTool.getDocEvent(true, 30, header, footer);

			document = SpdfTool.build(PageSize.A4, this, pdfPath, true, m_event, 2, 2);
		} catch (Exception e) {
			AppLog.log("ECORED0001", getClass(), "process", new String[] { "PDF generation error" }, e, null);
			document = null;
		}
		return document;
	}

	@Override
	public void buildHeadPages(Document d) throws DocumentException {
		Table table = new Table(1);
		Cell cell = new Cell();
		cell.setBackgroundColor(COLOR_HEAD_BKG);
		cell.setHorizontalAlignment(Cell.ALIGN_CENTER);
		cell.add(new Paragraph("\n", SpdfTool.TITLE0));
		cell.add(new Paragraph("Module " + m_module.getFieldValue("mdl_name"), SpdfTool.TITLE0));
		cell.add(new Paragraph("Configuration", SpdfTool.TITLE1));
		cell.add(new Paragraph("Version " + m_module.getFieldValue("mdl_version"), SpdfTool.TITLE1));
		cell.add(new Paragraph("\n", SpdfTool.TITLE0));
		table.addCell(cell);

		d.add(new Phrase("\n\n\n", SpdfTool.TITLE0));
		d.add(table);
		d.add(new Phrase("\n\n", SpdfTool.TITLE0));
		SpdfTool.insertImage(d, SpdfTool.getImageFromStaticResource("images/logo.jpg"), false, Image.ALIGN_CENTER);
		d.add(new Phrase("\n\n", SpdfTool.TITLE0));
	}

	@Override
	public void buildFootPages(Document d) throws DocumentException {
		// Empty
	}

	@Override
	public void buildContent(Document document) throws DocumentException {
		chapterModels(document);
		chapterModule(document);
		chapterConfiguration(document);
	}

	@Override
	public void setDocInfos(Document d) throws DocumentException {
		String module = m_module.getFieldValue("mdl_name");
		SpdfTool.properties(
				d,
				m_module.getDisplay() + ": " + module,
				"Configuration document for module: " + module,
				null,
				null);
	}

	/**
	 * Generic export of each object
	 * 
	 * @param d Document
	 */
	private void chapterConfiguration(Document d) throws DocumentException {
		// List all domains and objects to display
		Map<String, String> chap = new HashMap<>();
		Map<String, ObjectDB> obj = new HashMap<>();
		for (int i = 0; i < OBJECTS.length; i++) {
			String domain = OBJECTS[i][0];
			String object = OBJECTS[i][1];

			ObjectDB o = getObject(object, true);
			if (o == null)
				continue;

			List<String[]> v = o.search(false);
			if (!v.isEmpty()) {
				chap.put(domain, "1");
				obj.put(domain + ":" + object, o);
			}
		}

		String curDomain = null;
		Chapter chapter = null;
		for (int i = 0; i < OBJECTS.length; i++) {
			String domain = OBJECTS[i][0];
			String object = OBJECTS[i][1];

			// Bypass empty domain
			if (!chap.containsKey(domain))
				continue;

			// Add a Chapter ?
			if (curDomain == null || !domain.equals(curDomain)) {
				String title = getTranslate("Domain#" + domain);
				chapter = SpdfTool.addChapter(d, title, m_event, false);
				curDomain = domain;
			}

			boolean splitOption = "1".equals(OBJECTS[i][2]);
			boolean splitBool = "1".equals(OBJECTS[i][3]);
			boolean splitLarge = "1".equals(OBJECTS[i][4]);
			boolean showLov = "1".equals(OBJECTS[i][5]);

			// Object list in a section
			ObjectDB o = obj.get(domain + ":" + object);
			if (o != null) {
				SpdfTool.addSection(d, chapter, o.getDisplay(), m_event, false);
				SpdfTool.insertList(d, o, splitOption, splitBool, splitLarge, showLov, COLOR_HEAD_BKG, true);
			}
		}
	}

	private void chapterModels(Document d) throws DocumentException {
		ObjectDB model = getObject("Model", true);
		if (model == null)
			return;
		List<String[]> v = model.search(false);
		if (v.isEmpty())
			return;

		String title = getTranslate("Domain#DomainModeler");
		Chapter chapter = SpdfTool.addChapter(d, title, m_event, false);
		for (int i = 0; i < v.size(); i++) {
			String id = v.get(i)[0];
			if (model.select(id)) {
				if (i > 0)
					d.newPage();

				String modelName = model.getFieldValue("mod_name");
				String templateName = model.getFieldValue("mtp_name");
				SpdfTool.addSection(d, chapter, templateName + ": " + modelName, m_event, false);

				String imageId = model.getFieldValue("mod_image");
				if (!Tool.isEmpty(imageId))
					SpdfTool.insertImage(d, SpdfTool.getImageFromDBDoc(Grant.getSystemAdmin(), imageId), true,
							Image.MIDDLE);
			}
		}
	}

	private ObjectDB getObject(String name, boolean filter) {
		Grant sys = Grant.getSystemAdmin();

		if (!sys.accessObject(name)) // Just in case...
			return null;

		ObjectDB o = sys.getObject("pdf_" + name, name);
		o.resetFilters();
		if (filter) {
			ObjectField mdl = o.getField("row_module_id", false);
			if (mdl != null) {
				mdl.setFilter(m_module.getRowId());
				o.getField("mdl_name").setVisibility(ObjectField.VIS_HIDDEN);
			} else {
				o.getRowIdField().setFilter(m_module.getRowId());
			}
		}

		if ("Template".equals(name)) {
			ObjectField f = o.getField("tpl_ui", false);
			if (f != null)
				f.setVisibility(ObjectField.VIS_BOTH);
			f = o.getField("tpl_min", false);
			if (f != null)
				f.setVisibility(ObjectField.VIS_BOTH);
		} else if ("ObjectFieldArea".equals(name)) {
			ObjectField f = o.getField("ofa_ui", false);
			if (f != null)
				f.setVisibility(ObjectField.VIS_BOTH);
		}
		return o;
	}

	private void chapterModule(Document d) throws DocumentException {
		Chapter chapter = SpdfTool.addChapter(d, "Module", m_event, false);
		sectionObjInternal(d, chapter);
		sectionObjExternal(d, chapter);
	}

	private void sectionObjInternal(Document d, Chapter chapter) throws DocumentException {
		ObjectDB obj = getObject("ObjectInternal", true);
		if (obj == null)
			return;

		List<String[]> v = obj.search(false);
		for (int i = 0; i < v.size(); i++) {
			if (i > 0)
				d.newPage();

			String[] val = v.get(i);
			String objName = val[obj.getFieldIndex("obo_name")];
			String title = obj.getDisplay() + ": " + objName;
			Section section = SpdfTool.addSection(d, chapter, title, m_event, false);

			// Object properties
			obj.select(val[0]);

			// SpdfTool.insertForm(d, obj, COLOR_HEAD_BKG, false, false);

			try {
				PdfPTable t1 = SpdfTool.insertFieldArea(null, obj, obj.getFieldArea("ObjectInternal-Design"),
						COLOR_HEAD_BKG, false, false);
				PdfPTable t2 = SpdfTool.insertFieldArea(null, obj, obj.getFieldArea("ObjectInternal-UI"), COLOR_HEAD_BKG,
						false, false);
				PdfPTable t3 = SpdfTool.insertFieldArea(null, obj, obj.getFieldArea("ObjectInternal-Option"),
						COLOR_HEAD_BKG, false, false);

				d.add(t1);
				PdfPTable tb = new PdfPTable(2); // 2 columns
				tb.setWidthPercentage(100);

				PdfPCell c1 = new PdfPCell(new Paragraph(""));
				c1.setPadding(0);
				c1.setPaddingRight(2);
				c1.setBorder(Cell.NO_BORDER);
				c1.addElement(t2);
				tb.addCell(c1);

				PdfPCell c2 = new PdfPCell(new Paragraph(""));
				c2.setPadding(0);
				c2.setPaddingLeft(2);
				c2.setBorder(Cell.NO_BORDER);
				c2.addElement(t3);
				tb.addCell(c2);

				d.add(tb);
			} catch (Exception e) {
				AppLog.log("WCORED0001", getClass(), "sectionObjInternal",
						"Unable to generate section for object internal", e, null);
			}

			// Translate
			ObjectDB tso = getObject("TranslateObject", false);
			tso.setFieldFilter("tsl_object", "ObjectInternal:" + val[0]);
			List<String[]> r = tso.search(false);
			if (!r.isEmpty()) {
				SpdfTool.addSection(d, section, tso.getDisplay(), m_event, false);
				SpdfTool.insertList(d, tso, true, false, false, false, COLOR_HEAD_BKG, true);
			}

			// Functions
			ObjectDB fct = getObject("Function", false);
			fct.setFieldFilter("fct_object_id", val[0]);
			r = fct.search(false);
			if (!r.isEmpty()) {
				SpdfTool.addSection(d, section, fct.getDisplay(), m_event, false);
				SpdfTool.insertList(d, fct, false, false, false, false, COLOR_HEAD_BKG, true);
			}

			// Object fields
			ObjectDB att = getObject("ObjectFieldSystem", false);
			att.setFieldFilter("obf_object_id", val[0]);
			r = att.search(false);
			List<String> vf = new ArrayList<>();
			if (!r.isEmpty()) {
				SpdfTool.addSection(d, section, att.getDisplay(), m_event, false);

				int iFId = att.getFieldIndex("obf_field_id");
				int iFName = att.getFieldIndex("fld_name");
				int iFOrder = att.getFieldIndex("obf_order");
				int iFDfltOrd = att.getFieldIndex("obf_dfault_order");
				int iFFRef = att.getInputIndex("obf_ref_field_id.fld_name");
				int iFORef = att.getInputIndex("obf_ref_object_id.obo_name");
				int iFCascad = att.getFieldIndex("obf_cascad");
				int iFArea = att.getFieldIndex("ofa_name");

				PdfPTable table = SpdfTool.getTable(new int[] { 6, 7, 2, 2, 4, 4, 4 }, true);

				// Title
				table.addCell(SpdfTool.getHeaderCell("Name", Cell.ALIGN_CENTER, COLOR_HEAD_BKG));
				table.addCell(SpdfTool.getHeaderCell("Trans.", Cell.ALIGN_CENTER, COLOR_HEAD_BKG));
				table.addCell(SpdfTool.getHeaderCell("Order", Cell.ALIGN_CENTER, COLOR_HEAD_BKG));
				table.addCell(SpdfTool.getHeaderCell("Sort", Cell.ALIGN_CENTER, COLOR_HEAD_BKG));
				table.addCell(SpdfTool.getHeaderCell("Zone", Cell.ALIGN_CENTER, COLOR_HEAD_BKG));
				table.addCell(SpdfTool.getHeaderCell("Object", Cell.ALIGN_CENTER, COLOR_HEAD_BKG));
				table.addCell(SpdfTool.getHeaderCell("Reference", Cell.ALIGN_CENTER, COLOR_HEAD_BKG));

				ObjectDB tsf = getObject("TranslateField", false);
				int iLang = tsf.getFieldIndex("tsl_lang");
				int iValue = tsf.getFieldIndex("tsl_value");

				// Content
				for (int j = 0; j < r.size(); j++) {
					String[] val2 = r.get(j);
					vf.add(val2[iFName]);

					table.addCell(new PdfPCell(new Phrase(val2[iFName], SpdfTool.NORMAL)));

					tsf.setFieldFilter("tsl_object", "Field:" + val2[iFId]);
					List<String[]> l = tsf.search(false);
					String t = "";
					for (int k = 0; k < l.size(); k++) {
						String[] ts = l.get(k);
						t += (k > 0 ? "\n" : "") + ts[iLang] + " : " + ts[iValue];
					}
					table.addCell(new PdfPCell(new Phrase(t, SpdfTool.NORMAL)));
					table.addCell(new Phrase(val2[iFOrder], SpdfTool.NORMAL));
					table.addCell(new Phrase(val2[iFDfltOrd], SpdfTool.NORMAL));
					table.addCell(new PdfPCell(new Phrase(val2[iFArea], SpdfTool.NORMAL)));
					table.addCell(new PdfPCell(new Phrase(val2[iFORef], SpdfTool.NORMAL)));

					if (val2[iFFRef].length() > 0) {
						table.addCell(new PdfPCell(new Phrase(val2[iFFRef], SpdfTool.NORMAL)));
					} else if (val2[iFCascad].length() > 0) {
						switch (val2[iFCascad].charAt(0)) {
							case ObjectDB.DEL_CASCAD:
								table.addCell(new PdfPCell(new Phrase("Cascade", SpdfTool.NORMAL)));
								break;
							case ObjectDB.DEL_RESTRICT:
								table.addCell(new PdfPCell(new Phrase("Restrict", SpdfTool.NORMAL)));
								break;
							case ObjectDB.DEL_NULL:
								table.addCell(new PdfPCell(new Phrase("Null", SpdfTool.NORMAL)));
								break;
							default:
								table.addCell(new PdfPCell(new Phrase("Ignore", SpdfTool.NORMAL)));
								break;
						}
					} else {
						table.addCell(new PdfPCell(new Phrase("", SpdfTool.NORMAL)));
					}
				}

				d.add(table);
			}

			// Fields
			ObjectDB fld = getObject("Field", false);
			SpdfTool.addSection(d, section, fld.getDisplay(), m_event, false);

			int iDbn = fld.getFieldIndex("fld_dbname");
			int iVis = fld.getFieldIndex("fld_visible");
			int iUpd = fld.getFieldIndex("fld_updatable");
			int iReq = fld.getFieldIndex("fld_required");
			int iFid = fld.getFieldIndex("fld_fonctid");
			int iRsh = fld.getFieldIndex("fld_research");
			int iMor = fld.getFieldIndex("fld_more");
			int iLmo = fld.getFieldIndex("fld_listmore");
			int iTyp = fld.getFieldIndex("fld_type");
			int iReg = fld.getFieldIndex("flt_code");
			int iLov = fld.getFieldIndex("lov_name");
			int iSiz = fld.getFieldIndex("fld_size");
			int iPre = fld.getFieldIndex("fld_precision");
			int iDft = fld.getFieldIndex("fld_dfault");

			PdfPTable table = SpdfTool.getTable(new int[] { 5, 1, 1, 1, 1, 1, 1, 4, 4, 2, 3 }, true);
			table.addCell(SpdfTool.getHeaderCell("Name", COLOR_HEAD_BKG));
			table.addCell(SpdfTool.getHeaderCellVertical("Required", COLOR_HEAD_BKG));
			table.addCell(SpdfTool.getHeaderCellVertical("Update", COLOR_HEAD_BKG));
			table.addCell(SpdfTool.getHeaderCellVertical("Key", COLOR_HEAD_BKG));
			table.addCell(SpdfTool.getHeaderCellVertical("Search", COLOR_HEAD_BKG));
			table.addCell(SpdfTool.getHeaderCellVertical("More form", COLOR_HEAD_BKG));
			table.addCell(SpdfTool.getHeaderCellVertical("More list", COLOR_HEAD_BKG));
			table.addCell(SpdfTool.getHeaderCell("Type", COLOR_HEAD_BKG));
			table.addCell(SpdfTool.getHeaderCell("Column", COLOR_HEAD_BKG));
			table.addCell(SpdfTool.getHeaderCell("Visible", COLOR_HEAD_BKG));
			table.addCell(SpdfTool.getHeaderCell("Default value", COLOR_HEAD_BKG));

			List<String> vLov = new ArrayList<>();
			for (int j = 0; j < vf.size(); j++) {
				String name = vf.get(j);
				fld.setFieldFilter("fld_name", name);
				List<String[]> l = fld.search(false);
				if (l.isEmpty())
					continue;

				String[] val2 = l.get(0);
				table.addCell(new PdfPCell(new Phrase(name, SpdfTool.NORMAL)));
				table.addCell(SpdfTool.getCell(Tool.TRUE.equals(val2[iReq]) ? "X" : ""));
				table.addCell(SpdfTool.getCell(Tool.FALSE.equals(val2[iUpd]) ? "" : "X"));
				table.addCell(SpdfTool.getCell(Tool.TRUE.equals(val2[iFid]) ? "X" : ""));
				table.addCell(SpdfTool.getCell(Tool.TRUE.equals(val2[iRsh]) ? "X" : ""));
				table.addCell(SpdfTool.getCell(Tool.TRUE.equals(val2[iMor]) ? "X" : ""));
				table.addCell(SpdfTool.getCell(Tool.TRUE.equals(val2[iLmo]) ? "X" : ""));

				int t = Tool.parseInt(val2[iTyp], -1);
				String tp = ObjectField.getTypeLabel(t, Tool.parseInt(val2[iSiz], 0), Tool.parseInt(val2[iPre], 0));
				String tp2 = null;
				switch (t) {
					case ObjectField.TYPE_ENUM:
						tp2 = val2[iLov];
						break;
					case ObjectField.TYPE_ENUM_MULTI:
						tp2 = val2[iLov];
						break;
					case ObjectField.TYPE_REGEXP:
						tp2 = val2[iReg];
						break;
				}

				if (val2[iLov].length() > 0)
					vLov.add(val2[iLov]);

				Phrase tpp = new Phrase(tp, SpdfTool.NORMAL);
				if (tp2 != null)
					tpp.add(new Phrase("\n" + tp2, SpdfTool.SMALL));
				table.addCell(new PdfPCell(tpp));
				table.addCell(new PdfPCell(new Phrase(val2[iDbn].length() > 0 ? val2[iDbn] : "", SpdfTool.NORMAL)));

				switch (Tool.parseInt(val2[iVis])) {
					case ObjectField.VIS_BOTH:
						table.addCell(new PdfPCell(new Phrase(m_yes, SpdfTool.NORMAL)));
						break;
					case ObjectField.VIS_FORM:
						table.addCell(new PdfPCell(new Phrase("Form", SpdfTool.NORMAL)));
						break;
					case ObjectField.VIS_LIST:
						table.addCell(new PdfPCell(new Phrase("List", SpdfTool.NORMAL)));
						break;
					case ObjectField.VIS_HIDDEN:
						table.addCell(new PdfPCell(new Phrase(m_no, SpdfTool.NORMAL)));
						break;
					case ObjectField.VIS_FORBIDDEN:
						table.addCell(new PdfPCell(new Phrase("Forb", SpdfTool.NORMAL)));
						break;
				}

				table.addCell(new PdfPCell(new Phrase(val2[iDft], SpdfTool.NORMAL)));
			}
			d.add(table);

			// LOV
			if (!vLov.isEmpty()) {
				ObjectDB lov = getObject("FieldListValue", false);
				SpdfTool.addSection(d, section, lov.getDisplay(), m_event, false);

				int iCod = lov.getFieldIndex("lov_code");
				int iLng = lov.getFieldIndex("lov_lang");
				int iVal = lov.getFieldIndex("lov_value");
				int iOrd = lov.getFieldIndex("lov_order_by");
				for (int j = 0; j < vLov.size(); j++) {
					String name = vLov.get(j);
					d.add(new Paragraph(name, SpdfTool.TITLE2));

					lov.setFieldFilter("lov_name", name);
					r = lov.search(false);

					table = SpdfTool.getTable(new int[] { 3, 2, 5, 2 }, true);
					table.addCell(SpdfTool.getHeaderCell("Code", COLOR_HEAD_BKG));
					table.addCell(SpdfTool.getHeaderCell("Language", COLOR_HEAD_BKG));
					table.addCell(SpdfTool.getHeaderCell("Value", COLOR_HEAD_BKG));
					table.addCell(SpdfTool.getHeaderCell("Order", COLOR_HEAD_BKG));

					for (int k = 0; k < r.size(); k++) {
						String[] val2 = r.get(k);
						table.addCell(new PdfPCell(new Phrase(val2[iCod], SpdfTool.NORMAL)));
						table.addCell(new PdfPCell(new Phrase(val2[iLng], SpdfTool.NORMAL)));
						table.addCell(new PdfPCell(new Phrase(val2[iVal], SpdfTool.NORMAL)));
						table.addCell(new PdfPCell(new Phrase(val2[iOrd], SpdfTool.NORMAL)));
					}
					d.add(table);
				}
			}

			// Scripts
			boolean[] crud = Grant.getSystemAdmin().changeAccess(objName, false, true, false, false);
			try {
				ObjectDB o = Grant.getSystemAdmin().getMainObject(objName);
				addJavaMethods(d, o, section);
			} finally {
				Grant.getSystemAdmin().changeAccess(title, crud);
			}
		}
	}

	private void sectionObjExternal(Document d, Chapter chapter) throws DocumentException {
		ObjectDB obe = getObject("ObjectExternal", true);
		if (obe == null)
			return;

		List<String[]> v = obe.search(false);
		int iName = obe.getFieldIndex("obe_name");
		int iUrl = obe.getFieldIndex("obe_url");
		int iHelp = obe.getFieldIndex("obe_help");

		for (int i = 0; i < v.size(); i++) {
			String[] val = v.get(i);
			String name = val[iName];
			String title = obe.getDisplay() + ": " + name;
			Section section = SpdfTool.addSection(d, chapter, title, m_event, false);

			// Properties
			PdfPTable table = SpdfTool.getTable(new int[] { 2, 5 }, true);
			table.addCell(SpdfTool.getHeaderCell("Property", COLOR_HEAD_BKG));
			table.addCell(SpdfTool.getHeaderCell("Value", COLOR_HEAD_BKG));

			table.addCell(new PdfPCell(new Phrase("URL", SpdfTool.NORMAL)));
			table.addCell(new PdfPCell(new Phrase(val[iUrl], SpdfTool.NORMAL)));
			table.addCell(new PdfPCell(new Phrase("Help", SpdfTool.NORMAL)));
			table.addCell(new PdfPCell(new Phrase(val[iHelp], SpdfTool.NORMAL)));

			// Translate
			table.addCell(new PdfPCell(new Phrase("Translation", SpdfTool.NORMAL)));
			ObjectDB tso = getObject("TranslateExternal", false);
			if (tso != null) {
				tso.setFieldFilter("tsl_object", "ObjectExternal:" + val[0]);
				List<String[]> r = tso.search(false);
				int iLang = tso.getFieldIndex("tsl_lang");
				int iValue = tso.getFieldIndex("tsl_value");
				if (!r.isEmpty()) {
					Paragraph p = new Paragraph();
					for (int j = 0; j < r.size(); j++) {
						String[] val2 = r.get(j);
						p.add(new Phrase(val2[iLang] + ": " + val2[iValue] + (j < r.size() - 1 ? "\n" : ""),
								SpdfTool.NORMAL));
					}
					table.addCell(new PdfPCell(p));
				} else
					table.addCell(new PdfPCell());
			}

			// Functions
			ObjectDB fct = getObject("Function", false);
			if (fct != null) {
				fct.setFieldFilter("fct_object_id", val[0]);
				table.addCell(new PdfPCell(new Phrase(fct.getDisplay(), SpdfTool.NORMAL)));

				List<String[]> r = fct.search(false);
				int iCode = fct.getFieldIndex("fct_name");
				int iFonction = fct.getFieldIndex("fct_function");
				int iAction = fct.getFieldIndex("act_name");
				if (!r.isEmpty()) {
					Paragraph p = new Paragraph();
					for (int j = 0; j < r.size(); j++) {
						String[] val2 = r.get(j);
						String f = val2[iCode] + ": " + val2[iFonction];
						if (val2[iFonction].charAt(0) == 'A')
							f += " (" + val2[iAction] + ")";
						p.add(new Phrase(f + (j < r.size() - 1 ? "\n" : ""), SpdfTool.NORMAL));
					}
					table.addCell(new PdfPCell(p));
				} else
					table.addCell(new PdfPCell());
			}
			d.add(table);

			// Script
			ExternalObject ext = Grant.getSystemAdmin().getExternalObject(name);
			if (!Tool.isEmpty(ext.getScriptId()))
				addScript(d, "extobject", ext.readScript(true), section);
		}
	}

	private String getTranslate(String filter) {
		ObjectDB tsl = Grant.getSystemAdmin().getTmpObject("Translate");
		tsl.resetFilters();
		tsl.setFieldFilter("tsl_object", filter);
		tsl.setFieldFilter("tsl_lang", m_module.getGrant().getLang());
		List<String[]> v = tsl.search(false);
		if (!v.isEmpty()) {
			String[] val = v.get(0);
			return val[tsl.getFieldIndex("tsl_value")];
		}
		return filter;
	}

	private void addJavaMethods(Document d, ObjectDB obj, Section section) throws DocumentException {
		Class<?> c = obj.getClass();
		if (c.equals(ObjectDB.class))
			return;

		Method[] m = c.getDeclaredMethods();
		if (Tool.isEmpty(m))
			return;

		SpdfTool.addSection(d, section, "ObjectDB extends", m_event, false);
		PdfPTable table = SpdfTool.getTable(new int[] { 1, 2 }, true);
		table.addCell(SpdfTool.getHeaderCell("Java method", COLOR_HEAD_BKG));
		table.addCell(SpdfTool.getHeaderCell("Parameters", COLOR_HEAD_BKG));

		for (int i = 0; i < m.length; i++) {
			String method = m[i].getReturnType().getSimpleName() + " " + m[i].getName();
			table.addCell(SpdfTool.getCell(method, SpdfTool.NORMAL, Cell.ALIGN_LEFT, true, Color.WHITE));
			Paragraph par = new Paragraph();
			Class<?>[] p = m[i].getParameterTypes();
			for (int j = 0; p != null && j < p.length; j++)
				par.add(new Phrase((j > 0 ? ", " : "") + p[j].getSimpleName(), SpdfTool.NORMAL));
			table.addCell(par);
		}
		d.add(table);
	}

	private void addScript(Document d, String code, String source, Section section) throws DocumentException {
		String title = "Script";
		if (code != null)
			title += " " + code;

		SpdfTool.addSection(d, section, title, m_event, false);

		PdfPTable table = SpdfTool.getTable(new int[] { 1 }, true);
		table.addCell(SpdfTool.getHeaderCell("code", COLOR_HEAD_BKG));
		table.addCell(SpdfTool.getCell(source, SpdfTool.CODE, Cell.ALIGN_LEFT, true, Color.WHITE));
		d.add(table);
	}
}
