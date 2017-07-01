package picoded.dstack.jsql;

import java.util.logging.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import picoded.dstack.MetaTable;
import picoded.dstack.MetaObject;
import picoded.dstack.core.Core_MetaTable;
import picoded.security.NxtCrypt;
import picoded.struct.GenericConvertMap;
import picoded.struct.GenericConvertHashMap;
import picoded.dstack.jsql.connector.*;
import picoded.set.JSqlType;
import picoded.conv.ListValueConv;

/// JSql implmentation of MetaTable
///
/// Due to how complex this class is, it has been split apart into multiple sub classes
///
public class JSql_MetaTable extends Core_MetaTable {
	
	//--------------------------------------------------------------------------
	//
	// Constructor setup
	//
	//--------------------------------------------------------------------------
	
	/// The inner sql object
	protected JSql sqlObj = null;
	
	/// The tablename for the key value pair map
	protected String dataTableName = null;
	
	/// The tablename the parent key
	protected String baseTableName = null;
	
	/// JSql setup 
	///
	/// @param   JSQL connection
	/// @param   Table name to use 
	public JSql_MetaTable(JSql inJSql, String tablename) {
		super();
		sqlObj = inJSql;
		baseTableName = "MB_"+tablename;
		dataTableName = "MD_"+tablename;
	}
	
	//--------------------------------------------------------------------------
	//
	// Internal config vars
	//
	//--------------------------------------------------------------------------
	
	/// Object ID field type
	protected String objColumnType = "VARCHAR(64)";
	
	/// Key name field type
	protected String keyColumnType = "VARCHAR(64)";
	
	/// Type collumn type
	protected String typeColumnType = "TINYINT";
	
	/// Index collumn type
	protected String indexColumnType = "TINYINT";
	
	/// String value field type
	/// @TODO: Investigate performance issues for this approach
	protected String numColumnType = "DECIMAL(36,12)";
	
	/// String value field type
	protected String strColumnType = "VARCHAR(64)";
	
	/// Full text value field type
	protected String fullTextColumnType = "VARCHAR(MAX)";
	
	/// Timestamp field type
	protected String tStampColumnType = "BIGINT";
	
	/// Primary key type
	protected String pKeyColumnType = "BIGINT PRIMARY KEY AUTOINCREMENT";

	/// Raw datastorage type
	protected String rawDataColumnType = "BLOB";
	
	//--------------------------------------------------------------------------
	//
	// Backend system setup / teardown / maintenance (DStackCommon)
	//
	//--------------------------------------------------------------------------
	
	/// Setsup the backend storage table, etc. If needed
	@Override
	public void systemSetup() {
		
		// BASE Table constructor
		//----------------------------
		sqlObj.createTable( //
			baseTableName, //
			new String[] { //
			// Primary key, as classic int, this is used to lower SQL
			// fragmentation level, and index memory usage. And is not accessible.
			// Sharding and uniqueness of system is still maintained by GUID's
				"pKy", //
				// Time stamps
				"cTm", //object created time
				"uTm", //object updated time
				"eTm", //object expire time (for future use)
				// Object keys
				"oID" //_oid
			}, //
			new String[] { //
				pKeyColumnType, //Primary key
				// Time stamps
				tStampColumnType, //
				tStampColumnType, //
				tStampColumnType, //
				// Object keys
				objColumnType //
			} //
		);
		
		// DATA Table constructor
		//----------------------------
		sqlObj.createTable( //
			dataTableName, //
			new String[] { //
			// Primary key, as classic int, this is used to lower SQL
			// fragmentation level, and index memory usage. And is not accessible.
			// Sharding and uniqueness of system is still maintained by GUID's
				"pKy", //
				// Time stamps
				"cTm", //value created time
				"uTm", //value updated time
				"eTm", //value expire time (for future use)
				// Object keys
				"oID", //_oid
				"kID", //key storage
				"idx", //index collumn
				// Value storage (except text)
				"typ", //type collumn
				"nVl", //numeric value (if applicable)
				"sVl", //case insensitive string value (if applicable), or case sensitive hash
				// Text value storage
				"tVl", //Textual storage, placed last for row storage optimization
				"rVl" //Raw binary storage, placed last for row storage optimization
			}, //
			new String[] { //
			pKeyColumnType, //Primary key
				// Time stamps
				tStampColumnType, //
				tStampColumnType, //
				tStampColumnType, //
				// Object keys
				objColumnType, //
				keyColumnType, //
				indexColumnType, //
				// Value storage
				typeColumnType, //
				numColumnType, //
				strColumnType, //
				fullTextColumnType, //
				rawDataColumnType
			} //
		);
		
		// Unique index
		//------------------------------------------------

		// This optimizes query by object keys
		// + oID
		sqlObj.createIndex( //
			baseTableName, "oID", "UNIQUE", "unq" //
		); //
		
		// This optimizes query by object keys, 
		// with the following combinations
		// + oID
		// + oID, kID
		// + oID, kID, idx
		sqlObj.createIndex( //
			dataTableName, "oID, kID, idx", "UNIQUE", "unq" //
		); //
		
		// Foreign key constraint,
		// to migrate functionality over to JSQL class itself
		try {
			sqlObj.update_raw( //
				"ALTER TABLE "+dataTableName+ //
				"ADD CONSTRAINT "+dataTableName+"_fk"+ //
				"FOREIGN KEY (oID) REFERENCES "+baseTableName+"(oID)" + //
				"ON DELETE CASCADE" // NOTE : This slows performance down
			);
		} catch(Exception e) {
			// Silence exception
			// @TODO : properly handle conflicts only
		}

		// Key Values search index
		//------------------------------------------------
		
		// This optimizes for numeric values
		// + kID
		// + kID, nVl
		sqlObj.createIndex( //
			dataTableName, "kID, nVl", null, "knIdx" //
		); //
		
		// This optimizes for string values
		// + kID
		// + kID, sVl
		sqlObj.createIndex( //
			dataTableName, "kID, sVl", null, "ksIdx" //
		); //
		
		// Full text index, for textual data
		// @TODO FULLTEXT index support
		//------------------------------------------------
		//if (sqlObj.sqlType != JSqlType.sqlite) {
		//	sqlObj.createIndex( //
		//		tName, "tVl", "FULLTEXT", "tVlT" //
		//	);
		//} else {
		// sqlObj.createIndex( //
		// 	dataTableName, "tVl", null, "tVlI" // Sqlite uses normal index
		// ); //
		//}
			
		//
		// timestamp index, is this needed?
		//
		// Currently commented out till a usage is found for them
		// This can be easily recommented in.
		//
		// Note that the main reason this is commented out is because
		// updated time and created time does not work fully as intended
		// as its is more of a system point of view. Rather then adminstration
		// point of view. 
		//
		// A good example is at times several fields in buisness logic is set
		// to NOT want to update the updated time stamp of the object.
		//
		//------------------------------------------------
		
		// // By created time
		// sqlObj.createIndex( //
		// 	dataTableName, "cTm, kID, nVl, sVl", null, "cTm_valMap" //
		// ); //

		// // By updated time
		// sqlObj.createIndex( //
		// 	dataTableName, "uTm, kID, nVl, sVl", null, "uTm_valMap" //
		// ); //
			
		//sqlObj.createIndex( //
		//	tName, "uTm", null, "uTm" //
		//);
		
		//sqlObj.createIndex( //
		//	tName, "cTm", null, "cTm" //
		//);
	}
	
	/// Teardown and delete the backend storage table, etc. If needed
	public void systemDestroy() {
		sqlObj.dropTable(dataTableName);
		sqlObj.dropTable(baseTableName);
	}
	
	/// Removes all data, without tearing down setup
	@Override
	public void clear() {
		sqlObj.delete(dataTableName);
		sqlObj.delete(baseTableName);
	}
	
	//--------------------------------------------------------------------------
	//
	// Internal functions, used by MetaObject
	//
	//--------------------------------------------------------------------------
	
	/// [Internal use, to be extended in future implementation]
	///
	/// Removes the complete remote data map, for MetaObject.
	/// This is used to nuke an entire object
	///
	/// @param  Object ID to remove
	///
	/// @return  nothing
	protected void metaObjectRemoteDataMap_remove(String oid) {
		// Delete the data
		sqlObj.delete(
			dataTableName,
			"oID = ?",
			new Object[] { oid }
		);

		// Delete the parent key
		sqlObj.delete(
			baseTableName,
			"oID = ?",
			new Object[] { oid }
		);
	}
	
	/// Gets the complete remote data map, for MetaObject.
	/// Returns null if not exists
	protected Map<String, Object> metaObjectRemoteDataMap_get(String _oid) {
		return JSql_MetaTableUtils.JSqlObjectMapFetch(sqlObj, dataTableName, _oid, null);
	}
	
	/// Updates the actual backend storage of MetaObject
	/// either partially (if supported / used), or completely
	protected void metaObjectRemoteDataMap_update(String _oid, Map<String, Object> fullMap,
		Set<String> keys) {

		// Curent timestamp
		long now = JSql_MetaTableUtils.getCurrentTimestamp();
		
		// Ensure GUID is registered
		sqlObj.upsert( //
			baseTableName, //
			new String[] { "oID" }, //
			new Object[] { _oid }, // 
			new String[] { "uTm" }, //
			new Object[] { now }, //
			new String[] { "cTm", "eTm" }, //
			new Object[] { now, 0 }, // 
			null // The only misc col, is pKy, which is being handled by DB
		);
		
		// Does the data append
		JSql_MetaTableUtils.JSqlObjectMapAppend(sqlObj, dataTableName, _oid, fullMap, keys, true);
	}

	//--------------------------------------------------------------------------
	//
	// KeySet support
	//
	//--------------------------------------------------------------------------

	/// Get and returns all the GUID's, note that due to its 
	/// potential of returning a large data set, production use
	/// should be avoided.
	///
	/// @returns set of keys
	@Override
	public Set<String> keySet() {
		JSqlResult r = sqlObj.select(baseTableName, "oID");
		if (r == null || r.get("oID") == null) {
			return new HashSet<String>();
		}
		return ListValueConv.toStringSet(r.getObjectList("oID"));
	}

	//--------------------------------------------------------------------------
	// 
	// Query based optimization
	//
	//--------------------------------------------------------------------------
	
	/// Performs a search query, and returns the respective MetaObjects
	///
	/// CURRENTLY: It is entirely dependent on the whereValues object type to perform the relevent search criteria
	/// @TODO: Performs the search pattern using the respective type map
	///
	/// @param   where query statement
	/// @param   where clause values array
	/// @param   query string to sort the order by, use null to ignore
	/// @param   offset of the result to display, use -1 to ignore
	/// @param   number of objects to return max
	///
	/// @returns  The MetaObject[] array
	@Override
	public MetaObject[] query(String whereClause, Object[] whereValues, String orderByStr,
		int offset, int limit) {
		return JSql_MetaTableUtils.metaTableQuery(this, sqlObj, dataTableName, whereClause,
			whereValues, orderByStr, offset, limit);
		//return super.query( whereClause, whereValues, orderByStr, offset, limit );
	}
	
	/*

	/// Performs a search query, and returns the respective MetaObjects keys
	///
	/// CURRENTLY: It is entirely dependent on the whereValues object type to perform the relevent search criteria
	/// @TODO: Performs the search pattern using the respective type map
	///
	/// @param   where query statement
	/// @param   where clause values array
	/// @param   query string to sort the order by, use null to ignore
	/// @param   offset of the result to display, use -1 to ignore
	/// @param   number of objects to return max
	///
	/// @returns  The String[] array
	@Override
	public String[] queryKeys(String whereClause, Object[] whereValues, String orderByStr,
		int offset, int limit) {
		return JSql_MetaTableUtils.metaTableQueryKey(this, sqlObj, dataTableName, whereClause,
			whereValues, orderByStr, offset, limit);
		//return super.query( whereClause, whereValues, orderByStr, offset, limit );
	}
	
	/// Performs a search query, and returns the respective MetaObjects
	///
	/// @param   where query statement
	/// @param   where clause values array
	///
	/// @returns  The total count for the query
	@Override
	public long queryCount(String whereClause, Object[] whereValues) {
		return JSql_MetaTableUtils.metaTableCount(this, sqlObj, dataTableName, whereClause,
			whereValues, null, -1, -1);
	}
	
	*/

	//--------------------------------------------------------------------------
	// 
	// Get key names handling
	//
	//--------------------------------------------------------------------------
	
	/// Scans the object and get the various keynames used. 
	/// This is used mainly in adminstration interface, etc.
	///
	/// The seekDepth parameter is ignored in JSql mode, as its optimized.
	///
	/// @param  seekDepth, which detirmines the upper limit for iterating
	///         objects for the key names, use -1 to search all
	///
	/// @returns  The various key names used in the objects
	///
	@Override
	public Set<String> getKeyNames(int seekDepth) {
		JSqlResult r = sqlObj.select(dataTableName, "DISTINCT kID");
		if (r == null || r.get("kID") == null) {
			return new HashSet<String>();
		}
		
		return ListValueConv.toStringSet(r.getObjectList("kID"));
	}

}
