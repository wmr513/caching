package ignite;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;

public class SingleTest {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void main(String[] args) {

		System.out.println("Starting Apache Ignite...");
		CacheConfiguration cfg = new CacheConfiguration("names");
		cfg.setCacheMode(CacheMode.LOCAL);
		IgniteConfiguration ic = new IgniteConfiguration();
		ic.setClientMode(false);
		Ignite ignite = Ignition.start(ic);
		IgniteCache<String, String> cache = ignite.getOrCreateCache(cfg);
		System.out.println("Apache Ignite started");
		System.out.println();

		String customerId = getCustomerIdFromRequest();
		String newName = getNameFromRequest();

		String currentName = cache.get(customerId);
		System.out.println("Name in cache for customerId " + customerId + ": " + currentName);
		if (currentName == null) { currentName = getNameFromDatabase(customerId); }
		System.out.println("Updating customerId " + customerId + " from " + currentName + " to " + newName);
		cache.put(customerId, newName);
		
		currentName = cache.get(customerId);
		System.out.println("Name in cache for customerId " + customerId + ": " + currentName);
		ignite.close();
	}
	
	public static String getCustomerIdFromRequest() {
	    String customerId = "1";
	    System.out.println("Receiving customerId: 1");
		return customerId;
	}

	public static String getNameFromRequest() {
	    String name = "William";
	    System.out.println("Receiving name: William");
		return name;
	}
	
	public static String getNameFromDatabase(String customerId) {
		String name = "Mark";
		System.out.println("Retrieving current name from database: " + name);
		return name;
	}
}
