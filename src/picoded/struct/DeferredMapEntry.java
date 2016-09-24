package picoded.struct;

import java.util.*;

/// Utiltiy function to create a Map.Entry, which gets the value only when requested
///
/// AKA. put / get is only called when needed. 
/// Allowing skipping value checks by key name to be optimized out.
///
/// This is used by default in UnssuportedDefaultMap
public class DeferredMapEntry<K extends Object, V extends Object> implements Map.Entry<K, V> {
	
	// Internal vars
	//----------------------------------------------
	
	protected K key = null;
	protected Map<K, V> sourceMap = null;
	
	// Constructor
	//----------------------------------------------
	
	/// Constructor with key and value
	public DeferredMapEntry(Map<K, V> map, K inKey) {
		sourceMap = map;
		key = inKey;
	}
	
	// Map.Entry operators
	//----------------------------------------------
	
	/// Returns the key corresponding to this entry.
	public K getKey() {
		return key;
	}
	
	/// Returns the value corresponding to this entry.
	public V getValue() {
		return sourceMap.get(key);
	}
	
	/// Compares the specified object with this entry for equality.
	@SuppressWarnings("unchecked")
	public boolean equals(Object o) {
		if (o instanceof Map.Entry) {
			Map.Entry<K, V> e1 = this;
			Map.Entry<K, V> e2 = (Map.Entry<K, V>) o;
			
			return ((e1.getKey() == null ? e2.getKey() == null : e1.getKey().equals(e2.getKey())) && (e1.getValue() == null ? e2
				.getValue() == null : e1.getValue().equals(e2.getValue())));
		}
		return false;
	}
	
	/// Returns the hash code value for this map entry.
	///
	/// Note that you should not rely on hashCode =[
	/// See: http://stackoverflow.com/questions/785091/consistency-of-hashcode-on-a-java-string
	public int hashCode() {
		return (getKey() == null ? 0 : getKey().hashCode()) ^ (getValue() == null ? 0 : getValue().hashCode());
	}
	
	/// Replaces the value corresponding to this entry with the specified value (optional operation).
	public V setValue(V value) {
		return sourceMap.put(key, value);
	}
	
}