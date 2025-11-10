package me.miki.shindo.utils;

import com.mojang.authlib.Agent;
import com.mojang.authlib.AuthenticationService;
import com.mojang.authlib.UserAuthentication;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.util.UUIDTypeAdapter;
import me.miki.shindo.injection.interfaces.IMixinMinecraft;
import me.miki.shindo.libs.openauth.microsoft.MicrosoftAuthResult;
import me.miki.shindo.libs.openauth.microsoft.MicrosoftAuthenticationException;
import me.miki.shindo.libs.openauth.microsoft.MicrosoftAuthenticator;
import me.miki.shindo.logger.ShindoLogger;
import me.miki.shindo.management.account.Account;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

import java.util.UUID;

public class SessionUtils {

    private static SessionUtils instance;
    private final Minecraft mc = Minecraft.getMinecraft();
    private final UserAuthentication auth;

    //Creates a new Authentication Service.
    private SessionUtils() {
        UUID notSureWhyINeedThis = UUID.randomUUID(); //Idk, needs a UUID. Seems to be fine making it random
        AuthenticationService authService = new YggdrasilAuthenticationService(Minecraft.getMinecraft().getProxy(), notSureWhyINeedThis.toString());
        auth = authService.createUserAuthentication(Agent.MINECRAFT);
        authService.createMinecraftSessionService();
    }

    public static SessionUtils getInstance() {
        if (instance == null) {
            instance = new SessionUtils();
        }

        return instance;
    }

    //Online mode
    //Checks if your already loggin in to the account.
    public void setUser(String email, String password) {
        if (!Minecraft.getMinecraft().getSession().getUsername().equals(email) || Minecraft.getMinecraft().getSession().getToken().equals("0")) {

            this.auth.logOut();
            this.auth.setUsername(email);
            this.auth.setPassword(password);
            try {
                this.auth.logIn();
                Session session = new Session(this.auth.getSelectedProfile().getName(), UUIDTypeAdapter.fromUUID(auth.getSelectedProfile().getId()), this.auth.getAuthenticatedToken(), this.auth.getUserType().getName());
                setSession(session);
            } catch (Exception e) {
                ShindoLogger.error("Failed to login with the provided credentials.");
            }
        }

    }

    public MicrosoftAuthResult loginMicrosoftAccount(Account account) throws MicrosoftAuthenticationException {
        MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator();
        if (account.getRefreshToken() == null || account.getRefreshToken().isEmpty()) {
            throw new MicrosoftAuthenticationException("Missing Microsoft refresh token");
        }

        MicrosoftAuthResult result = authenticator.loginWithRefreshToken(account.getRefreshToken());

        applyMicrosoftAuthResult(result);
        account.setName(result.getProfile().getName());
        account.setUuid(result.getProfile().getId());
        account.setRefreshToken(result.getRefreshToken());

        return result;
    }

    public void applyMicrosoftAuthResult(MicrosoftAuthResult result) {
        ((IMixinMinecraft) mc).setSession(new Session(result.getProfile().getName(), result.getProfile().getId(), result.getAccessToken(), "legacy"));
    }

    //Sets the session.
    //You need to make this public, and remove the final modifier on the session Object.
    private void setSession(Session session) {
        ((IMixinMinecraft) mc).setSession(session);
    }

    //Login offline mode
    //Just like MCP does
    public void setUserOffline(String username) {
        this.auth.logOut();
        Session session = new Session(username, username, "0", "legacy");
        setSession(session);
    }

}
