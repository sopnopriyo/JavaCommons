package picoded.RESTBuilder.templates;

import java.util.*;
import java.util.Map.Entry;

import picoded.RESTBuilder.*;
import picoded.JStack.*;
import picoded.JStruct.*;
import picoded.servlet.*;
import picoded.conv.ConvertJSON;
import picoded.enums.HttpRequestType;

/// Account login template API
public class MetaTableApiBuilder {
	
	/////////////////////////////////////////////
	//
	// Common error messages
	//
	/////////////////////////////////////////////
	
	public static final String MISSING_REQUEST_PAGE = "Unexpected Exception: Missing requestPage()";
	public static final String MISSING_LOGIN_SESSION = "Authentication Error: Missing login session";
	public static final String MISSING_PERMISSION = "Permission Error: Missing permission for request (generic)";
	public static final String MISSING_PERMISSION_GROUP = "Permission Error: Missing group admin rights required for request";
	
	private MetaTable _metaTableObj = null;
	
	// Constructor
	public MetaTableApiBuilder(MetaTable metaTableObj) {
		_metaTableObj = metaTableObj;
	}
	
	/////////////////////////////////////////////
	//
	// Utility functions
	//
	/////////////////////////////////////////////
	
	/////////////////////////////////////////////
	//
	// Authenticator overwirte
	//
	/////////////////////////////////////////////
	
	@FunctionalInterface
	public interface MetaTableAPI_authenticator {
		public abstract boolean auth(BasePage requestPage, MetaTable requestedTable, MetaObject requestedObject, String apiRequestType );
	}
	
	/// Authentication filter, if null there is no authentication check for GET request (default behaviour)
	public MetaTableAPI_authenticator list_authenticationFilter = null;
	/// Authentication filter, if null there is no authentication check for GET request (default behaviour)
	public MetaTableAPI_authenticator get_authenticationFilter = null;
	/// Authentication filter, if null there is no authentication check for GET request (default behaviour)
	public MetaTableAPI_authenticator post_authenticationFilter = null;
	
	/////////////////////////////////////////////
	//
	// List searching API
	//
	/////////////////////////////////////////////
	
	///
	/// # list (POST / GET) [Requires login]
	///
	/// Lists the MetaObjects according to the search criteria
	///
	/// This JSON api is compatible with the datatables.js server side API.
	/// See: https://web.archive.org/web/20140627100023/http://datatables.net/manual/server-side
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	    | Description                                                                  |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | draw            | int (optional)     | Draw counter echoed back, and used by the datatables.js server-side API       |
	/// | start           | int (optional)     | Default 0: Record start listing, 0-indexed                                    |
	/// | length          | int (optional)     | Default 50: The number of records to return                                   |
	/// | orderBy         | String (optional)  | Default : order by _oid                                                       |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | headers         | String[](optional) | Default ["_oid"], the columns to return                                       |
	/// | query           | String (optional)  | Requested Query filter                                                        |
	/// | queryArgs       | String[] (optional)| Requested Query filter arguments                                              |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// 
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	    | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | draw            | int (optional)     | Draw counter echoed back, and used by the datatables.js server-side API       |
	/// | recordsTotal    | int (not critical) | Total amount of records. Before any search filter (But after base filters)    |
	/// | recordsFiltered | int (not critical) | Total amount of records. After all search filter                              |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | headers         | String[](optional) | Default ["_oid"], the collumns to return                                      |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | data            | array              | Array of row records                                                          |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | error           | String (Optional)  | Errors encounted if any                                                       |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public RESTFunction list_GET_and_POST = (req, res) -> {
		int draw = req.getInt("draw");
		int start = req.getInt("start");
		int limit = req.getInt("length");
		
		String orderByStr = req.getString("orderBy");
		if(orderByStr == null || orderByStr.isEmpty()){
			orderByStr = "oID";
		}
		
		String[] headers = req.getStringArray("headers");
		
		if(headers == null || headers.length < 1){
			headers = new String[] { "_oid" };
		}
		
		String query = req.getString("query");
		String[] queryArgs = req.getStringArray("queryArgs");
		
		//put back into response
		res.put("draw", draw);
		res.put("headers", headers);
		
		res.put("recordsTotal", _metaTableObj.size());
		if(query != null && !query.isEmpty() && queryArgs != null && queryArgs.length > 0){
			res.put("recordsFiltered", _metaTableObj.queryCount(query, queryArgs));
		}
		
		List<List<Object>> data = null;
		try{
			data = list_GET_and_POST_inner(draw, start, limit, headers, query, queryArgs, orderByStr);
			res.put("data",  data);
		}catch(Exception e){
			throw new RuntimeException(e);
		}
		
		return res;
	};
	
	public List<List<Object>> list_GET_and_POST_inner(int draw, int start, int length, String[] headers, String query, String[] queryArgs, String orderBy) throws RuntimeException{
		if(_metaTableObj == null){
			return null;
		}
		
		List<List<Object>> ret = new ArrayList<List<Object>>();
		
		try{
			if(headers != null && headers.length > 0){
				MetaObject[] metaObjs = null;
				
				if(query == null || query.isEmpty() || queryArgs == null || queryArgs.length == 0){
					metaObjs = _metaTableObj.query(null, null, orderBy, start, length);
				}else{
					metaObjs = _metaTableObj.query(query, queryArgs, orderBy, start, length);
				}
				
				for(MetaObject metaObj : metaObjs){
					List<Object> row = new ArrayList<Object>();
					for(String header : headers){
						row.add( metaObj.get(header) );
					}
					ret.add(row);
				}
			}
			
		}catch(Exception e){
			throw new RuntimeException("list_GET_and_POST_inner() ", e);
		}
		
		return ret;
	}
	
	/////////////////////////////////////////////
	//
	// Meta details operations
	//
	/////////////////////////////////////////////
	
	///
	/// # meta/${ObjectID} (GET) [Requires login]
	///
	/// Gets and return the meta object user info
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	   | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | _oid            | String             | object ID used to retrieve the meta object. If no oid is given, return null.  |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// 
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	    | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | _oid            | String             | The internal object ID used (or created)                                      |
	/// | meta            | {Object}           | Meta object that represents this account                                      |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | error           | String (Optional)  | Errors encounted if any                                                       |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public RESTFunction meta_GET = (req, res) -> {
		String oid = req.getString("_oid");
		if(oid == null) {
			oid = (req.rawRequestNamespace() != null)? req.rawRequestNamespace()[0] : null;
		}
		
		//put data back into response
		res.put("_oid", oid);
		
		try{
			MetaObject mObj = meta_GET_inner(oid);
			res.put("meta", mObj);
		}catch(Exception e){
			throw new RuntimeException(e);
		}
		
		return res;
	};
	
	public MetaObject meta_GET_inner(String oid) throws RuntimeException{
		if(_metaTableObj == null){
			return null;
		}
		
		if(oid == null || oid.isEmpty()){
			return null;
		}
		
		MetaObject[] metaObjs = null;
		metaObjs = _metaTableObj.query("_oid=?", new String[]{oid});
		
		
		if(metaObjs.length > 1){
			throw new RuntimeException("meta_GET_inner() -> More than 1 meta object was returned for _oid : "+oid);
		}else if(metaObjs.length < 1){
			return null;
		}else{
			return metaObjs[0];
		}
	}
	
	///
	/// # meta/${ObjectID} (POST) [Requires login]
	///
	/// Updates the accountID meta info, requires either the current user or SuperUser
	///
	/// Note: if ${ObjectID} is "new", it creates a new object
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	   | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | _oid            | String             | The internal object ID used (or created)                                      |
	/// | meta            | {Object}           | Meta object that represents this account                                      |
	/// | updateMode      | String (Optional)  | (Default) "delta" for only updating the given fields, or "full" for all       |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// 
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	   | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | updateMode      | String             | (Default) "delta" for only updating the given fields, or "full" for all       |
	/// | updateMeta      | {Object}           | The updated changes done                                                      |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | error           | String (Optional)  | Errors encounted if any                                                       |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public RESTFunction meta_POST = (req, res) -> {
		String oid = req.getString("_oid");
		if(oid == null) {
			oid = (req.rawRequestNamespace() != null)? req.rawRequestNamespace()[0] : null;
		}
		
		//put data back into response
		res.put("_oid", oid);
		
		String updateMode = req.getString("updateMode");
		res.put("updateMode", updateMode);
		res.put("updateMeta", null);
		
		Map<String, Object> givenMObj = null;
		if( req.get("meta") instanceof String ){
			String jsonMetaString = req.getString("meta");
			if(jsonMetaString != null && !jsonMetaString.isEmpty()){
				givenMObj = ConvertJSON.toMap(jsonMetaString);
			}
		}
		
		if(givenMObj != null){
			try{
				MetaObject updatedMObj = meta_POST_inner(oid, givenMObj, updateMode);
				res.put("updateMeta",updatedMObj);
			}catch(Exception e){
				throw new RuntimeException(e);
			}
		}else{
			res.put("error", "No meta object was found in the request");
		}
		
		return res;
	};

	public MetaObject meta_POST_inner(String oid, Map<String,Object> mObj, String updateMode) throws RuntimeException {
		boolean updateModeFull = false;
		if(updateMode != null && updateMode.equalsIgnoreCase("full")){
			updateModeFull = true;
		}
		
		MetaObject ret = null;
		
		try{
			if(oid.equalsIgnoreCase("new")){
				ret = _metaTableObj.append(null,  mObj);
				ret.saveAll();
				return ret;
			}else{
				//
				// Get the meta object from id
				//
				ret = _metaTableObj.get(oid);
				
				//
				// Terminate if null (no object)
				//
				if( ret == null ) {
					return null;
				}
				
				//
				// Else update the update withdata
				//
				if(updateModeFull){
					//full overwrite
					ret.clear();
					ret.putAll(mObj);
					ret.saveAll();
					return ret;
				}else{
					//delta update
					ret.putAll(mObj);
					ret.saveDelta();
					return ret;
				}
			}
		}catch(Exception e){
			throw new RuntimeException("meta_POST_inner()", e);
		}
	}
	
	///
	/// # delete/${ObjectID} (DELETE) [Requires login]
	///
	/// Deletes the current oid object from the table
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	   | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | _oid            | String             | The internal object ID to delete                                              |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// 
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	   | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | _oid            | String             | Returns oid of metaObject to delete                                           |
	/// | deleted         | boolean            | Returns true ONLY if the element was removed from the table                   |
	/// | error           | String (Optional)  | Errors encounted if any                                                       |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public RESTFunction meta_DELETE = (req, res) -> {
		String accountID = req.getString("_oid");
		
		if(accountID == null || accountID.isEmpty()){
			return res;
		}
		
		res.put("_oid", accountID);
		
		boolean deleted = meta_DELETE_inner(accountID);
		
		res.put("deleted", deleted);
		
		return res;
	};
	
	public boolean meta_DELETE_inner(String oid){
		if(_metaTableObj.containsKey(oid)){
			MetaObject removed = _metaTableObj.remove(oid);
			if(removed != null){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}


	//-------------------------------------------------------------------------------------------------------------------------
	//
	// Work in progress (not final) start
	//
	//-------------------------------------------------------------------------------------------------------------------------
	
	
	//-------------------------------------------------------------------------------------------------------------------------
	//
	// Work in progress (not final) end
	//
	//-------------------------------------------------------------------------------------------------------------------------
	
	/////////////////////////////////////////////
	//
	// RestBuilder template builder
	//
	/////////////////////////////////////////////
	
	///
	/// Takes the restbuilder and the account table object and implements its respective default API
	///
	public RESTBuilder setupRESTBuilder(RESTBuilder rb, String setPrefix ) {
		rb.getNamespace( setPrefix + "list" ).put( HttpRequestType.GET, list_GET_and_POST );
		rb.getNamespace( setPrefix + "list" ).put( HttpRequestType.POST, list_GET_and_POST );
		
		rb.getNamespace( setPrefix + "meta.*" ).put( HttpRequestType.GET, meta_GET );
		rb.getNamespace( setPrefix + "meta.*" ).put( HttpRequestType.POST, meta_POST );
		
		rb.getNamespace( setPrefix + "meta" ).put( HttpRequestType.GET, meta_GET );
		rb.getNamespace( setPrefix + "meta" ).put( HttpRequestType.POST, meta_POST );
		
		rb.getNamespace( setPrefix + "meta" ).put( HttpRequestType.DELETE, meta_DELETE );
		rb.getNamespace( setPrefix + "meta.*" ).put( HttpRequestType.DELETE, meta_DELETE );
		
		return rb;
	}
	
	/////////////////////////////////////////////
	//
	// Servlet constructor and setup
	//
	/////////////////////////////////////////////
//	
//	/// Default api set prefix
//	protected static String _apiSetPrefix_prefix = "account.";
//	
//	/// Internal prefix set for the API
//	protected String _apiSetPrefix = _apiSetPrefix_prefix;
//	
//	/// The prefix for the api
//	public String apiSetPrefix() {
//		return _apiSetPrefix;
//	}
//		
//	/// The prefix for the api
//	public void setApiSetPrefix(String api) {
//		_apiSetPrefix = api;
//	}
//	
//	/// Flags as JSON request
//	public boolean isJsonRequest() {
//		return true;
//	}
//	
//	/// Does the default setup
//	public void doSetup() throws Exception {
//		super.doSetup();
//		setupRESTBuilder( this.restBuilder(), this.accountAuthTable(), _apiSetPrefix );
//	}
//	
//	/// Process the request, not the authentication layer
//	public boolean doJSON(Map<String,Object> outputData, Map<String,Object> templateData) throws Exception {
//		return restBuilder().servletCall( _apiSetPrefix, this, outputData );
//	}

}