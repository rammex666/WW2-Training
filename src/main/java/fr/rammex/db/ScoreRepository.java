package fr.rammex.db;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import fr.rammex.model.SessionResult;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class ScoreRepository {

    private final MongoCollection<Document> collection;

    public ScoreRepository() {
        MongoDBService service = MongoDBService.getInstance();
        if (service.isConnected()) {
            MongoDatabase db = service.getDatabase();
            this.collection = db.getCollection("scores");
        } else {
            this.collection = null;
        }
    }

    public void save(SessionResult result) {
        if (collection == null) return;
        Document doc = new Document()
                .append("playerName", result.getPlayerName())
                .append("rank", result.getRank())
                .append("missionTitle", result.getMissionTitle())
                .append("score", result.getScore())
                .append("maxScore", result.getMaxScore())
                .append("grade", result.getGrade())
                .append("feedback", result.getFeedback())
                .append("dateTime", result.getDateTime());
        collection.insertOne(doc);
    }

    public List<SessionResult> findAll() {
        List<SessionResult> results = new ArrayList<>();
        if (collection == null) return results;
        for (Document doc : collection.find()) {
            SessionResult sr = new SessionResult(
                    doc.getString("playerName"),
                    doc.getString("rank"),
                    doc.getString("missionTitle"),
                    doc.getInteger("score", 0),
                    doc.getInteger("maxScore", 1)
            );
            sr.setFeedback(doc.getString("feedback"));
            results.add(sr);
        }
        return results;
    }

    public boolean isAvailable() {
        return collection != null;
    }
}
