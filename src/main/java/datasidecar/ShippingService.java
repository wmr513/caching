package caching.datasidecar;

import org.apache.ignite.Ignition;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

import common.AMQPCommon;

public class ShippingService {

	public static void main(String[] args) throws Exception {
		ShippingService service = new ShippingService();
		service.startService();
	}
				
	public void startService() throws Exception {		
		Channel channel = AMQPCommon.connect();
		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume("shipping.request.q", true, consumer);
		ClientConfiguration cfg = new ClientConfiguration().setAddresses("127.0.0.1:10800");
		IgniteClient ignite = Ignition.startClient(cfg);
		ClientCache<String, String> dataSidecar = ignite.getOrCreateCache("weight");
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println("Shipping Service Started.");
    
		try {
			while (true) {
				QueueingConsumer.Delivery msg = consumer.nextDelivery(1000);
				if (msg != null) {
					int totalWeight = 0;
					String items = new String(msg.getBody());
					String[] itemIds = items.split(",");
					for (int i=0;i<itemIds.length;i++) {
						long weight = new Long(dataSidecar.get(itemIds[i]));
						System.out.println("Weight of item " + itemIds[i] + " is " + weight + "kg");
						totalWeight += weight;
					}
					System.out.println("Total weight is " + totalWeight + "kg.");
					System.out.println("Determining shipping cost...");
					System.out.println();
				}
			}			
		} finally {
			AMQPCommon.close(channel);
		}
	}		
}
