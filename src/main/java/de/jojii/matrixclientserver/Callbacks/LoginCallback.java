package de.jojii.matrixclientserver.Callbacks;

import java.io.IOException;

import de.jojii.matrixclientserver.Bot.LoginData;

public interface LoginCallback {

    void onResponse(LoginData data) throws IOException;
}
