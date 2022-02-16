package caching.datasharing;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ReplicatedMap;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

import common.AMQPCommon;

public class ProductService {

	public static void main(String[] args) throws Exception {
		ProductService service = new ProductService();
		service.startService();
	}
				
	public void startService() throws Exception {		
		Channel channel = AMQPCommon.connect();
		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume("product.q", true, consumer);
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        ReplicatedMap<Object, Object> cache = hz.getReplicatedMap("products");
        loadCache(cache);
        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println("");
		displayCache(cache);
        
		try {
			while (true) {
				QueueingConsumer.Delivery msg = consumer.nextDelivery(1000);
				if (msg != null) {
					String product = new String(msg.getBody());
					if (product.equalsIgnoreCase("show")) {
						displayCache(cache);
					} else {
						System.out.println("");
						String[] productInfo = product.split(",");
						System.out.println("--> ADDING PRODUCT: " + productInfo[1]);
						cache.put(productInfo[0], productInfo[1]);					
						displayCache(cache);
					}
				}
			}			
		} finally {
	        hz.shutdown();
			AMQPCommon.close(channel);
		}
	}		

	private void loadCache(ReplicatedMap<Object, Object> cache) {
        cache.put("1", "Fundamentals of Software Architecture");
        cache.put("2", "Architecture: The Hard Parts");
        cache.put("3", "The Architects Elevator");
        cache.put("4", "Building Micro-Frontends");
        cache.put("5", "Java Message Service 2nd Edition");
	}
	
	private void displayCache(ReplicatedMap<Object, Object> cache) {
		System.out.println("");
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
}
