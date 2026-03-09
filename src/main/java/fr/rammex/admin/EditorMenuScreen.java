package fr.rammex.admin;

import fr.rammex.AppController;
import fr.rammex.model.Mission;
import fr.rammex.ui.UITheme;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.UUID;

public class EditorMenuScreen {

    private final AppController controller;

    public EditorMenuScreen(AppController controller) {
        this.controller = controller;
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + UITheme.BG_DARK + ";");
        root.setTop(buildHeader());
        root.setCenter(buildContent());
        root.setBottom(buildFooter());
        return new Scene(root, 1200, 800);
    }

    private HBox buildHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15, 25, 15, 25));
        header.setStyle("-fx-background-color: " + UITheme.BG_PANEL +
                "; -fx-border-color: " + UITheme.GOLD + "; -fx-border-width: 0 0 2 0;");

        Label title = new Label("⚙ ÉDITEUR DE MISSIONS");
        title.setStyle(UITheme.labelTitleStyle(22));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label user = new Label("Connecté : " + controller.getCurrentAdminUser());
        user.setStyle(UITheme.labelSecondaryStyle());

        Button logoutBtn = new Button("Déconnexion");
        logoutBtn.setStyle(UITheme.buttonRedStyle());
        logoutBtn.setOnAction(e -> { controller.adminLogout(); controller.showMainMenu(); });

        header.getChildren().addAll(title, spacer, user, logoutBtn);
        return header;
    }

    private VBox buildContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(25));

        // Action buttons row
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER_LEFT);

        Button createBtn = new Button("+ Créer une nouvelle mission");
        createBtn.setStyle(UITheme.buttonGoldStyle());
        createBtn.setOnAction(e -> {
            // Create blank mission with a unique ID
            String newId = "custom_" + UUID.randomUUID().toString().substring(0, 8);
            Mission blank = new Mission(newId, "Nouvelle Mission", "", "MOYEN",
                    "", "", "", 200, "INCONNU");
            controller.showMissionEditor(blank);
        });

        Button mapBtn = new Button("🗺 Gérer les maps");
        mapBtn.setStyle(UITheme.buttonGreenStyle());
        mapBtn.setOnAction(e -> controller.showMapManager());

        actions.getChildren().addAll(createBtn, mapBtn);

        // Mission list
        Label listTitle = new Label("MISSIONS DISPONIBLES");
        listTitle.setStyle(UITheme.labelTitleStyle(15));

        VBox missionList = new VBox(10);
        missionList.setStyle("-fx-background-color: " + UITheme.BG_PANEL + "; -fx-padding: 15;");

        List<Mission> missions = controller.getMissions();
        if (missions.isEmpty()) {
            Label empty = new Label("Aucune mission. Créez-en une.");
            empty.setStyle(UITheme.labelSecondaryStyle());
            missionList.getChildren().add(empty);
        } else {
            for (Mission m : missions) {
                missionList.getChildren().add(buildMissionRow(m));
            }
        }

        ScrollPane scroll = new ScrollPane(missionList);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: " + UITheme.BG_DARK + "; -fx-background-color: " + UITheme.BG_DARK + ";");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        content.getChildren().addAll(actions, listTitle, scroll);
        return content;
    }

    private HBox buildMissionRow(Mission m) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 15, 10, 15));
        row.setStyle("-fx-background-color: " + UITheme.BG_CARD +
                "; -fx-border-color: " + UITheme.BORDER_COLOR + "; -fx-border-width: 1;");

        String diffColor = UITheme.getDifficultyColor(m.getDifficulty());
        Label diff = new Label("▶ " + m.getDifficulty());
        diff.setStyle("-fx-text-fill: " + diffColor + "; -fx-font-weight: bold; -fx-font-size: 12px;");
        diff.setMinWidth(80);

        Label title = new Label(m.getTitle());
        title.setStyle(UITheme.labelNormalStyle() + " -fx-font-weight: bold;");

        Label terrain = new Label("[" + m.getTerrain() + "]");
        terrain.setStyle(UITheme.labelSecondaryStyle());

        Label pts = new Label(m.getMaxPoints() + " pts");
        pts.setStyle(UITheme.labelSecondaryStyle());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button editBtn = new Button("✏ Modifier");
        editBtn.setStyle(UITheme.buttonGreenStyle());
        editBtn.setOnAction(e -> controller.showMissionEditor(m));

        Button deleteBtn = new Button("✖ Supprimer");
        deleteBtn.setStyle(UITheme.buttonRedStyle());
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Supprimer la mission \"" + m.getTitle() + "\" ?",
                    ButtonType.YES, ButtonType.NO);
            confirm.setTitle("Confirmation");
            confirm.showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.YES) {
                    controller.deleteMission(m.getId());
                    controller.showEditorMenu(); // refresh
                }
            });
        });

        row.getChildren().addAll(diff, title, terrain, pts, spacer, editBtn, deleteBtn);
        return row;
    }

    private HBox buildFooter() {
        HBox footer = new HBox();
        footer.setPadding(new Insets(15, 25, 15, 25));
        footer.setStyle("-fx-background-color: " + UITheme.BG_PANEL + ";");
        Label info = new Label("Les modifications sont sauvegardées dans MongoDB et disponibles immédiatement pour tous les joueurs.");
        info.setStyle(UITheme.labelSecondaryStyle());
        footer.getChildren().add(info);
        return footer;
    }
}
