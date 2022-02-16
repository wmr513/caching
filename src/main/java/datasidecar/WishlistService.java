package caching.datasidecar;

import java.util.ArrayList;
import java.util.List;

import org.apache.ignite.Ignition;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;

import com.hazelcast.core.ReplicatedMap;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

import common.AMQPCommon;

public class WishlistService {

	List<String> wishlistItems = new ArrayList<String>();
	
	public static void main(String[] args) throws Exception {
		WishlistService service = new WishlistService();
		service.startService();
	}
				
	public void startService() throws Exception {		
		Channel channel = AMQPCommon.connect();
		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume("wishlist.request.q", true, consumer);
		ClientConfiguration cfg = new ClientConfiguration().setAddresses("127.0.0.1:10800");
		IgniteClient ignite = Ignition.startClient(cfg);
		ClientCache<String, String> dataSidecar = ignite.getOrCreateCache("desc");

		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println("Wishlist Service Started.");
		
        displayWishlist(dataSidecar);
        
		try {
			while (true) {
				QueueingConsumer.Delivery msg = consumer.nextDelivery(1000);
				if (msg != null) {
					String productId = new String(msg.getBody());
					
					if (productId.equalsIgnoreCase("show")) {
						displayWishlist(dataSidecar);
					} else {					
						if (!dataSidecar.containsKey(productId)) {
							System.out.println("item does not exist");
						} else {
							wishlistItems.add(productId);
							displayWishlist(dataSidecar);
						}
					}
				}
			}			
		} finally {
			AMQPCommon.close(channel);
		}
	}		

	private void displayWishlist(ClientCache<String, String> cache) {
		System.out.println("");
		System.out.println("Wishlist Items:");
		for (String productId : wishlistItems) {
			String[] parts = cache.get(productId).split(",");
			System.out.println("     " + parts[1] + " " + parts[0]);
		}
	}	
}
