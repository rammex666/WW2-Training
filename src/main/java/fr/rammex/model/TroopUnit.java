package fr.rammex.model;

public class TroopUnit {
    private String id;
    private String name;
    private String type; // INFANTRY, TANK, ARTILLERY, SNIPER, MEDIC, ENGINEER
    private String icon;
    private int strength;
    private int movementRange;
    private String description;
    private double x;
    private double y;
    private boolean placed;

    public TroopUnit(String id, String name, String type, String icon, int strength, int movementRange, String description) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.icon = icon;
        this.strength = strength;
        this.movementRange = movementRange;
        this.description = description;
        this.placed = false;
    }

    // Getters / Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getIcon() { return icon; }
    public int getStrength() { return strength; }
    public int getMovementRange() { return movementRange; }
    public String getDescription() { return description; }
    public double getX() { return x; }
    public double getY() { return y; }
    public boolean isPlaced() { return placed; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setPlaced(boolean placed) { this.placed = placed; }
}
