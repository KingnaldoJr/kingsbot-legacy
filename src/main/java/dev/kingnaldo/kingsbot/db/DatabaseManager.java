package dev.kingnaldo.kingsbot.db;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import dev.kingnaldo.kingsbot.KingsBot;
import org.bson.Document;

import java.util.stream.Collectors;

public class DatabaseManager {
    private static MongoClient mongoClient;
    private static MongoDatabase database;

    public static void connect() {
        MongoCredential credential = MongoCredential.createCredential(
                KingsBot.getConfig().db().username(),
                KingsBot.getConfig().db().database(),
                KingsBot.getConfig().db().password().toCharArray());

        MongoClientSettings settings = MongoClientSettings.builder()
                .credential(credential)
                .applyToSslSettings(builder -> builder.enabled(true))
                .applyToClusterSettings(builder ->
                        builder.hosts(KingsBot.getConfig().db().hosts().stream().map(host ->
                                new ServerAddress(host.hostname(), host.port())).collect(Collectors.toList())))
                .build();

        mongoClient = MongoClients.create(settings);
        database = mongoClient.getDatabase(KingsBot.getConfig().db().database());
    }

    public static void disconnect() {
        mongoClient.close();
    }

    public static MongoCollection<Document> getCollection(String collectionName) {
        return database.getCollection(collectionName);
    }
}
