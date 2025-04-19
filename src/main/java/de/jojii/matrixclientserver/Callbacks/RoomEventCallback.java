package de.jojii.matrixclientserver.Callbacks;

import java.io.IOException;

import de.jojii.matrixclientserver.Bot.Events.RoomEvent;

public interface RoomEventCallback {

    void onEventReceived(RoomEvent roomEvent) throws IOException;
}
