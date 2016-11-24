package picoded.JStruct.module.site;

/// Java imports
import java.util.List;

import picoded.JStruct.JStruct;
import picoded.JStruct.MetaObject;
import picoded.JStruct.MetaTable;
import picoded.struct.GenericConvertArrayList;
import picoded.struct.GenericConvertHashMap;
import picoded.struct.GenericConvertList;
import picoded.struct.GenericConvertMap;
/// Picoded imports

///
/// A product listing system, which is built ontop of MetaTable, and AtomicLongMap
///
public class ProductListing {
	
	/// Inventory owner metatable
	protected MetaTable productOwner = null;
	
	/// Inventory listing
	public MetaTable productItem = null;
	
	/// Product list max size
	protected int productMax = 250;
	
	private static String ownerID = "_ownerID";
	
	/// Empty constructor
	public ProductListing() {
		//Does nothing : manual setup
	}
	
	public ProductListing(JStruct inStruct, String prefix, String listing) {
		setupStandardTables(inStruct, prefix, listing);
	}
	
	public ProductListing(JStruct inStruct, String prefix) {
		setupStandardTables(inStruct, prefix, null);
	}
	
	///
	/// Setup the standard tables, with the given JStruct
	///
	/// @param   The JStruct object to build ontop of
	/// @param   The table name prefix to generate the various meta table
	///
	public void setupStandardTables(JStruct inStruct, String prefix, String listing) {
		productOwner = inStruct.getMetaTable(prefix);
		if (listing == null || listing.length() < 4) {
			productItem = inStruct.getMetaTable(prefix + "_productList");
		} else {
			productItem = inStruct.getMetaTable(prefix + "_" + listing);
		}
	}
	
	///
	/// Calls the systemSetup for the underlying MetaTable / AtomicLongMap
	///
	public void systemSetup() {
		productOwner.systemSetup();
		productItem.systemSetup();
	}
	
	///
	/// Calls the systemSetup for the underlying MetaTable / AtomicLongMap
	///
	public void systemTeardown() {
		productOwner.systemTeardown();
		productItem.systemTeardown();
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	//
	// Product listing
	//
	/////////////////////////////////////////////////////////////////////////////////////////
	
	///
	/// Gets a list of products assigned under an id
	///
	/// @param  The ownerID/eventID/ID assigned
	///
	/// @return List of meta objects representing the owner
	///
	public GenericConvertList<MetaObject> getList(String ownerID) {
		// Sanity check
		if (ownerID == null || ownerID.isEmpty()) {
			throw new RuntimeException("Missing ownerID");
		}
		
		// Return object
		GenericConvertList<MetaObject> ret = new GenericConvertArrayList<MetaObject>();
		
		// Fetch and populate
		MetaObject[] queryRet = productItem.query(ownerID + "=?", new String[] { ownerID },
			"_createdTime", 0, productMax);
		if (queryRet != null && queryRet.length > 0) {
			for (int i = 0; i < queryRet.length; ++i) {
				ret.add(queryRet[i]);
			}
		}
		
		// Return
		return ret;
	}
	
	///
	/// Updates the product listing under an id
	///
	/// @param  The id assigned
	/// @param  List of product objects to insert / update
	///
	/// @return List of meta objects representing the owner
	///
	public List<MetaObject> updateList(String ownerID, List<Object> inUpdateList) {
		//
		// Sanity check
		//
		if (ownerID == null || ownerID.isEmpty()) {
			throw new RuntimeException("Missing ownerID");
		}
		
		MetaObject ownerObj = productOwner.get(ownerID);
		if (ownerObj == null) {
			throw new RuntimeException("Missing product owner object for : " + ownerID);
		}
		
		if (inUpdateList == null) {
			throw new RuntimeException("Missing update list");
		}
		
		// Existing product list from ownerID
		GenericConvertList<MetaObject> prodList = getList(ownerID);
		
		// Update list to use
		GenericConvertList<Object> updateList = GenericConvertList.build(inUpdateList);
		
		//
		// Iterate the update list, updating if need be. La, la la
		//
		int iLen = updateList.size();
		for (int i = 0; i < iLen; ++i) {
			// Ensure it is a new object, avoid meta object changes bugs
			GenericConvertMap<String, Object> updateProduct = updateList.getGenericConvertStringMap(i,
				null);
			
			// Skip null rows
			if (updateProduct == null) {
				continue;
			}
			
			// Make new object, clone the values
			updateProduct = new GenericConvertHashMap<String, Object>(updateProduct);
			
			// Product _oid
			String update_oid = updateProduct.getString("_oid", null);
			if (update_oid != null && "new".equalsIgnoreCase(update_oid)) {
				update_oid = null;
			}
			
			// Sanitize updateProduct
			updateProduct = sanatizePurchaseData(updateProduct);
			
			// The meta object to create / update
			MetaObject updateMetaObject = null;
			
			// Get the meta object to "update"
			if (update_oid != null) {
				// Old meta object
				updateMetaObject = productItem.get(update_oid);
				
				// Security validation of owner ID
				if (!ownerID.equals(updateMetaObject.get(ownerID))) {
					throw new SecurityException("Unauthorized update call to object " + update_oid
						+ " with invalid ownerID " + ownerID);
				}
			} else {
				// New meta object
				updateMetaObject = productItem.newObject();
				updateMetaObject.put(ownerID, ownerID);
				updateMetaObject.saveDelta();
				
				prodList.add(updateMetaObject);
			}
			
			// Update the meta values
			updateMetaObject.putAll(updateProduct);
			
			//
			// @TODO : inventory quantity management
			//
			
			// Save
			updateMetaObject.saveDelta();
		}
		
		return prodList;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	//
	// Sales purchase order [utils]
	//
	/////////////////////////////////////////////////////////////////////////////////////////
	
	///
	/// Sanatizes a map data from protected purchase order data
	///
	/// @param Map to sanatize and return
	///
	/// @return The parameter
	///
	protected GenericConvertMap<String, Object> sanatizePurchaseData(
		GenericConvertMap<String, Object> inMap) {
		// Sanatize the item info
		inMap.remove("_oid");
		inMap.remove("_orderID");
		inMap.remove("_sellerID");
		
		inMap.remove(ownerID);
		inMap.remove("_ownerMeta");
		
		inMap.remove("_productID");
		inMap.remove("_productMeta");
		
		inMap.remove("_orderStatus");
		
		// Other systems reserved vars
		inMap.remove("_createTime");
		inMap.remove("_updateTime");
		
		// Reserved and not in use?
		inMap.remove("_purchaserID");
		return inMap;
	}
}
