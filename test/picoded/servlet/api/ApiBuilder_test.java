package picoded.servlet.api;

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
import picoded.core.conv.*;
import picoded.core.common.*;
import picoded.web.*;

public class ApiBuilder_test {
	
	//
	// The test vars to use
	//
	int testPort = 0; //Test port to use
	EmbeddedServlet testServlet = null; //Test servlet to use
	ApiBuilder builder = null; //API builder to use

	//
	// Standard setup and teardown
	//
	@Before
	public void setUp() {
		testPort = TestConfig.issuePortNumber();
		testServlet = null;
	}
	
	@After
	public void tearDown() throws Exception {
		if (testServlet != null) {
			testServlet.close();
			testServlet = null;
		}
		builder = null;
	}

	//
	// Non servlet test
	//
	@Test
	public void baseSetup() {
		assertNotNull(builder = new ApiBuilder());
	}
	
	//
	// Test the minimal implmentation of setting up an API, and executing directly
	//
	@Test
	public void simpleHelloWorld() {
		baseSetup();

		// Blank Key set checking
		Set<String> set = new HashSet<String>();
		assertEquals(set, builder.keySet());

		// Register the script
		builder.put("hello", (req,res) -> { res.put("hello","world"); return res; });
		
		// Validate result format
		assertEquals( "{\"hello\":\"world\"}", ConvertJSON.fromMap(builder.execute("hello", null)) );
	}

	//
	// More comprehensive test of the hello world example
	//
	@Test
	public void baseHelloWorld() {
		baseSetup();

		// Blank Key set checking
		Set<String> set = new HashSet<String>();
		assertEquals(set, builder.keySet());

		// Register the script
		builder.put("hello", (req,res) -> { 
			assertNotNull(req);
			assertNotNull(res);
			res.put("hello","world"); 
			return res; 
		});
		
		// Registerd key set testing
		set.add("hello");
		assertEquals(set, builder.keySet());
		assertNotNull(builder.get("hello"));

		// Assert result not null
		assertNotNull( builder.execute("hello", null) );

		// Validate result format
		assertEquals( "{\"hello\":\"world\"}", ConvertJSON.fromMap(builder.execute("hello", null)) );
	}

	//
	// An example of versioning in action
	//
	@Test
	public void helloWorldVersioning() {
		baseSetup();

		// base versioning
		assertEquals( "v0.0", builder.versionStr() );

		// Pointless version setup
		assertNotNull( builder.setVersion(0,0) );
		assertEquals( "v0.0", builder.versionStr() );
		
		// Bad world
		builder.put("hello", (req,res) -> { res.put("hello","bad-world"); return res; });
		assertEquals( "bad-world", builder.execute("hello", null).get("hello") );

		// Version incrementing
		assertNotNull( builder.setVersion(0,1) );
		assertEquals( "v0.1", builder.versionStr() );

		// Good world
		builder.put("hello", (req,res) -> { res.put("hello","good-world"); return res; });
		assertEquals( "good-world", builder.execute("hello", null).get("hello") );
	}
	//
	// Testing exception handling
	//
	@Test
	public void missingPathException() {
		baseSetup();

		// registering error endpoint
		builder.put("error", (req,res) -> { throw new RuntimeException("test-exception"); });

		// Exception to validate
		Exception testException = null;

		// Try to cause an exception
		try {
			builder.execute("does/not/exist", null);
		} catch(Exception e) {
			testException = e;
		}

		// Validate that an exception occur
		assertNotNull(testException);
	}

	//
	// Testing exception handling
	//
	@Test
	public void validException() {
		baseSetup();

		// registering error endpoint
		builder.put("error", (req,res) -> { throw new RuntimeException("test-exception"); });

		// Exception to validate
		Exception testException = null;

		// Try to cause an exception
		try {
			builder.execute("test-exception", null);
		} catch(Exception e) {
			testException = e;
		}

		// Validate that an exception occur
		assertNotNull(testException);
	}
}