package com.poker.model;

import lombok.Data;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class GameRoom {
    private String id;
    private String name;
    private Map<String, Player> players;
    private boolean votingRevealed;
    private String currentStory;
    private List<String> cardSet;

    public GameRoom(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.players = new ConcurrentHashMap<>();
        this.votingRevealed = false;
        this.currentStory = "";
        this.cardSet = Arrays.asList("0", "1", "2", "3", "5", "8", "13", "21", "34", "55", "89", "?", "☕");
    }

    public void addPlayer(Player player) {
        players.put(player.getId(), player);
    }

    public void removePlayer(String playerId) {
        players.remove(playerId);
    }

    public boolean allPlayersVoted() {
        if (players.isEmpty()) {
            return false;
        }
        return players.values().stream().allMatch(Player::isHasVoted);
    }

    public void revealVotes() {
        this.votingRevealed = true;
    }

    public void resetVoting() {
        this.votingRevealed = false;
        this.currentStory = "";
        players.values().forEach(Player::resetVote);
    }

    public Map<String, Integer> getVoteResults() {
        Map<String, Integer> results = new HashMap<>();
        for (Player player : players.values()) {
            if (player.getCurrentVote() != null && !player.getCurrentVote().equals("?")) {
                String vote = player.getCurrentVote();
                results.put(vote, results.getOrDefault(vote, 0) + 1);
            }
        }
        return results;
    }

    public Player getHost() {
        return players.values().stream()
                .filter(Player::isHost)
                .findFirst()
                .orElse(null);
    }
    
    // Explicit getters and setters for compatibility
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Map<String, Player> getPlayers() { return players; }
    public void setPlayers(Map<String, Player> players) { this.players = players; }
    public boolean isVotingRevealed() { return votingRevealed; }
    public void setVotingRevealed(boolean votingRevealed) { this.votingRevealed = votingRevealed; }
    public String getCurrentStory() { return currentStory; }
    public void setCurrentStory(String currentStory) { this.currentStory = currentStory; }
    public List<String> getCardSet() { return cardSet; }
    public void setCardSet(List<String> cardSet) { this.cardSet = cardSet; }
}
