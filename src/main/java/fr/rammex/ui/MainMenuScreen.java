package fr.rammex.ui;

import fr.rammex.AppController;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class MainMenuScreen {

    private AppController controller;

    public MainMenuScreen(AppController controller) {
        this.controller = controller;
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + UITheme.BG_DARK + ";");

        // === HEADER ===
        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(50, 20, 30, 20));

        // Decorative line
        Separator topLine = new Separator();
        topLine.setStyle("-fx-background-color: " + UITheme.GOLD + "; -fx-padding: 1;");

        Label star1 = new Label("✦ ✦ ✦");
        star1.setStyle("-fx-text-fill: " + UITheme.GOLD + "; -fx-font-size: 18px;");

        Label title = new Label("ACADÉMIE MILITAIRE");
        title.setStyle(UITheme.labelTitleStyle(42));

        Label subtitle = new Label("WWII TACTICAL TRAINING SYSTEM");
        subtitle.setStyle("-fx-text-fill: " + UITheme.TEXT_SECONDARY + "; -fx-font-size: 16px; -fx-letter-spacing: 3px;");

        Label star2 = new Label("✦ ✦ ✦");
        star2.setStyle("-fx-text-fill: " + UITheme.GOLD + "; -fx-font-size: 18px;");

        Label sub2 = new Label("Système d'Entraînement pour Personnel Militaire");
        sub2.setStyle("-fx-text-fill: " + UITheme.TEXT_SECONDARY + "; -fx-font-size: 13px; -fx-font-style: italic;");

        header.getChildren().addAll(topLine, star1, title, subtitle, star2, sub2);

        // === CENTER - Main buttons ===
        VBox center = new VBox(20);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(20));

        // Training mission button
        Button btnTrain = new Button("⚔  COMMENCER L'ENTRAÎNEMENT");
        btnTrain.setStyle(UITheme.buttonGoldStyle() + " -fx-font-size: 18px; -fx-padding: 15 40;");
        btnTrain.setPrefWidth(400);
        btnTrain.setOnAction(e -> controller.showMissionSelect());
        btnTrain.setOnMouseEntered(e -> btnTrain.setStyle(UITheme.buttonGoldStyle() +
                " -fx-font-size: 18px; -fx-padding: 15 40; -fx-background-color: " + UITheme.GOLD_LIGHT + ";"));
        btnTrain.setOnMouseExited(e -> btnTrain.setStyle(UITheme.buttonGoldStyle() +
                " -fx-font-size: 18px; -fx-padding: 15 40;"));

        Button btnLeaderboard = new Button("🏅  TABLEAU DES RÉSULTATS");
        btnLeaderboard.setStyle(UITheme.buttonGreenStyle() + " -fx-font-size: 16px; -fx-padding: 12 30;");
        btnLeaderboard.setPrefWidth(400);
        btnLeaderboard.setOnAction(e -> controller.showLeaderboard());

        // Info panel
        VBox infoPanel = new VBox(10);
        infoPanel.setStyle(UITheme.cardStyle());
        infoPanel.setMaxWidth(600);
        infoPanel.setAlignment(Pos.CENTER_LEFT);

        Label infoTitle = new Label("📋 OBJECTIF DE L'ENTRAÎNEMENT");
        infoTitle.setStyle(UITheme.labelTitleStyle(14));

        Label info2 = new Label("• Analysez la carte tactique et positionnez vos troupes");
        Label info3 = new Label("• Obtenez des points selon la qualité de vos décisions");
        Label info4 = new Label("• Recevez une analyse tactique complète de votre déploiement");

        for (Label l : new Label[]{info2, info3, info4}) {
            l.setStyle(UITheme.labelNormalStyle());
        }

        infoPanel.getChildren().addAll(infoTitle,  info2, info3, info4);

        center.getChildren().addAll(btnTrain, btnLeaderboard, infoPanel);

        // === FOOTER ===
        Label footer = new Label("« La victoire appartient à celui qui prépare le mieux. » — Général de Brigade");
        footer.setStyle("-fx-text-fill: " + UITheme.TEXT_SECONDARY + "; -fx-font-style: italic; -fx-font-size: 12px;");

        Button btnAdmin = new Button("⚙");
        btnAdmin.setStyle("-fx-background-color: transparent; -fx-text-fill: " + UITheme.TEXT_SECONDARY +
                "; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 2 6; -fx-border-color: " +
                UITheme.BORDER_COLOR + "; -fx-border-width: 1;");
        btnAdmin.setTooltip(new javafx.scene.control.Tooltip("Accès Administrateur"));
        btnAdmin.setOnAction(e -> controller.showAdminLogin());

        HBox footerBox = new HBox(10, footer, btnAdmin);
        footerBox.setAlignment(Pos.CENTER);
        footerBox.setPadding(new Insets(20));

        root.setTop(header);
        root.setCenter(center);
        root.setBottom(footerBox);

        return new Scene(root, 1200, 800);
    }
}
