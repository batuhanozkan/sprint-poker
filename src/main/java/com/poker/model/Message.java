package com.poker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private MessageType type;
    private String roomId;
    private String playerId;
    private String playerName;
    private String vote;
    private String story;
    private Object data;

    public enum MessageType {
        JOIN_ROOM,
        LEAVE_ROOM,
        CREATE_ROOM,
        PLAYER_JOINED,
        PLAYER_LEFT,
        VOTE,
        REVEAL_VOTES,
        RESET_VOTING,
        ROOM_UPDATE,
        ERROR
    }
    
    // Explicit getters and setters for compatibility
    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public String getVote() { return vote; }
    public void setVote(String vote) { this.vote = vote; }
    public String getStory() { return story; }
    public void setStory(String story) { this.story = story; }
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
}
