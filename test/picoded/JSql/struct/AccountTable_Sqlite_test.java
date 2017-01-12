package picoded.JSql.struct;

// Target test class
import picoded.TestConfig;
import picoded.JSql.*;
import picoded.JStruct.*;

// Test Case include
import org.junit.*;

// Test depends
import java.lang.String;

public class AccountTable_Sqlite_test extends AccountTable_test {
	
	/// To override for implementation
	///------------------------------------------------------
	
	public JSql sqlImplmentation() {
		return JSql.sqlite();
	}
	
	public String tableName = TestConfig.randomTablePrefix();
	
	@Override
	public AccountTable implementationConstructor() {
		JSqlStruct jsObj = new JSqlStruct(sqlImplmentation());
		return jsObj.getAccountTable("AT_" + tableName);
	}
	
	@Test
	public void implementationDefaultConstructor() {
		JSqlStruct jsObj = new JSqlStruct();
		jsObj.sqlObj = sqlImplmentation();
		jsObj.getAccountTable("AT_" + tableName);
	}
	
	@Test(expected = RuntimeException.class)
	public void setupAccountTableTest() {
		JSqlStruct jsObj = new JSqlStruct();
		jsObj.setupAccountTable("AT_" + tableName);
	}
	
	@Test(expected = RuntimeException.class)
	public void setupKeyValueMapTest() {
		JSqlStruct jsObj = new JSqlStruct();
		jsObj.setupKeyValueMap("AT_" + tableName);
	}
	
	@Test(expected = RuntimeException.class)
	public void setupAtomicLongMapTest() {
		JSqlStruct jsObj = new JSqlStruct();
		jsObj.setupAtomicLongMap("AT_" + tableName);
	}
	
	@Test(expected = RuntimeException.class)
	public void setupMetaTableTest() {
		JSqlStruct jsObj = new JSqlStruct();
		jsObj.setupMetaTable("AT_" + tableName);
	}
}
