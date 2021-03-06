package picoded.struct;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class UnsupportedDefaultMap_test {
	
	UnsupportedDefaultMap<String, String> unsupportedDefaultMapForVoidMethod = mock(
		UnsupportedDefaultMap.class, CALLS_REAL_METHODS);
	UnsupportedDefaultMap<String, String> unsupportedDefaultMap = mock(UnsupportedDefaultMap.class);
	
	@Before
	public void setUp() {
		
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void getTest() {
		when(unsupportedDefaultMap.get("key")).thenCallRealMethod();
		unsupportedDefaultMap.get("key");
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void putTest() {
		when(unsupportedDefaultMap.put("key", "value")).thenCallRealMethod();
		unsupportedDefaultMap.put("key", "value");
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void removeTest() {
		when(unsupportedDefaultMap.remove("key")).thenCallRealMethod();
		unsupportedDefaultMap.remove("key");
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void keySetTest() {
		when(unsupportedDefaultMap.keySet()).thenCallRealMethod();
		unsupportedDefaultMap.keySet();
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void clearTest() {
		unsupportedDefaultMapForVoidMethod.clear();
	}
	
	@Test
	public void clearValidTest() {
		Set<String> set = new HashSet<>();
		set.add("key1");
		set.add("key2");
		when(unsupportedDefaultMap.keySet()).thenReturn(set);
		doCallRealMethod().when(unsupportedDefaultMap).clear();
		unsupportedDefaultMap.clear();
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void containsKeyTest() {
		unsupportedDefaultMapForVoidMethod.containsKey("key");
	}
	
	@Test
	public void containsKeyValidTest() {
		when(unsupportedDefaultMap.containsKey("key1")).thenCallRealMethod();
		Set<String> set = new HashSet<>();
		set.add("key1");
		set.add("key2");
		when(unsupportedDefaultMap.keySet()).thenReturn(set);
		assertTrue(unsupportedDefaultMap.containsKey("key1"));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void containsValueExceptionTest() {
		when(unsupportedDefaultMap.containsValue("value")).thenCallRealMethod();
		when(unsupportedDefaultMap.entrySet()).thenCallRealMethod();
		when(unsupportedDefaultMap.keySet()).thenCallRealMethod();
		assertFalse(unsupportedDefaultMap.containsValue("value"));
	}
	
	@Test
	public void containsValueForNullTest() {
		when(unsupportedDefaultMap.containsValue(null)).thenCallRealMethod();
		when(unsupportedDefaultMap.entrySet()).thenReturn(new HashSet<>());
		assertFalse(unsupportedDefaultMap.containsValue(null));
	}
	
	@Test
	public void containsValueForInnerNullConditionTest() {
		when(unsupportedDefaultMap.containsValue(null)).thenCallRealMethod();
		Set<Map.Entry<String, String>> set = new HashSet<>();
		Map<String, String> map = new HashMap<>();
		map.put("key4", null);
		set.addAll(map.entrySet());
		when(unsupportedDefaultMap.entrySet()).thenReturn(set);
		assertTrue(unsupportedDefaultMap.containsValue(null));
		
	}
	
	@Test
	public void containsValueForInnerNullConditionTest1() {
		when(unsupportedDefaultMap.containsValue(null)).thenCallRealMethod();
		Set<Map.Entry<String, String>> set = new HashSet<>();
		Map<String, String> map = new HashMap<>();
		map.put("key5", "V2");
		set.addAll(map.entrySet());
		when(unsupportedDefaultMap.entrySet()).thenReturn(set);
		assertFalse(unsupportedDefaultMap.containsValue(null));
	}
	
	@Test
	public void containsValueForOuterNullConditionTest() {
		when(unsupportedDefaultMap.containsValue(null)).thenCallRealMethod();
		Set<Map.Entry<String, String>> set = new HashSet<>();
		Map<String, String> map = new HashMap<>();
		map.put("key5", null);
		set.addAll(map.entrySet());
		when(unsupportedDefaultMap.entrySet()).thenReturn(set);
		assertTrue(unsupportedDefaultMap.containsValue(null));
	}
	
	@Test
	public void containsValueForOuterNullConditionTest1() {
		when(unsupportedDefaultMap.containsValue("V2")).thenCallRealMethod();
		Set<Map.Entry<String, String>> set = new HashSet<>();
		Map<String, String> map = new HashMap<>();
		map.put("key5", "V2");
		set.addAll(map.entrySet());
		when(unsupportedDefaultMap.entrySet()).thenReturn(set);
		assertTrue(unsupportedDefaultMap.containsValue("V2"));
	}
	
	@Test
	public void containsValueForOuterNullConditionTest2() {
		when(unsupportedDefaultMap.containsValue("ABC")).thenCallRealMethod();
		Set<Map.Entry<String, String>> set = new HashSet<>();
		Map<String, String> map = new HashMap<>();
		map.put("key5", "V2");
		set.addAll(map.entrySet());
		when(unsupportedDefaultMap.entrySet()).thenReturn(set);
		assertFalse(unsupportedDefaultMap.containsValue("ABC"));
	}
	
	@Test
	public void containsValueForOuterNullConditionTest3() {
		when(unsupportedDefaultMap.containsValue("ABC")).thenCallRealMethod();
		Set<Map.Entry<String, String>> set = new HashSet<>();
		Map<String, String> map = new HashMap<>();
		map.put("key5", null);
		set.addAll(map.entrySet());
		when(unsupportedDefaultMap.entrySet()).thenReturn(set);
		assertFalse(unsupportedDefaultMap.containsValue("ABC"));
	}
	
	@Test
	public void containsValueValidTest() {
		when(unsupportedDefaultMap.containsValue("value1")).thenCallRealMethod();
		Set<Map.Entry<String, String>> set = new HashSet<>();
		Map<String, String> map = new HashMap<>();
		map.put("key1", "value1");
		map.put("key2", "value2");
		set.addAll(map.entrySet());
		when(unsupportedDefaultMap.entrySet()).thenReturn(set);
		assertTrue(unsupportedDefaultMap.containsValue("value1"));
	}
	
	@Test
	public void containsValueValidForNullTest() {
		when(unsupportedDefaultMap.containsValue(null)).thenCallRealMethod();
		Set<Map.Entry<String, String>> set = new HashSet<>();
		Map<String, String> map = new HashMap<>();
		map.put("key1", "value1");
		map.put("key2", null);
		set.addAll(map.entrySet());
		when(unsupportedDefaultMap.entrySet()).thenReturn(set);
		assertTrue(unsupportedDefaultMap.containsValue(null));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void entrySetTest() {
		when(unsupportedDefaultMap.entrySet()).thenCallRealMethod();
		when(unsupportedDefaultMap.keySet()).thenCallRealMethod();
		unsupportedDefaultMap.entrySet();
	}
	
	@Test
	public void entrySetValidTest() {
		when(unsupportedDefaultMap.entrySet()).thenCallRealMethod();
		Set<String> set = new HashSet<>();
		set.add("key1");
		set.add("key2");
		when(unsupportedDefaultMap.keySet()).thenReturn(set);
		assertNotNull(unsupportedDefaultMap.entrySet());
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void isEmptyTest() {
		unsupportedDefaultMapForVoidMethod.isEmpty();
	}
	
	@Test
	public void isEmptyTrueTest() {
		when(unsupportedDefaultMap.isEmpty()).thenCallRealMethod();
		Set<String> set = new HashSet<>();
		when(unsupportedDefaultMap.keySet()).thenReturn(set);
		assertTrue(unsupportedDefaultMap.isEmpty());
	}
	
	@Test
	public void isEmptyFalseTest() {
		when(unsupportedDefaultMap.isEmpty()).thenCallRealMethod();
		Set<String> set = new HashSet<>();
		set.add("my_entry");
		when(unsupportedDefaultMap.keySet()).thenReturn(set);
		assertFalse(unsupportedDefaultMap.isEmpty());
	}
	
	@Test(expected = java.lang.UnsupportedOperationException.class)
	public void putAllTest() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("my_key", "my_value");
		unsupportedDefaultMapForVoidMethod.putAll(map);
	}
	
	@Test
	public void putAllValidTest() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("my_key", "my_value");
		doCallRealMethod().when(unsupportedDefaultMap).putAll(map);
		unsupportedDefaultMap.putAll(map);
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void sizeTest() {
		unsupportedDefaultMapForVoidMethod.size();
	}
	
	@Test
	public void sizeValidTest() {
		when(unsupportedDefaultMap.size()).thenCallRealMethod();
		Set<String> set = new HashSet<>();
		set.add("my_entry");
		when(unsupportedDefaultMap.keySet()).thenReturn(set);
		assertEquals(1, unsupportedDefaultMap.size());
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void valuesTest() {
		unsupportedDefaultMapForVoidMethod.values();
	}
	
	@Test
	public void valuesValidTest() {
		Set<Map.Entry<String, String>> set = new HashSet<>();
		Map<String, String> map = new HashMap<>();
		map.put("key1", "value1");
		map.put("key2", "value2");
		set.addAll(map.entrySet());
		when(unsupportedDefaultMap.entrySet()).thenReturn(set);
		when(unsupportedDefaultMap.values()).thenCallRealMethod();
		assertEquals(set.size(), unsupportedDefaultMap.values().size());
	}
}
