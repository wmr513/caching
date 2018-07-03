package common;

import com.rabbitmq.client.Channel;

public class AMQPInitialize {

	public static void main(String[] args) throws Exception {
		Channel channel = AMQPCommon.connect();
		
		//create the durable queues
		channel.queueDeclare("name.q", true, false, false, null);
		channel.queueDeclare("datapump.q", true, false, false, null);
		System.out.println("queues created.");

		AMQPCommon.close(channel);
	}
}

