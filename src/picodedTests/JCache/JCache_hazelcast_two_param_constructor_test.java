package picodedTests.JCache;

import org.junit.*;
import static org.junit.Assert.*;
import java.lang.System;
import java.util.*;

import com.hazelcast.config.*;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import picoded.JCache.*;
import picodedTests.JCache.LocalCacheSetup;
import java.util.concurrent.ConcurrentMap;

public class JCache_hazelcast_two_param_constructor_test extends picodedTests.JCache.JCache_redis_test {
	
	static protected String hazelcastClusterName;
	static protected ClientConfig hazelcastConfig;
	
	static protected JCache hazelcastJCacheObj = null;
	
	static protected final String PASSWORD = "password123";
	
	/// Setsup the testing server
	@BeforeClass
	public static void oneTimeSetUp() {
		// one-time initialization code
		hazelcastClusterName = LocalCacheSetup.setupHazelcastServer(PASSWORD);
		
		// Config to use
		hazelcastConfig = new ClientConfig();
		hazelcastConfig.getGroupConfig().setName(hazelcastClusterName);
		hazelcastConfig.getGroupConfig().setPassword(PASSWORD);
		hazelcastConfig.setProperty("hazelcast.logging.type", "none");
		
	}
	
	/// Dispose the testing server
	@AfterClass
	public static void oneTimeTearDown() {
		
		// Close JCache if needed (reduce false error)
		if (hazelcastJCacheObj != null) {
			hazelcastJCacheObj.dispose();
			hazelcastJCacheObj = null;
		}
		
		try {
			Thread.sleep(5000);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		// one-time cleanup code
		LocalCacheSetup.teardownHazelcastServer();
	}
	
	/// Setsup the JCache object
	@Before
	public void setUp() {
		//JCacheObj = JCache.hazelcast(hazelcastClusterName);
		JCacheObj = JCache.hazelcast(hazelcastClusterName, PASSWORD);
		hazelcastJCacheObj = JCacheObj;
		
	}
	
	/// Dispose the JCache object
	@After
	public void tearDown() {
		if (JCacheObj != null) {
			JCacheObj.dispose();
			JCacheObj = null;
		}
	}
	
	@Test
	public void constructorTest() {
		/// Test case setup
		assertNotNull("JCacheObj object must not be null", JCacheObj);
		JCacheObj.dispose();
		JCacheObj = null;
		
		/// Constructor by config object
		JCacheObj = JCache.hazelcast(hazelcastConfig);
		assertNotNull("JCacheObj object must not be null", JCacheObj);
		JCacheObj.dispose();
		JCacheObj = null;
		
		/// Constructor by string, string
		JCacheObj = JCache.hazelcast(hazelcastClusterName, PASSWORD);
		assertNotNull("JCacheObj object must not be null", JCacheObj);
		JCacheObj.dispose();
		JCacheObj = null;
	}
	
}