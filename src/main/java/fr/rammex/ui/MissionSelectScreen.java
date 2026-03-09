package fr.rammex.ui;

import fr.rammex.AppController;
import fr.rammex.model.Mission;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class MissionSelectScreen {

    private AppController controller;

    public MissionSelectScreen(AppController controller) {
        this.controller = controller;
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + UITheme.BG_DARK + ";");

        // Header
        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(30, 20, 20, 20));
        header.setStyle("-fx-background-color: " + UITheme.BG_PANEL + "; -fx-border-color: " + UITheme.GOLD + "; -fx-border-width: 0 0 2 0;");

        Label title = new Label("⚔ SÉLECTION DE MISSION");
        title.setStyle(UITheme.labelTitleStyle(28));
        Label sub = new Label("Choisissez votre théâtre d'opérations — Seconde Guerre Mondiale");
        sub.setStyle(UITheme.labelSecondaryStyle());

        header.getChildren().addAll(title, sub);

        // Mission cards (from MongoDB + hardcoded)
        List<Mission> missions = controller.getMissions();
        HBox cardsBox = new HBox(25);
        cardsBox.setAlignment(Pos.CENTER);
        cardsBox.setPadding(new Insets(40));

        for (Mission m : missions) {
            VBox card = createMissionCard(m);
            cardsBox.getChildren().add(card);
        }

        ScrollPane cardsScroll = new ScrollPane(cardsBox);
        cardsScroll.setFitToHeight(true);
        cardsScroll.setStyle("-fx-background: " + UITheme.BG_DARK + "; -fx-background-color: " + UITheme.BG_DARK + ";");

        // Back button
        Button back = new Button("← Retour");
        back.setStyle(UITheme.buttonGreenStyle());
        back.setOnAction(e -> controller.showMainMenu());
        HBox backBox = new HBox(back);
        backBox.setPadding(new Insets(20));

        root.setTop(header);
        root.setCenter(cardsScroll);
        root.setBottom(backBox);

        return new Scene(root, 1200, 800);
    }

    private VBox createMissionCard(Mission m) {
        VBox card = new VBox(12);
        card.setPrefWidth(330);
        card.setStyle("-fx-background-color: " + UITheme.BG_CARD + "; " +
                "-fx-border-color: " + UITheme.BORDER_COLOR + "; " +
                "-fx-border-width: 1; -fx-padding: 20;");

        // Terrain badge
        String terrainIcon = getTerrainIcon(m.getTerrain());
        Label terrain = new Label(terrainIcon + " " + m.getTerrain());
        terrain.setStyle("-fx-text-fill: " + UITheme.TEXT_SECONDARY + "; -fx-font-size: 11px; -fx-font-weight: bold;");

        // Title
        Label title = new Label(m.getTitle());
        title.setStyle(UITheme.labelTitleStyle(15));
        title.setWrapText(true);

        // Difficulty
        String diffColor = UITheme.getDifficultyColor(m.getDifficulty());
        Label diff = new Label("▶ " + m.getDifficulty());
        diff.setStyle("-fx-text-fill: " + diffColor + "; -fx-font-weight: bold; -fx-font-size: 13px;");

        // Description
        Label desc = new Label(m.getDescription());
        desc.setStyle(UITheme.labelNormalStyle());
        desc.setWrapText(true);

        // Separator
        Separator sep = new Separator();

        // Objective
        Label objLabel = new Label("OBJECTIF :");
        objLabel.setStyle("-fx-text-fill: " + UITheme.GOLD + "; -fx-font-size: 11px; -fx-font-weight: bold;");
        Label obj = new Label(m.getObjectif());
        obj.setStyle(UITheme.labelSecondaryStyle());
        obj.setWrapText(true);

        // Troops count
        Label troops = new Label("🪖 " + m.getAvailableTroops().size() + " unités disponibles  |  🏆 " + m.getMaxPoints() + " pts max");
        troops.setStyle(UITheme.labelSecondaryStyle());

        // Select button
        Button btn = new Button("CHOISIR CETTE MISSION");
        btn.setStyle(UITheme.buttonGoldStyle());
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> controller.showPlayerSetup(m));
        btn.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #252510; -fx-border-color: " + UITheme.GOLD + "; -fx-border-width: 2; -fx-padding: 20;"));
        btn.setOnMouseExited(e -> card.setStyle("-fx-background-color: " + UITheme.BG_CARD + "; -fx-border-color: " + UITheme.BORDER_COLOR + "; -fx-border-width: 1; -fx-padding: 20;"));

        card.getChildren().addAll(terrain, title, diff, desc, sep, objLabel, obj, troops, btn);
        VBox.setVgrow(desc, Priority.ALWAYS);

        return card;
    }

    private String getTerrainIcon(String terrain) {
        switch (terrain) {
            case "NORMANDIE": return "🌊";
            case "ARDENNES": return "🌲";
            case "STALINGRAD": return "🏚";
            default: return "🗺";
        }
    }
}
