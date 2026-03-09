package fr.rammex.db;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import fr.rammex.model.*;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class MissionRepository {

    private final MongoCollection<Document> collection;

    public MissionRepository() {
        MongoDBService service = MongoDBService.getInstance();
        if (service.isConnected()) {
            MongoDatabase db = service.getDatabase();
            this.collection = db.getCollection("missions");
        } else {
            this.collection = null;
        }
    }

    public List<Mission> findAll() {
        List<Mission> missions = new ArrayList<>();
        if (collection == null) return missions;
        for (Document doc : collection.find()) {
            missions.add(fromDocument(doc));
        }
        return missions;
    }

    public void save(Mission mission, List<MapZone> zones) {
        if (collection == null) return;
        Document doc = toDocument(mission, zones);
        Document existing = collection.find(eq("missionId", mission.getId())).first();
        if (existing != null) {
            collection.replaceOne(eq("missionId", mission.getId()), doc);
        } else {
            collection.insertOne(doc);
        }
    }

    public void deleteById(String missionId) {
        if (collection == null) return;
        collection.deleteOne(eq("missionId", missionId));
    }

    public List<MapZone> getZonesForMission(String missionId) {
        if (collection == null) return new ArrayList<>();
        Document doc = collection.find(eq("missionId", missionId)).first();
        if (doc == null) return new ArrayList<>();
        return zonesFromDocument(doc);
    }

    private Document toDocument(Mission mission, List<MapZone> zones) {
        Document doc = new Document()
                .append("missionId", mission.getId())
                .append("title", mission.getTitle())
                .append("description", mission.getDescription())
                .append("difficulty", mission.getDifficulty())
                .append("mapName", mission.getMapName())
                .append("objectif", mission.getObjectif())
                .append("briefing", mission.getBriefing())
                .append("maxPoints", mission.getMaxPoints())
                .append("terrain", mission.getTerrain());

        // Troops
        List<Document> troopDocs = new ArrayList<>();
        for (TroopUnit t : mission.getAvailableTroops()) {
            troopDocs.add(new Document()
                    .append("id", t.getId())
                    .append("name", t.getName())
                    .append("type", t.getType())
                    .append("icon", t.getIcon())
                    .append("strength", t.getStrength())
                    .append("movementRange", t.getMovementRange())
                    .append("description", t.getDescription()));
        }
        doc.append("troops", troopDocs);

        // Zones
        List<Document> zoneDocs = new ArrayList<>();
        for (MapZone z : zones) {
            zoneDocs.add(new Document()
                    .append("id", z.getId())
                    .append("name", z.getName())
                    .append("description", z.getDescription())
                    .append("x", z.getX())
                    .append("y", z.getY())
                    .append("width", z.getWidth())
                    .append("height", z.getHeight())
                    .append("strategicValue", z.getStrategicValue())
                    .append("type", z.getType())
                    .append("rotation", z.getRotation()));
        }
        doc.append("zones", zoneDocs);

        // Criteria
        List<Document> critDocs = new ArrayList<>();
        for (EvaluationCriteria c : mission.getCriteria()) {
            critDocs.add(new Document()
                    .append("name", c.getName())
                    .append("description", c.getDescription())
                    .append("maxPoints", c.getMaxPoints())
                    .append("type", c.getType().name())
                    .append("zoneId", c.getZoneId())
                    .append("zoneType", c.getZoneType())
                    .append("requiredTroopType", c.getRequiredTroopType())
                    .append("secondTroopType", c.getSecondTroopType())
                    .append("requiredCount", c.getRequiredCount())
                    .append("minDistance", c.getMinDistance())
                    .append("mandatory", c.isMandatory()));
        }
        doc.append("criteria", critDocs);

        // Optimal zones
        doc.append("optimalZones", mission.getOptimalZones());

        // Saved enemies
        List<Document> enemyDocs = new ArrayList<>();
        for (EnemyUnit e : mission.getSavedEnemies()) {
            enemyDocs.add(new Document()
                    .append("id", e.getId())
                    .append("name", e.getName())
                    .append("type", e.getType())
                    .append("icon", e.getIcon())
                    .append("relX", e.getRelX())
                    .append("relY", e.getRelY()));
        }
        doc.append("savedEnemies", enemyDocs);

        return doc;
    }

    private Mission fromDocument(Document doc) {
        Mission m = new Mission(
                doc.getString("missionId"),
                doc.getString("title"),
                doc.getString("description"),
                doc.getString("difficulty"),
                doc.getString("mapName"),
                doc.getString("objectif"),
                doc.getString("briefing"),
                doc.getInteger("maxPoints", 200),
                doc.getString("terrain") != null ? doc.getString("terrain") : "INCONNU"
        );

        // Troops
        List<Document> troopDocs = doc.getList("troops", Document.class, new ArrayList<>());
        for (Document td : troopDocs) {
            m.addTroop(new TroopUnit(
                    td.getString("id"),
                    td.getString("name"),
                    td.getString("type"),
                    td.getString("icon"),
                    td.getInteger("strength", 50),
                    td.getInteger("movementRange", 2),
                    td.getString("description")
            ));
        }

        // Criteria
        List<Document> critDocs = doc.getList("criteria", Document.class, new ArrayList<>());
        for (Document cd : critDocs) {
            String typeStr = cd.getString("type");
            EvaluationCriteria.CriteriaType type = typeStr != null ? 
                    EvaluationCriteria.CriteriaType.valueOf(typeStr) : EvaluationCriteria.CriteriaType.POSITION;
            
            EvaluationCriteria ec = new EvaluationCriteria(
                    cd.getString("name"),
                    cd.getString("description"),
                    cd.getInteger("maxPoints", 50),
                    type
            );
            ec.setZoneId(cd.getString("zoneId"));
            ec.setZoneType(cd.getString("zoneType"));
            ec.setRequiredTroopType(cd.getString("requiredTroopType"));
            ec.setSecondTroopType(cd.getString("secondTroopType"));
            ec.setRequiredCount(cd.getInteger("requiredCount", 1));
            ec.setMinDistance(cd.getDouble("minDistance") != null ? cd.getDouble("minDistance") : 0.0);
            ec.setMandatory(cd.getBoolean("mandatory", true));
            m.addCriteria(ec);
        }

        // Optimal zones
        List<String> optZones = doc.getList("optimalZones", String.class, new ArrayList<>());
        optZones.forEach(m::addOptimalZone);

        // Saved enemies
        List<Document> enemyDocs = doc.getList("savedEnemies", Document.class, new ArrayList<>());
        for (Document ed : enemyDocs) {
            m.getSavedEnemies().add(new EnemyUnit(
                    ed.getString("id"),
                    ed.getString("name"),
                    ed.getString("type"),
                    ed.getString("icon"),
                    ed.getDouble("relX"),
                    ed.getDouble("relY")
            ));
        }

        return m;
    }

    private List<MapZone> zonesFromDocument(Document doc) {
        List<MapZone> zones = new ArrayList<>();
        List<Document> zoneDocs = doc.getList("zones", Document.class, new ArrayList<>());
        for (Document zd : zoneDocs) {
            MapZone z = new MapZone(
                    zd.getString("id"),
                    zd.getString("name"),
                    zd.getString("description"),
                    zd.getDouble("x"),
                    zd.getDouble("y"),
                    zd.getDouble("width"),
                    zd.getDouble("height"),
                    zd.getString("strategicValue"),
                    zd.getString("type")
            );
            Double rot = zd.getDouble("rotation");
            if (rot != null) z.setRotation(rot);
            zones.add(z);
        }
        return zones;
    }

    public boolean isAvailable() {
        return collection != null;
    }
}
