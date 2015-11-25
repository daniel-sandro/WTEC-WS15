package controllers;

import com.feth.play.module.pa.user.AuthUser;
import com.google.inject.Inject;
import models.User;
import play.Logger;
import play.mvc.Controller;
import play.mvc.WebSocket;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class OnlineController extends Controller {
    @Inject
    private JedisPool jedisPool;
    private Map<WebSocket<String>, User> members = new HashMap<>();

    public WebSocket<String> socket() {
        return WebSocket.whenReady((in, out) -> {
            Logger.debug("Client connected");

            in.onMessage((authSerial) -> {
                byte[] data = Base64.getDecoder().decode(authSerial);
                try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
                    AuthUser auth = (AuthUser) ois.readObject();
                    User u = User.findByAuthUserIdentity(auth);
                    synchronized (members) {
                        members.put(this.socket(), u);
                    }
                    try (Jedis j = jedisPool.getResource()) {
                        j.sadd("online_users", Long.toString(u.id));
                    }
                }
            });

            in.onClose(() -> {
                User u = members.get(this.socket());
                Logger.debug("User " + u.id + " disconnected");
                synchronized (members) {
                    members.remove(this.socket());
                }
                try (Jedis j = jedisPool.getResource()) {
                    j.srem("online_users", Long.toString(u.id));
                }
            });

            out.write("Server ready");
        });

    }
}
