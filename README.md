# Kafka application: Favourite colours

This streams application takes in favourite colours and usernames as input
and displays the frequency of the favourite colours currently in the stream.

## Prerequisites

+ Kafka
+ Java 8
+ Maven

## Executing the application

+ Start the zookeeper and kafka instance using:

```shell
bin\windows\zookeeper-server-start.bat config\zookeeper.properties
bin\windows\kafka-server-start.bat config\server.properties
```
+ Create the input topic:

```shell
bin\windows\kafka-topics.bat --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic colour-count-input
```

+ Create the log-compact topic for KTable:

```shell
bin\windows\kafka-topics.bat --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic user-keys-and-colours-topic
```

+ Create the output topic:

```shell
bin\windows\kafka-topics.bat --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic colour-count-output
```

+ Run the application

+ Start the kafka consumer:

```shell
bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic colour-count-output --from-beginning --formatter kafka.tools.DefaultMessageFormatter --property print.key=true --property print.value=true --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer
```

+ Start the kafka producer with input-topic:

```shell
bin\windows\kafka-console-producer.bat --broker-list localhost:9092 --topic colour-count-input
```
