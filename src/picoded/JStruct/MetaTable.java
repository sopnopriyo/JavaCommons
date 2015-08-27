package picoded.JStruct;

/// Java imports
import java.util.*;

/// Picoded imports
import picoded.conv.GUID;
import picoded.struct.CaseInsensitiveHashMap;
import picoded.struct.UnsupportedDefaultMap;
import picoded.JStruct.internal.*;
import picoded.struct.query.*;
import picoded.conv.ListValueConv;

/// MetaTable, servs as the core flexible backend storage implmentation for the whole
/// JStack setup. Its role can be viewed similarly to NoSql, or AWS SimpleDB
/// where almost everything is indexed and cached. 
/// 
/// On a performance basis, it is meant to trade off raw query performance of traditional optimized 
/// SQL lookup, over flexibility in data model. This is however heavily mintigated by the inclusion 
/// of a JCache layer for non-complex lookup cached reads. Which will in most cases be the main
/// read request load.
/// 
public interface MetaTable extends UnsupportedDefaultMap<String, MetaObject> {

	///
	/// Temp mode optimization, used to indicate pure session like data,
	/// that does not require persistance (or even SQL)
	///
	///--------------------------------------------------------------------------
	
	/// Gets if temp mode optimization hint is indicated
	/// Note that this only serve as a hint, as does not indicate actual setting
	///
	/// @returns boolean  temp mode value
	public boolean getTempHint();
	
	/// Sets temp mode optimization indicator hint
	/// Note that this only serve as a hint, as does not indicate actual setting
	///
	/// @param  mode  the new temp mode hint
	///
	/// @returns boolean  previous value if set
	public boolean setTempHint(boolean mode);
	
	///
	/// Backend system setup / teardown
	///--------------------------------------------------------------------------
	
	/// Setsup the backend storage table, etc. If needed
	public void systemSetup();
	
	/// Teardown and delete the backend storage table, etc. If needed
	public void systemTeardown();
	
	/// perform increment maintenance, meant for minor changes between requests
	public default void incrementalMaintenance() {
		// For JStruct, both is same
		maintenance();
	}
	
	/// Perform maintenance, mainly removing of expired data if applicable
	public default void maintenance() {
		// does nothing?
	}
	
	/// 
	/// MetaObject operations
	///--------------------------------------------------------------------------
	
	/// Generates a new blank object, with a GUID
	public MetaObject newObject();
	
	/// Gets the MetaObject, regardless of its actual existance
	public MetaObject uncheckedGet(String _oid);
	
	/// PUT, returns the object ID (especially when its generated), note that this
	/// adds the value in a merger style. Meaning for example, existing values not explicitely
	/// nulled or replaced are maintained
	public MetaObject append(String _oid, Map<String, Object> obj);
	
	/// 
	/// Query operations (to optimize on specific implementation)
	///--------------------------------------------------------------------------
	
	/// Performs a search query, and returns the respective MetaObjects
	///
	/// @param   where query statement
	/// @param   where clause values array
	///
	/// @returns  The MetaObject[] array
	public default MetaObject[] query(String whereClause, Object[] whereValues) {
		return query(whereClause, whereValues, null, 0, 0);
	}
	
	/// Performs a search query, and returns the respective MetaObjects
	///
	/// @param   where query statement
	/// @param   where clause values array
	/// @param   query string to sort the order by, use null to ignore
	///
	/// @returns  The MetaObject[] array
	public default MetaObject[] query(String whereClause, Object[] whereValues, String orderByStr ) {
		return query(whereClause, whereValues, orderByStr, 0, 0);
	}
	
	/// Performs a search query, and returns the respective MetaObjects
	///
	/// @param   where query statement
	/// @param   where clause values array
	/// @param   query string to sort the order by, use null to ignore
	/// @param   offset of the result to display, use -1 to ignore
	/// @param   number of objects to return max
	///
	/// @returns  The MetaObject[] array
	public default MetaObject[] query(String whereClause, Object[] whereValues, String orderByStr, int offset, int limit ) {
		
		// The return list
		List<MetaObject> retList = null;
		
		// Setup the query, if needed
		if(whereClause == null) { //null gets all
			retList = new ArrayList<MetaObject>( this.values() );
		} else {
			Query queryObj = Query.build(whereClause, whereValues);
			retList = queryObj.search(this);
		}
		
		// Sort, offset, convert to array, and return
		return JStructUtils.sortAndOffsetListToArray(retList, orderByStr, offset, limit);
	}
	
	/// 
	/// Get from key names operations (to optimize on specific implementation)
	///--------------------------------------------------------------------------
	
	/// Performs a custom search by configured keyname
	/// 
	/// @param   keyName to lookup for
	///
	/// @returns  The MetaObject[] array
	public default MetaObject[] getFromKeyName(String keyName) {
		return getFromKeyName(keyName, null, -1, -1);
	}
	
	/// Performs a custom search by configured keyname
	/// 
	/// @param   keyName to lookup for
	/// @param   query string to sort the order by, use null to ignore
	///
	/// @returns  The MetaObject[] array
	public default MetaObject[] getFromKeyName(String keyName, String orderByStr) {
		return getFromKeyName(keyName, orderByStr, -1, -1);
	}
	
	/// Performs a custom search by configured keyname
	/// 
	/// @param   keyName to lookup for
	/// @param   query string to sort the order by, use null to ignore
	/// @param   offset of the result to display, use -1 to ignore
	/// @param   number of objects to return max
	///
	/// @returns  The MetaObject[] array
	public default MetaObject[] getFromKeyName(String keyName, String orderByStr, int offset, int limit) {
		
		// The return list
		List<MetaObject> retList = new ArrayList<MetaObject>();
		
		// Iterate the list, add if containsKey
		for( MetaObject obj : values() ) {
			if(obj.containsKey(keyName)) {
				retList.add(obj);
			}
		}
		
		// Sort, offset, convert to array, and return
		return JStructUtils.sortAndOffsetListToArray(retList, orderByStr, offset, limit);
	}
	
	/// Performs a custom search by configured keyname, and returns its ID array
	/// 
	/// @param   keyName to lookup for
	///
	/// @returns  The MetaObject[] array
	public default String[] getFromKeyName_id(String keyName) {
		// The return list
		List<String> retList = new ArrayList<String>();
		
		// Iterate the list, add if containsKey
		for( MetaObject obj : values() ) {
			if(obj.containsKey(keyName)) {
				retList.add(obj._oid());
			}
		}
		
		// Return
		return retList.toArray(new String[retList.size()]);
	}
	
	/// 
	/// MetaType handling, does type checking and conversion
	///--------------------------------------------------------------------------
	
	/// Gets and return the internal MetaTypeMap
	public MetaTypeMap typeMap();
	
	/// Get convinent function
	public default MetaType getType(String name) {
		return typeMap().get(name);
	}
	
	/// Put convinent function
	public default MetaType putType(String name, Object value) {
		return typeMap().put( name, value );
	}
	
	/// Generic varient of put all
	public default <K,V> void setMappingType(Map<K, V> m) {
		typeMap().putAllGeneric(m);
	}
}