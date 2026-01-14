package com.poker.controller;

import com.poker.model.GameRoom;
import com.poker.model.Message;
import com.poker.model.Player;
import com.poker.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Controller
public class WebSocketController {

    @Autowired
    private GameService gameService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/room/create")
    public void createRoom(Message message) {
        try {
            GameRoom room = gameService.createRoom(message.getRoomId(), message.getPlayerName());
            Player player = room.getPlayers().values().stream()
                    .filter(p -> p.getName().equals(message.getPlayerName()))
                    .findFirst()
                    .orElse(null);

            if (player != null) {
                Message response = new Message();
                response.setType(Message.MessageType.ROOM_UPDATE);
                response.setRoomId(room.getId());
                response.setPlayerId(player.getId());
                response.setData(buildRoomData(room));
                messagingTemplate.convertAndSend("/topic/room/" + room.getId(), response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Message error = new Message();
            error.setType(Message.MessageType.ERROR);
            error.setData(e.getMessage());
            // Broadcast error to all clients in case playerId is not available
            messagingTemplate.convertAndSend("/topic/errors", error);
        }
    }

    @MessageMapping("/room/join")
    public void joinRoom(Message message) {
        try {
            // Eğer playerId gönderilmişse, o player'ı kullan
            if (message.getPlayerId() != null) {
                Player player = gameService.getPlayer(message.getPlayerId());
                GameRoom room = gameService.getRoom(message.getRoomId());
                
                if (player != null && room != null) {
                    Message response = new Message();
                    response.setType(Message.MessageType.PLAYER_JOINED);
                    response.setRoomId(room.getId());
                    response.setPlayerId(player.getId());
                    response.setPlayerName(player.getName());
                    response.setData(buildRoomData(room));
                    messagingTemplate.convertAndSend("/topic/room/" + room.getId(), response);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Message error = new Message();
            error.setType(Message.MessageType.ERROR);
            error.setData(e.getMessage());
            // Send error to room topic if roomId is available
            if (message.getRoomId() != null) {
                messagingTemplate.convertAndSend("/topic/room/" + message.getRoomId(), error);
            } else {
                messagingTemplate.convertAndSend("/topic/errors", error);
            }
        }
    }

    @MessageMapping("/room/leave")
    public void leaveRoom(Message message) {
        try {
            Player player = gameService.getPlayer(message.getPlayerId());
            if (player != null) {
                String roomId = player.getRoomId();
                gameService.leaveRoom(message.getPlayerId());
                
                GameRoom room = gameService.getRoom(roomId);
                if (room != null) {
                    Message response = new Message();
                    response.setType(Message.MessageType.PLAYER_LEFT);
                    response.setRoomId(roomId);
                    response.setPlayerName(message.getPlayerName());
                    response.setData(buildRoomData(room));
                    messagingTemplate.convertAndSend("/topic/room/" + roomId, response);
                }
            }
        } catch (Exception e) {
            Message error = new Message();
            error.setType(Message.MessageType.ERROR);
            error.setData(e.getMessage());
            messagingTemplate.convertAndSend("/queue/error/" + message.getPlayerId(), error);
        }
    }

    @MessageMapping("/vote")
    public void vote(Message message) {
        try {
            gameService.vote(message.getPlayerId(), message.getVote());
            Player player = gameService.getPlayer(message.getPlayerId());
            if (player != null) {
                GameRoom room = gameService.getRoom(player.getRoomId());
                if (room != null) {
                    Message response = new Message();
                    response.setType(Message.MessageType.ROOM_UPDATE);
                    response.setRoomId(room.getId());
                    response.setData(buildRoomData(room));
                    messagingTemplate.convertAndSend("/topic/room/" + room.getId(), response);
                }
            }
        } catch (Exception e) {
            Message error = new Message();
            error.setType(Message.MessageType.ERROR);
            error.setData(e.getMessage());
            messagingTemplate.convertAndSend("/queue/error/" + message.getPlayerId(), error);
        }
    }

    @MessageMapping("/reveal")
    public void revealVotes(Message message) {
        try {
            gameService.revealVotes(message.getRoomId());
            GameRoom room = gameService.getRoom(message.getRoomId());
            if (room != null) {
                Message response = new Message();
                response.setType(Message.MessageType.REVEAL_VOTES);
                response.setRoomId(room.getId());
                response.setData(buildRoomData(room));
                messagingTemplate.convertAndSend("/topic/room/" + room.getId(), response);
            }
        } catch (Exception e) {
            Message error = new Message();
            error.setType(Message.MessageType.ERROR);
            error.setData(e.getMessage());
            messagingTemplate.convertAndSend("/queue/error/" + message.getPlayerId(), error);
        }
    }

    @MessageMapping("/reset")
    public void resetVoting(Message message) {
        try {
            gameService.resetVoting(message.getRoomId(), message.getStory());
            GameRoom room = gameService.getRoom(message.getRoomId());
            if (room != null) {
                Message response = new Message();
                response.setType(Message.MessageType.RESET_VOTING);
                response.setRoomId(room.getId());
                response.setData(buildRoomData(room));
                messagingTemplate.convertAndSend("/topic/room/" + room.getId(), response);
            }
        } catch (Exception e) {
            Message error = new Message();
            error.setType(Message.MessageType.ERROR);
            error.setData(e.getMessage());
            messagingTemplate.convertAndSend("/queue/error/" + message.getPlayerId(), error);
        }
    }

    private Map<String, Object> buildRoomData(GameRoom room) {
        Map<String, Object> data = new HashMap<>();
        data.put("room", room);
        // Players'ı liste olarak gönder
        data.put("players", new java.util.ArrayList<>(room.getPlayers().values()));
        data.put("voteResults", room.getVoteResults());
        data.put("allVoted", room.allPlayersVoted());
        return data;
    }
}
