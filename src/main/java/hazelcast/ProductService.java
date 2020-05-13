package hazelcast;

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
        
		try {
			while (true) {
				QueueingConsumer.Delivery msg = consumer.nextDelivery(1000);
				if (msg != null) {
					String product = new String(msg.getBody());
					System.out.println("");
					String[] productInfo = product.split(",");
					System.out.println("--> ADDING PRODUCT: " + productInfo[1]);
					cache.put(productInfo[0], productInfo[1]);					
				}
				displayCache(cache);
			}			
		} finally {
	        hz.shutdown();
			AMQPCommon.close(channel);
		}
	}		

	private void loadCache(ReplicatedMap<Object, Object> cache) {
        cache.put("1", "Apple World Travel Adapter Kit");
        cache.put("2", "BUMB Cable Bag Travel Organizer");
        cache.put("3", "BatPower ProE Portable Charger");
        cache.put("4", "Tripod Desk Microphone Stand");
        cache.put("5", "Apple Lightning to 3.5mm Adapter");
        displayCache(cache);
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
