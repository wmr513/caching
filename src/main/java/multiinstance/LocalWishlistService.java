package caching.multiinstance;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.ClientConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import common.AMQPCommon;

public class LocalWishlistService {

	long cacheSize = 0;
	
	public static void main(String[] args) throws Exception {
		LocalWishlistService service = new LocalWishlistService();
		service.startService();
	}
				
	public void startService() throws Exception {		
		Channel channel = AMQPCommon.connect();
		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume("wishlist.request.q", true, consumer);
		
		CacheConfiguration l_cfg = new CacheConfiguration("wishlist");
		l_cfg.setCacheMode(CacheMode.LOCAL);
		IgniteConfiguration l_ic = new IgniteConfiguration();
		l_ic.setClientMode(false);
		Ignite l_ignite = Ignition.start(l_ic);
		IgniteCache<String, String> cache = l_ignite.getOrCreateCache(l_cfg);

        
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
						} else if (productInfo[0].equals("REMOVE")) {
							System.out.println("--> REMOVING ITEM: " + database.get(productInfo[1]));
							if (!cache.containsKey(productInfo[1])) {
								System.out.println("Item not in cache, removing from database");
							} else {
								System.out.println("removing item from cache and database");
								cache.remove(productInfo[1]);
							}
							database.remove(productInfo[1]);
						} else {
							System.out.println("--> GETTING ITEM: " + database.get(productInfo[1]));
							if (!cache.containsKey(productInfo[1])) {
								System.out.println("Item not in cache, reading from database and adding to cache");
								cache.put(productInfo[1], database.get(productInfo[1]));
							} else {  
								System.out.println("Reading item from cache");
							}
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
	
	private void displayCache(IgniteCache<String, String> cache) {		
		System.out.println("-----------------------");
		System.out.println();
		System.out.println("Wishlist Cache:");
		for (int i=0;i<10;i++) {
			String key = new Integer(i).toString();
			if (cache.containsKey(key.toString())) {
				System.out.println(key + ":" + cache.get(key));
			}
		}
		System.out.println();
	}
}
