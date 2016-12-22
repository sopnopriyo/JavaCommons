package picoded.struct.query.condition;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import picoded.struct.query.Query;
import picoded.struct.query.QueryType;

public class LessThan_test {
	
	private LessThan lessThan = null;
	
	@Before
	public void setUp() {
		
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test
	public void blankTest() {
		assertNull(lessThan);
	}
	
	@Test
	public void typeTest() {
		lessThan = construct();
		assertEquals(QueryType.LESS_THAN, lessThan.type());
	}
	
	@Test
	public void testValuesTest() {
		lessThan = construct();
		assertFalse(lessThan.testValues(null, null));
	}
	
	@Test
	public void operatorSymbolTest() {
		lessThan = construct();
		assertEquals("<", lessThan.operatorSymbol());
	}
	
	private LessThan construct() {
		Map<String, Object> defaultArgMap = new HashMap<>();
		return new LessThan("key", "myKey", defaultArgMap);
	}
}
