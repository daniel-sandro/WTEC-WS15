package controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import de.htwg.battleship.controller.HumanController;
import de.htwg.battleship.model.Position;
import de.htwg.battleship.model.Ship;
import de.htwg.battleship.model.ship.Destructor;
import de.htwg.battleship.model.ship.Flattop;
import de.htwg.battleship.model.ship.Rowboat;
import javafx.util.Pair;
import models.PlayBattleshipHuman;
import models.User;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.WebSocket;
import redis.clients.jedis.JedisPool;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class OnlineController extends Controller {
    // TODO: players can only play one game at a time
    private static final int NUM_THREADS = 8;
    @Inject
    private static JedisPool jedisPool;
    private static Random rnd = new Random();
    private static ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
    private static AtomicLong gameSequence = new AtomicLong();
    private static ConcurrentMap<WebSocket.Out<JsonNode>, User> onlineUsers = new ConcurrentHashMap<>();
    private static ConcurrentMap<Long, Pair<User, User>> requestedGames = new ConcurrentHashMap<>();
    private static ConcurrentMap<Long, PlayBattleshipController> ongoingGames = new ConcurrentHashMap<>();

    public WebSocket<JsonNode> socket() {
        final Http.Session session = session();
        final User currentUser = Application.getLocalUser(session());

        return new WebSocket<JsonNode>() {
            @Override
            public void onReady(In<JsonNode> in, Out<JsonNode> out) {
                try {
                    User u = Application.getLocalUser(session);
                    //Logger.debug("User " + u.id + " connected");

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
                        broadcastMessage(notification, new HashSet<User>() {{ add(u); }});
                    }
                } catch (RuntimeException e) {
                    //Logger.debug("Unknown user connected");
                } catch (JsonProcessingException e) {
                    Logger.error(e.getMessage(), e);
                }

                in.onMessage((data) -> {
                    Logger.debug(currentUser.id + " - " + data.toString());
                    String action = data.findPath("action").textValue();
                    switch (action) {
                        // TODO: add error handling
                        case "newgame":
                            onNewGameResponse(data, currentUser);
                            break;
                        case "ready":
                            onUserReady(data, currentUser);
                            break;
                        case "setrowboat":
                            onSetShip(data, currentUser, new Rowboat());
                            break;
                        case "setdestructor":
                            onSetShip(data, currentUser, new Destructor());
                            break;
                        case "setflattop":
                            onSetShip(data, currentUser, new Flattop());
                            break;
                        case "shoot":
                            onShoot(data, currentUser);
                            break;
                        case "userleaves":
                            onUserLeaves(data, currentUser);
                            break;
                        case "getOnlineUsers":
                            onGetOnlineUsers(data, currentUser);
                            break;
                        default:
                    }
                });

                in.onClose(() -> {
                    User u = onlineUsers.get(out);
                    if (u != null) {
                        //Logger.debug("User " + u.id + " disconnected");

                        // Remove user from the data structures
                        onlineUsers.remove(out);
                        // Wait for 100 ms to see if the user connects through another websocket
                        //Thread.sleep(100);
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
                                broadcastMessage(notification, new HashSet<User>() {{ add(u); }});
                            } catch (JsonProcessingException e) {
                                Logger.error(e.getMessage(), e);
                            }
                        }
                    } else {
                        //Logger.debug("Unknown user disconnected");
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
        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode gameRequest = JsonNodeFactory.instance.objectNode();
            gameRequest.put("action", "newgame");
            gameRequest.put("opponent", mapper.writeValueAsString(currentUser));
            //long gameId = gameSequence.addAndGet((long) rnd.nextInt());
            // TODO: randomize
            long gameId = gameSequence.incrementAndGet();
            gameRequest.put("gameid", gameId);
            requestedGames.put(gameId, new Pair<>(currentUser, user));
            sendMessage(user, gameRequest);
        } catch (JsonProcessingException e) {
            Logger.error(e.getMessage(), e);
        }
    }

    private static void onNewGameResponse(JsonNode data, User askedUser) {
        boolean response = data.findPath("response").asBoolean();
        long gameId = data.findPath("gameid").asLong();
        if (requestedGames.containsKey(gameId)) {
            User askingUser = requestedGames.remove(gameId).getKey();
            if (response) {
                // Setup the controller and start the game
                PlayBattleshipHuman askingPlayer = new PlayBattleshipHuman(askingUser);
                PlayBattleshipHuman askedPlayer = new PlayBattleshipHuman(askedUser);
                PlayBattleshipController gameController = new PlayBattleshipController(askingPlayer, askedPlayer);
                ongoingGames.put(gameId, gameController);
                executor.submit(() -> {
                    try {
                        gameController.ready.acquire(2);
                        gameController.startGame();
                        ongoingGames.remove(gameId);
                        User user1 = gameController.getPlayers().getKey().getUser();
                        User user2 = gameController.getPlayers().getValue().getUser();
                        try {
                            // Broadcast to the rest of users
                            ObjectMapper mapper = new ObjectMapper();
                            ObjectNode notPlayingAnymore = JsonNodeFactory.instance.objectNode();
                            notPlayingAnymore.put("action", "not_playing_anymore");
                            notPlayingAnymore.put("user1", mapper.writeValueAsString(user1));
                            notPlayingAnymore.put("user2", mapper.writeValueAsString(user2));
                            broadcastMessage(notPlayingAnymore, new HashSet<User>() {{ add(user1); add(user2); }});
                        } catch (JsonProcessingException e) {
                            Logger.error(e.getMessage(), e);
                        }
                    } catch (InterruptedException e) {
                        Logger.error(e.getMessage(), e);
                    }
                });

                // Notify to the user who started the game
                ObjectNode gameAccepted = JsonNodeFactory.instance.objectNode();
                gameAccepted.put("action", "newgame_response");
                gameAccepted.put("response", true);
                gameAccepted.put("gameid", gameId);
                sendMessage(askingUser, gameAccepted);

                // TODO: extract code to reuse
                try {
                    // Broadcast to the rest of users
                    ObjectMapper mapper = new ObjectMapper();
                    ObjectNode currentlyPlaying = JsonNodeFactory.instance.objectNode();
                    currentlyPlaying.put("action", "currently_playing");
                    currentlyPlaying.put("user1", mapper.writeValueAsString(askedUser));
                    currentlyPlaying.put("user2", mapper.writeValueAsString(askingUser));
                    broadcastMessage(currentlyPlaying, new HashSet<User>() {{ add(askedUser); }});
                } catch (JsonProcessingException e) {
                    Logger.error(e.getMessage(), e);
                }
            } else {
                // Notify to the user who started the game
                ObjectNode gameRejected = JsonNodeFactory.instance.objectNode();
                gameRejected.put("action", "newgame_response");
                gameRejected.put("response", false);
                sendMessage(askingUser, gameRejected);
            }
        } else {
            // TODO: return error
        }
    }

    private static void onUserReady(JsonNode data, User currentUser) {
        long gameId = data.findPath("gameid").asLong();
        PlayBattleshipController gameController = ongoingGames.get(gameId);
        if (gameController != null) {
            gameController.ready.release();
        } else {
            // TODO: return error
        }
    }

    private static void onGetOnlineUsers(JsonNode data, User currentUser) {
        Set<User> users = getOnlineUsers();
        ObjectNode msg = JsonNodeFactory.instance.objectNode();
        ArrayNode userList = JsonNodeFactory.instance.arrayNode();
        for (User u : users) {
            ObjectNode user = JsonNodeFactory.instance.objectNode();
            user.put("id", u.id);
            user.put("name", u.name);
            user.put("currentGame", u.getCurrentGame());
            if (!u.name.equals(currentUser.name)) {
                userList.add(user);
            }
        }

        msg.put("action", "onlineUsers_response");
        msg.put("response", userList);
        sendMessage(currentUser, msg);
    }

    private static void onSetShip(JsonNode data, User currentUser, Ship s) {
        long gameId = data.findPath("gameid").asLong();
        PlayBattleshipController gameController = ongoingGames.get(gameId);
        if (gameController != null) {
            int row = data.findPath("row").asInt();
            int col = data.findPath("col").asInt();
            Position p = new Position(row, col);
            boolean horizontal = data.findPath("horizontal").asBoolean();
            HumanController playerController = gameController.getPlayer(currentUser).getController();
            playerController.placeShip(s, p, horizontal);
        } else {
            // TODO: throw error
        }
    }

    private static void onShoot(JsonNode data, User currentUser) {
        long gameId = data.findPath("gameid").asLong();
        PlayBattleshipController gameController = ongoingGames.get(gameId);
        if (gameController != null) {
            int row = data.findPath("row").asInt();
            int col = data.findPath("col").asInt();
            Position p = new Position(row, col);
            HumanController playerController = gameController.getPlayer(currentUser).getController();
            playerController.shoot(p);
        }
    }

    public static void onUserLeaves(JsonNode data, User currentUser) {
        long gameId = data.findPath("gameid").asLong();
        PlayBattleshipController gameController = ongoingGames.get(gameId);
        if (gameController != null) {
            ongoingGames.remove(gameId);
            User opponent = gameController.getOpponent(currentUser).getUser();
            ObjectNode notification = JsonNodeFactory.instance.objectNode();
            notification.put("action", "opponentleft");
            sendMessage(opponent, notification);

            try {
                // Broadcast to the rest of users
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode notPlayingAnymore = JsonNodeFactory.instance.objectNode();
                notPlayingAnymore.put("action", "not_playing_anymore");
                notPlayingAnymore.put("user1", mapper.writeValueAsString(currentUser));
                notPlayingAnymore.put("user2", mapper.writeValueAsString(opponent));
                broadcastMessage(notPlayingAnymore, new HashSet<User>() {{ add(currentUser); }});
            } catch (JsonProcessingException e) {
                Logger.error(e.getMessage(), e);
            }
        }
    }

    public static Pair<PlayBattleshipHuman, PlayBattleshipHuman> getPlayers(long gameId) {
        PlayBattleshipController controller = ongoingGames.get(gameId);
        if (controller != null) {
            return controller.getPlayers();
        } else {
            return null;
        }
    }

    protected static void sendMessage(User u, JsonNode msg) {
        Logger.debug("Sending to " + u.id + " - " + msg.toString());
        Set<WebSocket.Out<JsonNode>> sockets = getUserSockets(u);
        for (WebSocket.Out<JsonNode> out : sockets) {
            out.write(msg);
        }
    }

    protected static void broadcastMessage(JsonNode msg, Set<User> excluded) {
        // TODO: untested
        if (excluded.isEmpty()) {
            Logger.debug("Broadcasting " + msg.toString());
        } else {
            Logger.debug(excluded
                    .stream()
                    .map(u -> u.id.toString())
                    .collect(Collectors.joining(", ", "Broadcasting except to {", "} - " + msg.toString())));
        }
        Set<WebSocket.Out<JsonNode>> targets = onlineUsers
                .entrySet()
                .stream()
                .filter(entry -> !excluded.contains(entry.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        for (WebSocket.Out<JsonNode> out : targets) {
            out.write(msg);
        }
    }

    /**
     * Returns a list of sockets that belong to the given user.
     * @param u The user connected to the sockets.
     * @return A list of sockets liked to the user.
     */
    private static Set<WebSocket.Out<JsonNode>> getUserSockets(User u) {
        Set<WebSocket.Out<JsonNode>> sockets = onlineUsers
                .entrySet()
                .stream()
                .filter(entry -> Objects.equals(entry.getValue(), u))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        return sockets;
    }

    /**
     * Checks if a User is currently playing.
     * @param u The user.
     * @return The ID of the game he's currently playing or null if the user isn't playing any game.
     */
    public static Long getCurrentGame(User u) {
        Map.Entry<Long, PlayBattleshipController> entry = ongoingGames
                .entrySet()
                .stream()
                .filter(e -> e.getValue().getPlayer(u) != null)
                .findFirst()
                .orElse(null);
        return entry == null ? null : entry.getKey();
    }

}
