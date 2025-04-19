package de.jojii.matrixclientserver.Callbacks;

import java.io.IOException;
import java.util.List;

import de.jojii.matrixclientserver.Bot.Events.RoomEvent;

public interface RoomEventsCallback {

    void onEventReceived(List<RoomEvent> roomEvent) throws IOException;
}
