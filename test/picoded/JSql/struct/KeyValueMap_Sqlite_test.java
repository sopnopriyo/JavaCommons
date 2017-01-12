package picoded.JSql.struct;

// Target test class
import java.util.HashSet;

import org.junit.Test;

import picoded.TestConfig;
import picoded.JSql.*;
import picoded.JStruct.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

// Test Case include

public class KeyValueMap_Sqlite_test extends KeyValueMap_test {
	
	/// To override for implementation
	///------------------------------------------------------
	
	public JSql sqlImplmentation() {
		return JSql.sqlite();
	}
	
	public String tableName = TestConfig.randomTablePrefix();
	
	@Override
	public KeyValueMap implementationConstructor() {
		JSqlStruct jsObj = new JSqlStruct(sqlImplmentation());
		return jsObj.getKeyValueMap("KVM_" + tableName);
	}
	
	@Test
	public void systemTeardownExceptionTest() {
		JSql_KeyValueMap jsObj = new JSql_KeyValueMap(sqlImplmentation(), "");
		jsObj.systemTeardown();
	}
	
	@Test(expected = RuntimeException.class)
	public void maintenanceExceptionTest() {
		JSql_KeyValueMap jsObj = new JSql_KeyValueMap(sqlImplmentation(), "");
		jsObj.maintenance();
	}
	
	@Test(expected = RuntimeException.class)
	public void getExpiryRawTest() {
		JSql_KeyValueMap jsObj = new JSql_KeyValueMap(sqlImplmentation(), "");
		jsObj.getExpiryRaw("");
	}
	
	@Test(expected = RuntimeException.class)
	public void setExpiryRawTest() {
		JSql_KeyValueMap jsObj = new JSql_KeyValueMap(sqlImplmentation(), "");
		jsObj.setExpiryRaw("", 1l);
	}
	
	@Test(expected = RuntimeException.class)
	public void getValueRawTest() {
		JSql_KeyValueMap jsObj = new JSql_KeyValueMap(sqlImplmentation(), "");
		jsObj.getValueRaw("", 1l);
	}
	
	@Test(expected = RuntimeException.class)
	public void setValueRawTest() {
		JSql_KeyValueMap jsObj = new JSql_KeyValueMap(sqlImplmentation(), "");
		jsObj.setValueRaw("", "", 1l);
	}
	
	@Test(expected = RuntimeException.class)
	public void getKeysExceptionTest() {
		JSql_KeyValueMap jsObj = new JSql_KeyValueMap(sqlImplmentation(), "");
		jsObj.getKeys("");
	}
	
	@Test
	public void getKeysNullKeyTest() {
		kvmObj.put("yes", "no");
		kvmObj.put("hello", "world");
		kvmObj.put("this", "world");
		kvmObj.put("is", "sparta");
		assertNotNull(kvmObj.getKeys(null));
	}
	
	@Test
	public void getKeysNullKeyNullValueTest() {
		assertEquals(new HashSet<String>(), kvmObj.getKeys(null));
	}
	
	@Test
	public void keySetTest() {
		kvmObj.put("yes", "no");
		kvmObj.put("hello", "world");
		kvmObj.put("this", "world");
		kvmObj.put("is", "sparta");
		assertNotNull(kvmObj.keySet());
	}
	
	@Test(expected = RuntimeException.class)
	public void removeTest() {
		JSql_KeyValueMap jsObj = new JSql_KeyValueMap(sqlImplmentation(), "");
		jsObj.remove("");
	}
}
