package picodedTests.JSql.struct;

// Target test class
import picoded.JSql.*;
import picoded.JSql.struct.*;
import picoded.JStruct.*;
import picoded.JStruct.internal.*;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

// Test depends
import java.nio.charset.Charset;
import java.lang.String;
import java.io.UnsupportedEncodingException;
import java.util.*;

import picodedTests.JStruct.*;
import picodedTests.*;

public class AccountTable_Sqlite_test extends AccountTable_test {
	
	/// To override for implementation
	///------------------------------------------------------
	
	public JSql sqlImplmentation() {
		return JSql.sqlite();
	}
	
	public String tableName = TestConfig.randomTablePrefix();
	
	public AccountTable implementationConstructor() {
		JSqlStruct jsObj = new JSqlStruct(sqlImplmentation());
		return jsObj.getAccountTable("AT_" + tableName);
	}
	
}
