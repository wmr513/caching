package caching.datasidecar;

import org.apache.ignite.Ignition;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import common.AMQPCommon;

public class CatalogService {

	public static void main(String[] args) throws Exception {
		CatalogService service = new CatalogService();
		service.startService();
	}
				
	public void startService() throws Exception {		
		Channel channel = AMQPCommon.connect();
		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume("product.q", true, consumer);
		ClientConfiguration cfg = new ClientConfiguration().setAddresses("127.0.0.1:10800");
		IgniteClient ignite = Ignition.startClient(cfg);
		ClientCache<String, String> descCache = ignite.getOrCreateCache("desc");
		ClientCache<String, String> sizeCache = ignite.getOrCreateCache("size");
		ClientCache<String, String> weightCache = ignite.getOrCreateCache("weight");
		System.out.println("Catalog data sidecar started");
		System.out.println();
		loadCaches(descCache, sizeCache, weightCache);

		displayDatabase();
        displayCache("Item Descriptions", descCache);
        displayCache("Item Dimensions", sizeCache);
        displayCache("Item Weights", weightCache);
        
		try {
			while (true) {
				QueueingConsumer.Delivery msg = consumer.nextDelivery(1000);
				if (msg != null) {
					String product = new String(msg.getBody());
					if (product.equalsIgnoreCase("show")) {
						displayDatabase();
				        displayCache("Item Descriptions", descCache);
				        displayCache("Item Dimensions", sizeCache);
				        displayCache("Item Weights", weightCache);
					} 
				}
			}			
		} finally {
			AMQPCommon.close(channel);
		}
	}		

	private void loadCaches(ClientCache<String, String> desc, ClientCache<String, String> size, ClientCache<String, String> weight) {
		desc.put("1", "Toaster,Silver");
		desc.put("2", "Microwave,Black");
		desc.put("3", "Skillet,Blue");
		desc.put("4", "Kettle,Silver");
		desc.put("5", "Mixer,Red");

		size.put("1", "27,16,19");
		size.put("2", "35,43,19");
		size.put("3", "48,30,9");
		size.put("4", "15,23,24");
		size.put("5", "30,25,40");

		weight.put("1", "1");
		weight.put("2", "10");
		weight.put("3", "1");
		weight.put("4", "2");
		weight.put("5", "5");
	}

	
	private void displayDatabase() {
		System.out.println("Catalog Database:");
		System.out.println("1,Toaster,Silver,27cm,16cm,19cm,1kg");
		System.out.println("2,Microwave,Black,35cm,43cm,19cm,10kg");
		System.out.println("3,Skillet,Blue,48cm,30cm,9cm,1kg");
		System.out.println("4,Kettle,Silver,15cm,23cm,24cm,2kg");
		System.out.println("5,Mixer,Red,30cm,25cm,40cm,5kg");
	}
	
	private void displayCache(String name, ClientCache<String, String> cache) {		
		System.out.println();
		System.out.println(name + " Data Sidecar:");
		for (int i=1;i<6;i++) {
			String key = new Integer(i).toString();
			System.out.println(key + ":" + cache.get(key));
		}
	}
}
