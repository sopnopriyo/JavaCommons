package picodedTests.conv;

// Target test class
import java.util.Calendar;

import picoded.conv.DateConv;
import picoded.conv.DateConv.ISODateFormat;

// Test Case include
import org.junit.*;

import static org.junit.Assert.*;

public class DateConv_test {
	@Test
	public void convMilliSecondsToISO() {
		long millisecondsDate = Long.parseLong("1441756800000");
		
		//check case
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(millisecondsDate);
		String calISODate = "0" + cal.get(Calendar.DATE) + "-0" + (cal.get(Calendar.MONTH) + 1) + "-"
			+ cal.get(Calendar.YEAR);
		
		String isoDate = DateConv.toISOFormat(millisecondsDate, ISODateFormat.DDMMYYYY, "-");
		
		assertEquals(calISODate, isoDate);
	}
	
	@Test
	public void convISOToMilliseconds() {
		String isoDate = "1990-05-20";
		
		String millisecondsDate = DateConv.toMillisecondsFormat(isoDate, ISODateFormat.YYYYMMDD, "-");
		
		String isoDateReconstructed = DateConv.toISOFormat(Long.parseLong(millisecondsDate), ISODateFormat.YYYYMMDD, "-");
		
		assertEquals(isoDate, isoDateReconstructed);
	}
	
	@Test
	public void changeISOFormat() {
		long millisecondsDate = Long.parseLong("1431756800000"); //16-5-2015
		
		String isoDate_dmy = DateConv.toISOFormat(millisecondsDate, ISODateFormat.DDMMYYYY, "-");
		assertEquals("16-05-2015", isoDate_dmy);
		
		String isoDate_ymd = DateConv.changeISODateFormat(isoDate_dmy, ISODateFormat.DDMMYYYY, ISODateFormat.YYYYMMDD,
			"-");
		assertEquals("2015-05-16", isoDate_ymd);
		
		String isoDate_mdy = DateConv.changeISODateFormat(isoDate_ymd, ISODateFormat.YYYYMMDD, ISODateFormat.MMDDYYYY,
			"-");
		assertEquals("05-16-2015", isoDate_mdy);
		
		String isoDate_ydm = DateConv.changeISODateFormat(isoDate_mdy, ISODateFormat.MMDDYYYY, ISODateFormat.YYYYDDMM,
			"-");
		assertEquals("2015-16-05", isoDate_ydm);
	}
}