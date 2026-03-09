package fr.rammex.service;

import fr.rammex.model.*;
import fr.rammex.model.EvaluationCriteria.CriteriaType;
import java.util.*;

public class MissionFactory {

    public static List<Mission> createAllMissions() {
        List<Mission> missions = new ArrayList<>();
        missions.add(createNormandieBeach());
        missions.add(createArdennesForest());
        missions.add(createStalingradUrban());
        return missions;
    }

    // ---- MISSION 1 : Débarquement en Normandie ----
    public static Mission createNormandieBeach() {
        Mission m = new Mission(
            "normandie_beach",
            "Opération Overlord - Secteur Omaha",
            "Sécuriser la plage d'Omaha et établir une tête de pont",
            "DIFFICILE",
            "normandie",
            "Prendre le contrôle des 3 sorties de plage et neutraliser les bunkers ennemis",
            "Sous-officier ! L'ennemi tient solidement les falaises. Vos hommes doivent progresser " +
            "sous le feu. Utilisez la fumée, les véhicules amphibies et le couvert naturel pour " +
            "atteindre les sorties. La marée monte — vous avez peu de temps.",
            350,
            "NORMANDIE"
        );

        m.addTroop(new TroopUnit("inf_1", "Escouade Infanterie A", "INFANTRY", "⚔", 70, 3, "Infanterie de base"));
        m.addTroop(new TroopUnit("inf_2", "Escouade Infanterie B", "INFANTRY", "⚔", 70, 3, "Infanterie de base"));
        m.addTroop(new TroopUnit("medic_1", "Médecin de combat", "MEDIC", "✚", 20, 2, "Soigne les blessés"));
        m.addTroop(new TroopUnit("eng_1", "Section Génie", "ENGINEER", "⚙", 50, 2, "Démineur essentiel"));
        m.addTroop(new TroopUnit("snip_1", "Tireur d'élite", "SNIPER", "🎯", 90, 1, "Haute précision"));
        m.addTroop(new TroopUnit("art_1", "Section Mortier", "ARTILLERY", "💥", 100, 1, "Appui feu indirect"));

        // Position Criteria
        m.addCriteria(EvaluationCriteria.position("Assaut Sortie Gauche", "Infanterie sur la sortie gauche", 70, "sortie_gauche", "INFANTRY"));
        m.addCriteria(EvaluationCriteria.position("Assaut Sortie Centre", "Infanterie sur la sortie centre", 70, "sortie_centre", "INFANTRY"));
        m.addCriteria(EvaluationCriteria.position("Appui Sniper", "Sniper en position élevée", 50, "colline_flanc", "SNIPER"));
        
        // Cohesion Criteria
        m.addCriteria(EvaluationCriteria.cohesion("Soutien Médical", "Le médecin doit être proche de l'infanterie", 60, "MEDIC", "INFANTRY", 0.15));
        
        // Avoidance Criteria
        m.addCriteria(EvaluationCriteria.avoidance("Éviter Tir Croisé", "Ne pas rester immobile au centre de la plage", 50, "plage_centre"));
        
        // Diversity Criteria
        EvaluationCriteria div = new EvaluationCriteria("Force Combinée", "Avoir au moins 2 types d'unités à la sortie centre", 50, CriteriaType.DIVERSITY);
        div.setZoneId("sortie_centre");
        div.setRequiredCount(2);
        m.addCriteria(div);

        return m;
    }

    // ---- MISSION 2 : Bataille des Ardennes ----
    public static Mission createArdennesForest() {
        Mission m = new Mission(
            "ardennes_foret",
            "Bataille des Ardennes - Tenue défensive",
            "Défendre le carrefour stratégique contre l'offensive ennemie",
            "MOYEN",
            "ardennes",
            "Tenir le carrefour pendant que les renforts arrivent. Bloquer toutes les routes d'accès.",
            "Sous-officier ! L'ennemi attaque avec des blindés. Défendez le carrefour !",
            300,
            "ARDENNES"
        );

        m.addTroop(new TroopUnit("inf_1", "Escouade Parachutiste A", "INFANTRY", "⚔", 85, 3, "Parachutistes"));
        m.addTroop(new TroopUnit("tank_1", "Sherman M4", "TANK", "🛡", 120, 2, "Blindé"));
        m.addTroop(new TroopUnit("art_1", "Canon antichar", "ARTILLERY", "💥", 110, 1, "Antichar"));
        m.addTroop(new TroopUnit("snip_1", "Observateur", "SNIPER", "🎯", 80, 1, "Repère l'ennemi"));
        m.addTroop(new TroopUnit("medic_1", "Médecin", "MEDIC", "✚", 20, 2, "Soins"));

        m.addCriteria(EvaluationCriteria.position("Contrôle Carrefour", "Unité au carrefour central", 80, "carrefour_centre", null));
        
        // Consolidation
        EvaluationCriteria cons = new EvaluationCriteria("Ligne de Défense Nord", "Au moins 2 unités sur la route nord", 80, CriteriaType.CONSOLIDATION);
        cons.setZoneId("route_nord");
        cons.setRequiredCount(2);
        m.addCriteria(cons);

        m.addCriteria(EvaluationCriteria.position("Antichar", "Canon antichar face à l'Est", 70, "route_est", "ARTILLERY"));
        
        // Cohesion
        m.addCriteria(EvaluationCriteria.cohesion("Appui Blindé", "Le Sherman doit soutenir l'infanterie", 70, "TANK", "INFANTRY", 0.2));

        return m;
    }

    // ---- MISSION 3 : Stalingrad Combat Urbain ----
    public static Mission createStalingradUrban() {
        Mission m = new Mission(
            "stalingrad_urbain",
            "Stalingrad - Assaut Urbain",
            "Prendre le contrôle du quartier industriel bâtiment par bâtiment",
            "ÉLITE",
            "stalingrad",
            "Capturer l'usine centrale et les 2 immeubles clés.",
            "Sous-officier ! Stalingrad est un enfer. Avancez méthodiquement.",
            400,
            "STALINGRAD"
        );

        m.addTroop(new TroopUnit("inf_1", "Escouade d'assaut A", "INFANTRY", "⚔", 90, 3, "Spécialistes"));
        m.addTroop(new TroopUnit("snip_1", "Sniper Vassili", "SNIPER", "🎯", 100, 1, "Sniper d'élite"));
        m.addTroop(new TroopUnit("eng_1", "Sapeurs", "ENGINEER", "⚙", 60, 2, "Explosifs"));
        m.addTroop(new TroopUnit("medic_1", "Infirmier", "MEDIC", "✚", 20, 1, "Soins rapides"));

        m.addCriteria(EvaluationCriteria.position("Assaut Usine", "Infanterie dans l'usine", 100, "usine_centrale", "INFANTRY"));
        
        // Cohesion: Engineer near assault
        m.addCriteria(EvaluationCriteria.cohesion("Brèche Génie", "L'ingénieur doit ouvrir la voie à l'assaut", 80, "ENGINEER", "INFANTRY", 0.1));
        
        m.addCriteria(EvaluationCriteria.position("Position Dominante", "Sniper sur le toit nord", 80, "toit_immeuble_nord", "SNIPER"));
        
        // Avoidance: Backstreet should be clear (retreat path)
        m.addCriteria(EvaluationCriteria.avoidance("Route de Repli", "Garder la rue arrière dégagée", 70, "rue_arriere"));

        // Diversity
        EvaluationCriteria div = new EvaluationCriteria("Groupe d'Assaut Mixte", "Combiner infanterie, génie et médecin", 70, CriteriaType.DIVERSITY);
        div.setZoneId("entree_usine");
        div.setRequiredCount(3);
        m.addCriteria(div);

        return m;
    }

    public static List<MapZone> getZonesForMission(String missionId) {
        switch (missionId) {
            case "normandie_beach": return getNormandieZones();
            case "ardennes_foret": return getArdennesZones();
            case "stalingrad_urbain": return getStalingradZones();
            default: return new ArrayList<>();
        }
    }

    private static List<MapZone> getNormandieZones() {
        List<MapZone> zones = new ArrayList<>();
        zones.add(new MapZone("plage_centre", "Plage Centrale", "Zone exposée", 0.35, 0.72, 0.3, 0.15, "LOW", "OFFENSIF"));
        zones.add(new MapZone("sortie_gauche", "Sortie Gauche", "Objectif prioritaire", 0.1, 0.5, 0.18, 0.18, "HIGH", "OFFENSIF"));
        zones.add(new MapZone("sortie_centre", "Sortie Centre", "Objectif prioritaire", 0.41, 0.48, 0.18, 0.18, "HIGH", "OFFENSIF"));
        zones.add(new MapZone("colline_flanc", "Colline Flanc Droit", "Position élevée", 0.72, 0.25, 0.2, 0.25, "HIGH", "COLLINE"));
        zones.add(new MapZone("couverture_arriere", "Zone Couverture Arrière", "Zone protégée", 0.55, 0.78, 0.2, 0.14, "MEDIUM", "DEFENSIF"));
        zones.add(new MapZone("arriere_gauche", "Arrière Gauche", "Position mortier", 0.05, 0.75, 0.18, 0.16, "MEDIUM", "COUVERTURE"));
        zones.add(new MapZone("falaise_est", "Falaises Est", "Position ennemie", 0.75, 0.05, 0.22, 0.35, "HIGH", "DEFENSIF"));
        return zones;
    }

    private static List<MapZone> getArdennesZones() {
        List<MapZone> zones = new ArrayList<>();
        zones.add(new MapZone("carrefour_centre", "Carrefour Central", "Objectif principal", 0.42, 0.42, 0.16, 0.16, "HIGH", "OFFENSIF"));
        zones.add(new MapZone("route_nord", "Route Nord", "Axe blindé ennemi", 0.35, 0.05, 0.3, 0.2, "HIGH", "PONT"));
        zones.add(new MapZone("route_est", "Route Est", "Axe blindé ennemi", 0.72, 0.35, 0.25, 0.2, "HIGH", "PONT"));
        zones.add(new MapZone("lisiere_foret", "Lisière Forêt", "Observation", 0.05, 0.3, 0.2, 0.35, "MEDIUM", "COUVERTURE"));
        zones.add(new MapZone("foret_dense", "Forêt Dense", "Embuscade", 0.05, 0.05, 0.28, 0.28, "MEDIUM", "COUVERTURE"));
        zones.add(new MapZone("arriere_centre", "Zone de Réserve", "Réserve", 0.38, 0.68, 0.24, 0.2, "LOW", "DEFENSIF"));
        zones.add(new MapZone("village_ouest", "Village Ouest", "Position secondaire", 0.05, 0.65, 0.22, 0.28, "MEDIUM", "VILLE"));
        return zones;
    }

    private static List<MapZone> getStalingradZones() {
        List<MapZone> zones = new ArrayList<>();
        zones.add(new MapZone("usine_centrale", "Usine Centrale", "Objectif capturer", 0.38, 0.38, 0.24, 0.24, "HIGH", "VILLE"));
        zones.add(new MapZone("toit_immeuble_nord", "Toit Nord", "Sniper dominant", 0.35, 0.05, 0.3, 0.18, "HIGH", "COLLINE"));
        zones.add(new MapZone("toit_immeuble_sud", "Toit Sud", "Sniper dominant", 0.35, 0.77, 0.3, 0.18, "HIGH", "COLLINE"));
        zones.add(new MapZone("entree_usine", "Entrée Usine", "Point d'assaut", 0.28, 0.45, 0.12, 0.12, "HIGH", "OFFENSIF"));
        zones.add(new MapZone("flanc_est_ruines", "Ruines Est", "Couverture flanc", 0.72, 0.38, 0.22, 0.24, "MEDIUM", "COUVERTURE"));
        zones.add(new MapZone("rue_arriere", "Rue Arrière", "Zone de repli", 0.05, 0.42, 0.2, 0.2, "LOW", "DEFENSIF"));
        zones.add(new MapZone("immeuble_nord_ouest", "Immeuble NO", "Flanquement", 0.05, 0.05, 0.25, 0.3, "MEDIUM", "VILLE"));
        return zones;
    }
}
