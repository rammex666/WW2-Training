package fr.rammex.admin;

import fr.rammex.AppController;
import fr.rammex.db.MongoDBService;
import fr.rammex.model.*;
import fr.rammex.ui.UITheme;
import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

public class MissionEditorScreen {

    private static final double MAP_W  = 720;
    private static final double MAP_H  = 560;
    private static final double HANDLE = 9;   // handle square size in px
    private static final double MIN_PX = 20;  // minimum zone size in px

    private final AppController controller;
    private final Mission mission;
    private final List<MapZone> zones;
    private final List<EnemyUnit> savedEnemies;

    // ── Left panel fields ──────────────────────────────────────────────────
    private TextField    titleField, objectifField, terrainField;
    private TextArea     descField, briefingField;
    private ComboBox<String> difficultyBox;
    private ComboBox<String> mapCombo;          // replaces mapNameField
    private Spinner<Integer> maxPointsSpinner;

    // ── Map canvas ─────────────────────────────────────────────────────────
    private Pane      mapPane;
    private ImageView mapImageView;   // background image (null = none)
    private Label     statusLabel;

    // ── Zone drawing (drag-to-create) ──────────────────────────────────────
    private double    dragStartX, dragStartY;
    private Rectangle previewRect;
    private boolean   drawingZone = false;

    // ── Zone selection & handles ───────────────────────────────────────────
    private MapZone selectedZone   = null;
    private StackPane selectedOverlay = null;
    private final Map<String, Rectangle> handleMap  = new LinkedHashMap<>();
    private final List<Node>             handleNodes = new ArrayList<>();

    // ── Right panel ────────────────────────────────────────────────────────
    private ScrollPane rightScrollPane;
    private Button[]   tabBtns;
    private String     activeTab = "zones";

    private VBox zonesListBox;
    private VBox troopsListBox;
    private VBox criteriaListBox;
    private VBox enemiesListBox;
    private VBox troopSection;
    private VBox criteriaSection;
    private VBox enemySection;

    private String selectedEnemyType = "INFANTRY";
    private double editorZoom = 1.0;

    // ──────────────────────────────────────────────────────────────────────

    public MissionEditorScreen(AppController controller, Mission mission) {
        this.controller   = controller;
        this.mission      = mission;
        this.zones        = new ArrayList<>(controller.getZonesForMission(mission.getId()));
        this.savedEnemies = new ArrayList<>(mission.getSavedEnemies());
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + UITheme.BG_DARK + ";");
        root.setTop(buildHeader());

        Node left   = buildFormPanel();
        Node center = buildMapSection();   // creates mapPane + wires mapCombo action
        Node right  = buildRightPanel();

        HBox main = new HBox(0, left, center, right);
        HBox.setHgrow(center, Priority.ALWAYS);
        root.setCenter(main);

        // Load map preview after mapPane exists
        String initMap = mission.getMapName();
        if (initMap != null && !initMap.isEmpty()) loadMapPreview(initMap);

        return new Scene(root, 1420, 900);
    }

    // ══════════════════════════════════════════════════════════════════════
    // HEADER
    // ══════════════════════════════════════════════════════════════════════

    private HBox buildHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 20, 12, 20));
        header.setStyle("-fx-background-color: " + UITheme.BG_PANEL +
                "; -fx-border-color: " + UITheme.GOLD + "; -fx-border-width: 0 0 2 0;");

        Label title = new Label("✏ ÉDITEUR DE MISSION");
        title.setStyle(UITheme.labelTitleStyle(18));

        Region sp1 = spacer();

        statusLabel = new Label("Zones : cliquer-glisser = créer • clic = sélectionner/éditer • clic droit = supprimer");
        statusLabel.setStyle("-fx-text-fill: " + UITheme.GOLD_LIGHT + "; -fx-font-size: 11px;");

        Region sp2 = spacer();

        Button saveBtn = new Button("💾 SAUVEGARDER");
        saveBtn.setStyle(UITheme.buttonGoldStyle());
        saveBtn.setOnAction(e -> saveMission());

        Button backBtn = new Button("← Retour");
        backBtn.setStyle(UITheme.buttonGreenStyle());
        backBtn.setOnAction(e -> controller.showEditorMenu());

        header.getChildren().addAll(title, sp1, statusLabel, sp2, saveBtn, backBtn);
        return header;
    }

    // ══════════════════════════════════════════════════════════════════════
    // LEFT FORM PANEL
    // ══════════════════════════════════════════════════════════════════════

    private VBox buildFormPanel() {
        VBox panel = new VBox(12);
        panel.setPrefWidth(255);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: " + UITheme.BG_PANEL +
                "; -fx-border-color: " + UITheme.BORDER_COLOR + "; -fx-border-width: 0 1 0 0;");

        Label formTitle = new Label("PARAMÈTRES DE LA MISSION");
        formTitle.setStyle(UITheme.labelTitleStyle(13));

        titleField    = field("Titre", mission.getTitle());
        descField     = textArea("Description", mission.getDescription());
        objectifField = field("Objectif", mission.getObjectif());
        terrainField  = field("Terrain (NORMANDIE…)", mission.getTerrain());

        difficultyBox = new ComboBox<>(FXCollections.observableArrayList("FACILE", "MOYEN", "DIFFICILE", "ÉLITE"));
        difficultyBox.setValue(mission.getDifficulty() != null ? mission.getDifficulty() : "MOYEN");
        difficultyBox.setMaxWidth(Double.MAX_VALUE);

        maxPointsSpinner = new Spinner<>(50, 1000, mission.getMaxPoints(), 25);
        maxPointsSpinner.setEditable(true);
        maxPointsSpinner.setMaxWidth(Double.MAX_VALUE);

        // ── Map ComboBox ────────────────────────────────────────────────
        List<String> mapNames = getAvailableMapNames();
        mapNames.add(0, "(aucune)");
        mapCombo = new ComboBox<>(FXCollections.observableArrayList(mapNames));
        String curMap = mission.getMapName();
        mapCombo.setValue((curMap != null && mapNames.contains(curMap)) ? curMap : "(aucune)");
        mapCombo.setMaxWidth(Double.MAX_VALUE);
        // Action wired later in buildMapSection() after mapPane exists

        briefingField = textArea("Briefing", mission.getBriefing());

        VBox fields = new VBox(5,
                lbl("Titre :"), titleField,
                lbl("Description :"), descField,
                lbl("Objectif :"), objectifField,
                lbl("Terrain :"), terrainField,
                lbl("Difficulté :"), difficultyBox,
                lbl("Points max :"), maxPointsSpinner,
                lbl("Map (image) :"), mapCombo,
                lbl("Briefing :"), briefingField
        );

        ScrollPane scroll = new ScrollPane(fields);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: " + UITheme.BG_PANEL + "; -fx-background-color: " + UITheme.BG_PANEL + ";");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        panel.getChildren().addAll(formTitle, scroll);
        return panel;
    }

    private List<String> getAvailableMapNames() {
        List<String> names = new ArrayList<>();
        // External maps directory
        File dir = new File(MongoDBService.getInstance().getMapsDirectory());
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles(f -> {
                String n = f.getName().toLowerCase();
                return n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg");
            });
            if (files != null) {
                Arrays.sort(files);
                for (File f : files) names.add(stripExt(f.getName()));
            }
        }
        // Known classpath maps
        for (String k : new String[]{"normandie", "ardennes", "stalingrad"}) {
            if (!names.contains(k) && classpathMapExists(k)) names.add(k);
        }
        return names;
    }

    private boolean classpathMapExists(String name) {
        for (String ext : new String[]{".png", ".jpg", ".jpeg", ".gif"}) {
            if (getClass().getResourceAsStream("/maps/" + name + ext) != null) return true;
        }
        return false;
    }

    // ══════════════════════════════════════════════════════════════════════
    // MAP SECTION
    // ══════════════════════════════════════════════════════════════════════

    private Node buildMapSection() {
        VBox section = new VBox(0);
        section.setStyle("-fx-background-color: " + UITheme.BG_DARK + ";");

        Label hint = new Label(
                "  [Zones] Glisser = créer • Clic = sélect./éditer • Clic droit = tourner/supprimer  " +
                "  [Zone sélect.] Glisser corps = déplacer • Glisser poignée = redimensionner  " +
                "  [Ennemis] Clic = placer • Clic token = supprimer  " +
                "  Ctrl+Molette = zoomer");
        hint.setStyle(UITheme.labelSecondaryStyle() +
                " -fx-padding: 4 10; -fx-background-color: " + UITheme.BG_PANEL + ";");
        hint.setWrapText(true);

        mapPane = new Pane();
        mapPane.setPrefSize(MAP_W, MAP_H);
        mapPane.setMinSize(MAP_W, MAP_H);
        mapPane.setMaxSize(MAP_W, MAP_H);
        mapPane.setStyle("-fx-background-color: #1a2010;");

        drawGrid();
        redrawZones();
        redrawEnemies();
        setupMapInteraction();

        // Wire map combo now that mapPane exists
        mapCombo.setOnAction(e -> {
            String sel = mapCombo.getValue();
            loadMapPreview("(aucune)".equals(sel) ? null : sel);
        });

        javafx.scene.Group mapGroup = new javafx.scene.Group(mapPane);
        ScrollPane mapScroll = new ScrollPane(mapGroup);
        mapScroll.setFitToWidth(false);
        mapScroll.setStyle("-fx-background: " + UITheme.BG_DARK + "; -fx-background-color: " + UITheme.BG_DARK + ";");
        VBox.setVgrow(mapScroll, Priority.ALWAYS);

        mapScroll.addEventFilter(javafx.scene.input.ScrollEvent.SCROLL, e -> {
            if (e.isControlDown()) {
                double factor = e.getDeltaY() > 0 ? 1.1 : 0.9;
                editorZoom = Math.max(0.3, Math.min(4.0, editorZoom * factor));
                mapPane.setScaleX(editorZoom);
                mapPane.setScaleY(editorZoom);
                e.consume();
            }
        });

        section.getChildren().addAll(hint, mapScroll);
        HBox.setHgrow(section, Priority.ALWAYS);
        return section;
    }

    private void loadMapPreview(String mapName) {
        if (mapName == null || mapName.isEmpty()) {
            if (mapImageView != null) mapImageView.setImage(null);
            return;
        }
        Image img = null;
        String mapsDir = MongoDBService.getInstance().getMapsDirectory();
        for (String ext : new String[]{".png", ".jpg", ".jpeg", ".gif"}) {
            File f = new File(mapsDir, mapName + ext);
            if (f.exists()) {
                try (FileInputStream fis = new FileInputStream(f)) { img = new Image(fis); break; }
                catch (Exception ignored) {}
            }
        }
        if (img == null) {
            for (String ext : new String[]{".png", ".jpg", ".jpeg", ".gif"}) {
                InputStream is = getClass().getResourceAsStream("/maps/" + mapName + ext);
                if (is != null) { img = new Image(is); break; }
            }
        }
        if (img == null) {
            statusLabel.setText("⚠ Image introuvable pour : " + mapName);
            return;
        }
        if (mapImageView == null) {
            mapImageView = new ImageView(img);
            mapImageView.setFitWidth(MAP_W);
            mapImageView.setFitHeight(MAP_H);
            mapImageView.setPreserveRatio(false);
            mapPane.getChildren().add(0, mapImageView); // behind grid
        } else {
            mapImageView.setImage(img);
        }
        statusLabel.setText("Map chargée : " + mapName);
    }

    private void drawGrid() {
        for (int x = 0; x < MAP_W; x += 40) {
            Rectangle l = new Rectangle(x, 0, 1, MAP_H);
            l.setFill(Color.web("#2a3020", 0.35));
            mapPane.getChildren().add(l);
        }
        for (int y = 0; y < MAP_H; y += 40) {
            Rectangle l = new Rectangle(0, y, MAP_W, 1);
            l.setFill(Color.web("#2a3020", 0.35));
            mapPane.getChildren().add(l);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // MAP INTERACTION  (draw, deselect on empty click)
    // ══════════════════════════════════════════════════════════════════════

    private void setupMapInteraction() {
        mapPane.setOnMousePressed(e -> {
            // Only fires if NOT consumed by a zone overlay or handle
            if ("zones".equals(activeTab)) {
                // Deselect current zone (click on empty space)
                if (selectedZone != null) {
                    selectedZone = null;
                    redrawZones();
                    refreshZonePanel();
                }
                // Start drawing new zone
                dragStartX = e.getX(); dragStartY = e.getY();
                drawingZone = true;
                previewRect = new Rectangle(dragStartX, dragStartY, 0, 0);
                previewRect.setFill(Color.web("#c9a84c", 0.18));
                previewRect.setStroke(Color.web("#c9a84c"));
                previewRect.setStrokeWidth(2);
                previewRect.getStrokeDashArray().addAll(6.0, 4.0);
                previewRect.setMouseTransparent(true);
                mapPane.getChildren().add(previewRect);
            } else if ("enemies".equals(activeTab)) {
                placeEnemyAt(e.getX(), e.getY());
            }
        });

        mapPane.setOnMouseDragged(e -> {
            if (!drawingZone || previewRect == null) return;
            double x = Math.min(e.getX(), dragStartX);
            double y = Math.min(e.getY(), dragStartY);
            double w = Math.abs(e.getX() - dragStartX);
            double h = Math.abs(e.getY() - dragStartY);
            previewRect.setX(x); previewRect.setY(y);
            previewRect.setWidth(w); previewRect.setHeight(h);
        });

        mapPane.setOnMouseReleased(e -> {
            if (!drawingZone || previewRect == null) return;
            drawingZone = false;
            double px = previewRect.getX(), py = previewRect.getY();
            double pw = previewRect.getWidth(), ph = previewRect.getHeight();
            mapPane.getChildren().remove(previewRect);
            previewRect = null;
            if (pw > MIN_PX && ph > MIN_PX) createZoneAt(px, py, pw, ph);
        });
    }

    private void createZoneAt(double px, double py, double pw, double ph) {
        String id   = "zone_" + UUID.randomUUID().toString().substring(0, 6);
        String name = "Zone " + (zones.size() + 1);
        MapZone z = new MapZone(id, name, "",
                px / MAP_W, py / MAP_H, pw / MAP_W, ph / MAP_H,
                "MEDIUM", "OFFENSIF");
        zones.add(z);
        selectedZone = z;
        redrawZones();
        refreshZonePanel();
        statusLabel.setText("Zone « " + name + " » créée — éditez dans le panneau droit, glissez pour déplacer, poignées pour redimensionner");
    }

    // ══════════════════════════════════════════════════════════════════════
    // ZONE OVERLAYS  (normal + selected-movable)
    // ══════════════════════════════════════════════════════════════════════

    private void redrawZones() {
        mapPane.getChildren().removeIf(n -> n instanceof StackPane || handleNodes.contains(n));
        handleNodes.clear();
        handleMap.clear();
        selectedOverlay = null;

        for (MapZone z : zones) {
            if (z == selectedZone) {
                selectedOverlay = buildSelectedOverlay(z);
                mapPane.getChildren().add(selectedOverlay);
                buildAllHandles(z);
            } else {
                mapPane.getChildren().add(buildStaticOverlay(z));
            }
        }
    }

    /** Static overlay for non-selected zones */
    private StackPane buildStaticOverlay(MapZone z) {
        StackPane sp = makeOverlayPane(z, false);
        // Consume press so mapPane doesn't start a draw
        sp.setOnMousePressed(e -> e.consume());
        sp.setOnMouseClicked(e -> {
            if (!"zones".equals(activeTab)) return;
            if (e.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                selectedZone = z;
                redrawZones();
                refreshZonePanel();
                statusLabel.setText("Zone « " + z.getName() + " » sélectionnée");
            }
            e.consume();
        });
        sp.setOnContextMenuRequested(e -> {
            if (!"zones".equals(activeTab)) { e.consume(); return; }
            showZoneContextMenu(z, sp, e.getScreenX(), e.getScreenY());
            e.consume();
        });
        return sp;
    }

    /** Movable overlay for the selected zone */
    private StackPane buildSelectedOverlay(MapZone z) {
        StackPane sp = makeOverlayPane(z, true);
        sp.setCursor(javafx.scene.Cursor.MOVE);

        double[] startScene = {0, 0};
        double[] startZone  = {0, 0}; // relative x, y at drag start
        boolean[] moved     = {false};

        sp.setOnMousePressed(e -> {
            moved[0]      = false;
            startScene[0] = e.getSceneX();
            startScene[1] = e.getSceneY();
            startZone[0]  = z.getX();
            startZone[1]  = z.getY();
            e.consume();
        });
        sp.setOnMouseDragged(e -> {
            moved[0] = true;
            double rdx = (e.getSceneX() - startScene[0]) / MAP_W;
            double rdy = (e.getSceneY() - startScene[1]) / MAP_H;
            double nx = clamp(startZone[0] + rdx, 0, 1 - z.getWidth());
            double ny = clamp(startZone[1] + rdy, 0, 1 - z.getHeight());
            z.setX(nx); z.setY(ny);
            sp.setLayoutX(nx * MAP_W);
            sp.setLayoutY(ny * MAP_H);
            repositionHandles(z);
            e.consume();
        });
        sp.setOnMouseReleased(e -> {
            if (!moved[0]) {
                // Simple click on selected zone = deselect
                selectedZone = null;
                redrawZones();
                refreshZonePanel();
            } else {
                refreshZonePanel(); // update form values
            }
            e.consume();
        });
        sp.setOnContextMenuRequested(e -> {
            showZoneContextMenu(z, sp, e.getScreenX(), e.getScreenY());
            e.consume();
        });
        return sp;
    }

    private StackPane makeOverlayPane(MapZone z, boolean selected) {
        double px = z.getX() * MAP_W, py = z.getY() * MAP_H;
        double pw = z.getWidth() * MAP_W, ph = z.getHeight() * MAP_H;

        StackPane sp = new StackPane();
        sp.setLayoutX(px); sp.setLayoutY(py);
        sp.setPrefSize(pw, ph);

        String color = UITheme.getZoneColor(z.getType());
        String borderColor = selected ? UITheme.GOLD_LIGHT
                : "HIGH".equals(z.getStrategicValue()) ? UITheme.GOLD : UITheme.BORDER_COLOR;
        sp.setStyle("-fx-background-color: " + color + (selected ? "55" : "30") +
                "; -fx-border-color: " + borderColor +
                "; -fx-border-width: " + (selected ? "2" : "1") + ";");

        Label nameLbl = new Label(z.getName());
        nameLbl.setStyle("-fx-text-fill: " + UITheme.TEXT_PRIMARY +
                "; -fx-font-size: 9px; -fx-font-weight: bold;" +
                " -fx-background-color: #00000077; -fx-padding: 2 4;");
        nameLbl.setWrapText(true);
        nameLbl.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        StackPane.setAlignment(nameLbl, Pos.TOP_CENTER);

        Label typeLbl = new Label(z.getType() + " • " + z.getStrategicValue());
        typeLbl.setStyle("-fx-text-fill: " + UITheme.TEXT_SECONDARY +
                "; -fx-font-size: 8px; -fx-background-color: #00000077; -fx-padding: 1 3;");
        StackPane.setAlignment(typeLbl, Pos.BOTTOM_CENTER);

        sp.getChildren().addAll(nameLbl, typeLbl);
        sp.setRotate(z.getRotation());
        return sp;
    }

    // ══════════════════════════════════════════════════════════════════════
    // ZONE CONTEXT MENU  (right-click: rotate / delete)
    // ══════════════════════════════════════════════════════════════════════

    private void showZoneContextMenu(MapZone z, javafx.scene.Node anchor,
                                     double screenX, double screenY) {
        javafx.scene.control.ContextMenu menu = new javafx.scene.control.ContextMenu();

        javafx.scene.control.MenuItem rotPlus = new javafx.scene.control.MenuItem("↻ Tourner +15°");
        rotPlus.setOnAction(ev -> {
            z.setRotation((z.getRotation() + 15) % 360);
            redrawZones();
            statusLabel.setText("Zone « " + z.getName() + " » tournée à " + (int) z.getRotation() + "°");
        });

        javafx.scene.control.MenuItem rotMinus = new javafx.scene.control.MenuItem("↺ Tourner -15°");
        rotMinus.setOnAction(ev -> {
            z.setRotation(((z.getRotation() - 15) % 360 + 360) % 360);
            redrawZones();
            statusLabel.setText("Zone « " + z.getName() + " » tournée à " + (int) z.getRotation() + "°");
        });

        javafx.scene.control.MenuItem rotReset = new javafx.scene.control.MenuItem("⊙ Remettre à 0°");
        rotReset.setOnAction(ev -> {
            z.setRotation(0);
            redrawZones();
            statusLabel.setText("Zone « " + z.getName() + " » rotation réinitialisée");
        });

        javafx.scene.control.MenuItem del = new javafx.scene.control.MenuItem("✖ Supprimer");
        del.setOnAction(ev -> {
            zones.remove(z);
            if (selectedZone == z) selectedZone = null;
            redrawZones();
            refreshZonePanel();
            statusLabel.setText("Zone « " + z.getName() + " » supprimée");
        });

        menu.getItems().addAll(rotPlus, rotMinus, rotReset,
                new javafx.scene.control.SeparatorMenuItem(), del);
        menu.show(anchor, screenX, screenY);
    }

    // ══════════════════════════════════════════════════════════════════════
    // RESIZE HANDLES  (8 directional handles on selected zone)
    // ══════════════════════════════════════════════════════════════════════

    private static final String[] DIRS = {"NW","N","NE","E","SE","S","SW","W"};

    private void buildAllHandles(MapZone z) {
        for (String dir : DIRS) {
            Rectangle h = buildHandle(z, dir);
            handleMap.put(dir, h);
            handleNodes.add(h);
            mapPane.getChildren().add(h);
        }
    }

    private Rectangle buildHandle(MapZone z, String dir) {
        Rectangle h = new Rectangle(HANDLE, HANDLE);
        h.setFill(Color.web(UITheme.GOLD));
        h.setStroke(Color.web(UITheme.BG_DARK));
        h.setStrokeWidth(1.5);
        h.setCursor(cursorForDir(dir));
        positionHandle(h, z, dir);

        double[] startScene = {0, 0};
        double[] startState = {0, 0, 0, 0}; // x, y, w, h (relative)

        h.setOnMousePressed(e -> {
            startScene[0] = e.getSceneX(); startScene[1] = e.getSceneY();
            startState[0] = z.getX(); startState[1] = z.getY();
            startState[2] = z.getWidth(); startState[3] = z.getHeight();
            e.consume();
        });
        h.setOnMouseDragged(e -> {
            double rdx = (e.getSceneX() - startScene[0]) / MAP_W;
            double rdy = (e.getSceneY() - startScene[1]) / MAP_H;
            applyResize(z, dir, rdx, rdy, startState);
            // Update overlay bounds live
            if (selectedOverlay != null) {
                selectedOverlay.setLayoutX(z.getX() * MAP_W);
                selectedOverlay.setLayoutY(z.getY() * MAP_H);
                selectedOverlay.setPrefWidth(z.getWidth() * MAP_W);
                selectedOverlay.setPrefHeight(z.getHeight() * MAP_H);
            }
            repositionHandles(z);
            e.consume();
        });
        h.setOnMouseReleased(e -> {
            refreshZonePanel();
            e.consume();
        });
        return h;
    }

    private void positionHandle(Rectangle h, MapZone z, String dir) {
        double px = z.getX() * MAP_W, py = z.getY() * MAP_H;
        double pw = z.getWidth() * MAP_W, ph = z.getHeight() * MAP_H;
        double mx = px + pw / 2, my = py + ph / 2;
        double hs = HANDLE / 2;
        switch (dir) {
            case "NW": h.setLayoutX(px-hs);    h.setLayoutY(py-hs);    break;
            case "N":  h.setLayoutX(mx-hs);    h.setLayoutY(py-hs);    break;
            case "NE": h.setLayoutX(px+pw-hs); h.setLayoutY(py-hs);    break;
            case "E":  h.setLayoutX(px+pw-hs); h.setLayoutY(my-hs);    break;
            case "SE": h.setLayoutX(px+pw-hs); h.setLayoutY(py+ph-hs); break;
            case "S":  h.setLayoutX(mx-hs);    h.setLayoutY(py+ph-hs); break;
            case "SW": h.setLayoutX(px-hs);    h.setLayoutY(py+ph-hs); break;
            case "W":  h.setLayoutX(px-hs);    h.setLayoutY(my-hs);    break;
        }
    }

    private void repositionHandles(MapZone z) {
        handleMap.forEach((dir, h) -> positionHandle(h, z, dir));
    }

    private void applyResize(MapZone z, String dir, double rdx, double rdy, double[] s) {
        double minRel = MIN_PX / MAP_W;
        double x = s[0], y = s[1], w = s[2], h = s[3];

        switch (dir) {
            case "NW":
                x = clamp(s[0]+rdx, 0, s[0]+s[2]-minRel);
                y = clamp(s[1]+rdy, 0, s[1]+s[3]-minRel);
                w = s[0]+s[2]-x; h = s[1]+s[3]-y;
                break;
            case "N":
                y = clamp(s[1]+rdy, 0, s[1]+s[3]-minRel);
                h = s[1]+s[3]-y;
                break;
            case "NE":
                y = clamp(s[1]+rdy, 0, s[1]+s[3]-minRel);
                w = Math.max(minRel, s[2]+rdx); h = s[1]+s[3]-y;
                break;
            case "E":
                w = Math.max(minRel, s[2]+rdx);
                break;
            case "SE":
                w = Math.max(minRel, s[2]+rdx);
                h = Math.max(minRel, s[3]+rdy);
                break;
            case "S":
                h = Math.max(minRel, s[3]+rdy);
                break;
            case "SW":
                x = clamp(s[0]+rdx, 0, s[0]+s[2]-minRel);
                w = s[0]+s[2]-x; h = Math.max(minRel, s[3]+rdy);
                break;
            case "W":
                x = clamp(s[0]+rdx, 0, s[0]+s[2]-minRel);
                w = s[0]+s[2]-x;
                break;
        }
        // Clamp final bounds to [0..1]
        if (x < 0) { w += x; x = 0; }
        if (y < 0) { h += y; y = 0; }
        if (x+w > 1) w = 1-x;
        if (y+h > 1) h = 1-y;
        w = Math.max(minRel, w);
        h = Math.max(minRel, h);

        z.setX(x); z.setY(y); z.setWidth(w); z.setHeight(h);
    }

    private javafx.scene.Cursor cursorForDir(String dir) {
        switch (dir) {
            case "NW": return javafx.scene.Cursor.NW_RESIZE;
            case "NE": return javafx.scene.Cursor.NE_RESIZE;
            case "SW": return javafx.scene.Cursor.SW_RESIZE;
            case "SE": return javafx.scene.Cursor.SE_RESIZE;
            case "N":  return javafx.scene.Cursor.N_RESIZE;
            case "S":  return javafx.scene.Cursor.S_RESIZE;
            case "E":  return javafx.scene.Cursor.E_RESIZE;
            case "W":  return javafx.scene.Cursor.W_RESIZE;
            default:   return javafx.scene.Cursor.DEFAULT;
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // ENEMY TOKENS
    // ══════════════════════════════════════════════════════════════════════

    private void redrawEnemies() {
        mapPane.getChildren().removeIf(n -> n instanceof Circle);
        for (EnemyUnit e : savedEnemies) mapPane.getChildren().add(buildEnemyToken(e));
    }

    private Circle buildEnemyToken(EnemyUnit enemy) {
        Circle c = new Circle(9);
        c.setCenterX(enemy.getRelX() * MAP_W);
        c.setCenterY(enemy.getRelY() * MAP_H);
        c.setFill(Color.web("#cc2222", 0.85));
        c.setStroke(Color.web("#ff6666")); c.setStrokeWidth(2);
        Tooltip.install(c, new Tooltip("ENNEMI: " + enemy.getName() + " [" + enemy.getType() + "]\nClic = supprimer"));
        c.setOnMouseClicked(e -> {
            savedEnemies.remove(enemy); redrawEnemies(); refreshEnemiesList();
            statusLabel.setText("Ennemi supprimé : " + enemy.getName());
            e.consume();
        });
        return c;
    }

    private void placeEnemyAt(double px, double py) {
        String name = enemyTypeName(selectedEnemyType);
        savedEnemies.add(new EnemyUnit("enemy_" + UUID.randomUUID().toString().substring(0,6),
                name, selectedEnemyType, "💀", px / MAP_W, py / MAP_H));
        redrawEnemies(); refreshEnemiesList();
        statusLabel.setText("Ennemi placé : " + name);
    }

    private String enemyTypeName(String t) {
        switch (t) {
            case "SNIPER":    return "Sniper Ennemi";
            case "TANK":      return "Panzer";
            case "ARTILLERY": return "Canon Ennemi";
            default:          return "Infanterie Ennemie";
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // RIGHT PANEL (tabs)
    // ══════════════════════════════════════════════════════════════════════

    private VBox buildRightPanel() {
        VBox panel = new VBox(0);
        panel.setPrefWidth(285);
        panel.setStyle("-fx-background-color: " + UITheme.BG_PANEL +
                "; -fx-border-color: " + UITheme.BORDER_COLOR + "; -fx-border-width: 0 0 0 1;");

        tabBtns = new Button[]{
            tabBtn("Zones",    "zones"),
            tabBtn("Troupes",  "troops"),
            tabBtn("Critères", "criteria"),
            tabBtn("Ennemis",  "enemies")
        };
        HBox tabs = new HBox(0);
        for (Button b : tabBtns) { tabs.getChildren().add(b); HBox.setHgrow(b, Priority.ALWAYS); }

        zonesListBox    = new VBox(5);
        troopsListBox   = new VBox(5);
        criteriaListBox = new VBox(5);
        enemiesListBox  = new VBox(5);

        troopSection    = buildTroopSection();
        criteriaSection = buildCriteriaSection();
        enemySection    = buildEnemySection();

        rightScrollPane = new ScrollPane();
        rightScrollPane.setFitToWidth(true);
        rightScrollPane.setStyle("-fx-background: " + UITheme.BG_PANEL + "; -fx-background-color: " + UITheme.BG_PANEL + ";");
        VBox.setVgrow(rightScrollPane, Priority.ALWAYS);

        for (Button b : tabBtns) {
            b.setOnAction(e -> {
                activeTab = (String) b.getUserData();
                updateTabStyles();
                showTabContent();
            });
        }
        updateTabStyles();
        showTabContent();

        panel.getChildren().addAll(tabs, rightScrollPane);
        return panel;
    }

    private void showTabContent() {
        switch (activeTab) {
            case "zones":
                refreshZonePanel();
                break;
            case "troops":
                refreshTroopsList();
                rightScrollPane.setContent(padded(new VBox(8, troopSection, new Separator(), troopsListBox)));
                break;
            case "criteria":
                refreshCriteriaList();
                rightScrollPane.setContent(padded(new VBox(8, criteriaSection, new Separator(), criteriaListBox)));
                break;
            case "enemies":
                refreshEnemiesList();
                rightScrollPane.setContent(padded(new VBox(8, enemySection, new Separator(), enemiesListBox)));
                break;
        }
    }

    // ── Zone panel ────────────────────────────────────────────────────────

    private void refreshZonePanel() {
        if (!"zones".equals(activeTab)) return;
        refreshZonesList();
        VBox top;
        if (selectedZone != null) {
            top = buildZoneEditForm(selectedZone);
        } else {
            Label hint = new Label("Glisser sur la carte pour créer une zone.\nClic sur zone = sélectionner.\nClic droit = supprimer.");
            hint.setStyle(UITheme.labelSecondaryStyle()); hint.setWrapText(true);
            top = new VBox(hint);
        }
        rightScrollPane.setContent(padded(new VBox(8, top, new Separator(), zonesListBox)));
    }

    private VBox buildZoneEditForm(MapZone z) {
        VBox form = new VBox(5);
        form.setStyle("-fx-background-color: " + UITheme.BG_CARD +
                "; -fx-border-color: " + UITheme.GOLD + "; -fx-border-width: 1; -fx-padding: 10;");

        Label title = new Label("✏ " + z.getName());
        title.setStyle(UITheme.labelTitleStyle(12));

        Label idLbl = new Label("ID : " + z.getId());
        idLbl.setStyle("-fx-text-fill: " + UITheme.TEXT_SECONDARY + "; -fx-font-size: 10px;");

        TextField nameF = sField(z.getName());
        TextField descF = sField(z.getDescription() != null ? z.getDescription() : "");
        descF.setPromptText("Description");

        ComboBox<String> typeBox = new ComboBox<>(FXCollections.observableArrayList(
                "OFFENSIF","DEFENSIF","COUVERTURE","COLLINE","VILLE","PONT"));
        typeBox.setValue(z.getType()); typeBox.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> valBox = new ComboBox<>(FXCollections.observableArrayList("HIGH","MEDIUM","LOW"));
        valBox.setValue(z.getStrategicValue()); valBox.setMaxWidth(Double.MAX_VALUE);

        // Live visual update on combo changes
        typeBox.setOnAction(e -> { z.setType(typeBox.getValue()); redrawZones(); });
        valBox.setOnAction(e  -> { z.setStrategicValue(valBox.getValue()); redrawZones(); });

        // Position/size display (read + editable via text)
        Label posLbl = new Label(String.format("Position : %.1f%% / %.1f%%   Taille : %.1f%% × %.1f%%",
                z.getX()*100, z.getY()*100, z.getWidth()*100, z.getHeight()*100));
        posLbl.setStyle("-fx-text-fill: " + UITheme.TEXT_SECONDARY + "; -fx-font-size: 10px;");

        Button applyBtn = new Button("✅ Appliquer");
        applyBtn.setStyle(UITheme.buttonGoldStyle() + " -fx-font-size: 11px; -fx-padding: 5 10;");
        applyBtn.setMaxWidth(Double.MAX_VALUE);
        applyBtn.setOnAction(e -> {
            z.setName(nameF.getText().isEmpty() ? "Zone" : nameF.getText());
            z.setDescription(descF.getText());
            z.setType(typeBox.getValue());
            z.setStrategicValue(valBox.getValue());
            redrawZones();
            refreshZonePanel();
            statusLabel.setText("Zone « " + z.getName() + " » mise à jour");
        });

        Button delBtn = new Button("✖ Supprimer");
        delBtn.setStyle(UITheme.buttonRedStyle() + " -fx-font-size: 11px; -fx-padding: 5 10;");
        delBtn.setMaxWidth(Double.MAX_VALUE);
        delBtn.setOnAction(e -> {
            zones.remove(z); selectedZone = null; redrawZones(); refreshZonePanel();
        });

        Label deselLbl = new Label("← Désélectionner");
        deselLbl.setStyle("-fx-text-fill: " + UITheme.TEXT_SECONDARY + "; -fx-font-size: 10px; -fx-cursor: hand;");
        deselLbl.setOnMouseClicked(e -> { selectedZone = null; redrawZones(); refreshZonePanel(); });

        form.getChildren().addAll(title, idLbl, lbl("Nom :"), nameF, lbl("Description :"), descF,
                lbl("Type :"), typeBox, lbl("Valeur stratégique :"), valBox,
                posLbl, applyBtn, delBtn, deselLbl);
        return form;
    }

    private void refreshZonesList() {
        zonesListBox.getChildren().clear();
        Label hdr = new Label("Zones (" + zones.size() + ") :");
        hdr.setStyle(UITheme.labelTitleStyle(11));
        zonesListBox.getChildren().add(hdr);

        for (MapZone z : new ArrayList<>(zones)) {
            HBox row = new HBox(5); row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(3, 6, 3, 6));
            boolean sel = z == selectedZone;
            row.setStyle("-fx-background-color: " + (sel ? UITheme.BG_PANEL : UITheme.BG_CARD) +
                    "; -fx-border-color: " + (sel ? UITheme.GOLD : UITheme.BORDER_COLOR) + "; -fx-border-width: 1;");

            String dot = "HIGH".equals(z.getStrategicValue()) ? "🟡" : "LOW".equals(z.getStrategicValue()) ? "🔴" : "🟢";
            Label name = new Label(dot + " " + z.getName());
            name.setStyle("-fx-text-fill: " + UITheme.TEXT_PRIMARY + "; -fx-font-size: 11px; -fx-cursor: hand;");
            name.setOnMouseClicked(e -> { selectedZone = z; redrawZones(); refreshZonePanel(); });

            Region sp = spacer();
            Button del = new Button("✖");
            del.setStyle("-fx-background-color: " + UITheme.RED_DANGER +
                    "; -fx-text-fill: white; -fx-font-size: 9px; -fx-padding: 1 4;");
            del.setOnAction(e -> {
                zones.remove(z);
                if (selectedZone == z) selectedZone = null;
                redrawZones(); refreshZonePanel();
            });
            row.getChildren().addAll(name, sp, del);
            zonesListBox.getChildren().add(row);
        }
    }

    // ── Troop section ────────────────────────────────────────────────────

    private VBox buildTroopSection() {
        VBox s = new VBox(5);
        TextField nameF = sField(null); nameF.setPromptText("Nom");
        ComboBox<String> typeBox = new ComboBox<>(FXCollections.observableArrayList(
                "1st","2ND ARMORED","101st","13e","ARTILLERY","CANNONIER","SNIPER","GRENADIER","MEDIC"));
        typeBox.setValue("1st"); typeBox.setMaxWidth(Double.MAX_VALUE);
        TextField iconF = sField(null); iconF.setPromptText("Icône (⚔ 🛡 💥 🎯)");
        Spinner<Integer> strSp = new Spinner<>(1,200,70); strSp.setMaxWidth(Double.MAX_VALUE);
        Spinner<Integer> rngSp = new Spinner<>(1,10,2);   rngSp.setMaxWidth(Double.MAX_VALUE);
        TextField descF = sField(null); descF.setPromptText("Description");

        Button add = new Button("+ Ajouter troupe");
        add.setStyle(UITheme.buttonGoldStyle() + " -fx-font-size: 11px; -fx-padding: 5 10;");
        add.setMaxWidth(Double.MAX_VALUE);
        add.setOnAction(e -> {
            if (nameF.getText().trim().isEmpty()) return;
            String id   = "troop_" + UUID.randomUUID().toString().substring(0,6);
            String icon = iconF.getText().isEmpty() ? "⚔" : iconF.getText();
            mission.addTroop(new TroopUnit(id, nameF.getText().trim(), typeBox.getValue(),
                    icon, strSp.getValue(), rngSp.getValue(), descF.getText()));
            nameF.clear(); iconF.clear(); descF.clear();
            refreshTroopsList();
        });
        s.getChildren().addAll(lbl("Nom :"),nameF,lbl("Type :"),typeBox,lbl("Icône :"),iconF,
                lbl("Force :"),strSp,lbl("Portée :"),rngSp,lbl("Desc :"),descF,add);
        return s;
    }

    private void refreshTroopsList() {
        troopsListBox.getChildren().clear();
        troopsListBox.getChildren().add(hdrLbl("Troupes (" + mission.getAvailableTroops().size() + ") :"));
        for (TroopUnit t : new ArrayList<>(mission.getAvailableTroops())) {
            HBox row = simpleRow(t.getIcon() + " " + t.getName(), UITheme.getTroopColor(t.getType()),
                    () -> { mission.getAvailableTroops().remove(t); refreshTroopsList(); });
            troopsListBox.getChildren().add(row);
        }
    }

    // ── Criteria section ─────────────────────────────────────────────────

    private VBox buildCriteriaSection() {
        VBox s = new VBox(5);
        TextField nameF = sField(null); nameF.setPromptText("Nom objectif");
        TextField descF = sField(null); descF.setPromptText("Description");
        TextField zoneF = sField(null); zoneF.setPromptText("ID zone cible");
        ComboBox<String> ttBox = new ComboBox<>(FXCollections.observableArrayList(
                "(aucun)","1st","2ND ARMORED","101st","13e","ARTILLERY","CANNONIER","SNIPER","GRENADIER","MEDIC"));
        ttBox.setValue("(aucun)"); ttBox.setMaxWidth(Double.MAX_VALUE);
        Spinner<Integer> ptsSp = new Spinner<>(10,500,50,10); ptsSp.setMaxWidth(Double.MAX_VALUE);

        // Quick zone-ID picker
        VBox zoneLinks = new VBox(2);
        for (MapZone z : zones) {
            Label chip = new Label("  ↳ " + z.getId() + " (" + z.getName() + ")");
            chip.setStyle("-fx-text-fill: " + UITheme.GOLD + "; -fx-font-size: 10px; -fx-cursor: hand;");
            chip.setOnMouseClicked(e -> zoneF.setText(z.getId()));
            zoneLinks.getChildren().add(chip);
        }

        Button add = new Button("+ Ajouter critère");
        add.setStyle(UITheme.buttonGoldStyle() + " -fx-font-size: 11px; -fx-padding: 5 10;");
        add.setMaxWidth(Double.MAX_VALUE);
        add.setOnAction(e -> {
            if (nameF.getText().trim().isEmpty() || zoneF.getText().trim().isEmpty()) return;
            EvaluationCriteria ec = new EvaluationCriteria(nameF.getText().trim(),
                    descF.getText(), ptsSp.getValue(), zoneF.getText().trim());
            if (!"(aucun)".equals(ttBox.getValue())) ec.setRequiredTroopType(ttBox.getValue());
            mission.addCriteria(ec);
            nameF.clear(); descF.clear(); zoneF.clear();
            refreshCriteriaList();
        });
        s.getChildren().addAll(lbl("Nom :"),nameF,lbl("Description :"),descF,
                lbl("Zone ID :"),zoneF,zoneLinks,lbl("Type troupe requis :"),ttBox,
                lbl("Points max :"),ptsSp,add);
        return s;
    }

    private void refreshCriteriaList() {
        criteriaListBox.getChildren().clear();
        criteriaListBox.getChildren().add(hdrLbl("Critères (" + mission.getCriteria().size() + ") :"));
        for (EvaluationCriteria c : new ArrayList<>(mission.getCriteria())) {
            VBox card = new VBox(2);
            card.setPadding(new Insets(3,6,3,6));
            card.setStyle("-fx-background-color: " + UITheme.BG_CARD +
                    "; -fx-border-color: " + UITheme.BORDER_COLOR + "; -fx-border-width: 1;");
            HBox row = new HBox(5); row.setAlignment(Pos.CENTER_LEFT);
            Label nm = new Label(c.getName() + " +" + c.getMaxPoints() + "pts");
            nm.setStyle("-fx-text-fill: " + UITheme.TEXT_PRIMARY + "; -fx-font-size: 11px;");
            nm.setWrapText(true);
            Region sp = spacer();
            Button del = delBtn(() -> { mission.getCriteria().remove(c); refreshCriteriaList(); });
            row.getChildren().addAll(nm, sp, del);
            Label info = new Label("Zone: " + c.getZoneId() +
                    (c.getRequiredTroopType() != null ? " • " + c.getRequiredTroopType() : ""));
            info.setStyle(UITheme.labelSecondaryStyle());
            card.getChildren().addAll(row, info);
            criteriaListBox.getChildren().add(card);
        }
    }

    // ── Enemy section ────────────────────────────────────────────────────

    private VBox buildEnemySection() {
        VBox s = new VBox(8);
        Label hint = new Label("Sélectionnez le type puis cliquez sur la carte.\nClic sur un token = le supprimer.");
        hint.setStyle(UITheme.labelSecondaryStyle()); hint.setWrapText(true);
        ComboBox<String> typeBox = new ComboBox<>(FXCollections.observableArrayList(
                "916gr","SNIPER","TANK","ARTILLERY","FSM","MARINE"));
        typeBox.setValue(selectedEnemyType); typeBox.setMaxWidth(Double.MAX_VALUE);
        typeBox.setOnAction(e -> selectedEnemyType = typeBox.getValue());
        Button clearBtn = new Button("🗑 Tout effacer");
        clearBtn.setStyle(UITheme.buttonRedStyle() + " -fx-font-size: 11px; -fx-padding: 5 10;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> { savedEnemies.clear(); redrawEnemies(); refreshEnemiesList(); });
        s.getChildren().addAll(hint, lbl("Type :"), typeBox, clearBtn);
        return s;
    }

    private void refreshEnemiesList() {
        enemiesListBox.getChildren().clear();
        enemiesListBox.getChildren().add(hdrLbl("Ennemis placés (" + savedEnemies.size() + ") :"));
        for (EnemyUnit en : savedEnemies) {
            Label row = new Label("💀 " + en.getName() + " [" + en.getType() + "]  "
                    + String.format("%.0f%%", en.getRelX()*100) + " / "
                    + String.format("%.0f%%", en.getRelY()*100));
            row.setStyle("-fx-text-fill: #cc4444; -fx-font-size: 10px;");
            enemiesListBox.getChildren().add(row);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // SAVE
    // ══════════════════════════════════════════════════════════════════════

    private void saveMission() {
        mission.setTitle(titleField.getText().trim());
        mission.setDescription(descField.getText().trim());
        mission.setObjectif(objectifField.getText().trim());
        String t = terrainField.getText().trim().toUpperCase();
        mission.setTerrain(t.isEmpty() ? "INCONNU" : t);
        mission.setDifficulty(difficultyBox.getValue());
        mission.setMaxPoints(maxPointsSpinner.getValue());
        mission.setBriefing(briefingField.getText().trim());
        String sel = mapCombo.getValue();
        mission.setMapName("(aucune)".equals(sel) ? "" : sel);

        mission.getSavedEnemies().clear();
        mission.getSavedEnemies().addAll(savedEnemies);

        controller.saveMission(mission, zones);
        statusLabel.setText("✅ Mission « " + mission.getTitle() + " » sauvegardée !");
    }

    // ══════════════════════════════════════════════════════════════════════
    // UTILITIES
    // ══════════════════════════════════════════════════════════════════════

    private void updateTabStyles() {
        for (Button b : tabBtns) {
            boolean on = activeTab.equals(b.getUserData());
            b.setStyle((on
                    ? "-fx-background-color: " + UITheme.GOLD + "; -fx-text-fill: " + UITheme.BG_DARK + ";"
                    : "-fx-background-color: " + UITheme.BG_PANEL + "; -fx-text-fill: " + UITheme.TEXT_PRIMARY + ";")
                    + " -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 4;");
        }
    }

    private Button tabBtn(String text, String id) {
        Button b = new Button(text); b.setUserData(id);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setStyle("-fx-background-color: " + UITheme.BG_PANEL + "; -fx-text-fill: " + UITheme.TEXT_PRIMARY +
                "; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 4;");
        return b;
    }

    private HBox simpleRow(String text, String color, Runnable onDelete) {
        HBox row = new HBox(5); row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(3,6,3,6));
        row.setStyle("-fx-background-color: " + UITheme.BG_CARD +
                "; -fx-border-color: " + UITheme.BORDER_COLOR + "; -fx-border-width: 1;");
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 11px;");
        row.getChildren().addAll(lbl, spacer(), delBtn(onDelete));
        return row;
    }

    private Button delBtn(Runnable action) {
        Button b = new Button("✖");
        b.setStyle("-fx-background-color: " + UITheme.RED_DANGER +
                "; -fx-text-fill: white; -fx-font-size: 9px; -fx-padding: 1 4;");
        b.setOnAction(e -> action.run());
        return b;
    }

    private VBox padded(VBox v) { v.setPadding(new Insets(10)); return v; }
    private Region spacer() { Region r = new Region(); HBox.setHgrow(r, Priority.ALWAYS); return r; }
    private Label  lbl(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-text-fill: " + UITheme.TEXT_SECONDARY + "; -fx-font-size: 11px;");
        return l;
    }
    private Label hdrLbl(String t) {
        Label l = new Label(t); l.setStyle(UITheme.labelTitleStyle(11)); return l;
    }

    private TextField field(String prompt, String val) {
        TextField f = new TextField(val != null ? val : "");
        f.setPromptText(prompt); styleField(f); return f;
    }
    private TextField sField(String val) {
        TextField f = new TextField(val != null ? val : "");
        styleField(f); f.setMaxWidth(Double.MAX_VALUE); return f;
    }
    private void styleField(TextField f) {
        f.setStyle("-fx-background-color: " + UITheme.BG_PANEL + "; -fx-text-fill: " + UITheme.TEXT_PRIMARY +
                "; -fx-border-color: " + UITheme.BORDER_COLOR + "; -fx-font-size: 12px; -fx-padding: 5;");
        f.setMaxWidth(Double.MAX_VALUE);
    }
    private TextArea textArea(String prompt, String val) {
        TextArea f = new TextArea(val != null ? val : "");
        f.setPromptText(prompt); f.setPrefRowCount(2); f.setWrapText(true);
        f.setStyle("-fx-control-inner-background: " + UITheme.BG_PANEL +
                "; -fx-text-fill: " + UITheme.TEXT_PRIMARY + "; -fx-font-size: 12px;");
        return f;
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
    private static String stripExt(String name) {
        int dot = name.lastIndexOf('.'); return dot > 0 ? name.substring(0, dot) : name;
    }
}
