package picoded.JStruct;

/// Java imports
import java.util.*;
import picoded.struct.*;

/// Represents a single object node in the MetaTable collection.
///
/// NOTE: This class should not be initialized directly, but through MetaTable class
public interface MetaObject extends GenericConvertMap<String, Object> {
	
	/// The object ID
	public String _oid();
	
	/// Gets and return its current value
	public Object get(Object key);
	
	/// Put and set its delta value, set null is considered "remove"
	public Object put(String key, Object value);
	
	/// Remove operation
	public Object remove(Object key);
	
	/// Gets and return valid keySet()
	public Set<String> keySet();
	
	/// Save the delta changes to storage
	public void saveDelta();
	
	/// Save all the configured data, ignore delta handling
	public void saveAll();
	
}