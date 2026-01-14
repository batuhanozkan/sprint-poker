package com.poker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player {
    private String id;
    private String name;
    private String roomId;
    private boolean isHost;
    private String currentVote;
    private boolean hasVoted;

    public Player(String name, String roomId, boolean isHost) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.roomId = roomId;
        this.isHost = isHost;
        this.currentVote = null;
        this.hasVoted = false;
    }

    public void vote(String vote) {
        this.currentVote = vote;
        this.hasVoted = true;
    }

    public void resetVote() {
        this.currentVote = null;
        this.hasVoted = false;
    }
    
    // Explicit getters and setters for compatibility
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public boolean isHost() { return isHost; }
    public void setHost(boolean host) { isHost = host; }
    public String getCurrentVote() { return currentVote; }
    public void setCurrentVote(String currentVote) { this.currentVote = currentVote; }
    public boolean isHasVoted() { return hasVoted; }
    public void setHasVoted(boolean hasVoted) { this.hasVoted = hasVoted; }
}
