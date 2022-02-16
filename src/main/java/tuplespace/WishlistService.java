package caching.tuplespace;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
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
        
        cache.put("1", "Printer");
		cache.put("2", "Laptop");
		displayCache(cache);

		try {
			while (true) {
				QueueingConsumer.Delivery msg = consumer.nextDelivery(1000);
				if (msg != null) {
					String product = new String(msg.getBody());
					String[] productInfo = product.split(",");
					
					if (productInfo[1].equalsIgnoreCase("show")) {
						displayCache(cache);
					} else {					
						System.out.println("");
						if (productInfo[0].equals("ADD")) {
							System.out.println("--> ADDING ITEM: " + productInfo[2]);
							cache.put(productInfo[1], productInfo[2]);
						} else {
							System.out.println("--> REMOVING ITEM: " + cache.get(productInfo[1]));
							if (!cache.containsKey(productInfo[1])) {
								System.out.println("Item not in cache");
							} else {
								cache.remove(productInfo[1]);
							}
						}
						//now 'datapump' the data to the writer...						
						System.out.println("--> DATAPUMP DATA...");
						byte[] message = msg.getBody();
						String routingKey = "datapump.q";
						channel.basicPublish("", routingKey, null, message);
					}
				}

				if (cache.size() != cacheSize) {
					cacheSize = cache.size();
					displayCache(cache);
				}
			}			
		} finally {
	        hz.shutdown();
			AMQPCommon.close(channel);
		}
	}	
		
	private void displayCache(ReplicatedMap<Object, Object> cache) {		
		System.out.println();
		System.out.println("-----------------------");
		System.out.println("Wishlist Cache:");
		cache.entrySet().forEach(entry -> {
		    System.out.println( entry.getKey() + ":" + entry.getValue() );
		});
	}
}
