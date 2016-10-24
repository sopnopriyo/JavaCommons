package picoded.conv;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

//Target test class
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static picoded.conv.GenericConvert.*;

public class GenericConvert_test {
	
	private static final double DELTA = 1e-15;
	
	/// Setup the temp vars
	@Before
	public void setUp() {
	}
	
	@After
	public void tearDown() {
		
	}
	
	//
	// Expected exception testing
	//
	
	/// Invalid constructor test
	@Test(expected = IllegalAccessError.class)
	public void invalidConstructor() throws Exception {
		new GenericConvert();
	}
	
	@Test
	public void toStringTest() {
		assertNotNull(GenericConvert.toString(-1, null));
		assertNull("", GenericConvert.toString(null, null));
		assertEquals("fallback", GenericConvert.toString(null, "fallback"));
		assertEquals("inout", GenericConvert.toString("inout", null));
		assertEquals("1", GenericConvert.toString(new Integer(1), null));
		assertEquals("1", GenericConvert.toString(new Integer(1)));
	}
	
	@Test
	public void toBooleanTest() {
		List<String> list = new ArrayList<String>();
		assertFalse(GenericConvert.toBoolean(list, false));
		/// for boolean true false test case
		Boolean boolean1 = new Boolean(true);
		assertTrue(GenericConvert.toBoolean(boolean1, false));
		boolean1 = new Boolean(false);
		assertFalse(GenericConvert.toBoolean(boolean1, true));
		/// for boolean number test case
		assertTrue(GenericConvert.toBoolean(1, true));
		assertFalse(toBoolean(new Boolean("1"), false));
		assertFalse(GenericConvert.toBoolean(0, true));
		/// for string test case
		assertFalse(GenericConvert.toBoolean(null, false));
		assertTrue(toBoolean(null, true));
		assertFalse(GenericConvert.toBoolean("", false));
		assertTrue(GenericConvert.toBoolean("+", false));
		assertTrue(GenericConvert.toBoolean("t", false));
		assertTrue(GenericConvert.toBoolean("T", false));
		assertTrue(GenericConvert.toBoolean("y", false));
		assertTrue(GenericConvert.toBoolean("Y", false));
		
		assertFalse(GenericConvert.toBoolean("-", false));
		assertFalse(GenericConvert.toBoolean("f", false));
		assertFalse(GenericConvert.toBoolean("F", false));
		assertFalse(GenericConvert.toBoolean("n", false));
		assertFalse(GenericConvert.toBoolean("N", false));
		
		assertTrue(GenericConvert.toBoolean("123", false));
		assertTrue(GenericConvert.toBoolean("12", false));
		assertFalse(GenericConvert.toBoolean("-1", false));
		assertFalse(GenericConvert.toBoolean("$%", false));
		
		assertTrue(toBoolean(999, false));
	}
	
	@Test
	public void toBooleanSingleParameterTest() {
		assertTrue(toBoolean("true"));
	}
	
	@Test
	public void toNumberTest() {
		List<String> list = new ArrayList<String>();
		assertNotEquals(list, GenericConvert.toNumber(list, 0).intValue());
		assertNotEquals("$%", GenericConvert.toNumber("$%", 0).intValue());
		assertEquals(10, GenericConvert.toNumber(10, 0).intValue());
		assertNotEquals("", GenericConvert.toNumber("", 0).intValue());
		assertEquals(new BigDecimal("01111111111111111"), GenericConvert.toNumber("01111111111111111", 0));
		assertEquals(new BigDecimal("2.1"), GenericConvert.toNumber("2.1", null));
		assertEquals(new BigDecimal("2.2"), GenericConvert.toNumber("2.2"));
	}
	
	@Test
	public void toNumberSingleTest() {
		assertEquals(new BigDecimal("1"), GenericConvert.toNumber("1"));
	}
	
	@Test
	public void toIntTest() {
		assertEquals(1, GenericConvert.toInt(null, 1));
		assertEquals(2, GenericConvert.toInt(2, 1));
		assertEquals(3, GenericConvert.toInt(3));
	}
	
	@Test
	public void toLongTest() {
		assertEquals(1l, GenericConvert.toLong(null, 1l));
		assertEquals(2l, GenericConvert.toLong(2l, 1l));
		assertEquals(3l, GenericConvert.toLong(3l));
	}
	
	@Test
	public void toFloatTest() {
		assertEquals(1.0f, GenericConvert.toFloat(null, 1.0f), DELTA);
		assertEquals(2.0f, GenericConvert.toFloat(2.0f, 1.0f), DELTA);
		assertEquals(3.0f, GenericConvert.toFloat(3.0f), DELTA);
	}
	
	@Test
	public void toDoubleTest() {
		assertEquals(1.0, GenericConvert.toDouble(null, 1.0), DELTA);
		assertEquals(2.0, GenericConvert.toDouble("2.0", 1.0), DELTA);
		assertEquals(3.0, GenericConvert.toDouble(3.0), DELTA);
		assertEquals(4.0, GenericConvert.toDouble(4.0, 1.0), DELTA);
	}
	
	@Test
	public void toByteTest() {
		assertEquals((byte) 'a', GenericConvert.toByte(null, (byte) 'a'));
		assertEquals((byte) 'b', GenericConvert.toByte("a", (byte) 'b'));
		assertEquals((byte) 'c', GenericConvert.toByte((byte) 'c'));
		assertEquals((byte) 4.0, GenericConvert.toByte(4.0, (byte) 'd'));
	}
	
	@Test
	public void toShortTest() {
		assertEquals((short) 'a', GenericConvert.toShort(null, (short) 'a'));
		assertEquals((short) 'b', GenericConvert.toShort("a", (short) 'b'));
		assertEquals((short) 'c', GenericConvert.toShort((short) 'c'));
		assertEquals((short) 4.0, GenericConvert.toShort(4.0, (short) 'd'));
	}
	
	@Test
	public void toUUIDTest() {
		assertNull(GenericConvert.toUUID("hello-world"));
		assertEquals(GUID.fromBase58("heijoworjdabcdefghijabc"),
			GenericConvert.toUUID(GUID.fromBase58("heijoworjdabcdefghijabc")));
		assertEquals(GUID.fromBase58("123456789o123456789o12"), GenericConvert.toUUID("123456789o123456789o12", null));
		assertNull(GenericConvert.toUUID("123456789o123456789o1o2", null));
		assertEquals(GUID.fromBase58("heijoworjdabcdefghijabc"),
			GenericConvert.toUUID(GUID.fromBase58("heijoworjdabcdefghijabc"), null));
		assertEquals(GUID.fromBase58("heijoworjdabcdefghijabc"),
			GenericConvert.toUUID(null, GUID.fromBase58("heijoworjdabcdefghijabc")));
		assertNull(GenericConvert.toUUID(null, null));
		assertEquals(GUID.fromBase58("heijoworjdabcdefghijabc"),
			GenericConvert.toUUID(GUID.fromBase58("heijoworjdabcdefghijabc"), "hello world"));
	}
	
	@Test
	public void toGUIDTest() {
		assertNull(GenericConvert.toGUID("hello-world"));
		assertEquals("ADAukG8u3ryYrm6pHFDB6o", GenericConvert.toGUID(GUID.fromBase58("heijoworjdabcdefghijabc")));
		assertEquals("ADAukG8u3ryYrm6pHFDB6o", GenericConvert.toGUID("ADAukG8u3ryYrm6pHFDB6o"));
		assertNull(GenericConvert.toGUID(100));
		assertEquals("ADAukG8u3ryYrm6pHFDB6o", GenericConvert.toGUID(null, "ADAukG8u3ryYrm6pHFDB6o"));
		assertEquals("ADAukG8u3ryYrm6pHFDB6o", GenericConvert.toGUID("ADAukG8u3ryYrm6pHFDB6o", null));
		assertNull(GenericConvert.toGUID(null, null));
		assertEquals("ADAukG8u3ryYrm6pHFDB6o", GenericConvert.toGUID("ADAukG8u3ryYrm6pHFDB6o", "hello world"));
		List<String> list = new ArrayList<String>();
		assertNotEquals(list, GenericConvert.toNumber(list, 0).intValue());
		assertNotEquals("$%", GenericConvert.toNumber("$%", 0).intValue());
		assertEquals(10, GenericConvert.toNumber(10, 0).intValue());
		assertNotEquals("", GenericConvert.toNumber("", 0).intValue());
		assertEquals(new BigDecimal("01111111111111111"), GenericConvert.toNumber("01111111111111111", 0));
		
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void fetchObjectTest() {
		assertNull(fetchObject(null, null, null));
		assertEquals("default", fetchObject(null, null, "default"));
		Map map = new HashMap();
		assertNull(fetchObject(map, null, null));
		map = new HashMap();
		map.put("key", "value");
		assertNull(fetchObject(map, null, null));
		assertEquals("value", fetchObject(map, "key", null));
		
		List list = new ArrayList();
		assertNull(fetchObject(list, null, null));
		list = new ArrayList();
		list.add("value");
		assertNull(fetchObject(list, null, null));
		assertNull("value", fetchObject(list, "key", null));
		assertEquals("value", fetchObject(list, "0", null));
		
		assertEquals("default", fetchObject("value", "0", "default"));
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void fetchObjectWithTwoParametersTest() {
		assertNull(fetchObject(null, null));
		Map map = new HashMap();
		assertNull(fetchObject(map, null));
		map = new HashMap();
		map.put("key", "value");
		assertNull(fetchObject(map, null));
		assertEquals("value", fetchObject(map, "key"));
		
		List list = new ArrayList();
		assertNull(fetchObject(list, null));
		list = new ArrayList();
		list.add("value");
		assertNull(fetchObject(list, null));
		assertNull("value", fetchObject(list, "key"));
		assertEquals("value", fetchObject(list, "0"));
		
		assertNull(fetchObject("value", "0"));
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void fetchNestedObjectTest() {
		assertNull(fetchNestedObject(null, null));
		
		assertNull(fetchNestedObject(null, null, null));
		assertEquals("default", fetchNestedObject(null, null, "default"));
		assertEquals("default", fetchNestedObject("string", null, "default"));
		Map map = new HashMap();
		assertEquals("default", fetchNestedObject(map, null, "default"));
		map.put("key", "value");
		assertEquals("value", fetchNestedObject(map, "key", "default"));
		assertEquals("value", fetchNestedObject(map, ".key", "default"));
		map.put("key1", "value1");
		assertEquals("value1", fetchNestedObject(map, "[key1]", "default"));
		assertEquals("default", fetchNestedObject(map, "[key2]", "default"));
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test(expected = RuntimeException.class)
	public void fetchNestedObjectinvalidTest() {
		Map map = new HashMap();
		map.put("key", "value");
		assertEquals("value1", fetchNestedObject(map, "[key1", "default"));
	}
	
	@Test
	public void toStringArrayTest() {
		assertNull(toStringArray(null));
		
		assertNull(toStringArray(null, null));
		
		assertNull(toStringArray(null, "default"));
		assertArrayEquals(new String[] { "key1", "key2" }, toStringArray(new String[] { "key1", "key2" }, "default"));
		assertArrayEquals(new String[] { "1", "2.2" }, toStringArray(new Object[] { "1", "2.2" }, "default"));
		assertArrayEquals(new String[] { "key1", "key2", "key3" },
			toStringArray("[\"key1\",\"key2\",\"key3\"]", "default"));
		List<String> list = new ArrayList<>();
		list.add("key1");
		list.add("key2");
		assertArrayEquals(new String[] { "key1", "key2" }, toStringArray(list, "default"));
	}
	
	@Test
	public void toStringMapTest() {
		assertNull(toStringMap(null));
		
		assertNull(toStringMap(null, null));
	}
}
