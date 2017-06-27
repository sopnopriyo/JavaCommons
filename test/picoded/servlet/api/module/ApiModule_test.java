package picoded.servlet.api.module;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.junit.*;

import picoded.servlet.util.EmbeddedServlet;
import picoded.TestConfig;
import picoded.servlet.*;
import picoded.servlet.api.*;
import picoded.servlet.api.module.*;
import picoded.set.*;
import picoded.web.*;
import picoded.conv.*;
import picoded.dstack.*;
import picoded.dstack.struct.simple.*;

///
/// Basic template to build and extend all ApiModule test cases
///
public class ApiModule_test {
	
	//-------------------------------------------------------------------------
	//
	// Internal vars
	//
	//-------------------------------------------------------------------------

	//
	// The servlet test vars to use
	//
	int testPort = 0; //Test port to use
	CorePage testPage = null;
	EmbeddedServlet testServlet = null; //Test servlet to use
	
	/// The base URL to test from, used this in the respective calls
	String testBaseUrl = null;

	/// The testing cookie jar, to keep between request (in a single test)
	Map<String,String[]> cookieJar = new HashMap<String,String[]>();

	//
	// The test folders to use
	//
	// File testFolder = new File("./test-files/test-specific/servlet/api/module/ApiModule/");
	//
	
	//-------------------------------------------------------------------------
	//
	// Testing servlet
	//
	//-------------------------------------------------------------------------

	/// The testing API Module servlet template
	public static class ApiModuleTestServlet extends CoreApiPage {
		
		/// The CommonStack implementation 
		public CommonStack testStack = null;

		/// The api builder test vars
		public ApiModule testModule = null;

		/// To overwrite when replacing the default Stack implementation from struct
		///
		/// @param   Base static CorePage used
		///
		/// @return  The DStack.CommonStack implementation, used for module setup
		public CommonStack stackSetup() {
			return new StructSimpleStack();
		}

		/// The overwrite with the respective ApiModule
		///
		/// @param   Base static CorePage used
		/// @param   API builder to setup on
		///
		/// @return  An ApiModule, to load (can be null)
		public ApiModule moduleSetup(CommonStack stack) {
			return null;
		}

		/// Called once when initialized per request, and by the initializeContext thread.
		///
		/// The distinction is important, as certain parameters (such as requesrt details),
		/// cannot be assumed to be avaliable in initializeContext, but is present for most requests
		@Override
		public void doSharedSetup() throws Exception {
			// Setup the CommonStack implementation	
			testStack = stackSetup();
			// Setup the testModule
			testModule = moduleSetup(testStack);
		}

		/// !To Override
		/// to configure the ApiBuilder steps
		///
		/// @param  The APIBuilder object used for setup
		@Override
		public void apiBuilderSetup(ApiBuilder api) {
			// Setup the module API (if built)
			if( testModule != null ) {
				testModule.setupApiBuilder(api);
			}
		}
	}

	/// Testing servlet to provide, for test extension
	public CorePage setupServlet() {
		return new ApiModuleTestServlet();
	}

	//-------------------------------------------------------------------------
	//
	// JUnit setup and teardown
	//
	//-------------------------------------------------------------------------

	//
	// Standard setup and teardown
	//
	@Before
	public void setUp() {
		// Setup the servlet, this will call the required builder setup
		testPort = TestConfig.issuePortNumber();
		testPage = setupServlet();
		testServlet = new EmbeddedServlet(testPort, testPage);
		testBaseUrl = "http://localhost:" + testPort + "/api/";
	}
	
	@After
	public void tearDown() throws Exception {
		if (testServlet != null) {
			testServlet.close();
			testServlet = null;
		}
		testPage = null;
		cookieJar = null;
	}

	//-------------------------------------------------------------------------
	//
	// Convinence function calls
	//
	//-------------------------------------------------------------------------

	/// Utility function to do a simple JSON POST request on the server URI
	///
	/// @param   Server subpath URI
	/// @param   Parameters to pass over (can be null)
	public Map<String,Object> requestJSON(String uri, Map<String,Object> params) {

		// Make a request with cookies
		ResponseHttp res = RequestHttp.post(testBaseUrl+uri, RequestHttp.simpleParameterConversion(params), null, cookieJar);
		
		// Store the cookie result
		cookieJar.putAll( res.cookiesMap() );

		// Process the result, to JSON map
		String rawResult = res.toString().trim();

		try {
			Map<String,Object> ret = ConvertJSON.toMap(rawResult);
			if( ret == null ) {
				throw new RuntimeException("Empty JSON response : "+rawResult);
			}
			return ret;
		} catch (Exception e) {
			throw new RuntimeException("Unexpected requestJSON formatting : \n"+rawResult);
		}
	}

	//-------------------------------------------------------------------------
	//
	// Sanity test
	//
	//-------------------------------------------------------------------------

	/// Minimal passing test, where intentionally the wrong URI is called
	/// to get a JSON API response error
	@Test
	public void constructorTest() {
		assertNotNull( testServlet );
		assertNotNull( requestJSON("wrong/URI",null).get("ERROR") );
	}
	
	/// Making sure IntentionalError, does actually give an error
	@Test
	public void intentionalErrorTest() {
		assertEquals("IntentionalError", requestJSON("IntentionalError",null).get("ERROR") );
		assertNotNull(requestJSON("IntentionalError",null).get("INFO"));
	}
}