package fr.rammex.admin;

import fr.rammex.AppController;
import fr.rammex.ui.UITheme;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class AdminLoginScreen {

    private final AppController controller;

    public AdminLoginScreen(AppController controller) {
        this.controller = controller;
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + UITheme.BG_DARK + ";");

        VBox center = new VBox(20);
        center.setAlignment(Pos.CENTER);
        center.setMaxWidth(400);
        center.setPadding(new Insets(40));
        center.setStyle(UITheme.cardStyle());

        Label title = new Label("⚙ ACCÈS ADMINISTRATEUR");
        title.setStyle(UITheme.labelTitleStyle(20));

        Label sub = new Label("Connectez-vous pour accéder à l'éditeur de missions");
        sub.setStyle(UITheme.labelSecondaryStyle());
        sub.setWrapText(true);
        sub.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        if (!controller.isMongoAvailable()) {
            Label warn = new Label("⚠ MongoDB non disponible.\nDémarrez mongod et relancez l'application.");
            warn.setStyle("-fx-text-fill: #cc4444; -fx-font-size: 13px;");
            warn.setWrapText(true);
            warn.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

            Button back = new Button("← Retour");
            back.setStyle(UITheme.buttonGreenStyle());
            back.setOnAction(e -> controller.showMainMenu());

            center.getChildren().addAll(title, warn, back);
        } else {
            Label userLabel = new Label("Identifiant :");
            userLabel.setStyle(UITheme.labelNormalStyle());

            TextField userField = new TextField();
            userField.setPromptText("admin");
            styleField(userField);

            Label passLabel = new Label("Mot de passe :");
            passLabel.setStyle(UITheme.labelNormalStyle());

            PasswordField passField = new PasswordField();
            passField.setPromptText("••••••••");
            styleField(passField);

            Label errorLabel = new Label("");
            errorLabel.setStyle("-fx-text-fill: #cc4444; -fx-font-size: 12px;");

            Button loginBtn = new Button("SE CONNECTER");
            loginBtn.setStyle(UITheme.buttonGoldStyle());
            loginBtn.setMaxWidth(Double.MAX_VALUE);
            loginBtn.setOnAction(e -> {
                String user = userField.getText().trim();
                String pass = passField.getText();
                if (user.isEmpty() || pass.isEmpty()) {
                    errorLabel.setText("Veuillez remplir tous les champs.");
                    return;
                }
                if (controller.adminLogin(user, pass)) {
                    controller.showEditorMenu();
                } else {
                    errorLabel.setText("Identifiants incorrects.");
                    passField.clear();
                }
            });

            // Allow Enter key to submit
            passField.setOnAction(e -> loginBtn.fire());

            Button back = new Button("← Retour");
            back.setStyle(UITheme.buttonGreenStyle());
            back.setMaxWidth(Double.MAX_VALUE);
            back.setOnAction(e -> controller.showMainMenu());

            center.getChildren().addAll(title, sub, userLabel, userField, passLabel, passField, errorLabel, loginBtn, back);
        }

        BorderPane wrapper = new BorderPane(center);
        wrapper.setStyle("-fx-background-color: " + UITheme.BG_DARK + ";");
        BorderPane.setAlignment(center, Pos.CENTER);

        return new Scene(wrapper, 1200, 800);
    }

    private void styleField(TextField f) {
        f.setStyle("-fx-background-color: " + UITheme.BG_PANEL + "; " +
                "-fx-text-fill: " + UITheme.TEXT_PRIMARY + "; " +
                "-fx-border-color: " + UITheme.BORDER_COLOR + "; " +
                "-fx-font-size: 14px; -fx-padding: 8;");
        f.setPrefWidth(300);
    }
}
