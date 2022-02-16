package caching.tuplespace;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import common.AMQPCommon;

public class DataWriter {

	public static void main(String[] args) throws Exception {
		DataWriter service = new DataWriter();
		service.startService();
	}
				
	public void startService() throws Exception {		
		Channel channel = AMQPCommon.connect();
		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume("datapump.q", true, consumer);
		
		CacheConfiguration l_cfg = new CacheConfiguration("database");
		l_cfg.setCacheMode(CacheMode.LOCAL);
		IgniteConfiguration l_ic = new IgniteConfiguration();
		l_ic.setClientMode(false);
		Ignite l_ignite = Ignition.start(l_ic);
		IgniteCache<String, String> cache = l_ignite.getOrCreateCache(l_cfg);

		cache.put("1", "Printer");
		cache.put("2", "Laptop");
		displayDatabase(cache);
		
		try {
			while (true) {
				QueueingConsumer.Delivery msg = consumer.nextDelivery();
				System.out.println("");
				System.out.println("--> received data from service: " + new String(msg.getBody()));
		        System.out.println("--> updating database...");
				String product = new String(msg.getBody());
				String[] productInfo = product.split(",");
				
				System.out.println("");
				if (productInfo[0].equals("ADD")) {
					cache.put(productInfo[1], productInfo[2]);
				} else {
					System.out.println("--> REMOVING ITEM: " + cache.get(productInfo[1]));
					cache.remove(productInfo[1]);
				}
		        displayDatabase(cache);
			}			
		} finally {
			AMQPCommon.close(channel);
		}
	}	

	private void displayDatabase(IgniteCache<String, String> cache) {		
		System.out.println();
		System.out.println("Database:");
		for (int i=0;i<10;i++) {
			String key = new Integer(i).toString();
			if (cache.containsKey(key.toString())) {
				System.out.println(key + ":" + cache.get(key));
			}
		}
	}
	
	
}
