package app.netlify.biswajit.colour_count;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.Arrays;
import java.util.Properties;

public class ColourStream {
    public static void main(String[] args) {
        /*
         * The program takes in a user-name and colour separated by a comma.
         * The users can update their favourite colours.
         * The final count of each of the favourite colours will be displayed in the output topic.
         */

        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka-colour-count");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        StreamsBuilder builder = new StreamsBuilder();
        // Stream from Kafka
        KStream<String, String> favouriteCountInput = builder.stream("colour-count-input");

        KStream<String, String> userKeysAndColours = favouriteCountInput
                // Filter key-value pairs containing ',' (We'll process these)
                .filter((key, value) -> value.contains(","))
                // Select a key that will be the user-id (lowercase for homogeneity)
                .selectKey((key, value) -> value.split(",")[0].toLowerCase())
                // Get the colour from the values (lowercase for homogeneity)
                .mapValues(values -> values.split(",")[1].toLowerCase())
                // Filter the undesired colours
                .filter((user, colour) -> Arrays.asList("green", "blue", "red").contains(colour));

        // Write the filtered data to a new KStreams topic
        userKeysAndColours.to("user-keys-and-colours-topic");

        // Read from the KStream topic as KTable
        KTable<String, String> userKeysAndColoursTable = builder.table("user-keys-and-colours-topic");

        KTable<String, Long> favouriteColours = userKeysAndColoursTable
                .groupBy((user, colour) -> new KeyValue<>(colour, colour))
                .count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as("CountsByColours"));

        // Output results to a kafka topic
        favouriteColours.toStream().to("colour-count-output", Produced.with(Serdes.String(), Serdes.Long()));

        KafkaStreams streams = new KafkaStreams(builder.build(), config);
        streams.start(); // to start the application

        // Prints the topology
        System.out.println(streams.toString());

        // Shutdown hook for closing the streams application gracefully
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
