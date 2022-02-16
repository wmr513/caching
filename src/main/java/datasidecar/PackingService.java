package caching.datasidecar;

import org.apache.ignite.Ignition;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

import common.AMQPCommon;

public class PackingService {

	public static void main(String[] args) throws Exception {
		PackingService service = new PackingService();
		service.startService();
	}
				
	public void startService() throws Exception {		
		Channel channel = AMQPCommon.connect();
		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume("packing.request.q", true, consumer);
		ClientConfiguration cfg = new ClientConfiguration().setAddresses("127.0.0.1:10800");
		IgniteClient ignite = Ignition.startClient(cfg);
		ClientCache<String, String> dataSidecar = ignite.getOrCreateCache("size");
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println("Packing Service Started.");
        
		try {
			while (true) {
				QueueingConsumer.Delivery msg = consumer.nextDelivery(1000);
				if (msg != null) {
					int totalArea = 0;
					int totalLength = 0;
					int totalWidth = 0;
					int totalHeight = 0;
					String items = new String(msg.getBody());
					String[] itemIds = items.split(",");
					for (int i=0;i<itemIds.length;i++) {
						String size = dataSidecar.get(itemIds[i]);
						String[] LWH = size.split(",");
						long area = new Long(LWH[0]).longValue() *
									new Long(LWH[1]).longValue() *
									new Long(LWH[2]).longValue();
						System.out.println("Size of item " + itemIds[i] + " is " + LWH[0] + "cm X "+ LWH[1] + "cm X "+ LWH[2] + "cm");
						System.out.println("Area of item " + itemIds[i] + " is " + area + " cubic cm");
						totalArea += area;
						totalLength += new Long(LWH[0]).longValue();
						totalWidth += new Long(LWH[1]).longValue();
						totalHeight += new Long(LWH[2]).longValue();
					}
					System.out.println("Total length is " + totalLength + "cm");
					System.out.println("Total width is " + totalWidth + "cm");
					System.out.println("Total height is " + totalHeight + "cm");
					System.out.println("Total area is " + totalArea + " cubic cm.");
					System.out.println("Determining number and size of boxes...");
					System.out.println();
				}
			}			
		} finally {
			AMQPCommon.close(channel);
		}
	}		
}
