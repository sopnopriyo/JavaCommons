package picoded.JCache;

import java.lang.String;

import picoded.JCache.*;
import picoded.JCache.dataStore.BaseInterface;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.Queue;

/// Database intreface base class.
public class JCache implements BaseInterface {
	
	/// Hazelcast implementation constructor, returns picoded.JCache.dataStore.JCache_redis
	///
	/// Setsup Hazelcast client, using the local clsuter name
	///
	/// @param  clustername  Hazelcast local cluster name to 'auto search' eg:"LocalHC"
	public static JCache hazelcast(String clustername) {
		return new picoded.JCache.dataStore.JCache_hazelcast(clustername);
	}
	
	/// @TODO : NOT YET IMPLEMENTED
	public static JCache hazelcast(String clustername, String password) {
		throw new RuntimeException(JCacheException.invalidDatastoreImplementationException);
	}
	
	/// @TODO : NOT YET IMPLEMENTED
	public static JCache hazelcast(String clustername, String password, String ipAddressWithPort) {
		throw new RuntimeException(JCacheException.invalidDatastoreImplementationException);
	}
	
	/// Hazelcast implementation constructor, returns picoded.JCache.dataStore.JCache_redis
	///
	/// Setsup Hazelcast client, using more complex config via HazelcastClientConfigObj
	/// You may refer to the source material for more complex setups
	///
	/// @see https://github.com/mrniko/redisson/wiki/Config-examples
	/// @see https://github.com/mrniko/redisson/blob/master/src/main/java/org/redisson/Config.java
	///
	/// @param  redissonConfigObj  redisson based config object
	public static JCache hazelcast(com.hazelcast.client.config.ClientConfig HazelcastClientConfigObj) {
		return new picoded.JCache.dataStore.JCache_hazelcast(HazelcastClientConfigObj);
	}
	
	/// Redis implementation constructor, returns picoded.JCache.dataStore.JCache_redis
	///
	/// Setsup Redis client, in single server mode
	///
	/// @param  ipAddressWithPort  IP address string, with port. eg:"127.0.0.1:6379"
	public static JCache redis(String ipAddressWithPort) {
		return new picoded.JCache.dataStore.JCache_redis(ipAddressWithPort);
	}
	
	/// Redis implementation constructor, returns picoded.JCache.dataStore.JCache_redis
	///
	/// Setsup Redis client, using more complex config via redissonConfigObject
	/// You may refer to the source material for more complex setups
	///
	/// @see https://github.com/mrniko/redisson/wiki/Config-examples
	/// @see https://github.com/mrniko/redisson/blob/master/src/main/java/org/redisson/Config.java
	///
	/// @param  redissonConfigObj  redisson based config object
	public static JCache redis(org.redisson.Config redissonConfigObj) {
		return new picoded.JCache.dataStore.JCache_redis(redissonConfigObj);
	}
	
	/// Internal refrence of the current JCache type the system is running as
	public JCacheType cacheType = JCacheType.invalid;
	
	/// Returns true, if dispose() function was called prior
	///
	/// @return boolean value, where true indicates the current cache connection has been terminated.
	public boolean isDisposed() {
		throw new RuntimeException(JCacheException.invalidDatastoreImplementationException);
	}
	
	/// Dispose of the respective SQL driver / connection
	public void dispose() {
		throw new RuntimeException(JCacheException.invalidDatastoreImplementationException);
	}
	
	/// Internal helper function that throws an exception, if connection is already 'disposed'
	public void throwIfIsDispose() throws JCacheException {
		if (isDisposed()) {
			throw new JCacheException(JCacheException.invalidDatastoreImplementationException);
		}
	}
	
	/// Gets a ConcurrentMap with the given name
	public <K, V> ConcurrentMap<K, V> getMap(String name) throws JCacheException {
		throw new JCacheException(JCacheException.invalidDatastoreImplementationException);
	}
	
	/// Gets a distributed concurrent lock
	///
	/// @param  name  The concurrent lock name
	public Lock getLock(String name) throws JCacheException {
		throw new JCacheException(JCacheException.invalidDatastoreImplementationException);
	}
	
	/// Gets a distributed concurrent Queue
	///
	/// @param  name  The concurrent Queue name
	public <K> Queue<K> getQueue(String name) throws JCacheException {
		throw new JCacheException(JCacheException.invalidDatastoreImplementationException);
	}
	
	/// Recreates the JCache connection if it has already been disposed of.
	/// Option to forcefully recreate the connection if needed.
	///
	/// @param  force  Boolean if true, forces the existing connection to terminate and be recreated
	public void recreate(boolean force) {
		throw new RuntimeException(JCacheException.invalidDatastoreImplementationException);
	}
	
	/// Recreates the JCache connection, if it is currently disposed of (force = false)
	///
	/// @see recreate(boolean force)
	public void recreate() {
		recreate(false);
	}
}
