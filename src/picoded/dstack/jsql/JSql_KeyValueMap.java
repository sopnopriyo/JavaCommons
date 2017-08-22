package picoded.dstack.jsql;

import java.util.logging.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import picoded.dstack.KeyValueMap;
import picoded.dstack.core.Core_KeyValueMap;
import picoded.security.NxtCrypt;
import picoded.struct.GenericConvertMap;
import picoded.struct.GenericConvertHashMap;
import picoded.dstack.jsql.connector.*;
import picoded.set.JSqlType;
import picoded.core.conv.ListValueConv;

/**
* Reference implementation of KeyValueMap data structure.
* This is done via a minimal implementation via internal data structures.
*
* Built ontop of the Core_KeyValueMap implementation.
**/
public class JSql_KeyValueMap extends Core_KeyValueMap {

	//--------------------------------------------------------------------------
	//
	// Constructor vars
	//
	//--------------------------------------------------------------------------

	/**
	* The inner sql object
	**/
	protected JSql sqlObj = null;

	/**
	* The tablename for the key value pair map
	**/
	protected String sqlTableName = null;

	/**
	* [internal use] JSql setup with a SQL connection and tablename
	**/
	public JSql_KeyValueMap(JSql inJSql, String tablename) {
		super();
		sqlTableName = "KV_"+tablename;
		sqlObj = inJSql;
	}

	//--------------------------------------------------------------------------
	//
	// Fundemental set/get value (core)
	//
	//--------------------------------------------------------------------------

	/**
	* [Internal use, to be extended in future implementation]
	* Returns the value, with validation
	*
	* Handles re-entrant lock where applicable
	*
	* @param key as String
	* @param now timestamp
	*
	* @return String value
	**/
	protected String getValueRaw(String key, long now) {
		// Search for the key
		JSqlResult r = sqlObj.select(sqlTableName, "*", "kID=?", new Object[] { key });
		long expiry = getExpiryRaw(r);

		if (expiry != 0 && expiry < now) {
			return null;
		}

		return r.get("kVl")[0].toString();
	}

	/**
	* [Internal use, to be extended in future implementation]
	* Sets the value, with validation
	*
	* Handles re-entrant lock where applicable
	*
	* @param key
	* @param value, null means removal
	* @param expire timestamp, 0 means not timestamp
	*
	* @return null
	**/
	protected String setValueRaw(String key, String value, long expire) {
		long now = currentSystemTimeInSeconds();
		sqlObj.upsert( //
			sqlTableName, //
			new String[] { "kID" }, //unique cols
			new Object[] { key }, //unique value
			//
			new String[] { "cTm", "eTm", "kVl" }, //insert cols
			new Object[] { now, expire, value } //insert values
		);
		return null;
	}

	//--------------------------------------------------------------------------
	//
	// Expiration and lifespan handling (core)
	//
	//--------------------------------------------------------------------------

	/**
	* [Internal use, to be extended in future implementation]
	* Gets the expire time from the JSqlResult
	**/
	protected long getExpiryRaw(JSqlResult r) throws JSqlException {
		// Search for the key
		Object rawTime = null;

		// Has value
		if (r != null && r.rowCount() > 0) {
			rawTime = r.get("eTm")[0];
		} else {
			return -1; //No value (-1)
		}

		// 0 represents expired value
		long ret = 0;
		if (rawTime != null) {
			if (rawTime instanceof Number) {
				ret = ((Number) rawTime).longValue();
			} else {
				ret = Long.parseLong(rawTime.toString());
			}
		}

		if (ret <= 0) {
			return 0;
		} else {
			return ret;
		}
	}

	/**
	* [Internal use, to be extended in future implementation]
	* Returns the expire time stamp value, raw without validation
	*
	* Handles re-entrant lock where applicable
	*
	* @param key as String
	*
	* @return long
	**/
	protected long getExpiryRaw(String key) {
		// Search for the key, get expire timestamp, and process it
		return getExpiryRaw( //
			sqlObj.select(sqlTableName, "eTm", "kID=?", new Object[] { key })
		);
	}

	/**
	* [Internal use, to be extended in future implementation]
	* Sets the expire time stamp value, raw without validation
	*
	* Handles re-entrant lock where applicable
	*
	* @param key as String
	* @param expire timestamp in seconds, 0 means NO expire
	*
	* @return long
	**/
	public void setExpiryRaw(String key, long time) {
		sqlObj.update("UPDATE " + sqlTableName + " SET eTm=? WHERE kID=?", time, key);
	}

	//--------------------------------------------------------------------------
	//
	// Backend system setup / teardown / maintenance (DStackCommon)
	//
	//--------------------------------------------------------------------------

	/**
	* Primary key type
	**/
	protected String pKeyColumnType = "BIGINT PRIMARY KEY AUTOINCREMENT";

	/**
	* Timestamp field type
	**/
	protected String tStampColumnType = "BIGINT";

	/**
	* Key name field type
	**/
	protected String keyColumnType = "VARCHAR(64)";

	/**
	* Value field type
	**/
	protected String valueColumnType = "VARCHAR(MAX)";

	/**
	* Setsup the backend storage table, etc. If needed
	**/
	public void systemSetup() {
		// Table constructor
		//-------------------
		sqlObj.createTable( //
			sqlTableName, //
			new String[] { //
			// Primary key, as classic int, this is used to lower SQL
			// fragmentation level, and index memory usage. And is not accessible.
			// Sharding and uniqueness of system is still maintained by meta keys
				"pKy", //
				// Time stamps
				"cTm", //value created time
				"eTm", //value expire time
				// Storage keys
				"kID", //
				// Value storage
				"kVl" //
			}, //
			new String[] { //
				pKeyColumnType, //Primary key
				// Time stamps
				tStampColumnType, tStampColumnType,
				// Storage keys
				keyColumnType, //
				// Value storage
				valueColumnType //
			} //
		);

		// Unique index
		//------------------------------------------------
		sqlObj.createIndex( //
			sqlTableName, "kID", "UNIQUE", "unq" //
		);

		// Value search index
		//------------------------------------------------
		if (sqlObj.sqlType() == JSqlType.MYSQL) {
			sqlObj.createIndex( //
				// kVl(190) is chosen, as mysql "standard prefix limitation" is 767
				// as a result, with mb4 where 4 byte represents a character. 767/4 = 191
				sqlTableName, "kVl(191)", null, "valMap" //
			);
		} else {
			sqlObj.createIndex( //
				sqlTableName, "kVl", null, "valMap" //
			);
		}
	}

	/**
	* Teardown and delete the backend storage table, etc. If needed
	**/
	public void systemDestroy() {
		sqlObj.dropTable(sqlTableName);
	}

	/**
	* Perform maintenance, mainly removing of expired data if applicable
	**/
	public void maintenance() {
		sqlObj.delete( //
			sqlTableName, //
			"eTm <= ? AND eTm > ?", //
			new Object[] { currentSystemTimeInSeconds(), 0 }
		);
	}

	/**
	* Removes all data, without tearing down setup
	**/
	@Override
	public void clear() {
		sqlObj.delete(sqlTableName);
	}

	//--------------------------------------------------------------------------
	//
	// SQL specific KeySet / remove optimization
	//
	//--------------------------------------------------------------------------

	/**
	* Search using the value, all the relevent key mappings
	*
	* Handles re-entrant lock where applicable
	*
	* @param key, note that null matches ALL
	*
	* @return array of keys
	**/
	@Override
	public Set<String> keySet(String value) {
		long now = currentSystemTimeInSeconds();
		JSqlResult r = null;
		if (value == null) {
			r = sqlObj.select(sqlTableName, "kID", "eTm <= ? OR eTm > ?",
				new Object[] { 0, now });
		} else {
			r = sqlObj.select(sqlTableName, "kID", "kVl = ? AND (eTm <= ? OR eTm > ?)",
				new Object[] { value, 0, now });
		}

		if (r == null || r.get("kID") == null) {
			return new HashSet<String>();
		}

		// Gets the various key names as a set
		return ListValueConv.toStringSet(r.getObjectList("kID", "[]"));
	}

	/**
	* Remove the value, given the key
	*
	* @param key param find the thae meta key
	*
	* @return  null
	**/
	@Override
	public String remove(Object key) {
		sqlObj.update("DELETE FROM `" + sqlTableName + "` WHERE kID = ?", key.toString());
		return null;
	}

}
