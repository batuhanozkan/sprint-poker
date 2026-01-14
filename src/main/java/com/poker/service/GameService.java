package com.poker.service;

import com.poker.model.GameRoom;
import com.poker.model.Player;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {
    private final Map<String, GameRoom> rooms = new ConcurrentHashMap<>();
    private final Map<String, Player> players = new ConcurrentHashMap<>();

    public GameRoom createRoom(String roomName, String hostName) {
        GameRoom room = new GameRoom(roomName);
        Player host = new Player(hostName, room.getId(), true);
        room.addPlayer(host);
        players.put(host.getId(), host);
        rooms.put(room.getId(), room);
        return room;
    }

    public GameRoom joinRoom(String roomId, String playerName) {
        GameRoom room = rooms.get(roomId);
        if (room == null) {
            throw new RuntimeException("Room not found");
        }
        Player player = new Player(playerName, roomId, false);
        room.addPlayer(player);
        players.put(player.getId(), player);
        return room;
    }

    public void leaveRoom(String playerId) {
        Player player = players.get(playerId);
        if (player != null) {
            GameRoom room = rooms.get(player.getRoomId());
            if (room != null) {
                room.removePlayer(playerId);
                if (room.getPlayers().isEmpty()) {
                    rooms.remove(room.getId());
                } else {
                    // If host left, assign new host
                    if (player.isHost() && !room.getPlayers().isEmpty()) {
                        Player newHost = room.getPlayers().values().iterator().next();
                        newHost.setHost(true);
                    }
                }
            }
            players.remove(playerId);
        }
    }

    public void vote(String playerId, String vote) {
        Player player = players.get(playerId);
        if (player != null) {
            player.vote(vote);
        }
    }

    public GameRoom getRoom(String roomId) {
        return rooms.get(roomId);
    }

    public Player getPlayer(String playerId) {
        return players.get(playerId);
    }

    public void revealVotes(String roomId) {
        GameRoom room = rooms.get(roomId);
        if (room != null) {
            room.revealVotes();
        }
    }

    public void resetVoting(String roomId, String story) {
        GameRoom room = rooms.get(roomId);
        if (room != null) {
            room.setCurrentStory(story);
            room.resetVoting();
        }
    }
}
