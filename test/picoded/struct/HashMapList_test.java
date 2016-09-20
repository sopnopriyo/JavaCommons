package picoded.struct;

// Target test class
import static org.junit.Assert.assertArrayEquals;

import java.util.ArrayList;
// Test Case include
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import picoded.struct.HashMapList;

///
/// Test Case for picoded.struct.CaseInsensitiveHashMap
///
public class HashMapList_test {
	
	@Before
	public void setUp() {
	}
	
	@After
	public void tearDown() {
	}
	
	/// The type erasure error?
	@Test
	public void typeErasureListError() {
		String[] exp = new String[] { "brand", "new", "world" };
		List<String> list = new ArrayList<String>();
		
		list.add(exp[0]);
		list.add(exp[1]);
		list.add(exp[2]);
		
		assertArrayEquals(exp, list.toArray(new String[list.size()]));
	}
	
	/// The type erasure error?
	@Test
	public void typeErasureMapError() {
		String[] exp = new String[] { "brand", "new", "world" };
		HashMapList<String, String> tObj = new HashMapList<String, String>();
		
		tObj.append("hello", exp[0]);
		tObj.append("hello", exp[1]);
		tObj.append("hello", exp[2]);
		
		Map<String, String[]> cObj = tObj.toMapArray(exp);
		assertArrayEquals(exp, cObj.get("hello"));
	}
}
