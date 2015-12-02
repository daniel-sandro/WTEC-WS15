package controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import de.htwg.battleship.model.Position;
import javafx.util.Pair;
import models.User;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.WebSocket;
import redis.clients.jedis.JedisPool;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class OnlineController extends Controller {
    @Inject
    private static JedisPool jedisPool;
    private static ConcurrentMap<WebSocket.Out<JsonNode>, User> onlineUsers = new ConcurrentHashMap<>();
    private static Random rnd = new Random();
    private static AtomicLong gameSequence = new AtomicLong();
    private static ConcurrentMap<Long, Pair<User, User>> ongoingGames = new ConcurrentHashMap<>();

    public WebSocket<JsonNode> socket() {
        final Http.Session session = session();
        final User currentUser = Application.getLocalUser(session());

        return new WebSocket<JsonNode>() {
            @Override
            public void onReady(In<JsonNode> in, Out<JsonNode> out) {
                try {
                    User u = Application.getLocalUser(session);
                    Logger.debug("User " + u.id + " connected");

                    // Add the new user to the data structures
                    boolean alreadyOnline = onlineUsers.containsValue(u);
                    onlineUsers.put(out, u);
                    /*try (Jedis j = jedisPool.getResource()) {
                        j.sadd("online_users", Long.toString(u.id));
                    }*/

                    // Notify logged users about the new player
                    if (!alreadyOnline) {
                        ObjectMapper mapper = new ObjectMapper();
                        ObjectNode notification = JsonNodeFactory.instance.objectNode();
                        notification.put("action", "newuser");
                        notification.put("newuser", mapper.writeValueAsString(currentUser));
                        for (WebSocket.Out<JsonNode> ws : onlineUsers.keySet()) {
                            if (ws != out) {
                                ws.write(notification);
                            }
                        }
                    }
                } catch (RuntimeException e) {
                    Logger.debug("Unknown user connected");
                } catch (JsonProcessingException e) {
                    Logger.error(e.getMessage(), e);
                }

                in.onMessage((data) -> {
                    String action = data.findPath("action").textValue();
                    switch (action) {
                        // TODO: add error handling
                        case "newgame":
                            newGameResponse(data, currentUser);
                            break;
                        case "clickfield":
                            clickFieldResponse(data, currentUser);
                            break;
                        default:
                    }
                });

                in.onClose(() -> {
                    User u = onlineUsers.get(out);
                    if (u != null) {
                        Logger.debug("User " + u.id + " disconnected");

                        // Remove user from the data structures
                        synchronized (onlineUsers) {
                            onlineUsers.remove(out);
                        }
                        boolean stillOnline = onlineUsers.containsValue(u);
                        /*try (Jedis j = jedisPool.getResource()) {
                            j.srem("online_users", Long.toString(u.id));
                        }*/

                        // Notify logged users about the leaving player
                        if (!stillOnline) {
                            try {
                                ObjectMapper mapper = new ObjectMapper();
                                ObjectNode notification = JsonNodeFactory.instance.objectNode();
                                notification.put("action", "userleaves");
                                notification.put("leavinguser", mapper.writeValueAsString(currentUser));
                                for (WebSocket.Out<JsonNode> ws : onlineUsers.keySet()) {
                                    ws.write(notification);
                                }
                            } catch (JsonProcessingException e) {
                                Logger.error(e.getMessage(), e);
                            }
                        }
                    } else {
                        Logger.debug("Unknown user disconnected");
                    }
                });
            }
        };
    }

    public static Set<User> getOnlineUsers() {
        return new HashSet<>(onlineUsers.values());
    }

    public static boolean isOnline(User user) {
        return onlineUsers.containsValue(user);
    }

    public static void notifyNewGame(User currentUser, User user) {
        // List of sockets that belong to the user
        List<WebSocket.Out<JsonNode>> sockets = getUserSockets(user);
        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode gameRequest = JsonNodeFactory.instance.objectNode();
            gameRequest.put("action", "newgame");
            gameRequest.put("opponent", mapper.writeValueAsString(currentUser));
            long gameId = gameSequence.addAndGet(rnd.nextInt());
            gameRequest.put("gameid", gameId);
            ongoingGames.put(gameId, new Pair<>(currentUser, user));
            for(WebSocket.Out<JsonNode> out : sockets) {
                out.write(gameRequest);
            }
        } catch (JsonProcessingException e) {
            Logger.error(e.getMessage(), e);
        }
    }

    private static void newGameResponse(JsonNode data, User askedUser) {
        boolean response = data.findPath("response").asBoolean();
        long gameId = data.findPath("gameid").asLong();
        User askingUser = ongoingGames.get(gameId).getKey();
        // Notify to the user who started the game
        if (response) {
            ObjectNode gameAccepted = JsonNodeFactory.instance.objectNode();
            gameAccepted.put("action", "newgame_response");
            gameAccepted.put("response", true);
            gameAccepted.put("gameid", gameId);
            for (WebSocket.Out<JsonNode> out : getUserSockets(askingUser)) {
                out.write(gameAccepted);
            }
        } else {
            ObjectNode gameRejected = JsonNodeFactory.instance.objectNode();
            gameRejected.put("action", "newgame_response");
            gameRejected.put("response", false);
            for (WebSocket.Out<JsonNode> out : getUserSockets(askingUser)) {
                out.write(gameRejected);
            }
        }
        // TODO: something else?
    }

    private static void clickFieldResponse(JsonNode data, User currentUser) {
        long gameId = data.findPath("gameid").asLong();
        Pair<User, User> players = ongoingGames.get(gameId);
        if (players != null) {
            int row = data.findPath("row").asInt();
            int col = data.findPath("col").asInt();
            Position p = new Position(row, col);
            User opponent = players.getKey().equals(currentUser) ? players.getValue() : players.getKey();
            try {
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode shootField = JsonNodeFactory.instance.objectNode();
                shootField.put("action", "shoot_field");
                shootField.put("gameid", gameId);
                shootField.put("position", mapper.writeValueAsString(p));
                for (WebSocket.Out<JsonNode> out : getUserSockets(opponent)) {
                    out.write(shootField);
                }
            } catch (JsonProcessingException e) {
                Logger.error(e.getMessage(), e);
            }
        } else {
            // Return some error
        }
    }

    public static Pair<User, User> getPlayers(long gameId) {
        return ongoingGames.get(gameId);
    }

    /**
     * Returns a list of sockets that belong to the given user.
     * @param u The user connected to the sockets.
     * @return A list of sockets liked to the user.
     */
    private static List<WebSocket.Out<JsonNode>> getUserSockets(User u) {
        List<WebSocket.Out<JsonNode>> sockets = onlineUsers
                .entrySet()
                .stream()
                .filter(entry -> Objects.equals(entry.getValue(), u))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        return sockets;
    }

    /**
     * Checks if a User is currently playing.
     * @param u The user.
     * @return 0 if the user is currently not playing or the long id of the game if he is currently playing
     */
    public static long isCurrentlyPlaying(User u) {
        for (Long l : ongoingGames.keySet()) {
            Pair p = ongoingGames.get(l);
            if (p.getKey().equals(u) || p.getValue().equals(u)) {
                return l;
            }
        }
        return 0;
    }
}
