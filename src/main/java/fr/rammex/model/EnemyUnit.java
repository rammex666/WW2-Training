package fr.rammex.model;

public class EnemyUnit {
    private String id;
    private String name;
    private String type;
    private String icon;
    private double relX; // 0..1 relative to map
    private double relY;

    public EnemyUnit(String id, String name, String type, String icon, double relX, double relY) {
        this.id = id; this.name = name; this.type = type;
        this.icon = icon; this.relX = relX; this.relY = relY;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getIcon() { return icon; }
    public double getRelX() { return relX; }
    public double getRelY() { return relY; }
}
