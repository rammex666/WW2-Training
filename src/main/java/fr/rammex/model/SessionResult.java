package fr.rammex.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SessionResult {
    private String playerName;
    private String rank;
    private String missionTitle;
    private int score;
    private int maxScore;
    private String grade;
    private String feedback;
    private String dateTime;

    public SessionResult(String playerName, String rank, String missionTitle, int score, int maxScore) {
        this.playerName = playerName;
        this.rank = rank;
        this.missionTitle = missionTitle;
        this.score = score;
        this.maxScore = maxScore;
        this.grade = calculateGrade();
        this.dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private String calculateGrade() {
        double pct = (double) score / maxScore * 100;
        if (pct >= 90) return "EXCELLENCE";
        if (pct >= 75) return "TRÈS BIEN";
        if (pct >= 60) return "BIEN";
        if (pct >= 40) return "PASSABLE";
        return "ÉCHEC";
    }

    public String getPlayerName() { return playerName; }
    public String getRank() { return rank; }
    public String getMissionTitle() { return missionTitle; }
    public int getScore() { return score; }
    public int getMaxScore() { return maxScore; }
    public String getGrade() { return grade; }
    public String getFeedback() { return feedback; }
    public String getDateTime() { return dateTime; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public double getPercentage() { return (double) score / maxScore * 100; }
}
