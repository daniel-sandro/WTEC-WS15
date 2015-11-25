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
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.*;
import java.util.stream.Collectors;

public class OnlineController extends Controller {
    @Inject
    private static JedisPool jedisPool;
    private static Map<WebSocket.Out<JsonNode>, User> onlineUsers = new HashMap<>();

    public WebSocket<JsonNode> socket() {
        final Http.Session session = session();

        return new WebSocket<JsonNode>() {
            @Override
            public void onReady(In<JsonNode> in, Out<JsonNode> out) {
                try {
                    User u = Application.getLocalUser(session);
                    Logger.debug("User " + u.id + " connected");
                    synchronized (onlineUsers) {
                        onlineUsers.put(out, u);
                    }
                    try (Jedis j = jedisPool.getResource()) {
                        j.sadd("online_users", Long.toString(u.id));
                    }
                } catch (RuntimeException e) {
                    Logger.debug("Unknown user connected");
                }

                in.onClose(() -> {
                    User u = onlineUsers.get(out);
                    if (u != null) {
                        Logger.debug("User " + u.id + " disconnected");
                        synchronized (onlineUsers) {
                            onlineUsers.remove(out);
                        }
                        try (Jedis j = jedisPool.getResource()) {
                            j.srem("online_users", Long.toString(u.id));
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
