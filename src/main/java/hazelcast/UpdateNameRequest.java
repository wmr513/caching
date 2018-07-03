package hazelcast;

import com.rabbitmq.client.Channel;
import common.AMQPCommon;

public class UpdateNameRequest {

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			System.out.println("dude - supply a name to change, will ya?");
			return;
		}

		String name = args[0];
		Channel channel = AMQPCommon.connect();
		byte[] message = name.getBytes();
		String routingKey = "name.q";
		System.out.println("Update first name to " + name);
		channel.basicPublish("", routingKey, null, message);
		
		AMQPCommon.close(channel);
	}
}
