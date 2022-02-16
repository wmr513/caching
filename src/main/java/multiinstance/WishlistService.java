package caching.multiinstance;

import org.apache.ignite.Ignition;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ReplicatedMap;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

import common.AMQPCommon;

public class WishlistService {

	long cacheSize = 0;
	
	public static void main(String[] args) throws Exception {
		WishlistService service = new WishlistService();
		service.startService();
	}
				
	public void startService() throws Exception {		
		Channel channel = AMQPCommon.connect();
		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume("wishlist.request.q", true, consumer);
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        ReplicatedMap<Object, Object> cache = hz.getReplicatedMap("wishlist");
        
        //simulate the database using Ignite distributed cache
		ClientConfiguration cfg = new ClientConfiguration().setAddresses("127.0.0.1:10800");
		IgniteClient ignite = Ignition.startClient(cfg);
		ClientCache<String, String> database = ignite.getOrCreateCache("wishlistdb");
		System.out.println("connected to db");
        
        loadWishlistDB(database);
        displayCache(cache);
        displayDatabase(database);

		try {
			while (true) {
				QueueingConsumer.Delivery msg = consumer.nextDelivery(1000);
				if (msg != null) {
					String product = new String(msg.getBody());
					String[] productInfo = product.split(",");
					
					if (productInfo[1].equalsIgnoreCase("show")) {
						displayCache(cache);
						displayDatabase(database);
					} else {					
						System.out.println("");
						if (productInfo[0].equals("ADD")) {
							System.out.println("--> ADDING ITEM: " + productInfo[2]);
							System.out.println("adding item to cache and database");
							cache.put(productInfo[1], productInfo[2]);
							database.put(productInfo[1], productInfo[2]);
						} else {
							System.out.println("--> REMOVING ITEM: " + database.get(productInfo[1]));
							if (!cache.containsKey(productInfo[1])) {
								System.out.println("Item not in cache, removing from database");
							} else {
								System.out.println("removing item from cache and database");
								cache.remove(productInfo[1]);
							}
							database.remove(productInfo[1]);
						}
						displayCache(cache);					
						displayDatabase(database);
					}
				}

				if (cache.size() != cacheSize) {
					cacheSize = cache.size();
					displayCache(cache);
					displayDatabase(database);
				}
			}			
		} finally {
	        hz.shutdown();
			AMQPCommon.close(channel);
		}
	}	
	
	private void loadWishlistDB(ClientCache<String, String> db) {
		db.put("1", "Printer");
		db.put("2", "Laptop");
		db.put("3", "Monitor");
	}
	
	private void displayDatabase(ClientCache<String, String> db) {
		System.out.println("Wishlist Database:");
		for (int i=0;i<10;i++) {
			String key = new Integer(i).toString();
			if (db.containsKey(key.toString())) {
				System.out.println(key + ":" + db.get(key));
			}
		}
		System.out.println();
	}
	
	private void displayCache(ReplicatedMap<Object, Object> cache) {		
		System.out.println("-----------------------");
		System.out.println();
		System.out.println("Wishlist Cache:");
		cache.entrySet().forEach(entry -> {
		    System.out.println( entry.getKey() + ":" + entry.getValue() );
		});
		System.out.println();
	}
}
