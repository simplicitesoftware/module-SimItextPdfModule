package com.simplicite.commons.SimItextPdfModule;

import com.simplicite.util.tools.*;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.text.PDFTextStripper;

import com.lowagie.text.Cell;
import com.lowagie.text.Chapter;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Section;
import com.lowagie.text.Table;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.html.simpleparser.StyleSheet;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfCopyFields;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.SimpleBookmark;
import com.simplicite.util.AppLog;
import com.simplicite.util.DocumentDB;
import com.simplicite.util.EnumItem;
import com.simplicite.util.ExternalObject;
import com.simplicite.util.FieldArea;
import com.simplicite.util.FieldAreas;
import com.simplicite.util.Globals;
import com.simplicite.util.Grant;
import com.simplicite.util.JobQueue;
import com.simplicite.util.ObjectDB;
import com.simplicite.util.ObjectField;
import com.simplicite.util.ObjectHooks;
import com.simplicite.util.Resource;
import com.simplicite.util.Tool;
import com.simplicite.util.engine.Platform;
import com.simplicite.webapp.ObjectContextWeb;

/**
 * Legacy PDF toolbox
 */
@SuppressWarnings("unused")
public class PDFTool {
	/** Hidden default constructor */
	private PDFTool() {
		// Do nothing
	}

	// Not final = may be overridden
	public static final Font TITLE0 = FontFactory.getFont(FontFactory.HELVETICA, 24, Font.BOLD, Color.BLACK);
	public static final Font TITLE1 = FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLD, Color.BLACK);
	public static final Font TITLE2 = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD, Color.BLACK);
	public static final Font HEADER = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD, Color.BLACK);
	public static final Font BOLD = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.BOLD, Color.BLACK);
	public static final Font NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.NORMAL, Color.BLACK);
	public static final Font MEDIUM = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL, Color.BLACK);
	public static final Font SMALL = FontFactory.getFont(FontFactory.HELVETICA, 7, Font.NORMAL, Color.BLACK);
	public static final Font TINY = FontFactory.getFont(FontFactory.HELVETICA, 6, Font.NORMAL, Color.BLACK);
	public static final Font MONO = FontFactory.getFont(FontFactory.COURIER, 9, Font.NORMAL, Color.BLACK);
	public static final Font CODE = FontFactory.getFont(FontFactory.COURIER, 7, Font.NORMAL, Color.BLACK);

	public static final Paragraph EMPTY = new Paragraph(new Phrase("", NORMAL));

	public static final PdfPCell CELL_WHITE = getColoredCell(Color.WHITE, true);
	public static final PdfPCell CELL_GRAY = getColoredCell(Color.GRAY, true);
	public static final PdfPCell CELL_BLACK = getColoredCell(Color.BLACK, true);

	public static final Color IMAGE_COLOR = new Color(220, 220, 220);

	// convert euro symbol
	public static final String EURO = "\u20ac";

	// iText workaround : force image scale / convert 96dpi resolution to 72dpi
	public static final float IMAGE_SCALE = 2f / 3f;

	/**
	 * Build a simple PDF document
	 * 
	 * @param pdf     Interface to implement the content
	 * @param pdfPath File path
	 * @param replace True to delete any older doc, False to stop if already exists
	 * @return Document or null
	 */
	public static Document build(PDFInterface pdf, String pdfPath, boolean replace) {
		return build(PageSize.A4, pdf, pdfPath, replace, null, 0, 0);
	}

	/**
	 * Build a simple PDF document
	 * 
	 * @param pageSize such as PageSize.A4 or PageSize.A4.rotate()
	 * @param pdf      Interface to implement the content
	 * @param pdfPath  File path
	 * @param replace  True to delete any older doc, False to stop if already exists
	 * @return Document or null
	 */
	public static Document build(Rectangle pageSize, PDFInterface pdf, String pdfPath, boolean replace) {
		return build(pageSize, pdf, pdfPath, replace, null, 0, 0);
	}

	/**
	 * Build a PDF document
	 * 
	 * @param pageSize such as PageSize.A4 or PageSize.A4.rotate()
	 * @param pdf      Interface to implement the content
	 * @param pdfPath  File path
	 * @param replace  True to delete any older doc, False to stop if already exists
	 * @param event    Doc event (may be null)
	 * @param tocPage  Insert a table of content in the given page, 0 = no TOC
	 * @param tocDepth Table of content depth (chapter=1, section=2,
	 *                 sub-section=3...)
	 * @return Document or null
	 */
	public static Document build(Rectangle pageSize, PDFInterface pdf, String pdfPath, boolean replace, PDFEvent event,
			int tocPage, int tocDepth) {
		Document document = null;
		try {
			document = open(pageSize, pdfPath, true, event);

			pdf.setDocInfos(document);
			pdf.buildHeadPages(document);
			pdf.buildContent(document);
			pdf.buildFootPages(document);

			close(document);

			// Insert a TOC ?
			if (tocPage > 0) {
				Document d = insertToc(pdf, document, pdfPath, tocPage, tocDepth, event);
				if (d != null)
					document = d;
			}
		} catch (Exception e) {
			AppLog.log("ECORED0001", PDFTool.class, "build", new String[] { "PDF build error" }, e, null);
			document = null;
		}

		return document;
	}

	/**
	 * Add properties to the PDF document
	 * 
	 * @param document PDF document
	 * @param title    Optional title
	 * @param subject  Optional subject
	 * @param keywords Optional keywords
	 * @param author   Optional author (defaults to platform name)
	 */
	public static void properties(Document document, String title, String subject, String keywords, String author) {
		if (!Tool.isEmpty(title))
			document.addTitle(title);

		if (!Tool.isEmpty(subject))
			document.addSubject(subject);

		if (!Tool.isEmpty(keywords))
			document.addKeywords(keywords);

		document.addAuthor(!Tool.isEmpty(author) ? author : Globals.getPlatformName());

		document.addCreator(Globals.getPlatformVendor());
	}

	/**
	 * <p>
	 * Open a new PDF document with file output
	 * </p>
	 * 
	 * @param pdfPath File path
	 * @param replace True to delete any older doc, False to stop if already exists
	 * @param event   Doc event (may be null)
	 * @return A document or null
	 */
	public static Document open(Rectangle pageSize, String pdfPath, boolean replace, PDFEvent event) {
		try {
			File file = new File(pdfPath);
			file.mkdirs();

			// keep or replace ?
			if (file.exists() && replace && !file.delete())
				AppLog.warning(PDFTool.class, "open", "Unable to delete file: " + file.getAbsolutePath(), null, null);

			return open(pageSize, new FileOutputStream(file), event);
		} catch (FileNotFoundException e) {
			AppLog.log("ECORED0001", PDFTool.class, "open", new String[] { "PDF open doc error" }, e, null);
			return null;
		}
	}

	/**
	 * <p>
	 * Open a new PDF document A4 with output stream
	 * </p>
	 * 
	 * @param out Output stream (can be HTTP response output stream of file output
	 *            stream)
	 * @return A document or null
	 */
	public static final Document open(OutputStream out) {
		return open(PageSize.A4, out, null);
	}

	/**
	 * <p>
	 * Open a new PDF document A4 with output stream
	 * </p>
	 * 
	 * @param out   Output stream (can be HTTP response output stream of file output
	 *              stream)
	 * @param event Doc event (may be null)
	 * @return A document or null
	 */
	public static final Document open(OutputStream out, PDFEvent event) {
		return open(PageSize.A4, out, event);
	}

	/**
	 * <p>
	 * Open a new PDF document with output stream
	 * </p>
	 * 
	 * @param pageSize such as PageSize.A4 or PageSize.A4.rotate()
	 * @param out      Output stream (can be HTTP response output stream of file
	 *                 output stream)
	 * @param event    Doc event (may be null)
	 * @return A document or null
	 */
	public static Document open(Rectangle pageSize, OutputStream out, PDFEvent event) {
		Document document = null;
		PdfWriter writer = null;
		try {
			if (event == null) {
				document = new Document(pageSize);
				writer = PdfWriter.getInstance(document, out);
			} else {
				document = new Document(
						pageSize,
						event.m_margin,
						event.m_margin,
						event.m_margin_top,
						event.m_margin_bottom);
				writer = PdfWriter.getInstance(document, out);
				writer.setPageEvent(event);
			}
			document.open();
			properties(document, null, null, null, null); // Add default properties
		} catch (DocumentException e) {
			AppLog.log("ECORED0001", PDFTool.class, "open", new String[] { "PDF open doc error" }, e, null);
			document = null;
		}
		return document;
	}

	/**
	 * <p>
	 * Close a document
	 * </p>
	 * 
	 * @param d Opened document
	 */
	public static void close(Document d) {
		try {
			if (d != null)
				d.close();
		} catch (Exception e) {
			AppLog.log("ECORED0001", PDFTool.class, "close", new String[] { "PDF close doc error" }, e, null);
		}
	}

	/**
	 * <p>
	 * Stamp an existing PDF with an image on each pages
	 * </p>
	 * 
	 * @param in    Input stream of source PDF
	 * @param out   Output stream of destination PDF
	 * @param image Image data
	 * @param posX  Absolute position X
	 * @param posY  Absolute position Y
	 */
	public static final void stamp(InputStream in, OutputStream out, byte[] image, float posX, float posY)
			throws Exception {
		PdfReader reader = new PdfReader(in);

		PdfStamper stamper = new PdfStamper(reader, out);
		Image img = Image.getInstance(image);

		for (int i = 1; i <= reader.getNumberOfPages(); i++) {
			PdfContentByte content = stamper.getUnderContent(i);
			img.setAbsolutePosition(posX, posY);
			content.addImage(img);
		}

		stamper.close();
		reader.close();
	}

	/**
	 * Build a document event handler
	 * 
	 * @param pagine    True to display the page numbers on page bottom
	 * @param margin    Border margin in pixels
	 * @param headerImg Path to the header image
	 * @param footerImg Path to the footer image
	 * @return event or null if file read fails
	 */
	public static PDFEvent getDocEvent(
			boolean pagine, int margin,
			String headerImg, String footerImg) {
		try {
			return getDocEvent(pagine, margin, FileTool.readFileAsBytes(headerImg),
					FileTool.readFileAsBytes(footerImg));
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Build a document event handler
	 * 
	 * @param pagine    True to display the page numbers on page bottom
	 * @param margin    Border margin in pixels
	 * @param headerImg Header image data
	 * @param footerImg Footer image data
	 * @return event
	 */
	public static PDFEvent getDocEvent(
			boolean pagine, int margin,
			byte[] headerImg, byte[] footerImg) {
		PDFEvent event = (new PDFTool()).new PDFEvent();
		event.m_pagine = pagine;
		event.m_margin = margin;

		event.m_margin_top = margin;
		if (headerImg != null) {
			try {
				event.m_headerImage = Image.getInstance(headerImg);
				event.m_margin_top += event.m_headerImage.getHeight() * IMAGE_SCALE;
			} catch (Exception e) {
				AppLog.warning(PDFTool.class, "open", "Header file not found: " + headerImg, null, null);
			}
		}

		event.m_margin_bottom = margin;
		if (footerImg != null) {
			try {
				event.m_footerImage = Image.getInstance(footerImg);
				event.m_margin_bottom += event.m_footerImage.getHeight() * IMAGE_SCALE;
			} catch (Exception e) {
				AppLog.warning(PDFTool.class, "open", "Footer file not found: " + footerImg, null, null);
			}
		}

		return event;
	}

	public static Chapter addChapter(Document d, String title, PDFEvent event, boolean isOpen)
			throws DocumentException {
		Paragraph sTitle = new Paragraph(title, TITLE1);
		Chapter chapter = new Chapter(sTitle, event.chapterIndex++);
		chapter.setBookmarkOpen(isOpen);
		event.sectionIndex = 2;
		d.add(chapter);
		return chapter;
	}

	public static Section addSection(Document d, Chapter chapter, String title, PDFEvent event, boolean isOpen)
			throws DocumentException {
		Paragraph sTitle = new Paragraph(title, TITLE2);
		Section section = chapter.addSection(sTitle, event.sectionIndex++);
		section.setBookmarkOpen(isOpen);
		d.add(section);
		return section;
	}

	public static Section addSection(Document d, Section section, String title, PDFEvent event, boolean isOpen)
			throws DocumentException {
		Paragraph sTitle = new Paragraph(title, TITLE2);
		Section child = section.addSection(sTitle, event.sectionIndex++);
		child.setBookmarkOpen(isOpen);
		d.add(child);
		return child;
	}

	/**
	 * <p>
	 * Build a Table cell
	 * </p>
	 * 
	 * @param text Simple text
	 * @return Table cell
	 */
	public static PdfPCell getCell(String text) {
		return getCell(text, NORMAL, Cell.ALIGN_LEFT, true, Color.WHITE);
	}

	/**
	 * <p>
	 * Build a Table cell
	 * </p>
	 * 
	 * @param text   Simple text
	 * @param font   Font to use
	 * @param align  Alignment
	 * @param border True to draw the border
	 * @param color  Background color
	 * @return Table cell
	 */
	public static PdfPCell getCell(String text, Font font, int align, boolean border, Color color) {
		try {
			PdfPCell cell = new PdfPCell(new Phrase(text, font));
			if (text == null)
				text = "";
			cell.setBackgroundColor(color);
			cell.setHorizontalAlignment(align);
			cell.setPadding(4);
			if (!border)
				cell.setBorder(Cell.NO_BORDER);
			return cell;
		} catch (Exception e) {
			return new PdfPCell(new Phrase(text));
		}
	}

	/**
	 * <p>
	 * Build a header cell
	 * </p>
	 * 
	 * @param text  header text
	 * @param align cell alignment
	 * @return Table cell
	 */
	public static PdfPCell getHeaderCell(String text, int align, Color color) {
		return getCell(text, HEADER, align, true, color);
	}

	/**
	 * <p>
	 * Build a header cell
	 * </p>
	 * 
	 * @param text Text
	 * @return Table cell
	 */
	public static PdfPCell getHeaderCell(String text, Color color) {
		return getCell(text, HEADER, Cell.ALIGN_CENTER, true, color);
	}

	/**
	 * <p>
	 * Build a header cell with vertical text
	 * </p>
	 * 
	 * @param text Text
	 * @return Table cell
	 */
	public static PdfPCell getHeaderCellVertical(String text, Color color) {
		PdfPCell cell = getCell(text, HEADER, Cell.ALIGN_CENTER, true, color);
		cell.setRotation(90);
		return cell;
	}

	/**
	 * <p>
	 * Build an empty colored cell
	 * </p>
	 * 
	 * @param color  Background color
	 * @param border True to draw the border
	 * @return Table cell
	 */
	public static PdfPCell getColoredCell(Color color, boolean border) {
		return getCell("", NORMAL, Cell.ALIGN_LEFT, border, color);
	}

	/**
	 * <p>
	 * Build decaled cell on the right
	 * </p>
	 * 
	 * @return Table cell
	 */
	public static PdfPCell getDecalCell(String text, Font font, int align, boolean border, Color color) {
		text = text.replaceAll("[\n]", "\n    ");
		return getCell("    " + text, font, align, true, color);
	}

	/**
	 * <p>
	 * Build a PDF table
	 * </p>
	 * 
	 * @param cols Table of column sizes or percents; table size = nbr of columns
	 * @return PDF Table
	 */
	public static PdfPTable getTable(
			int[] cols,
			boolean header,
			int spacingBefore,
			int spacingAfter) throws DocumentException {
		PdfPTable table = new PdfPTable(cols.length);
		table.setWidthPercentage(100);
		table.setWidths(cols);
		if (header)
			table.setHeaderRows(1);
		table.setKeepTogether(false);
		table.setSplitRows(true);
		table.setSplitLate(false);
		table.setSpacingBefore(spacingBefore);
		table.setSpacingAfter(spacingAfter);
		return table;
	}

	/**
	 * <p>
	 * Build a PDF table
	 * </p>
	 * 
	 * @param cols   Table of columns size in percents; table size = nbr of columns
	 * @param header True to display a header line
	 * @return PDF Table
	 */
	public static PdfPTable getTable(int[] cols, boolean header) throws DocumentException {
		return getTable(cols, header, 10, 10);
	}

	/**
	 * <p>
	 * Build a PDF table
	 * </p>
	 * 
	 * @param nbcols Nb of columns
	 * @param header True to display a header line
	 * @return PDF Table
	 */
	public static PdfPTable getTable(int nbcols, boolean header) throws DocumentException {
		int[] c = new int[nbcols];
		for (int i = 0; i < nbcols; i++)
			c[i] = 1;
		return getTable(c, header);
	}

	/**
	 * Generic method to serialize one object current list
	 * in distinct PDF tables : Mandatories, Options, Booleans, Long text/HTML,
	 * images
	 * 
	 * @param d             Document
	 * @param o             Object
	 * @param splitOption   Put the optional fields after the main table
	 * @param splitBoolean  Put the boolean fields after the main table
	 * @param splitLongText Put the large fields after the main table
	 * @param lov           True to insert the lists of values after all
	 */
	public static void insertList(Document d, ObjectDB o,
			boolean splitOption, boolean splitBoolean, boolean splitLongText,
			boolean lov, Color headColor, boolean lovCode)
			throws DocumentException {
		// Main table fields
		List<ObjectField> cols = new ArrayList<>();
		// Optional fields
		List<ObjectField> options = new ArrayList<>();
		// Booleans fields
		List<ObjectField> bools = new ArrayList<>();
		// Large fields
		List<ObjectField> larges = new ArrayList<>();

		for (int i = 0; i < o.getFields().size(); i++) {
			ObjectField f = o.getField(i);
			if (o.isFieldVisible(f, true, true)) {
				ObjectField f2 = o.getRootField(f);
				// Booleans
				if (splitBoolean && f.getType() == ObjectField.TYPE_BOOLEAN) {
					bools.add(f);
				}
				// Large field
				else if (splitLongText
						&& (f.getType() == ObjectField.TYPE_HTML || f.getType() == ObjectField.TYPE_LONG_STRING)) {
					larges.add(f);
				}
				// Optionals
				else if (splitOption
						&& ((f2 != null && !f2.isRequired()) || (f2 == null && !f.isRequired()))) {
					options.add(f);
				} else {
					cols.add(f);
				}
			}
		}

		insertList(d, o, cols, options, bools, larges, lov, headColor, lovCode);
	}

	public static void insertList(Document d, ObjectDB o,
			List<ObjectField> cols, List<ObjectField> options,
			List<ObjectField> bools, List<ObjectField> larges,
			boolean lov, Color headColor, boolean lovCode)
			throws DocumentException {
		boolean bOptions = !Tool.isEmpty(options);
		boolean bBools = !Tool.isEmpty(bools);
		boolean bLarges = !Tool.isEmpty(larges);

		float total = 0f;
		int[] c = new int[cols.size()];
		for (int i = 0; i < cols.size(); i++) {
			ObjectField f = cols.get(i);
			String label = f.getShortDisplay();
			if (Tool.isEmpty(label))
				label = f.getDisplay();

			if (f.getType() == ObjectField.TYPE_IMAGE)
				c[i] = 50;
			else if (!lovCode && (f.getType() == ObjectField.TYPE_ENUM || f.getType() == ObjectField.TYPE_ENUM_MULTI))
				c[i] = 30;
			else
				c[i] = Math.max(5, Math.min(50, Math.max(label.length(), f.getSize())));
			total += c[i];
		}

		List<Paragraph> pLov = new ArrayList<>();
		int col = 0;

		List<String[]> v = o.getCurrentList();
		for (int i = 0; v != null && i < v.size(); i++) {
			PdfPTable table;

			// Redraw Titles
			if (col == 0) {
				table = getTable(c, true, 10, 0);
				for (int j = 0; j < cols.size(); j++) {
					ObjectField f = cols.get(j);
					if (lov && i == 0 &&
							(f.getType() == ObjectField.TYPE_ENUM || f.getType() == ObjectField.TYPE_ENUM_MULTI))
						pLov.add(getLovParagraph(f));

					PdfPCell cell;
					String label = f.getShortDisplay();
					if (Tool.isEmpty(label))
						label = f.getDisplay();

					if (c[j] / total < 0.1f) {
						if (label.length() > f.getSize())
							label = label.substring(0, f.getSize());
						cell = getHeaderCell(label, headColor);
					} else
						cell = getHeaderCell(label, headColor);
					table.addCell(cell);
					col++;
				}
			} else {
				table = getTable(c, false, 0, 0);
			}

			// Record
			String[] val = v.get(i);
			if (o.select(val[0])) {
				for (int j = 0; j < cols.size(); j++) {
					ObjectField f = cols.get(j);

					// Image ?
					if (f.getType() == ObjectField.TYPE_IMAGE) {
						if (!Tool.isEmpty(f.getValue())) {
							try {
								String path = Platform.getDocDir() + "/" + o.getFilePath(f.getValue());
								table.addCell(getCellImage(path, IMAGE_COLOR));
							} catch (Exception e) {
								String fileName = o.getFileName(f.getValue());
								table.addCell(getCell(fileName == null ? "" : fileName, NORMAL, Cell.ALIGN_LEFT, true,
										Color.WHITE));
							}
						}
					} else // Textual
					{
						int align = (f.getType() == ObjectField.TYPE_FLOAT || f.getType() == ObjectField.TYPE_INT
								|| f.getType() == ObjectField.TYPE_BIGDECIMAL)
										? Cell.ALIGN_RIGHT
										: Cell.ALIGN_LEFT;
						table.addCell(getCell(getValue(o, f, lovCode), NORMAL, align, true, Color.WHITE));
					}
				}
				d.add(table);

				// Options table
				if (bOptions) {
					PdfPTable option = null;
					for (int j = 0; j < options.size(); j++) {
						ObjectField f = options.get(j);
						String value = getValue(o, f, lovCode);
						if (!Tool.isEmpty(value)) {
							if (option == null)
								option = getTable(new int[] { 1, 4 }, false, 0, 0);
							option.addCell(getHeaderCell(f.getDisplay(), Cell.ALIGN_LEFT, Color.WHITE));

							if (f.getType() == ObjectField.TYPE_IMAGE) {
								try {
									String path = Platform.getDocDir() + "/" + o.getFilePath(f.getValue());
									option.addCell(getCellImage(path, IMAGE_COLOR));
								} catch (Exception e) {
									option.addCell(getCell("", NORMAL, Cell.ALIGN_LEFT, true, Color.WHITE));
								}
							} else // textual
							{
								option.addCell(getCell(value));
							}
						}
					}
					if (option != null) {
						d.add(option);
						col = 0;
					}
				}

				// Booleans table
				if (bBools) {
					PdfPTable bool = getTable(bools.size(), false);
					bool.setSpacingBefore(0);
					bool.setSpacingAfter(5);
					for (int j = 0; j < bools.size(); j++) {
						ObjectField f = bools.get(j);
						PdfPCell cell = getCell(f.getDisplay(), SMALL, Cell.ALIGN_CENTER, true, Color.WHITE);
						bool.addCell(cell);
					}
					for (int j = 0; j < bools.size(); j++) {
						ObjectField f = bools.get(j);
						bool.addCell(getCell(getValue(o, f, lovCode), NORMAL, Cell.ALIGN_CENTER, true, Color.WHITE));
					}
					d.add(bool);
					col = 0;
				}

				// Large field table
				if (bLarges) {
					PdfPTable large = null;
					for (int j = 0; j < larges.size(); j++) {
						ObjectField f = larges.get(j);
						String value = getValue(o, f, lovCode);
						if (!Tool.isEmpty(value)) {
							if (large == null)
								large = getTable(new int[] { 1 }, false, 0, 0);
							large.addCell(getHeaderCell(f.getDisplay(), Cell.ALIGN_LEFT, Color.WHITE));
							large.addCell(getCell(value, CODE, Cell.ALIGN_LEFT, true, Color.WHITE));
						}
					}
					if (large != null) {
						d.add(large);
						col = 0;
					}
				}
			}
		}

		// Lov in options ?
		for (int j = 0; lov && j < options.size(); j++) {
			ObjectField f = options.get(j);
			if (f.getType() == ObjectField.TYPE_ENUM || f.getType() == ObjectField.TYPE_ENUM_MULTI)
				pLov.add(getLovParagraph(f));
		}

		// List of values
		if (lov && !pLov.isEmpty()) {
			int n = pLov.size() < 4 ? pLov.size() : 3;
			PdfPTable tlov = getTable(n, false);
			for (int i = 0; i < pLov.size(); i++) {
				Paragraph p = pLov.get(i);
				tlov.addCell(p);
			}
			d.add(tlov);
		}
	}

	public static String getValue(ObjectDB o, ObjectField f, boolean lovCode) {
		String value = "";

		if (f.isDocument()) {
			value = o.getFileName(f.getValue());
		} else if (f.getType() == ObjectField.TYPE_BOOLEAN) {
			Boolean b = f.getBoolean();
			value = (b == null) ? "" : (b.booleanValue() ? o.getGrant().T("YES") : o.getGrant().T("NO"));
		} else if (f.getType() == ObjectField.TYPE_FLOAT
				|| f.getType() == ObjectField.TYPE_BIGDECIMAL
				|| f.getType() == ObjectField.TYPE_DATE
				|| f.getType() == ObjectField.TYPE_DATETIME) {
			Grant g = o.getGrant();
			value = Tool.convertServiceToGui(f, f.getValue(), g.getDateFormat(), g.getNumberFormat(), g.getLang());
		} else if (f.getType() == ObjectField.TYPE_OBJECT) {
			boolean bRead = true;
			Grant g = o.getGrant();
			String name = "";
			try {
				String[] obj = f.getValue().split(":");
				name = obj[0];
				String rowId = obj[1];

				bRead = g.accessObject(name);
				if (!bRead)
					g.addAccessObject(name);
				ObjectDB tmp = g.getTmpObject(name);
				tmp.resetFilters();

				value = tmp.getDisplay();
				if (tmp.select(rowId))
					value += ": " + ObjectHooks.getUserKeyLabel(tmp, null);
			} catch (Exception e) {
				value = f.getValue();
			} finally {
				if (!bRead)
					g.delAccessObject(name);
			}
		} else {
			value = lovCode ? f.getValue() : f.getDisplayValue(f.getValue(), "\n");
		}

		if (Tool.isEmpty(value))
			return "";
		value = Tool.replaceText(value, "", EURO);
		return value;
	}

	public static PdfPCell getCellImage(String path, Color bkg) throws Exception {
		Image img = Image.getInstance(path);
		float w = img.getWidth();
		float h = img.getHeight();
		if (Float.floatToRawIntBits(w) == 0 || Float.floatToRawIntBits(h) == 0)
			return new PdfPCell();

		boolean fitImageToCell = (w > 100 || h > 100);
		PdfPCell cell = new PdfPCell(img, fitImageToCell);
		// if (h>w) cell.setFixedHeight(Math.min(h, 100));

		cell.setHorizontalAlignment(Cell.ALIGN_CENTER);
		cell.setPadding(4);
		if (bkg != null)
			cell.setBackgroundColor(bkg); // To show image transparency

		return cell;
	}

	public static Paragraph getLovParagraph(ObjectField f) {
		Paragraph p = new Paragraph();
		p.add(new Phrase(f.getListName() + ":\n", NORMAL));
		for (int i = 0; i < f.getList().getAllItems().size(); i++) {
			EnumItem item = f.getList().getAllItems().get(i);
			p.add(new Phrase(" - " + item.getCode() + ": " + item.getValue() + "\n", NORMAL));
		}
		return p;
	}

	/**
	 * Add HTML paragraph
	 * 
	 * @param html HTML
	 */
	@SuppressWarnings("unchecked")
	public static Paragraph addHTMLParagraph(String html) {
		Paragraph p = new Paragraph();
		StyleSheet st = new StyleSheet();
		try {
			List<Element> l = HTMLWorker.parseToList(new StringReader(html), st);
			for (int i = 0; i < l.size(); i++)
				p.add(l.get(i));
		} catch (IOException e) {
			AppLog.error(PDFTool.class, "addHTMLParagraph", "Unable to add HTML paragraph", e, null);
		}
		return p;
	}

	/**
	 * Insert object form in PDF (flow of field areas)
	 * 
	 * @param d           Document
	 * @param o           Object
	 * @param bkg         Field name background
	 * @param lovCode     Display LOV code or label
	 * @param emptyValues true to display empty values
	 * @throws DocumentException
	 */
	public static void insertForm(Document d, ObjectDB o, Color bkg, boolean lovCode, boolean emptyValues)
			throws DocumentException {
		FieldAreas fas = o.getFieldAreas();
		for (int i = 1; i < fas.size(); i++) // skip 0 = technical fields
		{
			FieldArea fa = fas.get(i);
			if (fa != null && fa.isVisible())
				insertFieldArea(d, o, fa, bkg, lovCode, emptyValues);
		}
	}

	/**
	 * Insert object form in PDF (flow of field areas)
	 * 
	 * @param d       Document
	 * @param o       Object
	 * @param bkg     Field name background
	 * @param lovCode Display LOV code or label
	 * @throws DocumentException
	 */
	public static void insertForm(Document d, ObjectDB o, Color bkg, boolean lovCode)
			throws DocumentException {
		insertForm(d, o, bkg, lovCode, false);
	}

	/**
	 * Insert a field area as a table of 2 columns
	 * 
	 * @param d           Document (optional)
	 * @param o           Object
	 * @param fa          Field area
	 * @param bkg         Field name background
	 * @param lovCode     Display LOV code or label
	 * @param emptyValues true to display empty values
	 * @throws DocumentException
	 */
	public static PdfPTable insertFieldArea(Document d, ObjectDB o, FieldArea fa, Color bkg, boolean lovCode,
			boolean emptyValues)
			throws DocumentException {
		int[] cols = new int[] { 1, 2 };
		boolean visible = false;

		PdfPTable table = getTable(cols, false, 10, 10);
		List<ObjectField> fields = fa.getFields();
		for (int i = 0; i < fields.size(); i++) {
			ObjectField f = fields.get(i);
			if (!emptyValues && f.isEmpty())
				continue;
			if (o.isFieldVisible(f, true, false)) {
				visible = true;
				insertFormCells(table, o, f, bkg, lovCode);
			}
		}
		if (d != null && visible) {
			d.add(new Paragraph(fa.getDisplay(), TITLE2));
			d.add(table);
		}
		return table;
	}

	private static void insertFormCells(PdfPTable table, ObjectDB o, ObjectField f, Color bkg, boolean lovCode) {
		table.addCell(getCell(f.getDisplay(), BOLD, Cell.ALIGN_RIGHT, true, bkg));
		if (f.getType() == ObjectField.TYPE_IMAGE) {
			if (!Tool.isEmpty(f.getValue())) {
				try {
					String path = Platform.getDocDir() + "/" + o.getFilePath(f.getValue());
					table.addCell(getCellImage(path, null));
				} catch (Exception e) {
					String fileName = o.getFileName(f.getValue());
					table.addCell(
							getCell(fileName == null ? "" : fileName, NORMAL, Cell.ALIGN_LEFT, true, Color.WHITE));
				}
			} else
				table.addCell(getCell(""));
		} else {
			table.addCell(getCell(getValue(o, f, lovCode)));
		}
	}

	/**
	 * Get image from DBDoc image
	 * 
	 * @param g     Grant
	 * @param docId Image document ID
	 */
	public static Image getImageFromDBDoc(Grant g, String docId) {
		return getImageFromDBDoc(g, DocumentDB.getDocument(docId, g));
	}

	/**
	 * Get image from from DBDoc image
	 * 
	 * @param g   Grant
	 * @param doc Image document
	 */
	public static Image getImageFromDBDoc(Grant g, DocumentDB doc) {
		try {
			return getImage(doc.getBytes(true));
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Get image from disposition resource image
	 * 
	 * @param g            Grant
	 * @param resourceCode Resource code
	 */
	public static Image getImageFromResource(Grant g, String resourceCode) {
		return getImageFromResource(g, resourceCode, null, null);
	}

	/**
	 * Get image from object resource image
	 * 
	 * @param obj          Object
	 * @param resourceCode Resource code
	 */
	public static Image getImageFromResource(ObjectDB obj, String resourceCode) {
		return getImageFromResource(obj.getGrant(), resourceCode, "ObjectInternal", obj.getId());
	}

	/**
	 * Get image from external object resource image
	 * 
	 * @param ext          External object
	 * @param resourceCode Resource code
	 */
	public static Image getImageFromResource(ExternalObject ext, String resourceCode) {
		return getImageFromResource(ext.getGrant(), resourceCode, "ObjectExternal", ext.getId());
	}

	/**
	 * Get image from resource image
	 * 
	 * @param g            Grant
	 * @param resourceCode Resource code
	 * @param objectName   Object name (typically Disposition, ObjectInternal or
	 *                     ObjectExternal)
	 * @param objectId     Object ID
	 */
	public static Image getImageFromResource(Grant g, String resourceCode, String objectName, String objectId) {
		Image img = null;
		try {
			Resource res = g.getResource(Resource.TYPE_IMAGE, resourceCode, objectName, objectId);
			if (res == null)
				throw new Exception("Resource image not found for code " + resourceCode + " and object " + objectName);
			img = getImage(res.getDocumentContent(g));
		} catch (Exception e) {
			AppLog.error(PDFTool.class, "getImageFromResource",
					"Unable to get image from resource " + resourceCode + " for object " + objectName, e, null);
		}
		return img;
	}

	/**
	 * Get image from static resource
	 * 
	 * @param path Static image resource path
	 */
	public static Image getImageFromStaticResource(String path) {
		try {
			return getImage(Tool.readStaticResource(path));
		} catch (Exception e) {
			AppLog.error(PDFTool.class, "getImageFromStaticResource",
					"Unable to get image from static resource " + path, e, null);
			return null;
		}
	}

	public static Image getImageFromStaticContent(Grant g, String relativePath) {
		return getImageFromStaticContent(relativePath);
	}

	/**
	 * Get image from static content
	 * 
	 * @param relativePath Static content image relative path
	 */
	public static Image getImageFromStaticContent(String relativePath) {
		String path = Platform.getContentDir() + "/" + relativePath;
		return getImage(path);
	}

	/**
	 * Get an image from specified file path
	 * 
	 * @param path Absolute path to image file
	 */
	public static Image getImage(String path) {
		Image img = null;
		try {
			byte[] data = FileTool.readFileAsBytes(path);
			if (data == null)
				throw new Exception("Unable to create image from data");
			img = getImage(data);
		} catch (Exception e) {
			AppLog.error(PDFTool.class, "getImage", "Unable to get image from path " + path, e, null);
		}
		return img;
	}

	/**
	 * Get an image from specified byte array data
	 * 
	 * @param data Image data
	 */
	public static Image getImage(byte[] data) {
		Image img = null;
		try {
			img = Image.getInstance(data);
			if (img == null)
				throw new Exception("Unable to create image from data");
		} catch (Exception e) {
			AppLog.error(PDFTool.class, "getImage", "Unable to get image from data", e, null);
		}
		return img;
	}

	/**
	 * Insert one image into the document
	 * 
	 * @param d         PDF document
	 * @param img       Image
	 * @param canRotate True to rotate the image when larger than doc width
	 * @param align     Image alignment ex: Image.MIDDLE
	 */
	public static void insertImage(Document d, Image img, boolean canRotate, int align) {
		try {
			float pageWidth = d.getPageSize().getWidth() - d.leftMargin() - d.rightMargin();
			float pageHeight = d.getPageSize().getHeight() - d.topMargin() - d.bottomMargin();

			img.setAlignment(align);

			float w = img.getWidth() * IMAGE_SCALE;
			float h = img.getHeight() * IMAGE_SCALE;
			img.scaleAbsolute(w, h);

			// Image fits in the page ?
			if (w <= pageWidth && h < pageHeight) {
				d.add(img);
				return;
			}

			// Rotate if larger than height ?
			if (canRotate && w > h) {
				img.setRotationDegrees(90f);
				// swap sizes
				float tmp = w;
				w = h;
				h = tmp;
			}

			// Scale ratio in the 2 directions
			float px = 100 * pageWidth / w;
			float py = 100 * pageHeight / h;

			// Fits only in height direction ?
			if (px < 100 && py >= 100)
				img.scalePercent(px * IMAGE_SCALE);
			// Fits only in width direction ?
			else if (py < 100 && px >= 100)
				img.scalePercent(py * IMAGE_SCALE);
			// Doesn't fit in any direction ?
			else if (px < 100 && py < 100)
				img.scalePercent(Math.min(px, py) * IMAGE_SCALE);

			d.add(img);
		} catch (Exception e) {
			AppLog.error(PDFTool.class, "insertImage", "Could not insert image into document", e, null);
		}
	}

	/**
	 * Create and insert the Table of Contents
	 * 
	 * @param d       Source document
	 * @param docPath Path to the PDF file
	 * @param tocPage TOC page number
	 * @param depth   TOC depth (chapter=1, section=2, sub-section=3...)
	 * @param event   Document page event
	 * @return A new document with the inserted TOC
	 */
	private static Document insertToc(PDFInterface pdf, Document d, String docPath,
			int tocPage, int depth, PDFEvent event) {
		Document copy = null;
		try {
			// Doc reader / nb pages
			File fileDoc = new File(docPath);
			byte[] b = FileTool.readFileAsBytes(fileDoc);
			PdfReader docReader = new PdfReader(b);
			// int nbDocPages = docReader.getNumberOfPages();

			// List of bookmarks (chapter/section)
			@SuppressWarnings("unchecked")
			ArrayList<Map<String, Object>> bookmarks = new ArrayList<Map<String, Object>>(
					SimpleBookmark.getBookmark(docReader));
			docReader.close();

			// Generate the TOC in a separated doc
			String tocPath = docPath + "_toc.pdf";
			File fileToc = new File(tocPath);
			Document dtoc = open(PageSize.A4, tocPath, true, event);
			if (dtoc == null)
				throw new Exception("Unable to open document for " + tocPath);
			PdfPTable tableToc = getTableToc(bookmarks, depth, 0, 0);
			dtoc.add(tableToc);
			dtoc.close();

			// TOC nb pages
			PdfReader tocReader = new PdfReader(FileTool.readFileAsBytes(fileToc));
			int tocNbPages = tocReader.getNumberOfPages();
			tocReader.close();
			if (!fileToc.delete())
				AppLog.warning(PDFTool.class, "insertToc", "Unable to delete TOC file: " + fileToc.getAbsolutePath(),
						null, null);

			// Regenerate the TOC with shifted pages
			tableToc = getTableToc(bookmarks, depth, tocPage, tocNbPages);

			// Rebuild the doc merging all (second scan)
			String copyPath = docPath + "_copy.pdf";
			event.chapterIndex = 1;
			event.sectionIndex = 2;
			copy = open(PageSize.A4, copyPath, true, event);
			if (copy == null)
				throw new Exception("Unable to open document for " + copyPath);

			pdf.setDocInfos(copy);
			pdf.buildHeadPages(copy);
			copy.newPage();
			copy.add(tableToc);
			pdf.buildContent(copy);
			pdf.buildFootPages(copy);

			close(copy);

			// Switch
			if (!fileDoc.delete())
				throw new IOException("Unable to delete file: " + fileDoc.getAbsolutePath());

			File fileCopy = new File(copyPath);
			Files.move(fileCopy.toPath(), fileDoc.toPath()); // Don't user renameTo here
		} catch (Exception e) {
			AppLog.error(PDFTool.class, "insertToc", e.getMessage(), e, null);
		}
		return copy;
	}

	private static PdfPTable getTableToc(java.util.List<Map<String, Object>> list,
			int depth, int tocPage, int tocNbPages) throws Exception {
		PdfPTable toc = getTable(new int[] { 10, 1 }, false);
		return getTableTocRecur(toc, list, tocPage, tocNbPages, depth, 0);
	}

	@SuppressWarnings("unchecked")
	private static PdfPTable getTableTocRecur(PdfPTable toc, java.util.List<Map<String, Object>> list,
			int tocPage, int tocNbPages, int depth, int level) {
		if (list == null)
			return toc;
		if (level >= depth)
			return toc;

		String spacing = "";
		for (int i = 0; i < level; i++)
			spacing = spacing + "  ";

		for (Map<String, Object> bookmark : list) {
			String info = (String) bookmark.get("Page");
			int p = Tool.parseInt(info.substring(0, info.indexOf(' ')));
			if (p >= tocPage)
				p += tocNbPages;

			Font font = TITLE1;
			if (level == 1)
				font = TITLE2;
			else if (level == 2)
				font = HEADER;

			PdfPCell title = getCell(spacing + bookmark.get("Title"), font, Cell.ALIGN_LEFT, false, Color.WHITE);
			PdfPCell page = getCell(String.valueOf(p), font, Cell.ALIGN_RIGHT, false, Color.WHITE);

			if (level == 0) {
				title.setPaddingBottom(2);
				title.setBorderWidthBottom(1);
				page.setBorderWidthBottom(1);
			}
			toc.addCell(title);
			toc.addCell(page);

			// Recursif sur les fils
			java.util.List<Map<String, Object>> kids = (java.util.List<Map<String, Object>>) bookmark.get("Kids");
			if (kids != null)
				getTableTocRecur(toc, kids, tocPage, tocNbPages, depth, level + 1);
		}
		return toc;
	}

	/**
	 * PDF events, manage specific header and footer
	 */
	public class PDFEvent extends PdfPageEventHelper {
		// Image
		public Image m_headerImage = null;
		public Image m_footerImage = null;

		// Display page nums
		public boolean m_pagine = false;

		// Same right/left margin
		public int m_margin = 0;
		// Top margin
		public int m_margin_top = 0;
		// Bottom margin
		public int m_margin_bottom = 0;

		// auto-increment
		public int chapterIndex = 1;
		public int sectionIndex = 2;

		// This is the contentbyte object of the writer
		protected PdfContentByte m_cb;

		// we will put the final number of pages in a template
		protected PdfTemplate m_template;

		// this is the BaseFont we are going to use for the header / footer
		protected BaseFont m_bf = null;

		// Initialisation
		@Override
		public void onOpenDocument(PdfWriter writer, Document document) {
			try {
				m_bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
				m_cb = writer.getDirectContent();
				m_template = m_cb.createTemplate(50, 20);
			} catch (Exception e) {
				// Silent
			}
		}

		@Override
		public void onStartPage(PdfWriter writer, Document document) {
			try {
				float h = document.getPageSize().getHeight();

				if (m_headerImage != null) {
					float x = m_headerImage.getWidth() * IMAGE_SCALE;
					float y = m_headerImage.getHeight() * IMAGE_SCALE;
					m_cb.addImage(m_headerImage,
							x, 0, 0, y,
							m_margin, h - m_margin_top + 5);
				}

				if (m_footerImage != null) {
					float x = m_footerImage.getWidth() * IMAGE_SCALE;
					float y = m_footerImage.getHeight() * IMAGE_SCALE;
					m_cb.addImage(m_footerImage,
							x, 0, 0, y,
							m_margin, (float) m_margin - 5);
				}
			} catch (Exception e) {
				AppLog.error(getClass(), "onStartPage", "Image", e, null);
			}
		}

		@Override
		public void onEndPage(PdfWriter writer, Document document) {
			if (m_pagine) {
				// Page courante
				int pageN = writer.getPageNumber();
				String text = "Page " + pageN + " / ";

				m_cb.beginText();
				m_cb.setFontAndSize(m_bf, 8);
				m_cb.setTextMatrix(495, (float) m_margin + 5);
				m_cb.showText(text);
				m_cb.endText();

				// Zone total des pages
				float len = m_bf.getWidthPoint(text, 8);
				m_cb.addTemplate(m_template, 495 + len, (float) m_margin + 5);
			}
		}

		// Fermeture pour le nombre de pages
		@Override
		public void onCloseDocument(PdfWriter writer, Document document) {
			if (m_pagine) {
				int nb = writer.getPageNumber() - 1;
				m_template.beginText();
				m_template.setFontAndSize(m_bf, 8);
				m_template.showText(String.valueOf(nb));
				m_template.endText();
			}
		}
	}

	/**
	 * Concat PDF
	 * 
	 * @param out Output filename
	 * @param in1 Input filename 1
	 * @param in2 Input filename 2
	 */
	public static void concat(String out, String in1, String in2) throws Exception {
		concat(new File(out), in1 == null ? null : new File(in1), in2 == null ? null : new File(in2));
	}

	/**
	 * Concat PDF
	 * 
	 * @param out Output file
	 * @param in1 Input file 1
	 * @param in2 Input file 2
	 */
	public static void concat(File out, File in1, File in2) throws Exception {
		try (
				FileOutputStream fos = new FileOutputStream(out);
				FileInputStream fis1 = in1 == null ? null : new FileInputStream(in1);
				FileInputStream fis2 = in2 == null ? null : new FileInputStream(in2);) {
			concat(fos, fis1, fis2);
		}
	}

	/**
	 * Concat PDF
	 * 
	 * @param in1 Input file content 1
	 * @param in2 Input file content 2
	 * @return Concatenated file content
	 */
	public static byte[] concat(byte[] in1, byte[] in2) throws Exception {
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			concat(bos, in1 == null ? null : new ByteArrayInputStream(in1),
					in2 == null ? null : new ByteArrayInputStream(in2));
			return bos.toByteArray();
		}
	}

	/**
	 * Concat PDF
	 * 
	 * @param os  Output stream
	 * @param is1 Input stream 1
	 * @param is2 Input stream 2
	 */
	public static void concat(OutputStream os, InputStream is1, InputStream is2) throws Exception {
		PdfCopyFields copy = new PdfCopyFields(os);
		if (is1 != null)
			copy.addDocument(new PdfReader(is1));
		if (is2 != null)
			copy.addDocument(new PdfReader(is2));
		copy.close();
	}

	/**
	 * PDF interface
	 */
	public interface PDFInterface {
		/** Set the doc infos */
		public void setDocInfos(Document d) throws DocumentException;

		/** Build the first page(s) */
		public void buildHeadPages(Document d) throws DocumentException;

		/** Build the core document */
		public void buildContent(Document d) throws DocumentException;

		/** Build the last page(s) */
		public void buildFootPages(Document d) throws DocumentException;
	}

	/**
	 * Export list to PDF
	 * 
	 * @param obj      Object
	 * @param rows     Data records
	 * @param mode     full or list
	 * @param response optional HTTP response
	 * @return temp file name if response is null
	 */
	public static String export(ObjectDB obj, List<String[]> rows, String mode, HttpServletResponse response) {
		final File tmpFile = response == null ? FileTool.getRandomFile(Platform.getExportDir(), "tmp_exportpdf", "pdf")
				: null;
		final File waitFile = tmpFile != null ? new File(Platform.getExportDir() + "/" + tmpFile.getName().substring(4))
				: null;

		Runnable r = new Runnable() {
			@Override
			public void run() {
				try (OutputStream out = response != null ? response.getOutputStream() : new FileOutputStream(tmpFile)) {
					export(obj, rows, mode, out);

					if (obj.getParameter(ImportExportTool.EXPORT_STOPPED) == null) {
						// Move the file in export directory, to be loaded by the UI
						if (waitFile != null) {
							if (!tmpFile.renameTo(waitFile)) // ZZZ they are in a same directory
								throw new IOException("Unable to rename " + tmpFile.getAbsolutePath() + " to "
										+ waitFile.getAbsolutePath());

							obj.setParameter(ImportExportTool.EXPORT_PROGRESS, "ok");
						}
					} else if (tmpFile != null && !tmpFile.delete()) {
						AppLog.warning(PDFTool.class, "export",
								"Unable to delete temporary file: " + tmpFile.getAbsolutePath(), null, obj.getGrant());
					}
				} catch (IOException e) {
					AppLog.log("ECORED0001", PDFTool.class, "export", obj.getName(), e);
					if (tmpFile != null && !tmpFile.delete())
						AppLog.warning(PDFTool.class, "export",
								"Unable to delete temporary file: " + tmpFile.getAbsolutePath(), null, obj.getGrant());
				}
			}
		};

		if (waitFile != null) {
			JobQueue.push("Simplicite-exportPDF-" + obj.getName(), r);
			return waitFile.getName();
		}

		r.run();
		return null;
	}

	/**
	 * Export list to PDF
	 * 
	 * @param obj  Object
	 * @param rows Data records
	 * @param mode full or list
	 * @param out  Output stream
	 */
	public static void export(ObjectDB obj, List<String[]> rows, String mode, OutputStream out) {
		Document d = null;
		String[] values = obj.getValues();
		String[] old = obj.getOldValues();
		try {
			Grant g = obj.getGrant();
			ObjectContextWeb ctx = new ObjectContextWeb();
			ctx.export = true;
			ctx.apply(obj);

			d = new Document(PageSize.A4.rotate(), 35, 35, 35, 35);

			d.addTitle(obj.getDisplay());
			d.addSubject(obj.getDisplay());
			d.addAuthor(Globals.getPlatformVendor());
			d.addCreator(Globals.getPlatformName());
			d.addCreationDate();

			Font m_title = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD, Color.BLACK);
			Font m_normal = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL, Color.BLACK);
			Font m_head = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.BOLD, Color.BLACK);

			PdfWriter.getInstance(d, out);
			d.open();

			Phrase sTitle = new Phrase(obj.getDisplay(), m_title);
			d.add(sTitle);

			List<ObjectField> fields = CSVTool.getVisibleColumns(obj, mode, false);

			Table table = new Table(fields.size(), 10);
			table.setPadding(2);
			table.setSpacing(0);
			table.setWidth(100);
			table.setBorderWidth(1);
			table.setBorderWidth(0);
			List<ObjectField> fs = new ArrayList<>();
			for (ObjectField f : fields) {
				if (f.isExportable()) {
					fs.add(f);
					String label = f.getShortDisplay();
					if (Tool.isEmpty(label))
						label = f.getDisplay();

					Cell cell = new Cell(new Phrase(label, m_head));
					cell.setBackgroundColor(Color.GRAY);
					cell.setHorizontalAlignment(Cell.ALIGN_CENTER);
					table.addCell(cell);
				}
			}
			table.endHeaders();

			// Insert data
			table.setBackgroundColor(Color.WHITE);
			for (int i = 0; i < rows.size(); i++) {
				obj.setValues(rows.get(i), false);
				for (ObjectField f : fs) {
					String val = f.getExportValue(g, f.getValue());

					if (f.getType() == ObjectField.TYPE_FLOAT || f.getType() == ObjectField.TYPE_INT
							|| f.getType() == ObjectField.TYPE_BIGDECIMAL)
						table.setAlignment(Element.ALIGN_RIGHT);
					else
						table.setAlignment(Element.ALIGN_LEFT);

					if (f.getType() == ObjectField.TYPE_DATE)
						val = Tool.toFormattedDate(val, g.getDateFormat());
					else if (f.getType() == ObjectField.TYPE_DATETIME)
						val = Tool.toFormattedDatetime(val, g.getDateFormat());

					table.addCell(new Phrase(val, m_normal));
				}
				if (obj.getParameter(ImportExportTool.EXPORT_STOPPED) != null)
					break;

				obj.setParameter(ImportExportTool.EXPORT_PROGRESS, String.valueOf(i + 1));
			}
			d.add(table);
			d.close();
			obj.setParameter(ImportExportTool.EXPORT_PROGRESS, "ok");
		} catch (Exception e) {
			AppLog.log("ECORED0001", PDFTool.class, "export", obj.getName(), e);
			d = null;
		} finally {
			obj.setValues(values);
			obj.setOldValues(old);
		}
	}

	/**
	 * Substitute strings in a PDF document file
	 * 
	 * @param in            Input PDF document file path
	 * @param out           Output PDF document file path
	 * @param substitutions Map of strings to substitute
	 */
	public static void substitute(String in, String out, Map<String, String> substitutions) throws IOException {
		try (PDDocument document = Loader.loadPDF(new File(in))) {
			substitute(document, substitutions).save(new File(out));
		}
	}

	/**
	 * Substitute strings in a PDF document file
	 * 
	 * @param in            Input PDF document file
	 * @param out           Output PDF document file with strings substituted
	 * @param substitutions Map of strings to substitute
	 */
	public static void substitute(File in, File out, Map<String, String> substitutions) throws IOException {
		try (PDDocument document = Loader.loadPDF(in)) {
			substitute(document, substitutions).save(out);
		}
	}

	/**
	 * Substitute strings in a PDF document
	 * 
	 * @param in            PDF document input stream
	 * @param out           PDF document output stream with strings substituted
	 * @param substitutions Map of strings to substitute
	 */
	public static void substitute(InputStream in, OutputStream out, Map<String, String> substitutions)
			throws IOException {
		try (PDDocument document = Loader.loadPDF(Tool.getBytes(in))) {
			substitute(document, substitutions).save(out);
		}
	}

	/**
	 * Substitute strings in a PDF document as byte array
	 * 
	 * @param data          Input PDF document byte array data
	 * @param substitutions Map of strings to substitute
	 * @return Output PDF document with strings substituted as byte array
	 */
	public static byte[] substitute(byte[] data, Map<String, String> substitutions) throws IOException {
		try (
				PDDocument document = Loader.loadPDF(data);
				ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			substitute(document, substitutions).save(bos);
			return bos.toByteArray();
		}
	}

	/**
	 * Substitute strings in a PDF document
	 * 
	 * @param document      PDF document
	 * @param substitutions Map of strings to substitute
	 * @return Document with strings substituted
	 */
	@SuppressWarnings("deprecation")
	public static PDDocument substitute(PDDocument document, Map<String, String> substitutions) throws IOException {
		if (document.isEncrypted())
			throw new IOException("Modifying encrypted PDF document is not possible");

		for (PDPage page : document.getPages()) {
			PDFStreamParser parser = new PDFStreamParser(page);
			List<Object> tokens = parser.parse();

			for (int j = 0; j < tokens.size(); j++) {
				Object next = tokens.get(j);
				if (next instanceof Operator) {
					Operator op = (Operator) next;

					String ps = "";
					int pr = 0;

					for (String key : substitutions.keySet()) {
						String val = substitutions.get(key);

						// Tj and TJ are the two operators that display strings in a PDF
						if (op.getName().equals("Tj")) {
							COSString prev = (COSString) tokens.get(j - 1);
							String s = StringUtils.replace(prev.getString(), key, val);
							prev.setValue(s.getBytes());
						} else if (op.getName().equals("TJ")) {
							COSArray prev = (COSArray) tokens.get(j - 1);
							for (int k = 0; k < prev.size(); k++) {
								Object o = prev.getObject(k);
								if (o instanceof COSString) {
									COSString cs = (COSString) o;
									String s = cs.getString();
									if (j == pr) {
										ps += s;
									} else {
										pr = j;
										ps = s;
									}
								}
							}

							if (ps.indexOf(key) >= 0) {
								String v = StringUtils.replace(ps, key, val);
								((COSString) prev.getObject(0)).setValue(v.getBytes());

								int total = prev.size() - 1;
								for (int k = total; k > 0; k--)
									prev.remove(k);
							}
						}
					}
				}
			}

			// now that the tokens are updated we will replace the page content stream.
			PDStream updatedStream = new PDStream(document);
			OutputStream out = updatedStream.createOutputStream(COSName.FLATE_DECODE);
			ContentStreamWriter tokenWriter = new ContentStreamWriter(out);
			tokenWriter.writeTokens(tokens);
			out.close();
			page.setContents(updatedStream);
		}

		return document;
	}

	/**
	 * Get text content from PDF file
	 * 
	 * @param file PDF file
	 * @return Text content
	 */
	public static String getText(File file) {
		try (PDDocument doc = Loader.loadPDF(file)) {
			return new PDFTextStripper().getText(doc);
		} catch (IOException e) {
			return "Error: " + e.getMessage();
		}
	}

	/**
	 * Get text content from PDF data
	 * 
	 * @param data PDF data
	 * @return Text content
	 */
	public static String getText(byte[] data) {
		try (PDDocument doc = Loader.loadPDF(data)) {
			return new PDFTextStripper().getText(doc);
		} catch (IOException e) {
			return "Error: " + e.getMessage();
		}
	}
}
