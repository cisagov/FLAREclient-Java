package com.bcmc.xor.flare.client.api.security;

import com.bcmc.xor.flare.client.api.domain.auth.User;
import com.bcmc.xor.flare.client.api.repository.UserRepository;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xor.flare.utils.crypto.PasswordUtil;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class could use javadoc
 */
public class ServerCredentialsUtils {

    private static final Logger log = LoggerFactory.getLogger(ServerCredentialsUtils.class);

    private static ServerCredentialsUtils instance;

    private final Map<String, Map<String, String>> serverCredentialsMap = new HashMap<>();

    private ServerCredentialsUtils() {

    }

    public static ServerCredentialsUtils getInstance() {
        if (instance == null) {
            synchronized (ServerCredentialsUtils.class) {
                if (instance == null) {
                    instance = new ServerCredentialsUtils();
                }
            }
        }
        return instance;
    }

    public Map<String, Map<String, String>> getServerCredentialsMap() {
        return serverCredentialsMap;
    }

    public static String encryptBasicAuthCredentials(String username, String password, String encryptionKey) {
        return PasswordUtil.getEncryptedPassword(generateBasicAuthHeader(username, password), encryptionKey);
    }

    public static String generateBasicAuthHeader(String username, String password) {
        return "Basic " + new String(generateBasicAuthString(username, password));
    }

    private static byte[] generateBasicAuthString(String username, String password) {
        String auth = username + ":" + password;
        return Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
    }

    /**
     * key? user's encrypted password? user's decrypted password?
     */
    public void addCredentialsForUserToMemory(Map<String, String> encryptedServerCredentialsMap, String user, String key) {
        Map<String, String> serverCredentialsForUser = new HashMap<>();
        if (serverCredentialsMap.containsKey(user)) {
            serverCredentialsForUser = serverCredentialsMap.get(user);
        }

        for (Map.Entry<String, String> entry: encryptedServerCredentialsMap.entrySet()) {
            serverCredentialsForUser.put(entry.getKey(), PasswordUtil.getPlainTextPassword(entry.getValue(), key));
        }

        serverCredentialsMap.put(user, serverCredentialsForUser);
    }

    public void loadAllCredentials(UserRepository userRepository) {
        List<User> users = userRepository.findAll();
        for (User user: users) {
            loadCredentialsForUser(user);
        }
    }

    public void loadCredentialsForUser(User user) {
        if (user == null) {
            return;
        }

        if (!user.getActivated()) {
            return;
        }

        if (user.getServerCredentials() != null && !user.getServerCredentials().isEmpty()) {
            log.debug("Loading credentials for '{}'", user.getLogin());
            ServerCredentialsUtils.getInstance().addCredentialsForUserToMemory(user.getServerCredentials(), user.getLogin(), user.getPassword());
        }
    }

    public void clearCredentialsForUser(String login) {
        getServerCredentialsMap().remove(login);
    }

    public void clearCredentialsForUser(User user) {
        getServerCredentialsMap().remove(user.getLogin());
    }

    public static void setInstance(ServerCredentialsUtils instance) {
        ServerCredentialsUtils.instance = instance;
    }
}
