package picoded.struct.query.condition;

import java.util.*;
import picoded.struct.query.*;

public class And extends CombinationBase {
	
	//
	// Constructor Setup
	//--------------------------------------------------------------------
	
	/// The constructor with the field name, and default argument
	///
	/// @param   set of children queries
	/// @param   default argument map to get test value
	///
	public And(Set<Query> child, Map<String,Object> defaultArgMap) {
		super(child, defaultArgMap);
	}
	
	/// The constructor with the field name, and default argument
	///
	/// @param   set of children queries
	/// @param   default argument map to get test value
	///
	public And(List<Query> child, Map<String,Object> defaultArgMap) {
		super(child, defaultArgMap);
	}
	
	//
	// Required overwrites
	//--------------------------------------------------------------------
	
	/// To test against a specified value map,
	/// Note that the test varient without the Map
	/// is expected to test against its cached varient
	/// that is setup by the constructor if applicable
	///
	/// [to override on extension]
	///
	/// @param   the object to test against
	/// @param   the argument map, if applicable
	///
	/// @returns  boolean indicating true / false
	public boolean test(Object t, Map<String,Object>argMap) {
		boolean result = false; //blank combination is a failure
		
		for(Query child : _children) {
			if( child.test(t, argMap) ) {
				result = true;
			} else {
				return false; //breaks and return false on first failure
			}
		}
		
		return result;
	}
	
	/// Gets the query type 
	///
	/// [to override on extension]
	public QueryType type() {
		return QueryType.AND;
	}
	
	/// The operator symbol support
	///
	/// [to override on extension]
	public String operatorSymbol() {
		return "AND";
	}
	
} 
