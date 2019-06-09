package hazelcast;

import com.rabbitmq.client.Channel;
import common.AMQPCommon;

public class AddProduct {

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			System.out.println("specify a product to add in productId,ProductDesc format");
			return;
		}

		String product = args[0];
		Channel channel = AMQPCommon.connect();
		byte[] message = product.getBytes();
		String routingKey = "product.q";
		System.out.println("adding product: " + product);
		channel.basicPublish("", routingKey, null, message);
		AMQPCommon.close(channel);
	}
}
