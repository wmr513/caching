# caching
Source code examples and demo from my microservices caching session at NFJS

To run the demo (based on the source code) play the .MOV file in the demo directory.

To run these code samples you will need Java 1.7 or higher, RabbitMQ (3.5.4) (I use the latest rabbitmq docker image from Pivotal), and Hazelcast 3.10.2 or higher.

Be sure to go into common.AMQPCommon.java and update the connection info for RabbitMQ: (you can get this info from the RabbitMQ logs or doing a "docker ps" if you are using the docker image)

```
public static Channel connect() throws Exception {	
	ConnectionFactory factory = new ConnectionFactory();	

-->	factory.setHost("192.163.98.101");

-->	factory.setPort(32768);

	Connection conn = factory.newConnection();	
	return conn.createChannel();	
}
```

You will also need to be sure and run the AMQPInitialize class to setup all of the exchanges, queues, and bindings used by these examples.
