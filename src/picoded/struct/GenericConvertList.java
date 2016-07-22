package picoded.struct;

import java.util.List;
import java.util.UUID;

import picoded.conv.GenericConvert;

/**
 * The <tt>List</tt> interface provides four methods for positional (indexed)
 * access to list elements. Lists (like Java arrays) are zero based. Note that
 * these operations may execute in time proportional to the index value for some
 * implementations (the <tt>LinkedList</tt> class, for example). Thus, iterating
 * over the elements in a list is typically preferable to indexing through it if
 * the caller does not know the implementation.
 * <p>
 *
 * The <tt>List</tt> interface provides a special iterator, called a
 * <tt>ListIterator</tt>, that allows element insertion and replacement, and
 * bidirectional access in addition to the normal operations that the
 * <tt>Iterator</tt> interface provides. A method is provided to obtain a list
 * iterator that starts at a specified position in the list.
 * <p>
 *
 * The <tt>List</tt> interface provides two methods to search for a specified
 * object. From a performance standpoint, these methods should be used with
 * caution. In many implementations they will perform costly linear searches.
 * <p>
 *
 * The <tt>List</tt> interface provides two methods to efficiently insert and
 * remove multiple elements at an arbitrary point in the list.
 * <p>
 *
 * Note: While it is permissible for lists to contain themselves as elements,
 * extreme caution is advised: the <tt>equals</tt> and <tt>hashCode</tt> methods
 * are no longer well defined on such a list.
 *
 * <p>
 * This interface is a member of the <a href="{@docRoot}
 * /../technotes/guides/collections/index.html"> Java Collections Framework</a>.
 *
 * @param <E>
 *            the type of elements in this list
 *
 * @see List
 */

public interface GenericConvertList<E> extends UnsupportedDefaultList<E>{
	
	// to string conversion
	//--------------------------------------------------------------------------------------------------
	
	/// To String conversion of generic object
	///
	/// @param index       The input value index to convert
	/// @param fallbck   The fallback default (if not convertable, aka null)
	///
	/// @returns         The converted string, always possible unless null
	public default String getString(int index, String fallbck) {
		return GenericConvert.toString(get(index), fallbck);
	}
	
	/// Default null fallback, To String conversion of generic object
	///
	/// @param index       The input value index to convert
	///
	/// @returns         The converted string, always possible unless null
	public default String getString(int index) {
		return GenericConvert.toString(get(index));
	}
	
	// to boolean conversion
	//--------------------------------------------------------------------------------------------------
	
	/// To boolean conversion of generic object
	///
	/// @param index       The input value index to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted string, always possible unless null
	public default boolean getBoolean(int index, boolean fallbck) {
		return GenericConvert.toBoolean(get(index), fallbck);
	}
	
	/// Default boolean fallback, To String conversion of generic object
	///
	/// @param index       The input value index to convert
	///
	/// @returns         The converted string to boolean, always possible 
	public default boolean getBoolean(int index) {
		return GenericConvert.toBoolean(get(index));
	}
	
	// to Number conversion
	//--------------------------------------------------------------------------------------------------
	
	/// To Number conversion of generic object
	///
	/// @param index       The input value index to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted string, always possible unless null
	public default Number getNumber(int index, Number fallbck) {
		return GenericConvert.toNumber(get(index), fallbck);
	}
	
	/// Default Number fallback, To String conversion of generic object
	///
	/// @param index       The input value index to convert
	///
	/// @returns         The converted string, always possible unless null
	public default Number getNumber(int index) {
		return GenericConvert.toNumber(get(index));
	}
	
	// to int conversion
	//--------------------------------------------------------------------------------------------------
	
	/// To int conversion of generic object
	///
	/// @param index       The input value index to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted string, always possible unless null
	public default int getInt(int index, int fallbck) {
		return GenericConvert.toInt(get(index), fallbck);
	}
	
	/// Default int fallback, To String conversion of generic object
	///
	/// @param index       The input value index to convert
	///
	/// @returns         The converted string, always possible unless null
	public default int getInt(String string) {
		return GenericConvert.toInt(get(string));
	}
	
	// to long conversion
	//--------------------------------------------------------------------------------------------------
	
	/// To long conversion of generic object
	///
	/// @param index       The input value index to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted string, always possible unless null
	public default long getLong(int index, long fallbck) {
		return GenericConvert.toLong(get(index), fallbck);
	}
	
	/// Default long fallback, To String conversion of generic object
	///
	/// @param index       The input value index to convert
	///
	/// @returns         The converted string, always possible unless null
	public default long getLong(int index) {
		return GenericConvert.toLong(get(index));
	}
	
	// to float conversion
	//--------------------------------------------------------------------------------------------------
	
	/// To float conversion of generic object
	///
	/// @param index       The input value index to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted string, always possible unless null
	public default float getFloat(int index, float fallbck) {
		return GenericConvert.toFloat(get(index), fallbck);
	}
	
	/// Default float fallback, To String conversion of generic object
	///
	/// @param index       The input value index to convert
	///
	/// @returns         The converted string, always possible unless null
	public default float getFloat(int index) {
		return GenericConvert.toFloat(get(index));
	}
	
	// to double conversion
	//--------------------------------------------------------------------------------------------------
	
	/// To double conversion of generic object
	///
	/// @param index       The input value index to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted string, always possible unless null
	public default double getDouble(int index, double fallbck) {
		return GenericConvert.toDouble(get(index), fallbck);
	}
	
	/// Default float fallback, To String conversion of generic object
	///
	/// @param index       The input value index to convert
	///
	/// @returns         The converted string, always possible unless null
	public default double getDouble(int index) {
		return GenericConvert.toDouble(get(index));
	}
	
	// to byte conversion
	//--------------------------------------------------------------------------------------------------
	
	/// To byte conversion of generic object
	///
	/// @param index       The input value index to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted string, always possible unless null
	public default byte getByte(int index, byte fallbck) {
		return GenericConvert.toByte(get(index), fallbck);
	}
	
	/// Default float fallback, To String conversion of generic object
	///
	/// @param index       The input value index to convert
	///
	/// @returns         The converted string, always possible unless null
	public default byte getByte(int index) {
		return GenericConvert.toByte(get(index));
	}
	
	// to short conversion
	//--------------------------------------------------------------------------------------------------
	
	/// To short conversion of generic object
	///
	/// @param index       The input value index to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted string, always possible unless null
	public default short getShort(int index, short fallbck) {
		return GenericConvert.toShort(get(index), fallbck);
	}
	
	/// Default short fallback, To String conversion of generic object
	///
	/// @param index       The input value index to convert
	///
	/// @returns         The converted string, always possible unless null
	public default short getShort(int index) {
		return GenericConvert.toShort(get(index));
	}
	
	// to UUID / GUID
	//--------------------------------------------------------------------------------------------------
	
	/// To UUID conversion of generic object
	///
	/// @param index       The input value index to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted UUID, always possible unless null
	public default UUID getUUID(int index, Object fallbck) {
		return GenericConvert.toUUID(get(index), fallbck);
	}
	
	/// Default Null fallback, To UUID conversion of generic object
	///
	/// @param input     The input value to convert
	///
	/// @returns         The converted value
	public default UUID getUUID(int index) {
		return GenericConvert.toUUID(get(index));
	}
	
	/// To GUID conversion of generic object
	///
	/// @param index       The input value index to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted UUID, always possible unless null
	public default String getGUID(int index, Object fallbck) {
		return GenericConvert.toGUID(get(index), fallbck);
	}
	
	/// Default Null fallback, To GUID conversion of generic object
	///
	/// @param input     The input value to convert
	///
	/// @returns         The converted value
	public default String getGUID(int index) {
		return GenericConvert.toGUID(get(index));
	}
	
	// to list
	// @TODO generic list conversion
	//--------------------------------------------------------------------------------------------------
	
	/// To List<Object> conversion of generic object
	///
	/// @param index       The input value index to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted Object[], always possible unless null
	public default List<Object> getObjectList(int index, Object fallbck) {
		return GenericConvert.toObjectList(get(index), fallbck);
	}
	
	/// Default Null fallback, To List<Object> conversion of generic object
	///
	/// @param input     The input value to convert
	///
	/// @default         The converted value
	public default List<Object> getObjectList(int index) {
		return GenericConvert.toObjectList(index);
	}
	
	// to array
	// @TODO generic array conversion
	//--------------------------------------------------------------------------------------------------
	
	// to string array
	//--------------------------------------------------------------------------------------------------
	
	/// To String[] conversion of generic object
	///
	/// @param index       The input value index to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted String[], always possible unless null
	public default String[] getStringArray(int index, Object fallbck) {
		return GenericConvert.toStringArray(get(index), fallbck);
	}
	
	/// Default Null fallback, To String[] conversion of generic object
	///
	/// @param input     The input value to convert
	///
	/// @returns         The converted value
	public default String[] getStringArray(String string) {
		return GenericConvert.toStringArray(get(string));
	}
	
	// to object array
	//--------------------------------------------------------------------------------------------------
	
	/// To Object[] conversion of generic object
	///
	/// @param index       The input value index to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted Object[], always possible unless null
	public default Object[] getObjectArray(int index, Object fallbck) {
		return GenericConvert.toObjectArray(index, fallbck);
	}
	
	/// Default Null fallback, To Object[] conversion of generic object
	///
	/// @param input     The input value to convert
	///
	/// @default         The converted value
	public default Object[] getObjectArray(int index) {
		return GenericConvert.toObjectArray(index);
	}
	
	// NESTED object fetch (related to fully qualified indexs handling)
	//--------------------------------------------------------------------------------------------------
	
	///
	/// Gets an object from the List,
	/// That could very well be, a list inside a list, inside a map, inside a .....
	///
	/// Note that at each iteration step, it attempts to do a FULL index match first, 
	/// before the next iteration depth
	///
	/// @param index       The input index to fetch, possibly nested
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The fetched object, always possible unless fallbck null
	public default Object getNestedObject(String index, Object fallbck) {
		return GenericConvert.fetchNestedObject(this, index, fallbck);
	}
	
	///
	/// Default Null fallback, for `getNestedObject(index,fallback)`
	///
	/// @param index       The input index to fetch, possibly nested
	///
	/// @returns         The fetched object, always possible unless fallbck null
	public default Object getNestedObject(String index) {
		return getNestedObject(index,null); 
	}
	
	
}