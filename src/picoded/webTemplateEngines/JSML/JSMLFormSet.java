package picoded.webTemplateEngines.JSML;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;

import picoded.enums.HttpRequestType;
import picoded.servlet.BasePage;
import picoded.servlet.RequestMap;
import picoded.servletUtil.FileServlet;
import picoded.struct.UnsupportedDefaultMap;

///
/// JSML Form set, which handles all the various utility and servlet functionality
///
public class JSMLFormSet implements UnsupportedDefaultMap<String, JSMLForm> {
	
	///////////////////////////////////////////////////////
	//
	// Instance variables 
	//
	///////////////////////////////////////////////////////
	
	/// The form set folder
	protected File formSetFolder = null;
	
	/// The form set URI base
	protected String formSetURI = null;
	
	/// The BasePage underneath
	protected BasePage basePage = null;
	
	///////////////////////////////////////////////////////
	//
	// Constructor
	//
	///////////////////////////////////////////////////////
	
	/// Blank constructor (will crash if not configured properly)
	public JSMLFormSet() {
		//Does nothing
	}
	
	/// BasePage constructor
	public JSMLFormSet(BasePage inPage) {
		basePage = inPage;
		formSetFolder = new File(inPage.getJsmlTemplatePath());
		formSetURI = inPage.getContextURI();
		// Validate
		validateFormSetFolder();
	}
	
	/// Constructor with formSetFolder, and formSetURI
	public JSMLFormSet(File folderPath, String folderUri) {
		formSetFolder = folderPath;
		formSetURI = folderUri;
		// Validate
		validateFormSetFolder();
	}
	
	/// Constructor with formSetFolder string path, and formSetURI 
	public JSMLFormSet(String folderPath, String folderUri) {
		formSetFolder = new File(folderPath);
		formSetURI = folderUri;
		// Validate
		validateFormSetFolder();
	}
	
	/// Utility function called by init, or paramtirized startup. To validate basePath
	public void validateFormSetFolder() {
		// Validate base path.
		if (formSetFolder == null) {
			throw new RuntimeException("JSMLFormSet init param 'formSetFolder' is required.");
		}
		
		if (!formSetFolder.exists()) {
			throw new RuntimeException("JSMLFormSet init param 'formSetFolder' ("
				+ formSetFolder.toString() + ") does actually not exist in file system.");
		}
		
		if (!formSetFolder.isDirectory()) {
			throw new RuntimeException("JSMLFormSet init param 'formSetFolder' value ("
				+ formSetFolder.toString() + ") is actually not a directory in file system.");
		}
		
		if (!formSetFolder.canRead()) {
			throw new RuntimeException("JSMLFormSet init param 'formSetFolder' value ("
				+ formSetFolder.toString() + ") is actually not readable in file system.");
		}
		
	}
	
	///////////////////////////////////////////////////////
	//
	// Utility functions
	//
	///////////////////////////////////////////////////////
	
	protected List<File> foldersInFormSet() {
		if (formSetFolder == null) {
			validateFormSetFolder();
		}
		List<File> ret = new ArrayList<File>();
		
		for (File entry : formSetFolder.listFiles()) {
			if (entry.getName().startsWith(".")) {
				continue; //skip private folders
			}
			
			if (entry.isDirectory()) {
				ret.add(entry);
			}
		}
		return ret;
	}
	
	/// Cache for nameList
	protected List<String> _folderNameListCache = null;
	
	/// Returns the list of names, found in the set. This is equivalent to .keySet()
	public List<String> nameList() {
		if (_folderNameListCache != null) {
			return _folderNameListCache;
		}
		
		List<String> ret = new ArrayList<String>();
		
		for (File entry : foldersInFormSet()) {
			ret.add(entry.getName());
		}
		
		return (_folderNameListCache = ret);
	}
	
	/// Deletes the files from the temp folder older than one hour.
	public void clearTempFiles() {
		long time = 60 * 60; // one hour
		clearTempFiles(time);
	}
	
	/// Deletes the files from the temp folder older than specified time.
	public void clearTempFiles(long seconds) {
		for (String name : keySet()) {
			get(name).clearTempFilesOlderThenGivenAgeInSeconds(seconds);
		}
	}
	
	///////////////////////////////////////////////////////
	//
	// Map operation functions
	//
	///////////////////////////////////////////////////////
	
	/// Returns the set of JSMLForm names
	public Set<String> keySet() {
		return new HashSet<String>(nameList());
	}
	
	/// Gets the JSMLForm 
	public JSMLForm get(Object key) {
		if (!(nameList().contains(key))) {
			return null;
		}
		String keyStr = key.toString();
		JSMLForm newForm = new JSMLForm(new File(formSetFolder, keyStr), formSetURI + "/" + keyStr
			+ "/", null);
		newForm.setFormSet(this);
		
		newForm.getDefinition();
		return newForm;
	}
	
	///////////////////////////////////////////////////////
	//
	// Servlet Utility functions
	//
	///////////////////////////////////////////////////////
	
	/// Cached FileServlet
	protected FileServlet _resourseFileServlet = null;
	
	/// Returns the File servlet
	public FileServlet resourseFileServlet() {
		if (_resourseFileServlet != null) {
			return _resourseFileServlet;
		}
		
		if (formSetFolder == null) {
			validateFormSetFolder();
		}
		
		return (_resourseFileServlet = new FileServlet(formSetFolder));
	}
	
	/// Process the file serving request for resoruces
	protected void processResourcesFileRequest( //
		HttpServletRequest servletRequest, //
		HttpServletResponse servletResponse, //
		boolean headersOnly //
	) {
		try {
			resourseFileServlet().processRequest(servletRequest, servletResponse, headersOnly);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/// Derive form data from request 
	@SuppressWarnings("unchecked")
	public Map<String, Object> formDataFromRequest(BasePage page, JSMLForm form) {
		
		RequestMap reqMap = page.requestParameters();
		Map<String, Object> formDataMap = null;
		
		// Check if its dummy data mode
		if (reqMap.getBoolean("dummy-data", false)) {
			formDataMap = form.getDummyData();
		} else {
			formDataMap = form.getBlankData();
		}
		
		// Normalize / delink map data
		if (formDataMap != null) {
			formDataMap = new HashMap<String, Object>(formDataMap);
		} else {
			formDataMap = new HashMap<String, Object>();
		}
		
		// Convert parameter map to data map
		Map<String, Object> processedRequestMap = form
			.requestParamsToParamsMap((Map<String, Object>) (Object) reqMap);
		formDataMap.putAll(processedRequestMap);
		
		if (formDataMap == null) {
			formDataMap = new HashMap<String, Object>();
		}
		
		return formDataMap;
	}
	
	/// Process the full JSML Form Collection servlet request
	public void processJSMLFormCollectionServlet(BasePage page) {
		processJSMLFormCollectionServlet(page, page.requestWildcardUriArray());
	}
	
	/// Process the full JSML Form Collection servlet request
	public void processJSMLFormCollectionServlet(BasePage page, String[] requestWildcardUri) {
		try {
			if (requestWildcardUri == null) {
				requestWildcardUri = new String[0];
			}
			
			String indexPage = "index.html";
			
			// Load index page
			if (requestWildcardUri == null || requestWildcardUri.length <= 0
				|| indexPage.equalsIgnoreCase(requestWildcardUri[0])) {
				
				File indexFile = new File(formSetFolder, "index.html");
				String indexFileStr = null;
				
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("JSMLnames", nameList());
				
				if (!(indexFile.exists()) || !(indexFile.canRead()) || !(indexFile.isFile())) {
					// 404 error if file not found
					page.getHttpServletResponse().sendError(HttpServletResponse.SC_NOT_FOUND);
					return;
				}
				
				indexFileStr = FileUtils.readFileToString(indexFile, "UTF-8");
				
				if (indexFileStr == null) {
					// 404 error if file not found
					page.getHttpServletResponse().sendError(HttpServletResponse.SC_NOT_FOUND);
					return;
				}
				
				page.getWriter().println(page.JMTE().parseTemplate(indexFileStr, params));
				return;
			}
			
			// Get the JSMLForm name
			String formName = requestWildcardUri[0];
			if (nameList().contains(formName)) {
				// JSMLForm
				JSMLForm form = get(formName);
				
				// Note if arr is larger then 2, its normally a file request
				if (requestWildcardUri.length <= 2) {
					// Get the form request mode
					String reqMode = null;
					Map<String, Object> formParams = null;
					
					// Get request mode parameters
					if (requestWildcardUri.length == 2) {
						reqMode = requestWildcardUri[1];
					}
					
					// Index mode
					if (reqMode == null || reqMode.length() <= 0 || reqMode.equalsIgnoreCase("input")) {
						formParams = formDataFromRequest(page, form);
						
						String formResult = form.generateHTML(formParams, false).toString();
						page.getWriter().println(page.JMTE().parseTemplate(formResult, formParams));
						return;
					}
					
					// Display mode
					if (reqMode.equalsIgnoreCase("display")) {
						formParams = formDataFromRequest(page, form);
						String formResult = form.generateHTML(formParams, true).toString();
						page.getWriter().println(page.JMTE().parseTemplate(formResult, formParams));
						return;
					}
					
					// PDF mode
					if (reqMode.equalsIgnoreCase("pdf")) {
						formParams = formDataFromRequest(page, form);
						byte[] pdfData = form.generatePDF(page, formParams, page.JMTE());
						HttpServletResponse response = page.getHttpServletResponse();
						
						response.setContentType("application/pdf");
						response.addHeader("Content-Disposition", "filename=" + formName + ".pdf");
						response.setContentLength(pdfData.length);
						
						page.getOutputStream().write(pdfData);
						return;
					}
					
					if (reqMode.equalsIgnoreCase("fullPdfHtml")) {
						formParams = formDataFromRequest(page, form);
						String fullPdfHtml = form.getFullPdfHTML(page, formParams);
						
						page.getWriter().println("-----------------");
						page.getWriter().println(fullPdfHtml);
						page.getWriter().println("-----------------");
						
						return;
					}
					
					// PDF link test mode
					if (reqMode.equalsIgnoreCase("pdfTest")) {
						formParams = formDataFromRequest(page, form);
						String[] pdfTestResults = form.getPDFLinkTest(page, formParams);
						
						for (String str : pdfTestResults) {
							page.getWriter().println(str);
							page.getWriter().println("-----------------");
						}
						
						return;
					}
				}
			}
			
			String reqStr = String.join("/", requestWildcardUri);
			// Security measure?
			// if( reqStr != null && reqStr.contains("/tmp") ) {
			// 	// 404 error if file not found
			// 	page.getHttpServletResponse().sendError(HttpServletResponse.SC_NOT_FOUND);
			// 	return;
			// }
			
			if (reqStr == null || reqStr.isEmpty()) {
				// 404 error if file not found
				page.getHttpServletResponse().sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			
			// Fallsback into File Servlet
			resourseFileServlet().processRequest( //
				page.getHttpServletRequest(), //
				page.getHttpServletResponse(), //
				page.requestType() == HttpRequestType.HEAD, //
				reqStr);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
