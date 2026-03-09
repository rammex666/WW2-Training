package fr.rammex;

import fr.rammex.admin.*;
import fr.rammex.db.*;
import fr.rammex.model.*;
import fr.rammex.service.*;
import fr.rammex.ui.*;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.*;

public class AppController {

    private Stage stage;
    private String playerName = "Sous-Lieutenant";
    private String playerRank = "Sous-Officier";
    private List<SessionResult> sessionHistory = new ArrayList<>();

    private String currentAdminUser = null;

    // Lazy-init repositories
    private UserRepository userRepository;
    private MissionRepository missionRepository;
    private ScoreRepository scoreRepository;

    public AppController(Stage stage) {
        this.stage = stage;
        stage.setTitle("⚔ WWII Tactical Training - Garry's Mod RP ⚔");
        stage.setMinWidth(1200);
        stage.setMinHeight(800);

        // Initialize repositories (connect to MongoDB if available)
        userRepository = new UserRepository();
        missionRepository = new MissionRepository();
        scoreRepository = new ScoreRepository();
    }

    // ── NAVIGATION ────────────────────────────────────────────────────────────

    public void showMainMenu() {
        MainMenuScreen menu = new MainMenuScreen(this);
        applyScene(menu.createScene());
        stage.show();
    }

    public void showMissionSelect() {
        MissionSelectScreen screen = new MissionSelectScreen(this);
        applyScene(screen.createScene());
    }

    public void showPlayerSetup(Mission mission) {
        PlayerSetupScreen screen = new PlayerSetupScreen(this, mission);
        applyScene(screen.createScene());
    }

    public void startMission(Mission mission, String name, String rank) {
        this.playerName = name;
        this.playerRank = rank;
        BattleMapScreen screen = new BattleMapScreen(this, mission);
        applyScene(screen.createScene());
    }

    public void showResults(Mission mission, ScoringEngine.EvaluationResult result) {
        SessionResult sr = new SessionResult(playerName, playerRank, mission.getTitle(),
                result.getTotalScore(), result.getMaxScore());
        sr.setFeedback(result.getTacticalAdvice());
        sessionHistory.add(sr);
        saveScore(sr);

        ResultScreen screen = new ResultScreen(this, mission, result, sr);
        applyScene(screen.createScene());
    }

    public void showLeaderboard() {
        List<SessionResult> scores = getLeaderboardScores();
        LeaderboardScreen screen = new LeaderboardScreen(this, scores);
        applyScene(screen.createScene());
    }

    // ── ADMIN NAVIGATION ─────────────────────────────────────────────────────

    public void showAdminLogin() {
        AdminLoginScreen screen = new AdminLoginScreen(this);
        applyScene(screen.createScene());
    }

    public void showEditorMenu() {
        EditorMenuScreen screen = new EditorMenuScreen(this);
        applyScene(screen.createScene());
    }

    public void showMissionEditor(Mission mission) {
        MissionEditorScreen screen = new MissionEditorScreen(this, mission);
        applyScene(screen.createScene());
    }

    public void showMapManager() {
        MapManagerScreen screen = new MapManagerScreen(this);
        applyScene(screen.createScene());
    }

    // ── DATA ACCESS ───────────────────────────────────────────────────────────

    /**
     * Returns hardcoded missions + MongoDB missions, deduplicated by id.
     * MongoDB missions take precedence over hardcoded ones with the same id.
     */
    public List<Mission> getMissions() {
        List<Mission> hardcoded = MissionFactory.createAllMissions();
        List<Mission> fromDb = missionRepository.findAll();

        // Build a map from DB missions (by id) for deduplication
        Map<String, Mission> dbMap = new LinkedHashMap<>();
        for (Mission m : fromDb) dbMap.put(m.getId(), m);

        List<Mission> merged = new ArrayList<>();
        // Add hardcoded only if not overridden in DB
        for (Mission m : hardcoded) {
            if (!dbMap.containsKey(m.getId())) merged.add(m);
        }
        // Add all DB missions
        merged.addAll(dbMap.values());

        return merged;
    }

    public List<MapZone> getZonesForMission(String missionId) {
        // If the mission is in MongoDB, get zones from there
        List<MapZone> dbZones = missionRepository.getZonesForMission(missionId);
        if (!dbZones.isEmpty()) return dbZones;
        // Otherwise use hardcoded zones
        return MissionFactory.getZonesForMission(missionId);
    }

    public void saveMission(Mission mission, List<MapZone> zones) {
        missionRepository.save(mission, zones);
    }

    public void deleteMission(String missionId) {
        missionRepository.deleteById(missionId);
    }

    public void saveScore(SessionResult sr) {
        scoreRepository.save(sr);
    }

    public List<SessionResult> getLeaderboardScores() {
        // If MongoDB is connected, load from DB; else use in-memory
        if (scoreRepository.isAvailable()) {
            return scoreRepository.findAll();
        }
        return sessionHistory;
    }

    // ── AUTH ──────────────────────────────────────────────────────────────────

    public boolean adminLogin(String username, String password) {
        if (!userRepository.isAvailable()) return false;
        boolean ok = userRepository.authenticate(username, password);
        if (ok) currentAdminUser = username;
        return ok;
    }

    public void adminLogout() {
        currentAdminUser = null;
    }

    public String getCurrentAdminUser() { return currentAdminUser; }
    public boolean isAdminLoggedIn() { return currentAdminUser != null; }
    public boolean isMongoAvailable() { return missionRepository.isAvailable(); }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private void applyScene(Scene scene) {
        var css = getClass().getResource("/styles.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
        stage.setScene(scene);
    }

    public Stage getStage() { return stage; }
    public String getPlayerName() { return playerName; }
    public String getPlayerRank() { return playerRank; }
    public List<SessionResult> getSessionHistory() { return sessionHistory; }
    public MissionRepository getMissionRepository() { return missionRepository; }
}
