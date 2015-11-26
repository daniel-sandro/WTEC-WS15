package controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import models.User;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.WebSocket;
import redis.clients.jedis.JedisPool;

import java.util.*;
import java.util.stream.Collectors;

public class OnlineController extends Controller {
    @Inject
    private static JedisPool jedisPool;
    private static Map<WebSocket.Out<JsonNode>, User> onlineUsers = new HashMap<>();

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
                    synchronized (onlineUsers) {
                        onlineUsers.put(out, u);
                    }
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
        List<WebSocket.Out<JsonNode>> sockets = onlineUsers
                .entrySet()
                .stream()
                .filter(entry -> Objects.equals(entry.getValue(), user))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode gameRequest = JsonNodeFactory.instance.objectNode();
            gameRequest.put("action", "newgame");
            gameRequest.put("oponent", mapper.writeValueAsString(currentUser));
            for(WebSocket.Out<JsonNode> out : sockets) {
                out.write(gameRequest);
            }
        } catch (JsonProcessingException e) {
            Logger.error(e.getMessage(), e);
        }
    }
}
