package fr.rammex.service;

import fr.rammex.model.*;
import java.util.*;
import java.util.stream.Collectors;

public class EnemyPlacementAI {

    private static final Random rand = new Random();

    /**
     * Generates enemy positions intelligently:
     * - HIGH strategic zones always get enemies (1-2 per zone on hard)
     * - MEDIUM zones get enemies on MOYEN+
     * - ELITE adds surprise enemies in unexpected positions
     */
    public static List<EnemyUnit> generateEnemies(Mission mission, List<MapZone> zones) {
        List<EnemyUnit> enemies = new ArrayList<>();
        int enemyId = 0;
        int level = difficultyLevel(mission.getDifficulty());

        List<MapZone> highZones = zones.stream()
                .filter(z -> z.getStrategicValue().equals("HIGH"))
                .collect(Collectors.toList());
        List<MapZone> medZones = zones.stream()
                .filter(z -> z.getStrategicValue().equals("MEDIUM"))
                .collect(Collectors.toList());

        Collections.shuffle(highZones, rand);
        Collections.shuffle(medZones, rand);

        // 1-2 enemies per HIGH zone
        for (MapZone zone : highZones) {
            int count = level >= 3 ? 1 + rand.nextInt(2) : 1;
            for (int i = 0; i < count; i++) {
                double rx = clamp(zone.getX() + 0.05 + rand.nextDouble() * Math.max(0.01, zone.getWidth() - 0.10));
                double ry = clamp(zone.getY() + 0.05 + rand.nextDouble() * Math.max(0.01, zone.getHeight() - 0.10));
                enemies.add(buildEnemy(enemyId++, zone.getType(), rx, ry));
            }
        }

        // MOYEN+ adds enemies on medium zones
        if (level >= 2) {
            int medCount = Math.min(medZones.size(), level - 1);
            for (int i = 0; i < medCount; i++) {
                MapZone zone = medZones.get(i);
                double rx = clamp(zone.getX() + 0.05 + rand.nextDouble() * Math.max(0.01, zone.getWidth() - 0.10));
                double ry = clamp(zone.getY() + 0.05 + rand.nextDouble() * Math.max(0.01, zone.getHeight() - 0.10));
                enemies.add(buildEnemy(enemyId++, zone.getType(), rx, ry));
            }
        }

        // ELITE: 2 extra surprise enemies
        if (level >= 4) {
            for (int i = 0; i < 2; i++) {
                double rx = 0.1 + rand.nextDouble() * 0.8;
                double ry = 0.1 + rand.nextDouble() * 0.8;
                enemies.add(buildEnemy(enemyId++, "OFFENSIF", rx, ry));
            }
        }

        return enemies;
    }

    private static EnemyUnit buildEnemy(int id, String zoneType, double rx, double ry) {
        String name, type;
        switch (zoneType) {
            case "COLLINE": name = "Sniper Ennemi"; type = "SNIPER"; break;
            case "PONT":    name = "Panzer";        type = "TANK";   break;
            case "DEFENSIF":name = "Canon Ennemi";  type = "ARTILLERY"; break;
            case "VILLE":   name = "Inf. Urbaine";  type = "INFANTRY";  break;
            default:        name = "Inf. Ennemie";  type = "INFANTRY";  break;
        }
        return new EnemyUnit("enemy_" + id, name, type, "💀", rx, ry);
    }

    private static double clamp(double v) { return Math.min(0.95, Math.max(0.05, v)); }

    private static int difficultyLevel(String d) {
        switch (d) {
            case "FACILE": return 1; case "MOYEN": return 2;
            case "DIFFICILE": return 3; case "ÉLITE": return 4;
            default: return 2;
        }
    }
}
