package hazelcast;

import java.util.Map;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

import common.AMQPCommon;

public class CustomerInfoService {

	public static void main(String[] args) throws Exception {
		Channel channel = AMQPCommon.connect();
		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume("name.q", true, consumer);
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        Map<String, String> cache = hz.getReplicatedMap("names");

		if (args.length > 0 && args[0].equalsIgnoreCase("load")) {
	        cache.put("1", "Mark");
		}
		
		System.out.println("");
        System.out.println("name in cache: " + cache.get("1"));

		try {
			while (true) {
				QueueingConsumer.Delivery msg = consumer.nextDelivery(1000);
				if (msg != null) {
					String data = new String(msg.getBody());
					System.out.println("");
					System.out.println("--> UPDATING NAME: " + data);
					System.out.println("--> DATAPUMP DATA...");
					System.out.println("");
					cache.put("1", new String(msg.getBody()));
					
					//now 'datapump' the data to the writer...
					byte[] message = msg.getBody();
					String routingKey = "datapump.q";
					channel.basicPublish("", routingKey, null, message);
				}
		        System.out.println("name in cache: " + cache.get("1"));
			}			
		} finally {
	        hz.shutdown();
			AMQPCommon.close(channel);
		}
	}	
}
