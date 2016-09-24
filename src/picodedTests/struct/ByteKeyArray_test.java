package picodedTests.struct;

// Target test class
import static org.junit.Assert.assertEquals;

import org.junit.After;
// Test Case include
import org.junit.Before;
import org.junit.Test;

import picoded.struct.ByteKeyArray;
// Test depends

///
/// Test Case for picoded.struct.ByteKeyArray
///
@SuppressWarnings("deprecation")
public class ByteKeyArray_test {
	
	private ByteKeyArray<String> tObj = null;
	
	@Before
	public void setUp() {
		tObj = new ByteKeyArray<String>();
	}
	
	@After
	public void tearDown() {
		tObj = null;
	}
	
	/// Tests: CaseInsensitiveHashMap.storageMode
	@Test
	public void storageMode_blank() {
		assertEquals("blank", tObj.storageMode());
	}
	
	/*
	/// Tests Basic UTF8 Byte array support
	@Test
	public void utf8byteArray() throws UnsupportedEncodingException{
		String msg = "HelloZZZ World 123 ~!@#";
		
		//byte[] bArr = msg.getBytes(Charset.forName("UTF-8")); 
		byte[] bArr = msg.getBytes("UTF-8"); //1.5 competible syntax
		
		for(int a=0; a<bArr.length; ++a) {
			assertEquals( (char)msg.charAt(a), ((char)bArr[a] & 0xFF));
			assertTrue( msg.charAt(a) <= (char)128 );
		}
		assertEquals( msg.length(), bArr.length );
	}
	
	/// Tests extended UTF-8 support 
	@Test
	public void charToStringToByte() {
		String tStr;
		byte[] bArr;
		char cVal;
		
		for( cVal = 0; cVal <= 255; ++cVal ) {
			tStr = String.valueOf(cVal);
			bArr = tStr.getBytes("UTF-8"); //1.5 competible syntax
			
			assertEquals( (char)cVal, ((char)bArr[0] & 0xFF));
		}
	}
	 */
	
	@Test
	public void byteArrayPointerTest() {
		byte arrLen = 5;
		byte[] setA = new byte[arrLen];
		
		for (byte a = 0; a < arrLen; ++a) {
			setA[a] = (byte) -a;
		}
		
		byte[] setB = setA;
		for (byte a = 0; a < arrLen; ++a) {
			assertEquals(-a, setA[a]);
			assertEquals(setA[a], setB[a]);
			
			//overwrite
			setB[a] = a;
			
			//checks
			assertEquals(a, setB[a]);
			assertEquals(setB[a], setA[a]);
		}
	}
	
}