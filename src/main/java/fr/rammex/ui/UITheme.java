package fr.rammex.ui;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;

public class UITheme {

    // Colors
    public static final String BG_DARK = "#1a1a0e";
    public static final String BG_PANEL = "#252510";
    public static final String BG_CARD = "#1e1e0a";
    public static final String GOLD = "#c9a84c";
    public static final String GOLD_LIGHT = "#e8c96a";
    public static final String GREEN_MILITARY = "#4a5c2a";
    public static final String GREEN_LIGHT = "#6b8c3a";
    public static final String RED_DANGER = "#8b2020";
    public static final String TEXT_PRIMARY = "#d4c89a";
    public static final String TEXT_SECONDARY = "#8a8060";
    public static final String BORDER_COLOR = "#4a4020";

    // Difficulty colors
    public static String getDifficultyColor(String diff) {
        switch (diff) {
            case "FACILE": return "#4a8c4a";
            case "MOYEN": return "#c9a84c";
            case "DIFFICILE": return "#c96a4a";
            case "ÉLITE": return "#8b2020";
            default: return TEXT_SECONDARY;
        }
    }

    // Troop type colors
    public static String getTroopColor(String type) {
        switch (type) {
            case "1st": return "#4a8c4a";
            case "2ND ARMORED": return "#6a6a2a";
            case "101st": return "#A88919";
            case "13e": return "#1936A8";
            case "ARTILLERY": return "#8c4a4a";
            case "CANNONIER": return "#A8199A";
            case "SNIPER": return "#4a6a8c";
            case "GRENADIER": return "#19A871";
            case "MEDIC": return "#FFFFFF";

            default: return "#6a6a6a";
        }
    }

    public static String getZoneColor(String type) {
        switch (type) {
            case "OFFENSIF": return "#8c2a2a";
            case "DEFENSIF": return "#2a4a8c";
            case "COUVERTURE": return "#2a6a2a";
            case "COLLINE": return "#6a5a2a";
            case "VILLE": return "#5a5a5a";
            case "PONT": return "#8c6a2a";
            default: return "#4a4a4a";
        }
    }

    // Style strings
    public static String panelStyle() {
        return "-fx-background-color: " + BG_PANEL + "; " +
               "-fx-border-color: " + BORDER_COLOR + "; " +
               "-fx-border-width: 1; " +
               "-fx-padding: 15;";
    }

    public static String cardStyle() {
        return "-fx-background-color: " + BG_CARD + "; " +
               "-fx-border-color: " + GOLD + "; " +
               "-fx-border-width: 1; " +
               "-fx-padding: 15;";
    }

    public static String buttonGoldStyle() {
        return "-fx-background-color: " + GOLD + "; " +
               "-fx-text-fill: " + BG_DARK + "; " +
               "-fx-font-weight: bold; " +
               "-fx-font-size: 14px; " +
               "-fx-padding: 10 25; " +
               "-fx-cursor: hand; " +
               "-fx-border-radius: 2; " +
               "-fx-background-radius: 2;";
    }

    public static String buttonGreenStyle() {
        return "-fx-background-color: " + GREEN_MILITARY + "; " +
               "-fx-text-fill: " + TEXT_PRIMARY + "; " +
               "-fx-font-weight: bold; " +
               "-fx-font-size: 13px; " +
               "-fx-padding: 8 20; " +
               "-fx-cursor: hand; " +
               "-fx-border-radius: 2; " +
               "-fx-background-radius: 2;";
    }

    public static String buttonRedStyle() {
        return "-fx-background-color: " + RED_DANGER + "; " +
               "-fx-text-fill: " + TEXT_PRIMARY + "; " +
               "-fx-font-weight: bold; " +
               "-fx-font-size: 13px; " +
               "-fx-padding: 8 20; " +
               "-fx-cursor: hand;";
    }

    public static String labelTitleStyle(int size) {
        return "-fx-text-fill: " + GOLD + "; " +
               "-fx-font-size: " + size + "px; " +
               "-fx-font-weight: bold;";
    }

    public static String labelNormalStyle() {
        return "-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 13px;";
    }

    public static String labelSecondaryStyle() {
        return "-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 12px;";
    }
}
