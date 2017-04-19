package picoded.dstack.struct.simple;

// Test system include
import static org.junit.Assert.*;
import org.junit.*;

// Java includes
import java.util.*;

// External lib includes
import org.apache.commons.lang3.RandomUtils;

// Test depends
import picoded.conv.GUID;
import picoded.struct.CaseInsensitiveHashMap;
import picoded.dstack.*;
import picoded.dstack.struct.simple.*;

// MetaTable base test class
public class StructSimple_MetaTable_test {
	
	/// Test object
	public MetaTable mtObj = null;
	
	// To override for implementation
	//-----------------------------------------------------
	public MetaTable implementationConstructor() {
		return new StructSimple_MetaTable();
	}
	
	// Setup and sanity test
	//-----------------------------------------------------
	@Before
	public void setUp() {
		mtObj = implementationConstructor();
		mtObj.systemSetup();
	}
	
	@After
	public void tearDown() {
		if (mtObj != null) {
			mtObj.systemDestroy();
		}
		mtObj = null;
	}
	
	@Test
	public void constructorTest() {
		// not null check
		assertNotNull(mtObj);
		
		// Incremental maintenance
		mtObj.incrementalMaintenance();
		
		// run maintaince, no exception?
		mtObj.maintenance();
	}
	
	// Subset assertion
	//-----------------------------------------------
	
	/// Utility function, to ensure the expected values exists in map
	/// while allowing future test cases not to break when additional values
	/// like create timestamp is added.
	public void assetSubset(Map<String, Object> expected, Map<String, Object> result) {
		for (Map.Entry<String, Object> entry : expected.entrySet()) {
			assertEquals(entry.getValue(), result.get(entry.getKey()));
		}
	}
	
	// Test cases
	//-----------------------------------------------
	
	// Test utility used to generate random maps
	protected HashMap<String, Object> randomObjMap() {
		HashMap<String, Object> objMap = new CaseInsensitiveHashMap<String, Object>();
		objMap.put(GUID.base58(), RandomUtils.nextInt(0, (Integer.MAX_VALUE - 3)));
		objMap.put(GUID.base58(), -(RandomUtils.nextInt(0, (Integer.MAX_VALUE - 3))));
		objMap.put(GUID.base58(), GUID.base58());
		objMap.put(GUID.base58(), GUID.base58());
		
		objMap.put("num", RandomUtils.nextInt(0, (Integer.MAX_VALUE - 3)));
		objMap.put("str_val", GUID.base58());
		
		return objMap;
	}
	
	// @Test
	// public void invalidSetup() { //Numeric as table prefix tend to cuase
	// problems
	// MetaTable m;
	//
	// try {
	// m = new MetaTable(JStackObj, "1" + TestConfig.randomTablePrefix());
	// fail(); // if we got here, no exception was thrown, which is bad
	// } catch (Exception e) {
	// final String expected = "Invalid table name (cannot start with numbers)";
	// assertTrue("Missing Exception - " + expected,
	// e.getMessage().indexOf(expected) >= 0);
	// }
	// }
	
	@Test
	public void newObjectTest() {
		MetaObject mObj = null;
		
		assertNotNull(mObj = mtObj.newObject());
		mObj.put("be", "happy");
		mObj.saveDelta();
		
		String guid = null;
		assertNotNull(guid = mObj._oid());
		
		assertNotNull(mObj = mtObj.get(guid));
		assertEquals("happy", mObj.get("be"));
	}
	
	@Test
	public void basicTest() {
		String guid = GUID.base58();
		
		// Sanity check
		assertNull(mtObj.get(guid));
		
		// Random object to put in
		HashMap<String, Object> objMap = randomObjMap();
		MetaObject mObj = null;
		
		// Puts in a new object, and get guid
		assertNotNull(guid = mtObj.newObject(objMap)._oid());
		
		// Get and check with guid
		objMap.put("_oid", guid);
		assetSubset(objMap, (Map<String, Object>) mtObj.get(guid));
		
		objMap = randomObjMap();
		assertNotNull(guid = mtObj.newObject(objMap)._oid());
		objMap.put("_oid", guid);
		assetSubset(objMap, mtObj.get(guid));
	}
	
	/// Checks if a blank object gets saved
	@Test
	public void blankObjectSave() {
		String guid = null;
		MetaObject p = null;
		assertFalse(mtObj.containsKey("hello"));
		assertNotNull(p = mtObj.newObject());
		assertNotNull(guid = p._oid());
		p.saveDelta();
		
		assertTrue(mtObj.containsKey(guid));
	}
	
	HashMap<String, Object> genNumStrObj(int number, String str) {
		HashMap<String, Object> objMap = new CaseInsensitiveHashMap<String, Object>();
		objMap.put("num", new Integer(number));
		objMap.put("str_val", str);
		return objMap;
	}
	
	HashMap<String, Object> genNumStrObj(int number, String str, int orderCol) {
		HashMap<String, Object> objMap = new CaseInsensitiveHashMap<String, Object>();
		objMap.put("num", new Integer(number));
		objMap.put("order", new Integer(orderCol));
		objMap.put("str_val", str);
		return objMap;
	}
	
	@Test
	public void indexBasedTest() {
		
		mtObj.newObject(genNumStrObj(1, "this"));
		mtObj.newObject(genNumStrObj(2, "is"));
		mtObj.newObject(genNumStrObj(3, "hello"));
		mtObj.newObject(genNumStrObj(4, "world"));
		mtObj.newObject(genNumStrObj(5, "program"));
		mtObj.newObject(genNumStrObj(6, "in"));
		mtObj.newObject(genNumStrObj(7, "this"));
		
		MetaObject[] qRes = null;
		assertNotNull(qRes = mtObj.query(null, null));
		assertEquals(7, qRes.length);
		assertEquals(7, mtObj.queryCount(null, null));
		
		assertNotNull(qRes = mtObj.query("num > ? AND num < ?", new Object[] { 2, 5 }, "num ASC"));
		assertEquals(2, qRes.length);
		assertEquals("hello", qRes[0].get("str_val"));
		assertEquals("world", qRes[1].get("str_val"));
		assertEquals(2, mtObj.queryCount("num > ? AND num < ?", new Object[] { 2, 5 }));
		
		assertNotNull(qRes = mtObj.query("str_val = ?", new Object[] { "this" }));
		assertEquals(2, qRes.length);
		assertEquals(2, mtObj.queryCount("str_val = ?", new Object[] { "this" }));
		
		assertNotNull(qRes = mtObj.query("num > ?", new Object[] { 2 }, "num ASC", 2, 2));
		assertEquals(2, qRes.length);
		assertEquals("program", qRes[0].get("str_val"));
		assertEquals("in", qRes[1].get("str_val"));
		
		assertEquals(5, mtObj.queryCount("num > ?", new Object[] { 2 }));
	}
	
	// /
	/// An exception occurs, if a query fetch occurs with an empty table
	// /
	@Test
	public void issue47_exceptionWhenTableIsEmpty() {
		MetaObject[] qRes = null;
		assertNotNull(qRes = mtObj.query(null, null));
		assertEquals(0, qRes.length);
	}
	
	// /
	/// Bad view index due to inner join instead of left join. Testing.
	// /
	/// AKA: Incomplete object does not appear in view index
	// /
	@Test
	public void innerJoinFlaw() {
		mtObj.newObject(genNumStrObj(1, "hello world"));
		
		HashMap<String, Object> objMap = new CaseInsensitiveHashMap<String, Object>();
		objMap.put("num", new Integer(2));
		mtObj.newObject(objMap).saveDelta();
		
		objMap = new CaseInsensitiveHashMap<String, Object>();
		objMap.put("str_val", "nope");
		mtObj.newObject(objMap);
		
		MetaObject[] qRes = null;
		assertNotNull(qRes = mtObj.query(null, null));
		assertEquals(3, qRes.length);
		
		assertNotNull(qRes = mtObj.query("num = ?", new Object[] { 1 }));
		assertEquals(1, qRes.length);
		
		assertNotNull(qRes = mtObj.query("num <= ?", new Object[] { 2 }));
		assertEquals(2, qRes.length);
		
		assertNotNull(qRes = mtObj.query("str_val = ?", new Object[] { "nope" }));
		assertEquals(1, qRes.length);
		
	}
	
	// /
	/// Handle right outer closign bracket in metatable meta names
	// /
	@Test
	public void mssqlOuterBrackerInMetaNameFlaw() {
		HashMap<String, Object> objMap = null;
		MetaObject[] qRes = null;
		
		//
		// Setup vars to test against
		//
		objMap = new CaseInsensitiveHashMap<String, Object>();
		objMap.put("num[0].val", new Integer(2));
		mtObj.newObject(objMap).saveDelta();
		
		objMap = new CaseInsensitiveHashMap<String, Object>();
		objMap.put("str[0].val", "nope");
		objMap.put("str[1].val", "rawr");
		mtObj.newObject(objMap);
		
		objMap = new CaseInsensitiveHashMap<String, Object>();
		objMap.put("num[0].val", new Integer(2));
		objMap.put("str[0].val", "nope");
		mtObj.newObject(objMap);
		
		//
		// Query to run
		//
		assertNotNull(qRes = mtObj.query("num[0].val = ?", new Object[] { 2 }));
		assertEquals(2, qRes.length);
		
		assertNotNull(qRes = mtObj.query("str[0].val = ?", new Object[] { "nope" }));
		assertEquals(2, qRes.length);
		
		assertNotNull(qRes = mtObj.query("str[1].val = ?", new Object[] { "rawr" }));
		assertEquals(1, qRes.length);
		
	}
	
	@Test
	public void missingStrError() {
		HashMap<String, Object> objMap = new HashMap<String, Object>();
		objMap.put("num", 123);
		
		String guid = GUID.base58();
		assertNull(mtObj.get(guid));
		assertNotNull(guid = mtObj.newObject(objMap)._oid());
		
		MetaObject[] qRes = null;
		assertNotNull(qRes = mtObj.query(null, null));
		assertEquals(1, qRes.length);
		
		objMap.put("_oid", guid);
		assetSubset(objMap, mtObj.get(guid));
	}
	
	@Test
	public void missingNumWithSomeoneElse() {
		mtObj.newObject(genNumStrObj(1, "hello world"));
		
		HashMap<String, Object> objMap = new HashMap<String, Object>();
		objMap.put("str_val", "^_^");
		
		String guid = GUID.base58();
		assertNull(mtObj.get(guid));
		assertNotNull(guid = mtObj.newObject(objMap)._oid());
		
		MetaObject[] qRes = null;
		assertNotNull(qRes = mtObj.query(null, null));
		assertEquals(2, qRes.length);
		
		assertTrue(guid.equals(qRes[0]._oid()) || guid.equals(qRes[1]._oid()));
		
		objMap.put("_oid", guid);
		assetSubset(objMap, mtObj.get(guid));
	}
	
	@Test
	public void getFromKeyName_basic() {
		
		mtObj.newObject(genNumStrObj(1, "one"));
		mtObj.newObject(genNumStrObj(2, "two"));
		
		MetaObject[] list = null;
		assertNotNull(list = mtObj.getFromKeyName("num"));
		assertEquals(2, list.length);
		
		String str = null;
		assertNotNull(str = list[0].getString("str_val"));
		assertTrue(str.equals("one") || str.equals("two"));
		
		assertNotNull(str = list[1].getString("str_val"));
		assertTrue(str.equals("one") || str.equals("two"));
		
	}
	
	@Test
	public void nonIndexedKeySaveCheck() {
		
		// Generates single node
		mtObj.newObject(genNumStrObj(1, "hello world"));
		MetaObject[] list = null;
		MetaObject node = null;
		
		// Fetch that single node
		assertNotNull(list = mtObj.getFromKeyName("num"));
		assertEquals(1, list.length);
		assertNotNull(node = list[0]);
		
		// Put non indexed key in node, and save
		node.put("NotIndexedKey", "123");
		node.saveDelta();
		
		// Get the value, to check
		assertEquals(123, mtObj.get(node._oid()).get("NotIndexedKey"));
		
		// Refetch node, and get data, and validate
		assertNotNull(list = mtObj.getFromKeyName("num"));
		assertEquals(1, list.length);
		assertNotNull(list[0]);
		assertEquals(node._oid(), list[0]._oid());
		assertEquals(123, node.get("NotIndexedKey"));
		assertEquals(123, list[0].get("NotIndexedKey"));
	}
	
	@Test
	public void getFromKeyName_customKeys() {
		
		// Generates single node
		mtObj.newObject(genNumStrObj(1, "hello world"));
		MetaObject[] list = null;
		MetaObject node = null;
		
		// Fetch that single node
		assertNotNull(list = mtObj.getFromKeyName("num"));
		assertEquals(1, list.length);
		assertNotNull(node = list[0]);
		
		// Put non indexed key in node, and save
		node.put("NotIndexedKey", "123");
		node.saveDelta();
		
		// Refetch node, and get data, and validate
		assertNotNull(list = mtObj.getFromKeyName("num"));
		assertEquals(1, list.length);
		assertNotNull(list[0]);
		assertEquals(node._oid(), list[0]._oid());
		assertEquals(123, node.get("NotIndexedKey"));
		assertEquals(123, list[0].get("NotIndexedKey"));
		
		// Fetch non indexed key
		assertNotNull(list = mtObj.getFromKeyName("NotIndexedKey"));
		assertEquals(1, list.length);
		
		// Assert equality
		assertEquals(node._oid(), list[0]._oid());
		
	}
	
	// Array values tests
	//-----------------------------------------------
	@Test
	public void jsonStorageTest() {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("name", "Hello");
		
		List<String> ohnoArray = Arrays.asList(new String[] { "oh", "no" });
		data.put("arrs", new ArrayList<String>(ohnoArray));
		
		MetaObject mo = null;
		assertNotNull(mo = mtObj.newObject(data));
		mo.saveDelta();
		
		MetaObject to = null;
		assertNotNull(to = mtObj.get(mo._oid()));
		
		data.put("_oid", mo._oid());
		assetSubset(data, to);
		
		assertEquals(ohnoArray, to.get("arrs"));
	}
	
	@Test
	public void binaryStorageTest() {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("name", "Hello");
		data.put("bin", new byte[] { 1, 2, 3, 4, 5 });
		
		MetaObject mo = null;
		assertNotNull(mo = mtObj.newObject(data));
		mo.saveDelta();
		
		MetaObject to = null;
		assertNotNull(to = mtObj.get(mo._oid()));
		
		assertTrue(data.get("bin") instanceof byte[]);
		assertTrue(to.get("bin") instanceof byte[]);
		
		assertArrayEquals((byte[]) (data.get("bin")), (byte[]) (to.get("bin")));
	}
	
	// Orderby sorting
	//-----------------------------------------------
	
	@Test
	public void T50_orderByTest() {
		
		// Lets just rescycle old test for the names
		mtObj.newObject(genNumStrObj(1, "this", 5));
		mtObj.newObject(genNumStrObj(2, "is", 4));
		mtObj.newObject(genNumStrObj(3, "hello", 3));
		mtObj.newObject(genNumStrObj(4, "world", 2));
		mtObj.newObject(genNumStrObj(5, "program", 1));
		mtObj.newObject(genNumStrObj(6, "in", 6));
		mtObj.newObject(genNumStrObj(7, "this", 7));
		
		// Replicated a bug, where u CANNOT use orderby on a collumn your not
		// doing a where search
		MetaObject[] qRes = mtObj.query("str_val = ?", new String[] { "this" }, "num ASC");
		assertEquals(qRes.length, 2);
		
		assertEquals("this", qRes[0].get("str_val"));
		assertEquals("this", qRes[1].get("str_val"));
		
		assertEquals(1, qRes[0].get("num"));
		assertEquals(7, qRes[1].get("num"));
		
		// Order by with offset
		qRes = mtObj.query(null, null, "num ASC", 2, 3);
		assertEquals(qRes.length, 3);
		
		assertEquals(3, qRes[0].get("num"));
		assertEquals(4, qRes[1].get("num"));
		assertEquals(5, qRes[2].get("num"));
		
		assertEquals("hello", qRes[0].get("str_val"));
		assertEquals("world", qRes[1].get("str_val"));
		assertEquals("program", qRes[2].get("str_val"));
		
		// Search
		qRes = mtObj.query("num >= ? AND num <= ?", new Object[] { 2, 6 });
		assertEquals(5, qRes.length);
		
		// Search with order by
		qRes = mtObj.query("num >= ? AND num <= ?", new Object[] { 2, 6 }, "order ASC");
		assertEquals(5, qRes.length);
		// To validate results
		
		// Search with order by with range
		qRes = mtObj.query("num >= ? AND num <= ?", new Object[] { 2, 6 }, "order ASC", 2, 2);
		assertEquals(2, qRes.length);
		
	}
	
	// @Test
	// public void orderByTestLoop() {
	// for(int i=0; i<25; ++i) {
	// orderByTest();
	//
	// tearDown();
	// setUp();
	// }
	// }
	
	// KeyName fetching test
	//-----------------------------------------------
	@Test
	public void getKeyNamesTest() {
		
		// Lets just rescycle old test for the names
		indexBasedTest();
		
		Set<String> keyNames = mtObj.getKeyNames();
		Set<String> expected = new HashSet<String>(Arrays.asList(new String[] { "_oid", "num",
			"str_val" }));
		assertNotNull(keyNames);
		assertTrue(keyNames.containsAll(expected));
		
	}
	
	// // Mapping tests
	// // [FEATURE DROPPED]
	// //-----------------------------------------------
	// 
	// @Test
	// public void testSingleMappingSystem() {
	// 	mtObj.typeMap().clear();
	// 	
	// 	mtObj.putType("num", "INTEGER");
	// 	mtObj.putType("float", "FLOAT");
	// 	mtObj.putType("double", "double");
	// 	mtObj.putType("long", "long");
	// 	
	// 	assertEquals(mtObj.getType("num"), MetaType.INTEGER);
	// 	assertEquals(mtObj.getType("float"), MetaType.FLOAT);
	// 	assertEquals(mtObj.getType("double"), MetaType.DOUBLE);
	// 	assertEquals(mtObj.getType("long"), MetaType.LONG);
	// }
	// 
	// @Test
	// public void testMapMappingSystem() {
	// 	mtObj.typeMap().clear();
	// 	
	// 	HashMap<String, Object> mapping = new HashMap<String, Object>();
	// 	mapping.put("num", "INTEGER");
	// 	mapping.put("float", "FLOAT");
	// 	mapping.put("double", "double");
	// 	mapping.put("long", "long");
	// 	mapping.put("mixed", "MIXED");
	// 	mapping.put("uuid-array", "UUID_ARRAY");
	// 	
	// 	mtObj.setMappingType(mapping);
	// 	
	// 	assertEquals(mtObj.getType("num"), MetaType.INTEGER);
	// 	assertEquals(mtObj.getType("float"), MetaType.FLOAT);
	// 	assertEquals(mtObj.getType("double"), MetaType.DOUBLE);
	// 	assertEquals(mtObj.getType("long"), MetaType.LONG);
	// 	assertEquals(mtObj.getType("mixed"), MetaType.MIXED);
	// 	assertEquals(mtObj.getType("uuid-array"), MetaType.UUID_ARRAY);
	// }
	
	// // Demo code : kept here for reference
	// //-----------------------------------------------
	// @Test
	// public void demoCode() {
	// 	// Initiate a meta table
	// 	MetaTable table = (new JStruct()).getMetaTable("demo");
	// 	
	// 	// Adding new object?
	// 	MetaObject mObj = table.newObject();
	// 	mObj.put("be", "happy");
	// 	mObj.put("num", new Integer(1));
	// 	mObj.saveDelta();
	// 	
	// 	// Doing a query
	// 	MetaObject[] qRes = null;
	// 	assertNotNull(qRes = table.query("num > ? OR be = ?", new Object[] { 0, "happy" }));
	// 	// Each object has a base68 GUID
	// 	String guid = qRes[0]._oid();
	// 	assertNotNull(table.queryKeys("num > ? OR be = ?", new Object[] { 0, "happy" }, null, 0, 0));
	// 	assertNotNull(table.get(guid, false));
	// 	assertNotNull(table.getFromKeyName("happy", null));
	// 	Map<String, Object> objMap = new CaseInsensitiveHashMap<String, Object>();
	// 	objMap.put("hello", qRes);
	// 	assertNotNull(table.append(guid, qRes[0]));
	// 	assertNotNull(table.append("test", qRes[0]));
	// 	assertNotNull(table.getKeyNames(0));
	// 	assertNotNull(table.getKeyNames(-1));
	// 	assertNotNull(table.getFromKeyName_id("happy"));
	// }
	
	// remove meta object support
	//-----------------------------------------------
	@Test
	public void removeViaMetaObject() {
		
		// Lets just rescycle old test for some dummy data
		basicTest();
		
		// Lets get MetaObject list
		MetaObject[] oRes = null;
		assertNotNull(oRes = mtObj.query(null, null));
		assertTrue(oRes.length > 0);

		// Lets remove one object
		mtObj.remove(oRes[0]);

		// Lets query to make sure its removed
		MetaObject[] qRes = null;
		assertNotNull(qRes = mtObj.query(null, null));
		assertEquals(oRes.length - 1, qRes.length);
	}
	
	@Test
	public void removeViaMetaOID() {
		
		// Lets just rescycle old test for some dummy data
		basicTest();
		
		// Lets get MetaObject list
		MetaObject[] oRes = null;
		assertNotNull(oRes = mtObj.query(null, null));
		assertTrue(oRes.length > 0);

		// Lets remove one object
		mtObj.remove(oRes[0]._oid());

		// Lets query to make sure its removed
		MetaObject[] qRes = null;
		assertNotNull(qRes = mtObj.query(null, null));
		assertEquals(oRes.length - 1, qRes.length);
	}
	
}