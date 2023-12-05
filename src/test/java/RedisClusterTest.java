import io.redislettuce.core.RedisURI;
import io.redislettuce.core.api.StatefulRedisClusterConnection;
import io.redislettuce.core.api.sync.RedisCommands;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RedisClusterTest {

    private static final int REDIS_PORT = 6379;

    private static GenericContainer<?> redisContainer1;
    private static GenericContainer<?> redisContainer2;
    private static GenericContainer<?> redisContainer3;

    private static Network network;

    private static StatefulRedisClusterConnection<String, String> redisConnection;

    @BeforeAll
    static void setUp() {
        network = Network.newNetwork();

        redisContainer1 = new GenericContainer<>("redis:latest")
                .withExposedPorts(REDIS_PORT)
                .withNetwork(network)
                .withNetworkAliases("redis1");
        redisContainer1.start();

        redisContainer2 = new GenericContainer<>("redis:latest")
                .withExposedPorts(REDIS_PORT)
                .withNetwork(network)
                .withNetworkAliases("redis2");
        redisContainer2.start();

        redisContainer3 = new GenericContainer<>("redis:latest")
                .withExposedPorts(REDIS_PORT)
                .withNetwork(network)
                .withNetworkAliases("redis3");
        redisContainer3.start();

        List<RedisURI> redisURIs = List.of(
                RedisURI.create(redisContainer1.getContainerIpAddress(), redisContainer1.getMappedPort(REDIS_PORT)),
                RedisURI.create(redisContainer2.getContainerIpAddress(), redisContainer2.getMappedPort(REDIS_PORT)),
                RedisURI.create(redisContainer3.getContainerIpAddress(), redisContainer3.getMappedPort(REDIS_PORT))
        );

        redisConnection = io.redislettuce.core.RedisClusterClient
                .create(redisURIs)
                .connect();
    }

    @AfterAll
    static void tearDown() {
        redisConnection.close();
        redisContainer1.stop();
        redisContainer2.stop();
        redisContainer3.stop();
    }

    @Test
    void testRedisCluster() {
        RedisCommands<String, String> syncCommands = redisConnection.sync();

        syncCommands.set("key1", "value1");
        syncCommands.set("key2", "value2");

        assertEquals("value1", syncCommands.get("key1"));
        assertEquals("value2", syncCommands.get("key2"));
    }
}
