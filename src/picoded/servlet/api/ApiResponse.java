package picoded.servlet.api;

import java.util.*;

import picoded.conv.*;
import picoded.struct.*;

import picoded.struct.GenericConvertMap;
import picoded.struct.GenericConvertHashMap;
import picoded.servlet.CorePage;

/**
* API Request map information
* For the API function to process
**/
public class ApiResponse extends GenericConvertHashMap<String, Object> {


	//-----------------------------------------------------------------
	//
	//  Constructor vars
	//
	//-----------------------------------------------------------------

	/**
	* The base API builder
	**/
	protected ApiBuilder builder = null;

	//-----------------------------------------------------------------
	//
	//  Overwrites vars (that would have taken from builder instead)
	//
	//-----------------------------------------------------------------

	/**
	* CorePage overwrite
	**/
	protected CorePage corePage = null;

	//-----------------------------------------------------------------
	//
	//  Constructor
	//
	//-----------------------------------------------------------------

	/**
	* Initialize the class
	*
	* @param   Parent ApiBuilder
	**/
	ApiResponse( ApiBuilder parent ) {
		// Setup parent API builder object
		builder = parent;
	}

	//---------------------------------------------------------------------------------
	//
	// CorePage, and HttpServletResponse support
	//
	//---------------------------------------------------------------------------------

	/**
	* Gets and return the CorePage (if used)
	* Note: Currently this is protected until substential use case for public is found
	*
	* @return CorePage (if used)
	**/
	protected CorePage getCorePage() {
		if( corePage != null ) {
			return corePage;
		}

		if( builder != null && builder.corePageServlet != null ) {
			return builder.corePageServlet;
		}

		return null;
	}

	/**
	* Gets and return the java HttpServletRequest (if used)
	*
	* @return  HttpServletRequest (if used)
	**/
	public javax.servlet.http.HttpServletResponse getHttpServletResponse() {
		CorePage core = getCorePage();
		if( core != null ) {
			return core.getHttpServletResponse();
		}
		return null;
	}

}
