package fr.rammex.db;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static com.mongodb.client.model.Filters.eq;

public class UserRepository {

    private final MongoCollection<Document> collection;
    private final MongoDBService service;

    public UserRepository() {
        this.service = MongoDBService.getInstance();
        if (service.isConnected()) {
            MongoDatabase db = service.getDatabase();
            this.collection = db.getCollection("users");
            ensureDefaultAdmin();
        } else {
            this.collection = null;
        }
    }

    private void ensureDefaultAdmin() {
        if (collection == null) return;
        if (collection.countDocuments() == 0) {
            String username = service.getAdminInitialUsername();
            String password = service.getAdminInitialPassword();
            createUser(username, password, "admin");
            System.out.println("[UserRepository] Compte admin initial créé : " + username);
        }
    }

    public boolean authenticate(String username, String password) {
        if (collection == null) return false;
        String hashed = sha256(password);
        Document found = collection.find(eq("username", username)).first();
        return found != null && hashed.equals(found.getString("passwordHash"));
    }

    public void createUser(String username, String password, String role) {
        if (collection == null) return;
        Document existing = collection.find(eq("username", username)).first();
        if (existing != null) return;
        Document doc = new Document("username", username)
                .append("passwordHash", sha256(password))
                .append("role", role);
        collection.insertOne(doc);
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 unavailable", e);
        }
    }

    public boolean isAvailable() {
        return collection != null;
    }
}
