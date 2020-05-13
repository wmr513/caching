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

		java.util.Scanner input = new java.util.Scanner(System.in);
		System.out.print("Enter customer id: ");
		String customerId = input.nextLine();
		String currentName = "Mark";
		if (cache.get(customerId) != null) {
			currentName = cache.get(customerId);
		}
		System.out.print("Hello " + currentName + ". Enter new name: ");
		String newName = input.nextLine();
		System.out.println();
		input.close();

		System.out.println("Name in cache for customerId " + customerId + ": " + currentName);
		if (currentName == null) { currentName = getNameFromDatabase(customerId, currentName); }
		System.out.println("Updating customerId " + customerId + " from " + currentName + " to " + newName);
		cache.put(customerId, newName);
		
		currentName = cache.get(customerId);
		System.out.println("Name in cache for customerId " + customerId + ": " + currentName);
		ignite.close();
	}
	
	public static String getNameFromDatabase(String customerId, String currentName) throws Exception {
		String name = currentName;
		System.out.println("Retrieving current name from database: " + name);
		Thread.sleep(2000);
		System.out.println("Name in cache for customerId " + customerId + ": " + name);
		System.out.println();
		Thread.sleep(1000);
		return name;
	}
}
