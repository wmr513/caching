package hazelcast;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import common.AMQPCommon;

public class DataWriter {

	public static void main(String[] args) throws Exception {
		Channel channel = AMQPCommon.connect();
		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume("datapump.q", true, consumer);
        System.out.println("database value: mark");

		try {
			while (true) {
				QueueingConsumer.Delivery msg = consumer.nextDelivery();
				System.out.println("");
				System.out.println("--> received data from service: " + new String(msg.getBody()));
		        System.out.println("--> update database...");
		        System.out.println("database value: " + new String(msg.getBody()));
				System.out.println("");
			}			
		} finally {
			AMQPCommon.close(channel);
		}
	}	

}
