package fr.rammex.model;

import java.util.List;

public class EvaluationCriteria {
    public enum CriteriaType {
        POSITION,       // Unit X in Zone Y
        AVOIDANCE,      // No unit in Zone Y (danger)
        CONSOLIDATION,  // At least N units of type T in Zone Y
        COHESION,       // Unit of type T1 must be near unit of type T2
        DIVERSITY,      // At least N different types in Zone Y
        COVERAGE        // At least N zones of type T must be occupied
    }

    private String name;
    private String description;
    private int maxPoints;
    private CriteriaType type;
    
    // Parameters used depending on type
    private String zoneId;          // Target zone
    private String zoneType;        // Target zone type (e.g. COLLINE)
    private String requiredTroopType; // Primary troop type
    private String secondTroopType;   // For COHESION
    private int requiredCount;      // For CONSOLIDATION, DIVERSITY, COVERAGE
    private double minDistance;     // For COHESION (relative 0..1)
    private boolean mandatory;      // If false, it's a bonus/malus

    public EvaluationCriteria(String name, String description, int maxPoints, CriteriaType type) {
        this.name = name;
        this.description = description;
        this.maxPoints = maxPoints;
        this.type = type;
        this.mandatory = true;
        this.requiredCount = 1;
    }

    // Compatibility constructor for Editor
    public EvaluationCriteria(String name, String description, int maxPoints, String zoneId) {
        this(name, description, maxPoints, CriteriaType.POSITION);
        this.zoneId = zoneId;
    }

    // Static helpers for easy creation
    public static EvaluationCriteria position(String name, String desc, int pts, String zoneId, String troopType) {
        EvaluationCriteria c = new EvaluationCriteria(name, desc, pts, CriteriaType.POSITION);
        c.zoneId = zoneId;
        c.requiredTroopType = troopType;
        return c;
    }

    public static EvaluationCriteria avoidance(String name, String desc, int pts, String zoneId) {
        EvaluationCriteria c = new EvaluationCriteria(name, desc, pts, CriteriaType.AVOIDANCE);
        c.zoneId = zoneId;
        return c;
    }

    public static EvaluationCriteria cohesion(String name, String desc, int pts, String type1, String type2, double dist) {
        EvaluationCriteria c = new EvaluationCriteria(name, desc, pts, CriteriaType.COHESION);
        c.requiredTroopType = type1;
        c.secondTroopType = type2;
        c.minDistance = dist;
        return c;
    }

    // Getters and Setters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getMaxPoints() { return maxPoints; }
    public CriteriaType getType() { return type; }
    public String getZoneId() { return zoneId; }
    public String getZoneType() { return zoneType; }
    public String getRequiredTroopType() { return requiredTroopType; }
    public String getSecondTroopType() { return secondTroopType; }
    public int getRequiredCount() { return requiredCount; }
    public double getMinDistance() { return minDistance; }
    public boolean isMandatory() { return mandatory; }

    public void setZoneId(String zoneId) { this.zoneId = zoneId; }
    public void setZoneType(String zoneType) { this.zoneType = zoneType; }
    public void setRequiredTroopType(String requiredTroopType) { this.requiredTroopType = requiredTroopType; }
    public void setSecondTroopType(String secondTroopType) { this.secondTroopType = secondTroopType; }
    public void setRequiredCount(int requiredCount) { this.requiredCount = requiredCount; }
    public void setMinDistance(double minDistance) { this.minDistance = minDistance; }
    public void setMandatory(boolean mandatory) { this.mandatory = mandatory; }
}
