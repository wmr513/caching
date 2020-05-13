HOME=$DEMO_HOME

CLASSPATH=$HOME/src/main/resources
CLASSPATH=$CLASSPATH:$HOME/bin
CLASSPATH=$CLASSPATH:$HOME/lib/commons-io-1.2.jar
CLASSPATH=$CLASSPATH:$HOME/lib/rabbitmq-client.jar
CLASSPATH=$CLASSPATH:$HOME/lib/hazelcast-all-3.10.2.jar

java -cp $CLASSPATH hazelcast.DataWriter $1
