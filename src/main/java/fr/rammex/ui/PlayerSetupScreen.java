package fr.rammex.ui;

import fr.rammex.AppController;
import fr.rammex.model.Mission;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class PlayerSetupScreen {

    private AppController controller;
    private Mission mission;

    public PlayerSetupScreen(AppController controller, Mission mission) {
        this.controller = controller;
        this.mission = mission;
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + UITheme.BG_DARK + ";");

        // Header
        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(30, 20, 20, 20));
        header.setStyle("-fx-background-color: " + UITheme.BG_PANEL + "; -fx-border-color: " + UITheme.GOLD + "; -fx-border-width: 0 0 2 0;");
        Label title = new Label("📋 IDENTIFICATION DU PERSONNEL");
        title.setStyle(UITheme.labelTitleStyle(24));
        Label sub = new Label("Mission : " + mission.getTitle());
        sub.setStyle(UITheme.labelSecondaryStyle());
        header.getChildren().addAll(title, sub);

        // Form
        VBox form = new VBox(20);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(50));
        form.setMaxWidth(600);

        // Briefing box
        VBox briefing = new VBox(10);
        briefing.setStyle(UITheme.cardStyle() + " -fx-border-color: " + UITheme.GOLD + ";");
        Label briefTitle = new Label("📻 BRIEFING OPÉRATIONNEL");
        briefTitle.setStyle(UITheme.labelTitleStyle(14));
        Label briefText = new Label(mission.getBriefing());
        briefText.setStyle(UITheme.labelNormalStyle() + " -fx-font-style: italic;");
        briefText.setWrapText(true);
        briefing.getChildren().addAll(briefTitle, briefText);

        // Name field
        Label nameLabel = new Label("Votre nom de code :");
        nameLabel.setStyle(UITheme.labelNormalStyle());
        TextField nameField = new TextField(controller.getPlayerName());
        nameField.setStyle("-fx-background-color: " + UITheme.BG_PANEL + "; " +
                "-fx-text-fill: " + UITheme.TEXT_PRIMARY + "; " +
                "-fx-border-color: " + UITheme.BORDER_COLOR + "; " +
                "-fx-font-size: 14px; -fx-padding: 8;");

        // Rank selector
        Label rankLabel = new Label("Votre grade :");
        rankLabel.setStyle(UITheme.labelNormalStyle());
        ComboBox<String> rankBox = new ComboBox<>();
        rankBox.getItems().addAll(
            "Corporal",
            "Sergent",
            "Staff-Sergent",
            "Technical-Sergent",
            "First-Sergent",
            "Master-Sergent",
            "2nd Lieutenant",
            "1st Lieutenant",
            "Captain",
            "Major",
            "Lieutenant-Colonel",
            "Colonel"
        );
        rankBox.setValue(controller.getPlayerRank());
        rankBox.setStyle("-fx-background-color: " + UITheme.BG_PANEL + "; " +
                "-fx-text-fill: " + UITheme.TEXT_PRIMARY + ";");

        // Buttons
        HBox buttons = new HBox(20);
        buttons.setAlignment(Pos.CENTER);

        Button backBtn = new Button("← Retour");
        backBtn.setStyle(UITheme.buttonGreenStyle());
        backBtn.setOnAction(e -> controller.showMissionSelect());

        Button startBtn = new Button("⚔  LANCER LA MISSION");
        startBtn.setStyle(UITheme.buttonGoldStyle() + " -fx-font-size: 16px; -fx-padding: 12 30;");
        startBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String rank = rankBox.getValue();
            if (name.isEmpty()) name = "Inconnu";
            controller.startMission(mission, name, rank);
        });

        buttons.getChildren().addAll(backBtn, startBtn);
        form.getChildren().addAll(briefing, nameLabel, nameField, rankLabel, rankBox, buttons);

        ScrollPane scroll = new ScrollPane(form);
        scroll.setStyle("-fx-background: " + UITheme.BG_DARK + "; -fx-background-color: " + UITheme.BG_DARK + ";");
        scroll.setFitToWidth(true);

        root.setTop(header);
        root.setCenter(scroll);

        return new Scene(root, 1200, 800);
    }
}
