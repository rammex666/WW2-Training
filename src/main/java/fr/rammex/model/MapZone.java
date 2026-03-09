package fr.rammex.model;

public class MapZone {
    private String id;
    private String name;
    private String description;
    private double x; // relative 0..1
    private double y; // relative 0..1
    private double width;
    private double height;
    private String strategicValue; // HIGH, MEDIUM, LOW
    private String type; // DEFENSIF, OFFENSIF, COUVERTURE, PONT, COLLINE, VILLE
    private double rotation = 0; // degrees

    public MapZone(String id, String name, String description,
                   double x, double y, double width, double height,
                   String strategicValue, String type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.strategicValue = strategicValue;
        this.type = type;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public String getStrategicValue() { return strategicValue; }
    public String getType() { return type; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setStrategicValue(String strategicValue) { this.strategicValue = strategicValue; }
    public void setType(String type) { this.type = type; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setWidth(double width) { this.width = width; }
    public void setHeight(double height) { this.height = height; }
    public double getRotation() { return rotation; }
    public void setRotation(double rotation) { this.rotation = rotation; }
}
