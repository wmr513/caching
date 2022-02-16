package caching.datasidecar;

import com.rabbitmq.client.Channel;
import common.AMQPCommon;

public class AddItemToWishlist {

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			System.out.println("specify a product_id to add to your wishlist");
			return;
		}

		String productId = args[0];
		Channel channel = AMQPCommon.connect();
		byte[] message = productId.getBytes();
		String routingKey = "wishlist.request.q";
		if (!productId.equals("show")) {
			System.out.println("adding id: " + productId);
		}
		channel.basicPublish("", routingKey, null, message);
		AMQPCommon.close(channel);
	}
}
