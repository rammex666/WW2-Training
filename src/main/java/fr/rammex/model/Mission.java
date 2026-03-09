package fr.rammex.model;

import java.util.List;
import java.util.ArrayList;

public class Mission {
    private String id;
    private String title;
    private String description;
    private String difficulty; // FACILE, MOYEN, DIFFICILE, ÉLITE
    private String mapName;
    private String objectif;
    private String briefing;
    private int maxPoints;
    private List<String> optimalZones; // Zone names where troops should ideally be placed
    private List<TroopUnit> availableTroops;
    private List<EvaluationCriteria> criteria;
    private String terrain; // NORMANDIE, ARDENNES, STALINGRAD, PACIFIQUE
    private List<EnemyUnit> savedEnemies; // Admin-placed enemies (empty = use AI)

    public Mission(String id, String title, String description, String difficulty,
                   String mapName, String objectif, String briefing, int maxPoints, String terrain) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.mapName = mapName;
        this.objectif = objectif;
        this.briefing = briefing;
        this.maxPoints = maxPoints;
        this.terrain = terrain;
        this.availableTroops = new ArrayList<>();
        this.criteria = new ArrayList<>();
        this.optimalZones = new ArrayList<>();
        this.savedEnemies = new ArrayList<>();
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDifficulty() { return difficulty; }
    public String getMapName() { return mapName; }
    public String getObjectif() { return objectif; }
    public String getBriefing() { return briefing; }
    public int getMaxPoints() { return maxPoints; }
    public String getTerrain() { return terrain; }
    public List<TroopUnit> getAvailableTroops() { return availableTroops; }
    public List<EvaluationCriteria> getCriteria() { return criteria; }
    public List<String> getOptimalZones() { return optimalZones; }

    public List<EnemyUnit> getSavedEnemies() { return savedEnemies; }

    public void addTroop(TroopUnit troop) { availableTroops.add(troop); }
    public void addCriteria(EvaluationCriteria c) { criteria.add(c); }
    public void addOptimalZone(String zone) { optimalZones.add(zone); }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public void setMapName(String mapName) { this.mapName = mapName; }
    public void setObjectif(String objectif) { this.objectif = objectif; }
    public void setBriefing(String briefing) { this.briefing = briefing; }
    public void setMaxPoints(int maxPoints) { this.maxPoints = maxPoints; }
    public void setTerrain(String terrain) { this.terrain = terrain; }
}
