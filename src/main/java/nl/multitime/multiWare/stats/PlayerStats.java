package nl.multitime.multiWare.stats;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerStats implements ConfigurationSerializable {

    private final UUID playerId;
    private int gamesPlayed;
    private int gamesWon;
    private int totalPoints;
    private int highestScore;
    private final Map<String, Integer> minigameWins;

    public PlayerStats(UUID playerId) {
        this.playerId = playerId;
        this.gamesPlayed = 0;
        this.gamesWon = 0;
        this.totalPoints = 0;
        this.highestScore = 0;
        this.minigameWins = new HashMap<>();
    }

    public PlayerStats(Map<String, Object> map) {
        this.playerId = UUID.fromString((String) map.get("playerId"));
        this.gamesPlayed = (Integer) map.get("gamesPlayed");
        this.gamesWon = (Integer) map.get("gamesWon");
        this.totalPoints = (Integer) map.get("totalPoints");
        this.highestScore = (Integer) map.get("highestScore");
        this.minigameWins = new HashMap<>();

        Map<String, Integer> wins = (Map<String, Integer>) map.get("minigameWins");
        if (wins != null) {
            this.minigameWins.putAll(wins);
        }
    }

    public void incrementGamesPlayed() {
        this.gamesPlayed++;
    }

    public void incrementGamesWon() {
        this.gamesWon++;
    }

    public void addPoints(int points) {
        this.totalPoints += points;

        if (points > this.highestScore) {
            this.highestScore = points;
        }
    }

    public void addMinigameWin(String minigameName) {
        minigameWins.put(minigameName, minigameWins.getOrDefault(minigameName, 0) + 1);
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public int getGamesWon() {
        return gamesWon;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public int getHighestScore() {
        return highestScore;
    }

    public Map<String, Integer> getMinigameWins() {
        return new HashMap<>(minigameWins);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("playerId", playerId.toString());
        map.put("gamesPlayed", gamesPlayed);
        map.put("gamesWon", gamesWon);
        map.put("totalPoints", totalPoints);
        map.put("highestScore", highestScore);
        map.put("minigameWins", minigameWins);
        return map;
    }

    public static PlayerStats deserialize(Map<String, Object> map) {
        return new PlayerStats(map);
    }
}
