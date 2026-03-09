package fr.rammex.db;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class MongoDBService {

    private static MongoDBService instance;
    private MongoClient client;
    private MongoDatabase database;
    private Properties config;
    private boolean connected = false;

    private MongoDBService() {
        config = loadConfig();
        try {
            String uri = config.getProperty("mongodb.uri", "mongodb://localhost:27017");
            String dbName = config.getProperty("mongodb.database", "wwii_training");
            client = MongoClients.create(uri);
            database = client.getDatabase(dbName);
            // Test connection
            database.listCollectionNames().first();
            connected = true;
            ensureMapsDirectory();
        } catch (Exception e) {
            System.err.println("[MongoDB] Connexion impossible : " + e.getMessage());
            connected = false;
        }
    }

    public static synchronized MongoDBService getInstance() {
        if (instance == null) {
            instance = new MongoDBService();
        }
        return instance;
    }

    private Properties loadConfig() {
        Properties props = new Properties();
        try (InputStream is = MongoDBService.class.getResourceAsStream("/config.properties")) {
            if (is != null) props.load(is);
        } catch (Exception e) {
            System.err.println("[Config] Impossible de charger config.properties : " + e.getMessage());
        }
        return props;
    }

    private void ensureMapsDirectory() {
        String dir = config.getProperty("maps.directory", "./maps");
        try {
            Files.createDirectories(Paths.get(dir));
        } catch (Exception e) {
            System.err.println("[Maps] Impossible de créer le dossier maps : " + e.getMessage());
        }
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public boolean isConnected() {
        return connected;
    }

    public String getMapsDirectory() {
        return config.getProperty("maps.directory", "./maps");
    }

    public String getAdminInitialUsername() {
        return config.getProperty("admin.initial.username", "admin");
    }

    public String getAdminInitialPassword() {
        return config.getProperty("admin.initial.password", "admin123");
    }

    public void close() {
        if (client != null) {
            try { client.close(); } catch (Exception ignored) {}
        }
    }
}
