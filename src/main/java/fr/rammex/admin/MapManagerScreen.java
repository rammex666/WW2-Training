package fr.rammex.admin;

import fr.rammex.AppController;
import fr.rammex.db.MongoDBService;
import fr.rammex.ui.UITheme;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;

public class MapManagerScreen {

    private final AppController controller;
    private final String mapsDir;
    private VBox fileListBox;
    private Label statusLabel;

    public MapManagerScreen(AppController controller) {
        this.controller = controller;
        this.mapsDir = MongoDBService.getInstance().getMapsDirectory();
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + UITheme.BG_DARK + ";");

        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15, 25, 15, 25));
        header.setStyle("-fx-background-color: " + UITheme.BG_PANEL +
                "; -fx-border-color: " + UITheme.GOLD + "; -fx-border-width: 0 0 2 0;");

        Label title = new Label("🗺 GESTIONNAIRE DE MAPS");
        title.setStyle(UITheme.labelTitleStyle(20));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        statusLabel = new Label("Dossier : " + mapsDir);
        statusLabel.setStyle(UITheme.labelSecondaryStyle());

        Button backBtn = new Button("← Retour");
        backBtn.setStyle(UITheme.buttonGreenStyle());
        backBtn.setOnAction(e -> controller.showEditorMenu());

        header.getChildren().addAll(title, spacer, statusLabel, backBtn);

        // Content
        VBox content = new VBox(20);
        content.setPadding(new Insets(25));

        Label dirLabel = new Label("Dossier des maps : " + Paths.get(mapsDir).toAbsolutePath());
        dirLabel.setStyle(UITheme.labelNormalStyle());
        dirLabel.setWrapText(true);

        Label hint = new Label("Les images déposées ici seront chargées dans l'éditeur. " +
                "Le nom de fichier (sans extension) correspond au champ 'Map' de la mission.");
        hint.setStyle(UITheme.labelSecondaryStyle());
        hint.setWrapText(true);

        Button addBtn = new Button("+ Ajouter une image (PNG / JPG)");
        addBtn.setStyle(UITheme.buttonGoldStyle());
        addBtn.setOnAction(e -> addMapImage());

        fileListBox = new VBox(8);
        refreshFileList();

        ScrollPane scroll = new ScrollPane(fileListBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: " + UITheme.BG_DARK + "; -fx-background-color: " + UITheme.BG_DARK + ";");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        content.getChildren().addAll(dirLabel, hint, addBtn, new Separator(), scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        root.setTop(header);
        root.setCenter(content);

        return new Scene(root, 1200, 800);
    }

    private void addMapImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Sélectionner une image de map");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("JPEG", "*.jpg", "*.jpeg")
        );

        File selected = chooser.showOpenDialog(controller.getStage());
        if (selected == null) return;

        // Ask for a target name
        TextInputDialog nameDialog = new TextInputDialog(stripExtension(selected.getName()));
        nameDialog.setTitle("Nom de la map");
        nameDialog.setHeaderText("Entrez le nom de la map (sans extension)");
        nameDialog.setContentText("Nom :");

        nameDialog.showAndWait().ifPresent(name -> {
            if (name.trim().isEmpty()) return;
            String ext = selected.getName().substring(selected.getName().lastIndexOf('.'));
            Path dest = Paths.get(mapsDir, name.trim() + ext);
            try {
                Files.createDirectories(dest.getParent());
                Files.copy(selected.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
                statusLabel.setText("✅ Map ajoutée : " + dest.getFileName());
                refreshFileList();
            } catch (Exception ex) {
                statusLabel.setText("❌ Erreur : " + ex.getMessage());
            }
        });
    }

    private void refreshFileList() {
        fileListBox.getChildren().clear();
        Label title = new Label("Images disponibles :");
        title.setStyle(UITheme.labelTitleStyle(13));
        fileListBox.getChildren().add(title);

        File dir = new File(mapsDir);
        if (!dir.exists() || !dir.isDirectory()) {
            Label empty = new Label("Dossier introuvable : " + mapsDir);
            empty.setStyle("-fx-text-fill: #cc4444;");
            fileListBox.getChildren().add(empty);
            return;
        }

        File[] files = dir.listFiles(f -> {
            String name = f.getName().toLowerCase();
            return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg");
        });

        if (files == null || files.length == 0) {
            Label empty = new Label("Aucune image dans ce dossier.");
            empty.setStyle(UITheme.labelSecondaryStyle());
            fileListBox.getChildren().add(empty);
            return;
        }

        Arrays.sort(files);
        for (File f : files) {
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(8, 12, 8, 12));
            row.setStyle("-fx-background-color: " + UITheme.BG_CARD +
                    "; -fx-border-color: " + UITheme.BORDER_COLOR + "; -fx-border-width: 1;");

            Label name = new Label("🖼 " + f.getName());
            name.setStyle(UITheme.labelNormalStyle());

            long sizeKb = f.length() / 1024;
            Label size = new Label(sizeKb + " KB");
            size.setStyle(UITheme.labelSecondaryStyle());

            String mapId = stripExtension(f.getName());
            Label id = new Label("ID map: " + mapId);
            id.setStyle("-fx-text-fill: " + UITheme.GOLD + "; -fx-font-size: 11px;");

            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);

            Button delBtn = new Button("✖ Supprimer");
            delBtn.setStyle(UITheme.buttonRedStyle() + " -fx-font-size: 11px; -fx-padding: 4 10;");
            delBtn.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                        "Supprimer le fichier " + f.getName() + " ?", ButtonType.YES, ButtonType.NO);
                confirm.showAndWait().ifPresent(bt -> {
                    if (bt == ButtonType.YES) {
                        f.delete();
                        refreshFileList();
                        statusLabel.setText("Fichier supprimé : " + f.getName());
                    }
                });
            });

            row.getChildren().addAll(name, size, id, sp, delBtn);
            fileListBox.getChildren().add(row);
        }
    }

    private String stripExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }
}
