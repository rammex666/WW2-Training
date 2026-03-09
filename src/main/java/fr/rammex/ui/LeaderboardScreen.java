package fr.rammex.ui;

import fr.rammex.AppController;
import fr.rammex.model.SessionResult;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.List;

public class LeaderboardScreen {

    private AppController controller;
    private List<SessionResult> results;

    public LeaderboardScreen(AppController controller, List<SessionResult> results) {
        this.controller = controller;
        this.results = results != null ? results : new java.util.ArrayList<>();
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + UITheme.BG_DARK + ";");

        // Header
        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(25, 20, 15, 20));
        header.setStyle("-fx-background-color: " + UITheme.BG_PANEL + "; -fx-border-color: " + UITheme.GOLD + "; -fx-border-width: 0 0 2 0;");
        Label title = new Label("🏅 TABLEAU D'HONNEUR");
        title.setStyle(UITheme.labelTitleStyle(28));
        Label sub = new Label("Historique des sessions d'entraînement tactique");
        sub.setStyle(UITheme.labelSecondaryStyle());
        header.getChildren().addAll(title, sub);

        // Content
        VBox center = new VBox(20);
        center.setPadding(new Insets(30));

        if (results.isEmpty()) {
            Label empty = new Label("Aucune session enregistrée.\nComplétez une mission pour voir vos résultats ici.");
            empty.setStyle(UITheme.labelNormalStyle() + " -fx-text-alignment: center; -fx-font-size: 16px;");
            empty.setAlignment(Pos.CENTER);
            center.setAlignment(Pos.CENTER);
            center.getChildren().add(empty);
        } else {
            // Table
            TableView<SessionResult> table = new TableView<>();
            table.setStyle("-fx-background-color: " + UITheme.BG_CARD + "; -fx-text-fill: " + UITheme.TEXT_PRIMARY + ";");

            TableColumn<SessionResult, String> rankCol = new TableColumn<>("Grade");
            rankCol.setCellValueFactory(new PropertyValueFactory<>("rank"));
            rankCol.setPrefWidth(120);
            styleColumn(rankCol);

            TableColumn<SessionResult, String> nameCol = new TableColumn<>("Nom");
            nameCol.setCellValueFactory(new PropertyValueFactory<>("playerName"));
            nameCol.setPrefWidth(140);
            styleColumn(nameCol);

            TableColumn<SessionResult, String> missionCol = new TableColumn<>("Mission");
            missionCol.setCellValueFactory(new PropertyValueFactory<>("missionTitle"));
            missionCol.setPrefWidth(280);
            styleColumn(missionCol);

            TableColumn<SessionResult, Integer> scoreCol = new TableColumn<>("Score");
            scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));
            scoreCol.setPrefWidth(80);
            styleColumn(scoreCol);

            TableColumn<SessionResult, Integer> maxCol = new TableColumn<>("Max");
            maxCol.setCellValueFactory(new PropertyValueFactory<>("maxScore"));
            maxCol.setPrefWidth(70);
            styleColumn(maxCol);

            TableColumn<SessionResult, String> gradeCol = new TableColumn<>("Mention");
            gradeCol.setCellValueFactory(new PropertyValueFactory<>("grade"));
            gradeCol.setPrefWidth(120);
            styleColumn(gradeCol);

            TableColumn<SessionResult, String> dateCol = new TableColumn<>("Date");
            dateCol.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
            dateCol.setPrefWidth(140);
            styleColumn(dateCol);

            table.getColumns().addAll(rankCol, nameCol, missionCol, scoreCol, maxCol, gradeCol, dateCol);
            table.getItems().addAll(results);
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            // Stats summary
            int totalSessions = results.size();
            double avgScore = results.stream().mapToDouble(SessionResult::getPercentage).average().orElse(0);
            String bestGrade = results.stream()
                    .max((a, b) -> Double.compare(a.getPercentage(), b.getPercentage()))
                    .map(SessionResult::getGrade).orElse("N/A");

            HBox stats = new HBox(40);
            stats.setAlignment(Pos.CENTER);
            stats.setStyle(UITheme.cardStyle());

            stats.getChildren().addAll(
                createStatBox("Sessions", String.valueOf(totalSessions)),
                createStatBox("Score Moyen", String.format("%.0f%%", avgScore)),
                createStatBox("Meilleure Mention", bestGrade)
            );

            center.getChildren().addAll(stats, table);
        }

        // Back button
        Button back = new Button("← Retour au Menu");
        back.setStyle(UITheme.buttonGoldStyle());
        back.setOnAction(e -> controller.showMainMenu());
        HBox backBox = new HBox(back);
        backBox.setPadding(new Insets(20));
        backBox.setAlignment(Pos.CENTER);

        root.setTop(header);
        root.setCenter(new ScrollPane(center) {{
            setStyle("-fx-background: " + UITheme.BG_DARK + "; -fx-background-color: " + UITheme.BG_DARK + ";");
            setFitToWidth(true);
        }});
        root.setBottom(backBox);

        return new Scene(root, 1200, 800);
    }

    private VBox createStatBox(String label, String value) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        Label val = new Label(value);
        val.setStyle(UITheme.labelTitleStyle(22));
        Label lbl = new Label(label);
        lbl.setStyle(UITheme.labelSecondaryStyle());
        box.getChildren().addAll(val, lbl);
        return box;
    }

    private <T> void styleColumn(TableColumn<SessionResult, T> col) {
        col.setStyle("-fx-text-fill: " + UITheme.TEXT_PRIMARY + ";");
    }
}
