package picoded.fileUtils;

import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/// PDFGenerator is a utility class to covert either a HTML string or a HTML file to a PDF file
///
/// ### Example Usage
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
///
/// // covert a HTML file to a pdf file
///
/// String pdfOutputFile = //test-files/temp/fileUtils//pdfFile.pdf
/// String htmlInputFile = //test-files/fileUtils/PDFGenerator/pdf-generator-html.html
/// PDFGenerator.generatePDFfromHTML(pdfOutputFile, htmlInputFile);
///
/// // covert a HTML string to a pdf file
///
/// String pdfOutputFile = //test-files/temp/fileUtils//pdfFile.pdf
/// String htmlString = "<table><tr><th>Cell A</th></tr><tr><td>Cell Data</td></tr></table>"
/// PDFGenerator.generatePDFfromRawHTML(pdfOutputFile, htmlString);
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
///
public class PDFGenerator {
	
	/// Generates a pdf file given the HTML file path
	///
	/// @param   pdfFile         pdf file path string
	/// @param   htmlFilePath    HTML file path string
	///
	/// @returns  true if the HTML file is converted and saved in a pdf file
	public static boolean generatePDFfromHTMLfile(String pdfFile, String htmlFilePath) {
		OutputStream outputStream = null;
		try {
			String url2 = new File(htmlFilePath).toURI().toString();
			outputStream = new FileOutputStream(pdfFile);
			ITextRenderer renderer = new ITextRenderer();
			//renderer.setDocument(new File(url2).toURI().toString());
			renderer.setDocument(new File(htmlFilePath));
			renderer.writeNextDocument();
			renderer.layout();
			
			// generate pdf
			renderer.createPDF(outputStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return true;
	}
	
	/// Generates a pdf file given the RAW html string
	///
	/// @param   outputpdfpath    pdf file path string
	/// @param   rawHtml          raw HTML string
	///
	/// @returns  true if the HTML raw string is converted and saved in a pdf file.
	public static boolean generatePDFfromRawHTML(String outputpdfpath, String rawHtml) {
		OutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(outputpdfpath);
			ITextRenderer renderer = new ITextRenderer();
			renderer.setDocumentFromString(rawHtml);
			renderer.layout();
			
			// generate pdf
			renderer.createPDF(outputStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return true;
	}
}