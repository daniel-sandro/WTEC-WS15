package controllers;

import com.google.inject.Inject;
import models.User;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.WebSocket;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.HashMap;
import java.util.Map;

public class OnlineController extends Controller {
    @Inject
    private JedisPool jedisPool;
    private Map<WebSocket<String>, User> members = new HashMap<>();

    public WebSocket<String> socket() {
        final Http.Session session = session();

        return new WebSocket<String>() {
            @Override
            public void onReady(In<String> in, Out<String> out) {
                try {
                    User u = Application.getLocalUser(session);
                    Logger.debug("User " + u.id + " connected");
                    synchronized (members) {
                        members.put(this, u);
                    }
                    try (Jedis j = jedisPool.getResource()) {
                        j.sadd("online_users", Long.toString(u.id));
                    }
                } catch (RuntimeException e) {
                    Logger.debug("Unknown user connected");
                }

                WebSocket<String> thisSocket = this;

                in.onClose(() -> {
                    User u = members.get(thisSocket);
                    if (u != null) {
                        Logger.debug("User " + u.id + " disconnected");
                        synchronized (members) {
                            members.remove(thisSocket);
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
}
