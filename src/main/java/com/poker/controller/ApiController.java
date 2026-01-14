package com.poker.controller;

import com.poker.model.GameRoom;
import com.poker.model.Player;
import com.poker.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private GameService gameService;

    @PostMapping("/room/create")
    public ResponseEntity<Map<String, Object>> createRoom(@RequestBody Map<String, String> request) {
        try {
            String roomName = request.get("roomName");
            String playerName = request.get("playerName");
            
            if (roomName == null || playerName == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "roomName ve playerName gerekli"));
            }
            
            GameRoom room = gameService.createRoom(roomName, playerName);
            Player host = room.getHost();
            
            Map<String, Object> response = new HashMap<>();
            response.put("roomId", room.getId());
            response.put("roomName", room.getName());
            response.put("playerId", host != null ? host.getId() : null);
            response.put("isHost", true);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/room/join")
    public ResponseEntity<Map<String, Object>> joinRoom(@RequestBody Map<String, String> request) {
        try {
            String roomId = request.get("roomId");
            String playerName = request.get("playerName");
            
            if (roomId == null || playerName == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "roomId ve playerName gerekli"));
            }
            
            GameRoom room = gameService.joinRoom(roomId, playerName);
            Player player = room.getPlayers().values().stream()
                    .filter(p -> p.getName().equals(playerName))
                    .findFirst()
                    .orElse(null);
            
            Map<String, Object> response = new HashMap<>();
            response.put("roomId", room.getId());
            response.put("roomName", room.getName());
            response.put("playerId", player != null ? player.getId() : null);
            response.put("isHost", false);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<Map<String, Object>> getRoom(@PathVariable String roomId) {
        try {
            GameRoom room = gameService.getRoom(roomId);
            if (room == null) {
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("room", room);
            response.put("players", room.getPlayers().values());
            response.put("voteResults", room.getVoteResults());
            response.put("allVoted", room.allPlayersVoted());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
