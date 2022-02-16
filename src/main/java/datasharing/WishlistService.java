package caching.datasharing;

import java.util.ArrayList;
import java.util.List;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ReplicatedMap;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

import common.AMQPCommon;

public class WishlistService {

	List<String> wishlistItems = new ArrayList<String>();
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
        ReplicatedMap<Object, Object> cache = hz.getReplicatedMap("products");
        displayCache(cache);
        displayWishlist(cache);

		try {
			while (true) {
				QueueingConsumer.Delivery msg = consumer.nextDelivery(1000);
				if (msg != null) {
					String productId = new String(msg.getBody());
					
					if (productId.equalsIgnoreCase("show")) {
						displayCache(cache);
						displayWishlist(cache);
					} else {					
						if (!cache.containsKey(productId)) {
							System.out.println("item does not exist");
						} else {
							wishlistItems.add(productId);
							displayWishlist(cache);
						}
					}
				}

				if (cache.size() != cacheSize) {
					displayCache(cache);
					cacheSize = cache.size();
				}
			}			
		} finally {
	        hz.shutdown();
			AMQPCommon.close(channel);
		}
	}	
	
	private void displayCache(ReplicatedMap<Object, Object> cache) {
		System.out.println("");
		System.out.println("Product Cache:");
		int i = 1;
		while (true) {
			String productId = new Integer(i).toString();
			if (cache.containsKey(productId)) {
		        System.out.println(i + ":" + cache.get(productId));
		        i++;
			} else {
				return;
			}
		}
	}

	private void displayWishlist(ReplicatedMap<Object, Object> cache) {
		System.out.println("");
		System.out.println("Wishlist Items for Mark:");
		for (String productId : wishlistItems) {
			System.out.println("    " + cache.get(productId));
		}
	}	
}
