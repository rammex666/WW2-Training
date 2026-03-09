package fr.rammex.service;

import fr.rammex.model.*;
import java.util.*;
import java.util.stream.Collectors;

public class ScoringEngine {

    public static EvaluationResult evaluate(Mission mission, Map<String, String> placedTroops, List<MapZone> zones) {
        EvaluationResult result = new EvaluationResult();
        result.setMaxScore(mission.getMaxPoints());

        int totalScore = 0;
        List<String> feedback = new ArrayList<>();
        List<CriteriaResult> criteriaResults = new ArrayList<>();

        // Map troop IDs to TroopUnit objects for easier property access
        Map<String, TroopUnit> troopMap = mission.getAvailableTroops().stream()
                .collect(Collectors.toMap(TroopUnit::getId, t -> t));

        // Map zones for easy lookup
        Map<String, MapZone> zoneMap = zones.stream()
                .collect(Collectors.toMap(MapZone::getId, z -> z));

        // Process each criterion
        for (EvaluationCriteria criteria : mission.getCriteria()) {
            CriteriaEvalResult eval = evaluateCriteria(criteria, placedTroops, troopMap, zoneMap, zones);
            
            int pointsEarned = eval.points;
            totalScore += pointsEarned;
            
            String icon = pointsEarned >= criteria.getMaxPoints() ? "✅" : (pointsEarned > 0 ? "⚠" : "❌");
            feedback.add(icon + " " + criteria.getName() + " : +" + pointsEarned + " pts" + (eval.feedbackDetail != null ? " (" + eval.feedbackDetail + ")" : ""));
            
            criteriaResults.add(new CriteriaResult(criteria.getName(), pointsEarned, criteria.getMaxPoints(), eval.satisfied));
        }

        // Global Bonuses & Penalties
        totalScore += evaluateGlobalFactors(mission, placedTroops, zoneMap, feedback);

        // Cap score and ensure non-negative
        totalScore = Math.max(0, Math.min(totalScore, mission.getMaxPoints() + 50)); 
        
        result.setTotalScore(totalScore);
        result.setFeedbackLines(feedback);
        result.setCriteriaResults(criteriaResults);
        result.setTacticalAdvice(generateTacticalAdvice(mission, placedTroops, zones));

        return result;
    }

    private static CriteriaEvalResult evaluateCriteria(EvaluationCriteria c, Map<String, String> placed, Map<String, TroopUnit> troops, Map<String, MapZone> zoneMap, List<MapZone> allZones) {
        switch (c.getType()) {
            case POSITION:
                return evalPosition(c, placed, troops, zoneMap, allZones);
            case AVOIDANCE:
                return evalAvoidance(c, placed, zoneMap);
            case CONSOLIDATION:
                return evalConsolidation(c, placed, troops);
            case COHESION:
                return evalCohesion(c, placed, troops, zoneMap);
            case DIVERSITY:
                return evalDiversity(c, placed, troops);
            default:
                return new CriteriaEvalResult(0, false, "Type inconnu");
        }
    }

    private static CriteriaEvalResult evalPosition(EvaluationCriteria c, Map<String, String> placed, Map<String, TroopUnit> troops, Map<String, MapZone> zoneMap, List<MapZone> allZones) {
        boolean inZone = false;
        boolean rightType = true;
        
        for (Map.Entry<String, String> entry : placed.entrySet()) {
            if (entry.getValue().equals(c.getZoneId())) {
                inZone = true;
                if (c.getRequiredTroopType() != null) {
                    TroopUnit t = troops.get(entry.getKey());
                    if (t != null && !t.getType().equals(c.getRequiredTroopType())) {
                        rightType = false;
                    } else {
                        rightType = true; // Found at least one correct type
                        break;
                    }
                }
            }
        }

        if (inZone && rightType) return new CriteriaEvalResult(c.getMaxPoints(), true, null);
        if (inZone && !rightType) return new CriteriaEvalResult(c.getMaxPoints() / 2, true, "mauvais type d'unité");
        
        // Check proximity
        for (String zoneId : placed.values()) {
            if (isNearOptimalZone(zoneId, c.getZoneId(), allZones)) {
                return new CriteriaEvalResult(c.getMaxPoints() / 3, false, "position sous-optimale");
            }
        }
        
        return new CriteriaEvalResult(0, false, "zone non occupée");
    }

    private static CriteriaEvalResult evalAvoidance(EvaluationCriteria c, Map<String, String> placed, Map<String, MapZone> zoneMap) {
        long count = placed.values().stream().filter(zid -> zid.equals(c.getZoneId())).count();
        if (count == 0) return new CriteriaEvalResult(c.getMaxPoints(), true, null);
        return new CriteriaEvalResult(0, false, count + " unité(s) exposée(s)");
    }

    private static CriteriaEvalResult evalConsolidation(EvaluationCriteria c, Map<String, String> placed, Map<String, TroopUnit> troops) {
        long count = placed.entrySet().stream()
                .filter(e -> e.getValue().equals(c.getZoneId()))
                .filter(e -> c.getRequiredTroopType() == null || troops.get(e.getKey()).getType().equals(c.getRequiredTroopType()))
                .count();
        
        if (count >= c.getRequiredCount()) return new CriteriaEvalResult(c.getMaxPoints(), true, null);
        if (count > 0) return new CriteriaEvalResult(c.getMaxPoints() / 2, false, count + "/" + c.getRequiredCount() + " unités");
        return new CriteriaEvalResult(0, false, "aucune unité");
    }

    private static CriteriaEvalResult evalCohesion(EvaluationCriteria c, Map<String, String> placed, Map<String, TroopUnit> troops, Map<String, MapZone> zoneMap) {
        List<MapZone> list1 = placed.entrySet().stream()
                .filter(e -> troops.get(e.getKey()).getType().equals(c.getRequiredTroopType()))
                .map(e -> zoneMap.get(e.getValue()))
                .collect(Collectors.toList());
        
        List<MapZone> list2 = placed.entrySet().stream()
                .filter(e -> troops.get(e.getKey()).getType().equals(c.getSecondTroopType()))
                .map(e -> zoneMap.get(e.getValue()))
                .collect(Collectors.toList());

        if (list1.isEmpty() || list2.isEmpty()) return new CriteriaEvalResult(0, false, "unités manquantes");

        boolean closeEnough = false;
        double minDist = Double.MAX_VALUE;
        for (MapZone z1 : list1) {
            for (MapZone z2 : list2) {
                double d = Math.hypot(z1.getX() - z2.getX(), z1.getY() - z2.getY());
                minDist = Math.min(minDist, d);
                if (d <= c.getMinDistance()) {
                    closeEnough = true;
                    break;
                }
            }
        }

        if (closeEnough) return new CriteriaEvalResult(c.getMaxPoints(), true, null);
        return new CriteriaEvalResult(0, false, "trop éloignées");
    }

    private static CriteriaEvalResult evalDiversity(EvaluationCriteria c, Map<String, String> placed, Map<String, TroopUnit> troops) {
        Set<String> types = placed.entrySet().stream()
                .filter(e -> e.getValue().equals(c.getZoneId()))
                .map(e -> troops.get(e.getKey()).getType())
                .collect(Collectors.toSet());
        
        if (types.size() >= c.getRequiredCount()) return new CriteriaEvalResult(c.getMaxPoints(), true, null);
        return new CriteriaEvalResult(0, false, types.size() + "/" + c.getRequiredCount() + " types");
    }

    private static int evaluateGlobalFactors(Mission mission, Map<String, String> placed, Map<String, MapZone> zoneMap, List<String> feedback) {
        int bonus = 0;
        
        // Completion Bonus
        if (placed.size() == mission.getAvailableTroops().size() && !placed.isEmpty()) {
            bonus += 20;
            feedback.add("🎖 Bonus : Déploiement complet (+20 pts)");
        }

        // Strategic Value Occupation
        long highValueZones = zoneMap.values().stream().filter(z -> "HIGH".equals(z.getStrategicValue())).count();
        long occupiedHighValue = placed.values().stream().distinct()
                .filter(zid -> "HIGH".equals(zoneMap.get(zid).getStrategicValue()))
                .count();
        
        if (highValueZones > 0 && occupiedHighValue == 0) {
            bonus -= 30;
            feedback.add("⚠ Pénalité : Aucun point stratégique majeur occupé (-30 pts)");
        } else if (occupiedHighValue > 0) {
            int stratBonus = (int) occupiedHighValue * 5;
            bonus += stratBonus;
            feedback.add("⭐ Bonus : Occupation stratégique (+" + stratBonus + " pts)");
        }

        return bonus;
    }

    private static boolean isNearOptimalZone(String zoneId, String targetZoneId, List<MapZone> zones) {
        MapZone placed = zones.stream().filter(z -> z.getId().equals(zoneId)).findFirst().orElse(null);
        MapZone target = zones.stream().filter(z -> z.getId().equals(targetZoneId)).findFirst().orElse(null);
        if (placed == null || target == null) return false;
        double dist = Math.sqrt(Math.pow(placed.getX() - target.getX(), 2) + Math.pow(placed.getY() - target.getY(), 2));
        return dist < 0.20;
    }

    private static String generateTacticalAdvice(Mission mission, Map<String, String> placedTroops, List<MapZone> zones) {
        // ... (Keep existing advice or enhance if needed)
        return "Analysez le terrain et assurez-vous de la cohérence de vos unités.";
    }

    /**
     * Evaluate by pixel positions (relX, relY in [0..1]).
     * Converts each position to nearest zone then delegates to evaluate().
     */
    public static EvaluationResult evaluateByPosition(Mission mission,
                                                       Map<String, double[]> troopPositions, List<MapZone> zones) {
        Map<String, String> zoneMap = new HashMap<>();
        for (Map.Entry<String, double[]> entry : troopPositions.entrySet()) {
            String zoneId = findZoneAtPosition(entry.getValue()[0], entry.getValue()[1], zones);
            if (zoneId != null) zoneMap.put(entry.getKey(), zoneId);
        }
        return evaluate(mission, zoneMap, zones);
    }

    private static String findZoneAtPosition(double relX, double relY, List<MapZone> zones) {
        // Prefer zones that contain the point
        for (MapZone z : zones) {
            if (relX >= z.getX() && relX <= z.getX() + z.getWidth()
                    && relY >= z.getY() && relY <= z.getY() + z.getHeight()) {
                return z.getId();
            }
        }
        // Fallback: nearest zone center
        return zones.stream()
                .min(Comparator.comparingDouble(z -> {
                    double cx = z.getX() + z.getWidth() / 2;
                    double cy = z.getY() + z.getHeight() / 2;
                    return Math.hypot(relX - cx, relY - cy);
                }))
                .map(MapZone::getId)
                .orElse(null);
    }

    // Helper classes
    private static class CriteriaEvalResult {
        int points;
        boolean satisfied;
        String feedbackDetail;

        CriteriaEvalResult(int points, boolean satisfied, String feedbackDetail) {
            this.points = points;
            this.satisfied = satisfied;
            this.feedbackDetail = feedbackDetail;
        }
    }

    public static class EvaluationResult {
        private int totalScore;
        private int maxScore;
        private List<String> feedbackLines = new ArrayList<>();
        private List<CriteriaResult> criteriaResults = new ArrayList<>();
        private String tacticalAdvice;

        public int getTotalScore() { return totalScore; }
        public int getMaxScore() { return maxScore; }
        public List<String> getFeedbackLines() { return feedbackLines; }
        public List<CriteriaResult> getCriteriaResults() { return criteriaResults; }
        public String getTacticalAdvice() { return tacticalAdvice; }

        public void setTotalScore(int v) { this.totalScore = v; }
        public void setMaxScore(int v) { this.maxScore = v; }
        public void setFeedbackLines(List<String> v) { this.feedbackLines = v; }
        public void setCriteriaResults(List<CriteriaResult> v) { this.criteriaResults = v; }
        public void setTacticalAdvice(String v) { this.tacticalAdvice = v; }
        public double getPercentage() { return maxScore > 0 ? (double) totalScore / maxScore * 100 : 0; }
        public String getGrade() {
            double pct = getPercentage();
            if (pct >= 90) return "EXCELLENCE";
            if (pct >= 75) return "TRÈS BIEN";
            if (pct >= 60) return "BIEN";
            if (pct >= 40) return "PASSABLE";
            return "ÉCHEC";
        }
    }

    public static class CriteriaResult {
        private String name;
        private int earned;
        private int max;
        private boolean satisfied;

        public CriteriaResult(String name, int earned, int max, boolean satisfied) {
            this.name = name;
            this.earned = earned;
            this.max = max;
            this.satisfied = satisfied;
        }

        public String getName() { return name; }
        public int getEarned() { return earned; }
        public int getMax() { return max; }
        public boolean isSatisfied() { return satisfied; }
    }
}
