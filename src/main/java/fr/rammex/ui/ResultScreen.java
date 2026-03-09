package fr.rammex.ui;

import fr.rammex.AppController;
import fr.rammex.model.*;
import fr.rammex.service.ScoringEngine;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;

public class ResultScreen {

    private AppController controller;
    private Mission mission;
    private ScoringEngine.EvaluationResult result;
    private SessionResult sessionResult;

    public ResultScreen(AppController controller, Mission mission,
                        ScoringEngine.EvaluationResult result, SessionResult sessionResult) {
        this.controller = controller;
        this.mission = mission;
        this.result = result;
        this.sessionResult = sessionResult;
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + UITheme.BG_DARK + ";");

        // === HEADER ===
        VBox header = new VBox(5);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(25, 20, 15, 20));
        header.setStyle("-fx-background-color: " + UITheme.BG_PANEL + "; -fx-border-color: " + UITheme.GOLD + "; -fx-border-width: 0 0 2 0;");

        Label title = new Label("📊 RAPPORT D'ÉVALUATION TACTIQUE");
        title.setStyle(UITheme.labelTitleStyle(24));
        Label sub = new Label(mission.getTitle() + " | " + controller.getPlayerRank() + " " + controller.getPlayerName());
        sub.setStyle(UITheme.labelSecondaryStyle());
        header.getChildren().addAll(title, sub);

        // === CENTER CONTENT ===
        HBox content = new HBox(25);
        content.setPadding(new Insets(25));
        content.setAlignment(Pos.TOP_CENTER);

        // LEFT: Score display
        VBox scorePanel = new VBox(20);
        scorePanel.setAlignment(Pos.CENTER);
        scorePanel.setPrefWidth(280);
        scorePanel.setStyle(UITheme.cardStyle());

        // Grade display
        String gradeColor = getGradeColor(result.getGrade());
        Label gradeLabel = new Label(result.getGrade());
        gradeLabel.setStyle("-fx-text-fill: " + gradeColor + "; -fx-font-size: 32px; -fx-font-weight: bold;");

        // Score circle visual
        StackPane scoreCircle = new StackPane();
        Circle outerCircle = new Circle(70);
        outerCircle.setFill(Color.web(UITheme.BG_DARK));
        outerCircle.setStroke(Color.web(gradeColor));
        outerCircle.setStrokeWidth(4);
        Label scoreText = new Label(result.getTotalScore() + "\n/" + result.getMaxScore());
        scoreText.setStyle("-fx-text-fill: " + UITheme.GOLD + "; -fx-font-size: 20px; -fx-font-weight: bold; -fx-text-alignment: center;");
        scoreText.setAlignment(Pos.CENTER);
        scoreCircle.getChildren().addAll(outerCircle, scoreText);

        // Percentage bar
        double pct = result.getPercentage();
        Label pctLabel = new Label(String.format("%.0f%%", pct));
        pctLabel.setStyle("-fx-text-fill: " + gradeColor + "; -fx-font-size: 22px; -fx-font-weight: bold;");

        ProgressBar progressBar = new ProgressBar(pct / 100.0);
        progressBar.setPrefWidth(200);
        progressBar.setStyle("-fx-accent: " + gradeColor + "; -fx-background-color: " + UITheme.BG_DARK + ";");

        // Stars
        int stars = getStars(pct);
        Label starsLabel = new Label("⭐".repeat(stars) + "☆".repeat(3 - stars));
        starsLabel.setStyle("-fx-font-size: 28px;");

        Label dateLabel = new Label("📅 " + sessionResult.getDateTime());
        dateLabel.setStyle(UITheme.labelSecondaryStyle());

        scorePanel.getChildren().addAll(gradeLabel, scoreCircle, pctLabel, progressBar, starsLabel, dateLabel);

        // CENTER: Criteria breakdown
        VBox criteriaPanel = new VBox(10);
        criteriaPanel.setPrefWidth(350);

        Label critTitle = new Label("📋 DÉTAIL PAR CRITÈRE");
        critTitle.setStyle(UITheme.labelTitleStyle(14));
        criteriaPanel.getChildren().add(critTitle);

        for (ScoringEngine.CriteriaResult cr : result.getCriteriaResults()) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(6, 10, 6, 10));
            row.setStyle("-fx-background-color: " + UITheme.BG_CARD + "; -fx-border-color: " + UITheme.BORDER_COLOR + "; -fx-border-width: 1;");

            Label statusIcon = new Label(cr.isSatisfied() ? "✅" : (cr.getEarned() > 0 ? "⚠" : "❌"));
            statusIcon.setStyle("-fx-font-size: 14px;");

            VBox textBox = new VBox(2);
            Label crName = new Label(cr.getName());
            crName.setStyle("-fx-text-fill: " + UITheme.TEXT_PRIMARY + "; -fx-font-size: 12px; -fx-font-weight: bold;");
            Label crPts = new Label(cr.getEarned() + " / " + cr.getMax() + " pts");
            crPts.setStyle("-fx-text-fill: " + (cr.isSatisfied() ? "#4a8c4a" : UITheme.RED_DANGER) + "; -fx-font-size: 11px;");
            textBox.getChildren().addAll(crName, crPts);

            row.getChildren().addAll(statusIcon, textBox);
            criteriaPanel.getChildren().add(row);
        }

        // Feedback lines
        Label feedTitle = new Label("💬 RETOUR D'ÉVALUATION");
        feedTitle.setStyle(UITheme.labelTitleStyle(12));
        criteriaPanel.getChildren().add(feedTitle);

        for (String line : result.getFeedbackLines()) {
            Label feedLine = new Label(line);
            feedLine.setStyle(UITheme.labelSecondaryStyle());
            feedLine.setWrapText(true);
            criteriaPanel.getChildren().add(feedLine);
        }

        ScrollPane critScroll = new ScrollPane(criteriaPanel);
        critScroll.setStyle("-fx-background: " + UITheme.BG_DARK + "; -fx-background-color: " + UITheme.BG_DARK + ";");
        critScroll.setFitToWidth(true);
        critScroll.setPrefWidth(370);

        // RIGHT: Tactical analysis
        VBox analysisPanel = new VBox(12);
        analysisPanel.setPrefWidth(310);
        analysisPanel.setStyle(UITheme.cardStyle());

        Label anaTitle = new Label("🎓 ANALYSE TACTIQUE");
        anaTitle.setStyle(UITheme.labelTitleStyle(14));

        Label anaText = new Label(result.getTacticalAdvice());
        anaText.setStyle(UITheme.labelNormalStyle() + " -fx-font-style: italic;");
        anaText.setWrapText(true);

        Separator sep = new Separator();

        Label recommendTitle = new Label("📌 RECOMMANDATIONS");
        recommendTitle.setStyle("-fx-text-fill: " + UITheme.GOLD + "; -fx-font-size: 12px; -fx-font-weight: bold;");

        Label recommend = new Label(getRecommendation(pct, mission.getDifficulty()));
        recommend.setStyle(UITheme.labelNormalStyle());
        recommend.setWrapText(true);

        analysisPanel.getChildren().addAll(anaTitle, anaText, sep, recommendTitle, recommend);

        content.getChildren().addAll(scorePanel, critScroll, analysisPanel);
        HBox.setHgrow(critScroll, Priority.ALWAYS);

        // === BOTTOM BUTTONS ===
        HBox buttons = new HBox(20);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(20));
        buttons.setStyle("-fx-background-color: " + UITheme.BG_PANEL + "; -fx-border-color: " + UITheme.GOLD + "; -fx-border-width: 2 0 0 0;");

        Button retryBtn = new Button("🔄 Rejouer cette Mission");
        retryBtn.setStyle(UITheme.buttonGreenStyle() + " -fx-font-size: 14px;");
        retryBtn.setOnAction(e -> controller.showPlayerSetup(mission));

        Button menuBtn = new Button("📋 Choisir une Autre Mission");
        menuBtn.setStyle(UITheme.buttonGreenStyle() + " -fx-font-size: 14px;");
        menuBtn.setOnAction(e -> controller.showMissionSelect());

        Button leaderboardBtn = new Button("🏅 Voir le Classement");
        leaderboardBtn.setStyle(UITheme.buttonGoldStyle() + " -fx-font-size: 14px;");
        leaderboardBtn.setOnAction(e -> controller.showLeaderboard());

        Button homeBtn = new Button("🏠 Menu Principal");
        homeBtn.setStyle(UITheme.buttonGreenStyle());
        homeBtn.setOnAction(e -> controller.showMainMenu());

        buttons.getChildren().addAll(homeBtn, retryBtn, menuBtn, leaderboardBtn);

        root.setTop(header);
        root.setCenter(content);
        root.setBottom(buttons);

        return new Scene(root, 1200, 800);
    }

    private String getGradeColor(String grade) {
        switch (grade) {
            case "EXCELLENCE": return UITheme.GOLD;
            case "TRÈS BIEN": return "#4a8c4a";
            case "BIEN": return "#6a8c4a";
            case "PASSABLE": return "#8c6a2a";
            default: return UITheme.RED_DANGER;
        }
    }

    private int getStars(double pct) {
        if (pct >= 80) return 3;
        if (pct >= 50) return 2;
        if (pct >= 20) return 1;
        return 0;
    }

    private String getRecommendation(double pct, String difficulty) {
        if (pct >= 90) return "Déploiement exemplaire ! Vous maîtrisez les concepts tactiques WWII. Essayez une mission de difficulté supérieure.";
        if (pct >= 75) return "Très bon déploiement. Affinez le placement de vos unités spécialisées (sniper, artillerie) pour maximiser l'efficacité.";
        if (pct >= 60) return "Déploiement correct mais améliorable. Concentrez-vous sur les zones à haute valeur stratégique.";
        if (pct >= 40) return "Relisez le briefing attentivement. Identifiez les zones prioritaires (encadrées en or) et concentrez vos unités là-bas.";
        return "Recommencez la mission. Lisez chaque critère d'évaluation avant de placer vos troupes.";
    }
}
