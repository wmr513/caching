package ignite;

import org.apache.ignite.Ignition;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;

public class DistributedTest {

	public static void main(String[] args) throws Exception {
		System.out.println("Connecting to Apache Ignite server...");
		ClientConfiguration cfg = new ClientConfiguration().setAddresses("127.0.0.1:10800");
		IgniteClient ignite = Ignition.startClient(cfg);
		ClientCache<String, String> cache = ignite.getOrCreateCache("names");
		System.out.println("Connected");
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
