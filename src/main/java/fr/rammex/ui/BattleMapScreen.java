package fr.rammex.ui;

import fr.rammex.AppController;
import fr.rammex.db.MongoDBService;
import fr.rammex.model.*;
import fr.rammex.service.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

public class BattleMapScreen {

    private static final double MAP_W = 870;
    private static final double MAP_H = 680;
    private static final double TOKEN_R = 20;

    private final AppController controller;
    private final Mission mission;
    private final List<MapZone> zones;
    private final List<EnemyUnit> enemies;

    private TroopUnit selectedTroop = null;
    private final Map<String, double[]> troopPositions = new HashMap<>();
    private final Map<String, StackPane> troopTokens = new HashMap<>();

    private Label statusLabel;
    private Label selectedTroopLabel;
    private VBox troopListBox;
    private Pane mapPane;
    private boolean zonesVisible = true;
    private final List<Node> zoneOverlays = new ArrayList<>();
    private double missionZoom = 1.0;

    public BattleMapScreen(AppController controller, Mission mission) {
        this.controller = controller;
        this.mission = mission;
        this.zones = controller.getZonesForMission(mission.getId());
        // Use admin-placed enemies if any, otherwise generate via AI
        if (mission.getSavedEnemies() != null && !mission.getSavedEnemies().isEmpty()) {
            this.enemies = mission.getSavedEnemies();
        } else {
            this.enemies = EnemyPlacementAI.generateEnemies(mission, zones);
        }
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + UITheme.BG_DARK + ";");
        root.setTop(buildHeader());
        root.setLeft(buildLeftPanel());

        Pane mp = buildMapPane();
        javafx.scene.Group mapGroup = new javafx.scene.Group(mp);
        javafx.scene.control.ScrollPane mapScroll = new javafx.scene.control.ScrollPane(mapGroup);
        mapScroll.setStyle("-fx-background: " + UITheme.BG_DARK + "; -fx-background-color: " + UITheme.BG_DARK + ";");
        mapScroll.addEventFilter(javafx.scene.input.ScrollEvent.SCROLL, e -> {
            if (e.isControlDown()) {
                double factor = e.getDeltaY() > 0 ? 1.1 : 0.9;
                missionZoom = Math.max(0.3, Math.min(4.0, missionZoom * factor));
                mp.setScaleX(missionZoom);
                mp.setScaleY(missionZoom);
                e.consume();
            }
        });
        root.setCenter(mapScroll);

        root.setRight(buildRightPanel());
        return new Scene(root, 1350, 850);
    }

    // ── HEADER ───────────────────────────────────────────────────────────────

    private HBox buildHeader() {
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 20, 12, 20));
        header.setStyle("-fx-background-color: " + UITheme.BG_PANEL +
                "; -fx-border-color: " + UITheme.GOLD + "; -fx-border-width: 0 0 2 0;");

        Label missionLabel = new Label("\u2694 " + mission.getTitle().toUpperCase());
        missionLabel.setStyle(UITheme.labelTitleStyle(18));

        Region s1 = new Region(); HBox.setHgrow(s1, Priority.ALWAYS);

        statusLabel = new Label("S\u00e9lectionnez une unit\u00e9 puis cliquez sur la carte");
        statusLabel.setStyle("-fx-text-fill: " + UITheme.GOLD_LIGHT +
                "; -fx-font-size: 13px; -fx-font-style: italic;");

        Region s2 = new Region(); HBox.setHgrow(s2, Priority.ALWAYS);

        Button toggleBtn = new Button("\uD83D\uDC41 Zones");
        toggleBtn.setStyle(UITheme.buttonGreenStyle());
        toggleBtn.setOnAction(e -> {
            zonesVisible = !zonesVisible;
            zoneOverlays.forEach(n -> n.setVisible(zonesVisible));
            toggleBtn.setText(zonesVisible ? "\uD83D\uDC41 Zones" : "Zones masqu\u00e9es");
        });

        Button evalBtn = new Button("\u2705 \u00c9VALUER LE D\u00c9PLOIEMENT");
        evalBtn.setStyle(UITheme.buttonGoldStyle());
        evalBtn.setOnAction(e -> evaluateDeployment());

        header.getChildren().addAll(missionLabel, s1, statusLabel, s2, toggleBtn, evalBtn);
        return header;
    }

    // ── LEFT PANEL ───────────────────────────────────────────────────────────

    private VBox buildLeftPanel() {
        VBox panel = new VBox(12);
        panel.setPrefWidth(240);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: " + UITheme.BG_PANEL +
                "; -fx-border-color: " + UITheme.BORDER_COLOR + "; -fx-border-width: 0 1 0 0;");

        Label title = new Label("\uD83E\uDEA6 UNIT\u00c9S DISPONIBLES");
        title.setStyle(UITheme.labelTitleStyle(13));

        selectedTroopLabel = new Label("Cliquer sur une unit\u00e9 puis placer sur la carte");
        selectedTroopLabel.setStyle("-fx-text-fill: " + UITheme.TEXT_SECONDARY +
                "; -fx-font-size: 11px; -fx-font-style: italic;");
        selectedTroopLabel.setWrapText(true);

        troopListBox = new VBox(8);
        for (TroopUnit troop : mission.getAvailableTroops()) {
            troopListBox.getChildren().add(buildTroopCard(troop));
        }

        ScrollPane scroll = new ScrollPane(troopListBox);
        scroll.setStyle("-fx-background: " + UITheme.BG_PANEL +
                "; -fx-background-color: " + UITheme.BG_PANEL + ";");
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Button resetBtn = new Button("\uD83D\uDD04 R\u00e9initialiser");
        resetBtn.setStyle(UITheme.buttonRedStyle());
        resetBtn.setMaxWidth(Double.MAX_VALUE);
        resetBtn.setOnAction(e -> resetPlacements());

        Label tip = new Label("\uD83D\uDCA1 Clic sur la carte = placer  \u2022  Clic sur le jeton = retirer  \u2022  Glisser = d\u00e9placer  \u2022  Ctrl+Molette = zoomer");
        tip.setStyle(UITheme.labelSecondaryStyle());
        tip.setWrapText(true);

        panel.getChildren().addAll(title, selectedTroopLabel, scroll, resetBtn, tip);
        return panel;
    }

    // ── RIGHT PANEL ──────────────────────────────────────────────────────────

    private VBox buildRightPanel() {
        VBox panel = new VBox(12);
        panel.setPrefWidth(220);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: " + UITheme.BG_PANEL +
                "; -fx-border-color: " + UITheme.BORDER_COLOR + "; -fx-border-width: 0 0 0 1;");

        Label objTitle = new Label("\uD83C\uDFAF CRIT\u00c8RES D'\u00c9VALUATION");
        objTitle.setStyle(UITheme.labelTitleStyle(13));

        VBox criteriaBox = new VBox(10);
        for (EvaluationCriteria crit : mission.getCriteria()) {
            VBox card = new VBox(4);
            card.setStyle("-fx-background-color: " + UITheme.BG_CARD + "; -fx-padding: 8;" +
                    " -fx-border-color: " + UITheme.BORDER_COLOR + "; -fx-border-width: 1;");
            Label n = new Label(crit.getName());
            n.setStyle("-fx-text-fill: " + UITheme.GOLD + "; -fx-font-size: 12px; -fx-font-weight: bold;");
            n.setWrapText(true);
            Label d = new Label(crit.getDescription());
            d.setStyle(UITheme.labelSecondaryStyle()); d.setWrapText(true);
            Label p = new Label("+" + crit.getMaxPoints() + " pts");
            p.setStyle("-fx-text-fill: #6a8c4a; -fx-font-size: 12px; -fx-font-weight: bold;");
            card.getChildren().addAll(n, d, p);
            criteriaBox.getChildren().add(card);
        }

        ScrollPane critScroll = new ScrollPane(criteriaBox);
        critScroll.setStyle("-fx-background: " + UITheme.BG_PANEL +
                "; -fx-background-color: " + UITheme.BG_PANEL + ";");
        critScroll.setFitToWidth(true);
        VBox.setVgrow(critScroll, Priority.ALWAYS);

        Separator sep = new Separator();

        boolean adminPlaced = mission.getSavedEnemies() != null && !mission.getSavedEnemies().isEmpty();
        Label enemyTitle = new Label(adminPlaced ? "\uD83D\uDC80 ENNEMIS (ADMIN)" : "\uD83D\uDC80 ENNEMIS AUTO-G\u00c9N\u00c9R\u00c9S");
        enemyTitle.setStyle("-fx-text-fill: #cc4444; -fx-font-size: 11px; -fx-font-weight: bold;");
        Label enemyDesc = new Label("Difficult\u00e9 : " + mission.getDifficulty() +
                "\n" + enemies.size() + " unit\u00e9s ennemies d\u00e9ploy\u00e9es" +
                (adminPlaced ? "\n(placement manuel)" : ""));
        enemyDesc.setStyle(UITheme.labelSecondaryStyle()); enemyDesc.setWrapText(true);

        Label totalPts = new Label("TOTAL MAX : " + mission.getMaxPoints() + " pts");
        totalPts.setStyle(UITheme.labelTitleStyle(13));

        panel.getChildren().addAll(objTitle, critScroll, sep, enemyTitle, enemyDesc, totalPts);
        return panel;
    }

    // ── MAP PANE ─────────────────────────────────────────────────────────────

    private Pane buildMapPane() {
        mapPane = new Pane();
        mapPane.setPrefSize(MAP_W, MAP_H);
        mapPane.setMaxSize(MAP_W, MAP_H);

        // 1. Background: image or drawn fallback
        ImageView iv = tryLoadMapImage();
        if (iv != null) {
            mapPane.getChildren().add(iv);
        } else {
            drawFallbackBackground();
        }

        // 2. Zone semi-transparent overlays
        for (MapZone zone : zones) {
            Node overlay = buildZoneOverlay(zone);
            zoneOverlays.add(overlay);
            mapPane.getChildren().add(overlay);
        }

        // 3. Enemy tokens (placed first so troop tokens render on top)
        for (EnemyUnit enemy : enemies) {
            mapPane.getChildren().add(buildEnemyToken(enemy));
        }

        // 4. Map click -> place selected troop
        mapPane.setOnMouseClicked(e -> {
            if (selectedTroop != null) {
                placeTroopAt(selectedTroop, e.getX(), e.getY());
            }
        });

        return mapPane;
    }

    private ImageView tryLoadMapImage() {
        if (mission.getMapName() == null || mission.getMapName().isEmpty()) return null;
        String[] exts = {".png", ".jpg", ".jpeg", ".gif"};

        // 1. Search external maps directory (uploaded via MapManager)
        String mapsDir = MongoDBService.getInstance().getMapsDirectory();
        for (String ext : exts) {
            File extFile = new File(mapsDir, mission.getMapName() + ext);
            if (extFile.exists()) {
                try (InputStream is = new FileInputStream(extFile)) {
                    Image img = new Image(is);
                    ImageView iv = new ImageView(img);
                    iv.setFitWidth(MAP_W); iv.setFitHeight(MAP_H);
                    iv.setPreserveRatio(false);
                    return iv;
                } catch (Exception ignored) {}
            }
        }

        // 2. Search classpath /maps/ (bundled resources)
        for (String ext : exts) {
            InputStream is = getClass().getResourceAsStream("/maps/" + mission.getMapName() + ext);
            if (is != null) {
                Image img = new Image(is);
                ImageView iv = new ImageView(img);
                iv.setFitWidth(MAP_W); iv.setFitHeight(MAP_H);
                iv.setPreserveRatio(false);
                return iv;
            }
        }
        return null;
    }

    private void drawFallbackBackground() {
        javafx.scene.shape.Rectangle bg = new javafx.scene.shape.Rectangle(MAP_W, MAP_H);
        bg.setFill(Color.web("#1a2010"));
        mapPane.getChildren().add(bg);
        // Grid
        for (int x = 0; x < MAP_W; x += 40) {
            javafx.scene.shape.Rectangle line = new javafx.scene.shape.Rectangle(x, 0, 1, MAP_H);
            line.setFill(Color.web("#2a3020", 0.5));
            mapPane.getChildren().add(line);
        }
        for (int y = 0; y < MAP_H; y += 40) {
            javafx.scene.shape.Rectangle line = new javafx.scene.shape.Rectangle(0, y, MAP_W, 1);
            line.setFill(Color.web("#2a3020", 0.5));
            mapPane.getChildren().add(line);
        }
        // Terrain specifics
        switch (mission.getTerrain()) {
            case "NORMANDIE":
                javafx.scene.shape.Rectangle water = new javafx.scene.shape.Rectangle(0, 590, MAP_W, 90);
                water.setFill(Color.web("#0a2040", 0.7));
                mapPane.getChildren().add(water);
                Label wLbl = new Label("\u2248 MER \u2248");
                wLbl.setStyle("-fx-text-fill: #2060a0; -fx-font-size: 14px; -fx-font-weight: bold;");
                wLbl.setLayoutX(380); wLbl.setLayoutY(625);
                mapPane.getChildren().add(wLbl);
                break;
            case "ARDENNES":
                addForestPatch(30, 30, 200, 200);
                addForestPatch(600, 500, 200, 150);
                break;
            default:
                break;
        }
    }

    private void addForestPatch(double x, double y, double w, double h) {
        javafx.scene.shape.Rectangle r = new javafx.scene.shape.Rectangle(x, y, w, h);
        r.setFill(Color.web("#1a3010", 0.6)); r.setStroke(Color.web("#2a4020"));
        mapPane.getChildren().add(r);
        Label lbl = new Label("\uD83C\uDF32 For\u00eat");
        lbl.setStyle("-fx-text-fill: #2a5020; -fx-font-size: 10px;");
        lbl.setLayoutX(x + 5); lbl.setLayoutY(y + 5);
        mapPane.getChildren().add(lbl);
    }

    private Node buildZoneOverlay(MapZone zone) {
        double w = zone.getWidth() * MAP_W;
        double h = zone.getHeight() * MAP_H;
        double x = zone.getX() * MAP_W;
        double y = zone.getY() * MAP_H;

        StackPane overlay = new StackPane();
        overlay.setLayoutX(x); overlay.setLayoutY(y);
        overlay.setPrefSize(w, h);

        boolean isHigh = "HIGH".equals(zone.getStrategicValue());
        String zoneColor = UITheme.getZoneColor(zone.getType());
        overlay.setStyle("-fx-background-color: " + zoneColor + "44; -fx-border-color: " +
                (isHigh ? UITheme.GOLD : UITheme.BORDER_COLOR) +
                "; -fx-border-width: " + (isHigh ? "2" : "1") + ";");

        Label nameLbl = new Label(zone.getName());
        nameLbl.setStyle("-fx-text-fill: " + UITheme.TEXT_PRIMARY +
                "; -fx-font-size: 9px; -fx-font-weight: bold;" +
                " -fx-background-color: #00000066; -fx-padding: 2;");
        nameLbl.setWrapText(true);
        nameLbl.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        StackPane.setAlignment(nameLbl, Pos.TOP_CENTER);
        overlay.getChildren().add(nameLbl);

        overlay.setRotate(zone.getRotation());

        overlay.setOnMouseEntered(e -> statusLabel.setText(
                "\uD83D\uDCCD " + zone.getName() + " \u2014 " + zone.getDescription() + " | " + zone.getStrategicValue()));
        overlay.setOnMouseExited(e -> updateStatusDefault());

        // Pass clicks through to place troop
        overlay.setOnMouseClicked(e -> {
            if (selectedTroop != null) {
                double px = x + e.getX();
                double py = y + e.getY();
                placeTroopAt(selectedTroop, px, py);
                e.consume();
            }
        });

        return overlay;
    }

    // ── TROOP TOKENS ─────────────────────────────────────────────────────────

    private void placeTroopAt(TroopUnit troop, double px, double py) {
        px = Math.max(TOKEN_R, Math.min(MAP_W - TOKEN_R, px));
        py = Math.max(TOKEN_R, Math.min(MAP_H - TOKEN_R, py));

        // Remove previous token for this troop
        StackPane old = troopTokens.remove(troop.getId());
        if (old != null) mapPane.getChildren().remove(old);

        troopPositions.put(troop.getId(), new double[]{px / MAP_W, py / MAP_H});

        StackPane token = buildTroopToken(troop, px, py);
        troopTokens.put(troop.getId(), token);
        mapPane.getChildren().add(token);
        token.toFront();

        updateTroopCardStyle(troop, true);
        statusLabel.setText("\u2705 " + troop.getName() + " d\u00e9ploy\u00e9e \u2014 Clic sur le jeton pour retirer, glisser pour d\u00e9placer");
        selectedTroop = null;
        selectedTroopLabel.setText("Cliquer sur une unit\u00e9 puis placer sur la carte");
        refreshCardStyles();
    }

    private StackPane buildTroopToken(TroopUnit troop, double px, double py) {
        StackPane token = new StackPane();
        Circle bg = new Circle(TOKEN_R);
        bg.setFill(Color.web(UITheme.getTroopColor(troop.getType()), 0.9));
        bg.setStroke(Color.web(UITheme.GOLD)); bg.setStrokeWidth(2.5);
        Label icon = new Label(troop.getIcon());
        icon.setStyle("-fx-font-size: 13px;");
        token.getChildren().addAll(bg, icon);
        token.setLayoutX(px - TOKEN_R);
        token.setLayoutY(py - TOKEN_R);

        Tooltip.install(token, new Tooltip(troop.getName() + "\n" + troop.getType() +
                " \u2022 Force " + troop.getStrength() + "\nClic = retirer | Glisser = d\u00e9placer"));

        // Drag state
        boolean[] dragging = {false};
        double[] last = {0, 0};

        token.setOnMousePressed(e -> {
            dragging[0] = false;
            last[0] = e.getSceneX(); last[1] = e.getSceneY();
            token.toFront(); e.consume();
        });
        token.setOnMouseDragged(e -> {
            dragging[0] = true;
            double dx = e.getSceneX() - last[0];
            double dy = e.getSceneY() - last[1];
            token.setLayoutX(token.getLayoutX() + dx);
            token.setLayoutY(token.getLayoutY() + dy);
            last[0] = e.getSceneX(); last[1] = e.getSceneY();
            e.consume();
        });
        token.setOnMouseReleased(e -> {
            if (dragging[0]) {
                double newX = Math.max(-TOKEN_R, Math.min(MAP_W - TOKEN_R, token.getLayoutX()));
                double newY = Math.max(-TOKEN_R, Math.min(MAP_H - TOKEN_R, token.getLayoutY()));
                token.setLayoutX(newX); token.setLayoutY(newY);
                troopPositions.put(troop.getId(),
                        new double[]{(newX + TOKEN_R) / MAP_W, (newY + TOKEN_R) / MAP_H});
                statusLabel.setText("\u2705 " + troop.getName() + " repositionn\u00e9e");
            } else {
                // Simple click: remove
                removeTroop(troop.getId());
                statusLabel.setText(troop.getName() + " retir\u00e9e de la carte");
            }
            e.consume();
        });

        return token;
    }

    private StackPane buildEnemyToken(EnemyUnit enemy) {
        StackPane token = new StackPane();
        Circle bg = new Circle(TOKEN_R - 2);
        bg.setFill(Color.web("#8b0000", 0.85));
        bg.setStroke(Color.web("#ff4444")); bg.setStrokeWidth(2);
        Label icon = new Label("\uD83D\uDC80"); icon.setStyle("-fx-font-size: 12px;");
        token.getChildren().addAll(bg, icon);
        token.setLayoutX(enemy.getRelX() * MAP_W - (TOKEN_R - 2));
        token.setLayoutY(enemy.getRelY() * MAP_H - (TOKEN_R - 2));
        Tooltip.install(token, new Tooltip("ENNEMI : " + enemy.getName() + "\nType : " + enemy.getType()));
        token.setOnMouseClicked(e -> {
            statusLabel.setText("\u26a0 Ennemi d\u00e9tect\u00e9 : " + enemy.getName() + " [" + enemy.getType() + "]");
            e.consume();
        });
        return token;
    }

    // ── TROOP CARDS ──────────────────────────────────────────────────────────

    private VBox buildTroopCard(TroopUnit troop) {
        VBox card = new VBox(4);
        card.setStyle(styleCard("unselected"));

        HBox row = new HBox(8); row.setAlignment(Pos.CENTER_LEFT);
        String color = UITheme.getTroopColor(troop.getType());
        Label iconLbl = new Label(troop.getIcon());
        iconLbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 16px;");
        Label nameLbl = new Label(troop.getName());
        nameLbl.setStyle("-fx-text-fill: " + UITheme.TEXT_PRIMARY +
                "; -fx-font-size: 12px; -fx-font-weight: bold;");
        nameLbl.setWrapText(true);
        row.getChildren().addAll(iconLbl, nameLbl);

        Label typeLbl = new Label(troop.getType() + " \u2022 Force: " + troop.getStrength());
        typeLbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 10px;");
        card.getChildren().addAll(row, typeLbl);

        card.setOnMouseClicked(e -> {
            if (troopPositions.containsKey(troop.getId())) {
                statusLabel.setText("\u26a0 D\u00e9j\u00e0 d\u00e9ploy\u00e9e. Clic sur le jeton pour la retirer.");
                return;
            }
            selectedTroop = troop;
            selectedTroopLabel.setText("\u25b6 " + troop.getName() + "\n" + troop.getDescription());
            refreshCardStyles();
            statusLabel.setText("Unit\u00e9 s\u00e9lectionn\u00e9e : " + troop.getName() + " \u2014 Cliquez sur la carte");
        });
        card.setOnMouseEntered(e -> {
            if (selectedTroop != troop && !troopPositions.containsKey(troop.getId()))
                card.setStyle(styleCard("hover"));
        });
        card.setOnMouseExited(e -> {
            if (selectedTroop != troop && !troopPositions.containsKey(troop.getId()))
                card.setStyle(styleCard("unselected"));
        });
        return card;
    }

    private void refreshCardStyles() {
        List<TroopUnit> troops = mission.getAvailableTroops();
        for (int i = 0; i < troops.size(); i++) {
            TroopUnit t = troops.get(i);
            VBox card = (VBox) troopListBox.getChildren().get(i);
            if (troopPositions.containsKey(t.getId())) card.setStyle(styleCard("placed"));
            else if (t == selectedTroop)               card.setStyle(styleCard("selected"));
            else                                        card.setStyle(styleCard("unselected"));
        }
    }

    private void updateTroopCardStyle(TroopUnit troop, boolean placed) {
        int idx = mission.getAvailableTroops().indexOf(troop);
        if (idx >= 0) ((VBox) troopListBox.getChildren().get(idx))
                .setStyle(styleCard(placed ? "placed" : "unselected"));
    }

    private String styleCard(String state) {
        switch (state) {
            case "selected":   return "-fx-background-color: #2a3015; -fx-border-color: " + UITheme.GOLD + "; -fx-border-width: 2; -fx-padding: 8; -fx-cursor: hand;";
            case "hover":      return "-fx-background-color: #252520; -fx-border-color: " + UITheme.GOLD + "; -fx-border-width: 1; -fx-padding: 8; -fx-cursor: hand;";
            case "placed":     return "-fx-background-color: #102510; -fx-border-color: #4a8c4a; -fx-border-width: 2; -fx-padding: 8; -fx-cursor: hand; -fx-opacity: 0.65;";
            default:           return "-fx-background-color: " + UITheme.BG_CARD + "; -fx-border-color: " + UITheme.BORDER_COLOR + "; -fx-border-width: 1; -fx-padding: 8; -fx-cursor: hand;";
        }
    }

    // ── ACTIONS ──────────────────────────────────────────────────────────────

    private void removeTroop(String troopId) {
        StackPane token = troopTokens.remove(troopId);
        if (token != null) mapPane.getChildren().remove(token);
        troopPositions.remove(troopId);
        TroopUnit t = mission.getAvailableTroops().stream()
                .filter(u -> u.getId().equals(troopId)).findFirst().orElse(null);
        if (t != null) updateTroopCardStyle(t, false);
    }

    private void resetPlacements() {
        new ArrayList<>(troopPositions.keySet()).forEach(this::removeTroop);
        selectedTroop = null;
        selectedTroopLabel.setText("Cliquer sur une unit\u00e9 puis placer sur la carte");
        statusLabel.setText("D\u00e9ploiement r\u00e9initialis\u00e9.");
        refreshCardStyles();
    }

    private void evaluateDeployment() {
        if (troopPositions.isEmpty()) {
            statusLabel.setText("\u26a0 D\u00e9ployez au moins une unit\u00e9 avant d'\u00e9valuer !");
            return;
        }
        ScoringEngine.EvaluationResult result = ScoringEngine.evaluateByPosition(mission, troopPositions, zones);
        controller.showResults(mission, result);
    }

    private void updateStatusDefault() {
        statusLabel.setText(selectedTroop != null
                ? "Unit\u00e9 s\u00e9lectionn\u00e9e : " + selectedTroop.getName() + " \u2014 Cliquez sur la carte"
                : "S\u00e9lectionnez une unit\u00e9 puis cliquez sur la carte");
    }
}
