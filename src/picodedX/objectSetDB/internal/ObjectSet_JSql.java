package picoded.objectSetDB.internal;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Collections;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.*;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

import picoded.JSql.*;
import picoded.objectSetDB.*;

/// Object Set implementation of a SINGLE JSql connection, note this does not have CLOB support
///
/// *******************************************************************************
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
///
/// *******************************************************************************
///
public class ObjectSet_JSql extends AbstractMap<String, Map<String, Object>> {
	
	//---------------------//
	// Constructor         //
	//---------------------//
	
	/// Constructor with the JSql object, and the deployed table name
	public ObjectSet_JSql(JSql inSql, String tableName) {
		JSqlObj = inSql;
		sqlTableName = "OS$" + tableName;
	}
	
	//--------------------------------------------------//
	// Protected variables with public getter functions //
	//--------------------------------------------------//
	
	/// The JSQL object that acts as the persistent object store
	protected JSql JSqlObj = null;
	
	/// Internal SQL table name
	public String sqlTableName;
	
	/// Returns the initialized tableName
	public String tableName() {
		return sqlTableName.substring(3);
	}
	
	/// Returns the initialized JSqlObject
	public JSql JSqlObject() {
		return JSqlObj;
	}
	
	//---------------------//
	// Protected variables //
	//---------------------//
	
	/// Internal self used logger
	private static Logger logger = Logger.getLogger(ObjectSet_JSql.class.getName());
	
	/// Object byte space default as 260
	protected int objColumnLength = 260;
	
	/// Key byte space default as 260
	protected int keyColumnLength = 260;
	
	/// Value byte space default as 4000
	protected int valColumnLength = 4000;
	
	/// Numeric accuracy key space. NUMERIC(28,10)
	protected int numericPrecision = 28;
	protected int numericDecimals = 10;
	
	/// Various table collumn names, and classification (used in upsert)?
	protected static String[] uniqueColumnNames = new String[] { "oKey", "mKey", "vIdx" };
	protected static String[] valueColumnNames = new String[] { "vTyp", "vNum", "vStr" };
	protected static String[] timeStampColumnNames = new String[] { "uTim", "cTim", "eTim", "oTim" };
	
	protected static String[] allColumnNames = new String[] { //
	"oKey", "mKey", "vIdx", //Object Key, Meta Key, Value Index
		"vTyp", "vNum", "vStr", //Value Type, Numeric / String
		"uTim", "cTim", "eTim", //Updated, Created, Expire timestamp
		"oTim" //Orphan flag
	};
	
	//-----------------------------------//
	// JSql database storage table setup //
	//-----------------------------------//
	
	/// Performs the required tableSetup for the JSQL database
	public ObjectSet_JSql tableSetup() throws JSqlException {
		JSqlObj.execute("CREATE TABLE IF NOT EXISTS `" + sqlTableName + "` (" + // Table Name
			// --------------------------------------------------------------------------
			"oKey VARCHAR(" + objColumnLength + "), " + // Object namespace
			"mKey VARCHAR(" + keyColumnLength + "), " + // Obj.Key namespace
			// --------------------------------------------------------------------------
			"vIdx INT, " + // The value index (multi map support), -1 is reserved for 'length'
			"vTyp INT, " + // The value type (0:null, 1:String, 2:Int/Long, 3:Double, 4:Float)
			// --------------------------------------------------------------------------
			"vNum NUMERIC(" + numericPrecision + "," + numericDecimals + "), " + // The numeric accuracy for storage
			"vStr VARCHAR(" + valColumnLength + "), " + // String or numeric expression value if applicable
			// --------------------------------------------------------------------------
			"uTim BIGINT, " + // Last updated time stamp
			"cTim BIGINT, " + // Created time stamp
			"eTim BIGINT, " + // Expire time stamp
			"oTim BIGINT " + // Orphan record detection and clearing flag, this is reserved for orphan record cleanup
			")" //
		);
		
		// Create the unique for the table
		// --------------------------------------------------------------------------
		JSqlObj.createTableIndexQuerySet(sqlTableName, StringUtils.join(uniqueColumnNames, ","), "UNIQUE", "unq")
			.execute();
		
		// Create the various indexs used for the table
		// --------------------------------------------------------------------------
		for (int a = 0; a < allColumnNames.length; ++a) {
			
			if (allColumnNames[a].equals("vStr")) {
				//Value string index, is FULLTEXT in mysql (as normal index does not work)
				if (JSqlObj.sqlType == JSqlType.mysql) {
					JSqlObj.createTableIndexQuerySet(sqlTableName, "vStr", "FULLTEXT").execute();
					continue;
				}
			}
			
			JSqlObj.createTableIndexQuerySet(sqlTableName, allColumnNames[a]).execute();
		}
		
		return this;
	}
	
	/// Performs the required tableSetup for the JSQL database, after defining the various column lengths
	public ObjectSet_JSql tableSetup(int inObjColumnLength, int inKeyColumnLength, int inValColumnLength)
		throws JSqlException {
		objColumnLength = inObjColumnLength;
		keyColumnLength = inKeyColumnLength;
		valColumnLength = inValColumnLength;
		return tableSetup();
	}
	
	/// Drops the database table with all relevant data
	public ObjectSet_JSql tableDrop() throws JSqlException {
		JSqlObj.execute("DROP TABLE IF EXISTS `" + sqlTableName + "`");
		return this;
	}
	
	//-----------------------------------//
	// Meta data put commands            //
	//-----------------------------------//
	
	private boolean putRaw( //
		String oKey, String mKey, int vIdx, // unique identifier
		int vTyp, // type identifier
		String vStr, Number vNum, // value storage
		long expireUnixTime // expire timestamp, if applicable
	) throws JSqlException {
		return putRaw(oKey, mKey, vIdx, vTyp, vStr, vNum, expireUnixTime, (System.currentTimeMillis() / 1000L));
	}
	
	/// Internal usage put, note that this does not handle any of the storage logic
	private boolean putRaw( //
		String oKey, String mKey, int vIdx, // unique identifier
		int vTyp, // type identifier
		String vStr, Number vNum, // value storage
		long expireUnixTime, long updateTime // expire timestamp, if applicable
	) throws JSqlException {
		
		Object vActualNum = vNum;
		
		//Sqlite workaround
		if (vNum != null) {
			if (vTyp == 4 && JSqlObj.sqlType == JSqlType.sqlite) {
				vActualNum = new Double(vNum.doubleValue()); //the closest we can get
			}
		}
		
		//-1 expire time, means ignore, use misc field
		if (expireUnixTime <= 0) {
			// JSql based upsert
			return JSqlObj.upsertQuerySet( //prepare querySet
				//
				// Table Name
				//----------------------------------------
				sqlTableName,
				//
				// Unique Values
				//----------------------------------------
				uniqueColumnNames, // { "oKey", "mKey", "vIdx" }
				new Object[] { oKey, mKey, vIdx },
				//
				// Insert Values
				//----------------------------------------
				new String[] { "vTyp", "vNum", "vStr", "uTim" }, //
				new Object[] { vTyp, vActualNum, vStr, updateTime },
				//
				// Default Values (created timestamp)
				//----------------------------------------
				new String[] { "cTim" }, new Object[] { updateTime },
				//
				// Misc value to preserve, such as expire timestamp
				//----------------------------------------
				new String[] { "eTim", "oTim" } //
				).execute();
		} else {
			
			// JSql based upsert
			return JSqlObj.upsertQuerySet( //prepare querySet
				//
				// Table Name
				//----------------------------------------
				sqlTableName,
				//
				// Unique Values
				//----------------------------------------
				uniqueColumnNames, // { "oKey", "mKey", "vIdx" }
				new Object[] { oKey, mKey, vIdx },
				//
				// Insert Values
				//----------------------------------------
				new String[] { "vTyp", "vNum", "vStr", "uTim", "eTim" }, //
				new Object[] { vTyp, vActualNum, vStr, updateTime, expireUnixTime },
				//
				// Default Values (created timestamp)
				//----------------------------------------
				new String[] { "cTim" }, new Object[] { updateTime },
				//
				// Misc value to preserve, such as expire timestamp
				//----------------------------------------
				new String[] { "oTim" } //
				).execute();
		}
	}
	
	// Dynamic put call, based on value object type. With expire unix timestamp
	public boolean put(String oKey, String mKey, int vIdx, Object val, long expireUnixTime, long updateTime)
		throws JSqlException {
		
		// return putRaw(oKey, mKey, vIdx, vTyp, vStr, vInt, vDci, expireUnixTime)
		
		// Evaluating the various put values
		if (val == null) {
			return putRaw(oKey, mKey, vIdx, 0, null, //
				null, expireUnixTime, updateTime);
		} else if (String.class.isInstance(val)) {
			return putRaw(oKey, mKey, vIdx, 1, (val.toString()), //
				null, expireUnixTime, updateTime);
		} else if (Integer.class.isInstance(val) || Long.class.isInstance(val)) {
			return putRaw(oKey, mKey, vIdx, 2, null, //
				(new Long(((Number) val).longValue())), expireUnixTime, updateTime);
		} else if (Double.class.isInstance(val)) {
			return putRaw(oKey, mKey, vIdx, 3, null, //
				(new Double(((Number) val).doubleValue())), expireUnixTime, updateTime);
		} else if (Float.class.isInstance(val)) {
			return putRaw(oKey, mKey, vIdx, 4, null, //
				(new Float(((Number) val).floatValue())), expireUnixTime, updateTime);
		} else {
			String valClassName = val.getClass().getName();
			throw new JSqlException("Unknown value object type : " + (valClassName));
		}
		
		//return false;
	}
	
	// Dynamic put call, based on value object type
	public boolean put(String oKey, String mKey, int vIdx, Object val) throws JSqlException {
		return put(oKey, mKey, vIdx, val, -1, (System.currentTimeMillis() / 1000L));
	}
	
	// Default put call, places the value at index 0
	public boolean put(String oKey, String mKey, Object val) throws JSqlException {
		return put(oKey, mKey, 0, val, -1, (System.currentTimeMillis() / 1000L));
	}
	
	//-----------------------------------//
	// Meta data get commands            //
	//-----------------------------------//
	
	/// Gets a JSqlResult set for the result set
	public JSqlResult getRaw(String oKey, String mKey, int vIdx, int limit) throws JSqlException {
		return JSqlObj.selectQuerySet(
		//prepare select querySet
			sqlTableName, //
			null, // SELECT *
			"oKey=? AND mKey=? AND vIdx=?", // Where keys
			new Object[] { oKey, mKey, vIdx }, // where values
			"vIdx ASC", //order by
			limit, vIdx // select indexes
			).query();
	}
	
	/// Extracts the value from the JSqlResult generated by getRaw, note that expired values returns null
	public static Object valueFromRawResult(JSqlResult jRes, int vPt) {
		if (jRes == null) {
			return null;
		}
		
		long expireValue = expireValueFromRawResult(jRes, vPt);
		if (expireValue > 10 && expireValue < (System.currentTimeMillis() / 1000L)) {
			return null;
		}
		
		Object vTypObj = jRes.readRowCol(vPt, "vTyp");
		int vTyp = ((Number) vTypObj).intValue();
		
		if (vTyp == 0) {
			return null;
		}
		
		Object vStrObj = jRes.readRowCol(vPt, "vStr");
		String vStr = (vStrObj != null) ? (vStrObj.toString()) : null;
		
		if (vTyp == 1) {
			return vStr;
		}
		
		Number vNumObj = (Number) (jRes.readRowCol(vPt, "vNum"));
		
		if (vTyp == 2) {
			return new Long(vNumObj.longValue());
		} else if (vTyp == 3) {
			return new Double(vNumObj.doubleValue());
		} else if (vTyp == 4) {
			return new Float(vNumObj.floatValue());
		}
		
		return null;
	}
	
	public Object get(String oKey, String mKey, int vIdx) throws JSqlException {
		JSqlResult jRes = getRaw(oKey, mKey, vIdx, 1);
		if (jRes.rowCount() > 0) {
			return valueFromRawResult(jRes, 0);
		}
		return null;
	}
	
	public Object get(String oKey, String mKey) throws JSqlException {
		return get(oKey, mKey, 0);
	}
	
	//-----------------------------------//
	// get timestamp commands         //
	//-----------------------------------//
	
	/// extract long timestamp value, from result
	protected static long timestampValueFromRawResult(JSqlResult jRes, String col, int vPt) {
		if (jRes == null || jRes.rowCount() <= 0) {
			return -1;
		}
		Number inVal = ((Number) jRes.readRowCol(vPt, col));
		long ret = (inVal != null) ? inVal.longValue() : -1;
		return (ret > 10) ? ret : -1;
	}
	
	/// Extracts the expire timestamp from the JSqlResult generated by getRaw
	public static long updatedValueFromRawResult(JSqlResult jRes, int vPt) {
		return timestampValueFromRawResult(jRes, "uTim", vPt);
	}
	
	/// Extracts the created timestamp from the JSqlResult generated by getRaw
	public static long createdValueFromRawResult(JSqlResult jRes, int vPt) {
		return timestampValueFromRawResult(jRes, "cTim", vPt);
	}
	
	/// Extracts the expire timestamp from the JSqlResult generated by getRaw
	public static long expireValueFromRawResult(JSqlResult jRes, int vPt) {
		return timestampValueFromRawResult(jRes, "eTim", vPt);
	}
	
	//-----------------------------------//
	// Expire timestamp commands         //
	//-----------------------------------//
	
	// Gets a JSqlResult set of expired values (without the values)
	private JSqlResult getExpireValueRaw(String oKey, String mKey, int vIdx, int limit) throws JSqlException {
		return JSqlObj.selectQuerySet(
		//prepare select querySet
			sqlTableName, //
			"eTim", // SELECT *
			"oKey=? AND mKey=? AND vIdx=?", // Where keys
			new Object[] { oKey, mKey, vIdx }, // where values
			"vIdx ASC", //order by
			limit, vIdx // select indexes
			).query();
	}
	
	/// Get the expired value for the object / meta key
	public long getExpireValue(String oKey, String mKey, int vIdx) throws JSqlException {
		return expireValueFromRawResult(getExpireValueRaw(oKey, mKey, vIdx, 1), 0);
	}
	
	/// Get the expired value for the object / meta key
	public void setExpireValue(String oKey, String mKey, int vIdx, long expireUnixTime) throws JSqlException {
		JSqlObj.execute("UPDATE `" + sqlTableName + "` SET eTim=? WHERE oKey=? AND mKey=? AND vIdx=?", //
			expireUnixTime, oKey, mKey, vIdx);
	}
	
	/// Scans the JSql table for any expired value, and remove them
	public ObjectSet_JSql clearExpiredValues() throws JSqlException {
		return clearExpiredValues((System.currentTimeMillis() / 1000L));
	}
	
	/// Scans the JSql table for any expired value before given timestamp, and remove them
	public ObjectSet_JSql clearExpiredValues(long expireTime) throws JSqlException {
		long nowTime = (System.currentTimeMillis() / 1000L);
		
		if (expireTime > nowTime) {
			throw new JSqlException("Clear Expire timestamp cannot be in the future");
		}
		
		JSqlObj.execute("DELETE FROM `" + sqlTableName + "` WHERE eTim > 10 && eTim < ?", //
			expireTime);
		return this;
	}
	
	//-----------------------------------//
	// Meta data DEL commands            //
	//-----------------------------------//
	
	/// Delets all value in table
	public boolean deleteAll() throws JSqlException {
		return JSqlObj.execute("DELETE FROM `" + sqlTableName + "`");
	}
	
	/// Deletes all records related to an object
	public boolean delete(String oKey) throws JSqlException {
		return JSqlObj.execute("DELETE FROM `" + sqlTableName + "` WHERE oKey=?", oKey);
	}
	
	/// Deletes all records related to an object, and meta key
	public boolean delete(String oKey, String mKey) throws JSqlException {
		return JSqlObj.execute("DELETE FROM `" + sqlTableName + "` WHERE oKey=? AND mKey=?", oKey, mKey);
	}
	
	/// Deletes all records related to an object, and meta key, and index
	public boolean delete(String oKey, String mKey, int vIdx) throws JSqlException {
		return JSqlObj.execute("DELETE FROM `" + sqlTableName + "` WHERE oKey=? AND mKey=? AND vIdx=?", oKey, mKey, vIdx);
	}
	
	//-----------------------------------//
	// Legacy stuff, to recycle / delete //
	//-----------------------------------//
	
	/*
	/// Fetches oKey / key / value, using partial matching search (SQL LIKE)
	public String[] getMultiple(String oKey, String key) throws JSqlException {
		JSqlResult r = JSqlObj.query("SELECT * FROM " + sqlTableName + " WHERE obj = ? AND metaKey = ?", oKey, key);

		String[] res = null;
		int len;
		if ((len = r.fetchAllRows()) > 0) {
			res = new String[len];
			for (int pt = 0; pt < len; pt++) {
				res[pt] = (String) r.readRowCol(pt, "val");
			}
		}
		return res;
	}

	/// Fetches oKey / key / value, using partial matching search (SQL LIKE)
	public HashMap<String, String> getMultipleSet(String oKey, String key, String val) throws JSqlException {
		JSqlResult r = JSqlObj.query("SELECT * FROM " + sqlTableName + " WHERE obj = ? AND metaKey = ? AND val = ?", oKey, key, val);

		HashMap<String, String> res = new HashMap<String, String>();
		int pt = r.rowCount();
		if (r.fetchAllRows() > 0) {
			for (int i = 0; i < pt; i++) {
				res.put((String) r.readRowCol(i, "metaKey"), (String) r.readRowCol(i, "val"));
			}
			return res;
		}
		return null;
	}

	/// Gets all the key value pairs related to the object. And returns as as
	// HashMap< metaKey, value >
	public HashMap<String, String> getObj(String oKey) throws JSqlException {
		JSqlResult r = getObj_JSql(oKey);

		HashMap<String, String> res = new HashMap<String, String>();
		int pt = r.rowCount();
		if (r.fetchAllRows() > 0) {
			for (int i = 0; i < pt; i++) {
				res.put((String) r.readRowCol(i, "metaKey"), (String) r.readRowCol(i, "val"));
			}
			return res;
		}
		return null;
	}

	/// Returns the JSqlResult raw varient
	public JSqlResult getObj_JSql(String oKey) throws JSqlException {
		return JSqlObj.query("SELECT * FROM `" + sqlTableName + "` WHERE obj=?;", oKey);
	}

	/// Returns the JSqlResult raw varient
	/// [TODO: High priority]
	public JSqlResult getAll_JSql() throws JSqlException {
		return JSqlObj.query("SELECT * FROM `" + sqlTableName + "`");
	}

	/// Fetches everything, in oKey, key, value pairs
	public HashMap<String, HashMap<String, Object>> getAll() throws JSqlException {
		JSqlResult r = getAll_JSql();

		HashMap<String, HashMap<String, Object>> res = new HashMap<String, HashMap<String, Object>>();
		HashMap<String, Object> objMap = null;
		String prvoKey = null;
		String oKey = null;
		int len;

		if ((len = r.fetchAllRows()) > 0) {
			for (int i = 0; i < len; i++) {
				// Gets current object id
				oKey = (String) r.readRowCol(i, "obj");

				// Gets the previous objMap, if needed
				if (prvoKey != oKey) {
					objMap = res.get(oKey);

					// result set dosent have the object map yet
					if (objMap == null) {
						objMap = new HashMap<String, Object>();
						res.put(oKey, objMap);
					}

					prvoKey = oKey;
				} // else assumes there isnt a need to change objMap

				objMap.put((String) r.readRowCol(i, "metaKey"), (String) r.readRowCol(i, "val"));
			}
		}
		return res;
	}

	// Fetches a HashMap<Objectid, key> matching to a val
	// [TODO: Low priority]
	public HashMap<String, String> getKeyMap(String val) throws JSqlException {
		// throw new JSqlException("Not Yet Implemented");
		JSqlResult r = getKeyMap_JSql(val);
		HashMap<String, String> res = null;
		int pt = r.rowCount();
		if (r.fetchAllRows() > 0) {
			for (int i = 0; i < pt; i++) {
				res = new HashMap<String, String>();
				Set<Map.Entry<String, Object>> mapEntrySet = r.readRow(i).entrySet();
				for (Map.Entry<String, Object> mapEntry : mapEntrySet) {
					res.put(mapEntry.getKey(), (String) mapEntry.getValue());
				}
			}
		}
		return res;
	}

	/// Returns the JSqlResult raw varient
	/// [TODO: Mid priority]
	public JSqlResult getKeyMap_JSql(String val) throws JSqlException {
		return JSqlObj.query("SELECT obj, metaKey FROM `" + sqlTableName + "` WHERE val = ?", val);
	}

	/// Fetches an array of keys, matching the object id and value
	/// [TODO: Mid priority]
	public String[] getObjKeyMap(String oKey, String val) throws JSqlException {
		// throw new JSqlException("Not Yet Implemented");
		JSqlResult r = getObjKeyMap_JSql(oKey, val);
		String[] res = null;
		int pt = r.rowCount();
		int len;
		if ((len = r.fetchAllRows()) > 0) {
			res = new String[len];
			for (pt = 0; pt < len; pt++) {
				res[pt] = (String) r.readRowCol(pt, "metaKey");
			}
		}
		return res;
	}

	/// Returns the JSqlResult raw varient
	/// [TODO: Mid priority]
	public JSqlResult getObjKeyMap_JSql(String oKey, String val) throws JSqlException {
		return JSqlObj.query("SELECT metaKey FROM `" + sqlTableName + "` WHERE obj=? AND val = ?", oKey, val);
	}

	/// Fetches a HashMap<oKey, val> matched to key
	/// [TODO: Mid priority]
	public HashMap<String, Object> getValMap(String key) throws JSqlException {
		// throw new JSqlException("Not Yet Implemented");
		try {
			HashMap<String, Object> res = null;
			JSqlResult r = getValMap_JSql(key);
			int pt = r.rowCount();
			if (r.fetchAllRows() > 0) {
				for (int i = 0; i < pt; i++) {
					// res = new HashMap<String, Object>();
					res = r.readRow(i);
				}
			}
			return res;
		} catch (JSqlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/// Returns the JSqlResult raw varient
	/// [TODO: Mid priority]
	public JSqlResult getValMap_JSql(String key) throws JSqlException {
		return JSqlObj.query("SELECT obj, val FROM `" + sqlTableName + "` WHERE metaKey = ?", key);
	}

	/// returns true if database table is empty
	/// [TODO: Low priority]
	public boolean isEmpty() throws JSqlException {
		throw new JSqlException("Not Yet Implemented");
	}

	/// returns database number of objects, key, values sets
	/// [TODO: Low priority]
	public long size() throws JSqlException {
		throw new JSqlException("Not Yet Implemented");
	}

	/// returns the total number of objects
	/// [TODO: Low priority]
	public long objectCount() throws JSqlException {
		throw new JSqlException("Not Yet Implemented");
	}

	/// returns the meta count in an object
	/// [TODO: Low priority]
	public long objectMetaCount(String oKey) throws JSqlException {
		throw new JSqlException("Not Yet Implemented");
	}
	 */
	
	///----------------------------------------
	/// Map compliance
	///----------------------------------------
	@Override
	public Set<Map.Entry<String, Map<String, Object>>> entrySet() {
		throw new RuntimeException("entrySet / Iterator support is not (yet) implemented");
		
		/*
		 if (entries == null) {
			entries = new AbstractSet() {
		 public void clear() {
		 list.clear();
		 }

		 public Iterator iterator() {
		 return list.iterator();
		 }

		 public int size() {
		 return list.size();
		 }
			};
		 }
		 return entries;
		 // */
	}
}
