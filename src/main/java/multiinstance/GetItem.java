package caching.multiinstance;

import com.rabbitmq.client.Channel;
import common.AMQPCommon;

public class GetItem {

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			System.out.println("specify an item to add to your wishlist");
			return;
		}

		String product = "GET," + args[0];
		Channel channel = AMQPCommon.connect();
		byte[] message = product.getBytes();
		String routingKey = "wishlist.request.q";
		channel.basicPublish("", routingKey, null, message);
		AMQPCommon.close(channel);
	}
}
