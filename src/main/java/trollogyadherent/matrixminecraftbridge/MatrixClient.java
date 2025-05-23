package trollogyadherent.matrixminecraftbridge;

import java.io.IOException;
import java.util.List;

import org.json.JSONObject;

import de.jojii.matrixclientserver.Bot.Client;
import de.jojii.matrixclientserver.Bot.Events.RoomEvent;
import de.jojii.matrixclientserver.Bot.Member;
import de.jojii.matrixclientserver.Callbacks.MemberCallback;
import de.jojii.matrixclientserver.Callbacks.RoomEventsCallback;
import de.jojii.matrixclientserver.File.FileHelper;

public class MatrixClient {

    public Client client;
    private static MatrixClient instance;
    private MinecraftMessager minecraftMessagerInstance;
    private Config configInstance;
    private String roomId;

    private MatrixClient() {

    }

    public void onReady() {}

    public void connect() {
        configInstance = Config.getInstance();
        if (configInstance.getConfigData() == null) {
            MatrixMinecraftBridge.LOG.warn("Incorrect or missing config file!");
            return;
        }
        Client c = new Client(
            configInstance.getConfigData()
                .getHost());
        client = c;
        try {
            if (configInstance.getConfigData()
                .getPassword()
                .trim()
                .length() > 0) {
                final ConfigData finalConfigData = configInstance.getConfigData();

                c.login(
                    configInstance.getConfigData()
                        .getUsername(),
                    configInstance.getConfigData()
                        .getPassword(),
                    data -> {
                        if (data.isSuccess()) {

                            JSONObject newConfig = new JSONObject();
                            newConfig.put("host", finalConfigData.getHost());
                            newConfig.put("username", finalConfigData.getUsername());
                            newConfig.put("password", "");
                            newConfig.put("usertoken", data.getAccess_token());
                            newConfig.put("roomid", finalConfigData.getRoomId());
                            newConfig.put("startupMessage", finalConfigData.getStartupMessage());
                            newConfig.put("stopMessage", finalConfigData.getStopMessage());
                            newConfig.put("joinMessage", finalConfigData.getJoinMessage());
                            newConfig.put("leaveMessage", finalConfigData.getLeaveMessage());
                            newConfig.put("deathMessage", finalConfigData.getDeathMessage());
                            newConfig.put("achievementMessage", finalConfigData.getAchievementMessage());
                            // save new configfile
                            FileHelper.writeFile(MatrixMinecraftBridge.CONFIGFILELOCATION, newConfig.toString(1));

                            clientLoggedIn(c);
                        } else {
                            System.err.println("error logging in! Check your credentials");
                        }
                    });
            } else {
                c.login(
                    configInstance.getConfigData()
                        .getToken(),
                    data -> { clientLoggedIn(c); });
            }
        } catch (Exception e) {
            MatrixMinecraftBridge.LOG.error("Something went wrong while connecting to matrix");
        }
    }

    private void clientLoggedIn(Client c) {
        if (configInstance.getConfigData() == null) {
            MatrixMinecraftBridge.LOG.error("Warning! Incorrect or missing config file!");
            return;
        }
        MatrixMinecraftBridge.LOG.info("LOGGED IN MATRIX");
        RoomEventsCallback event = new RoomEventsCallback() {

            @Override
            public void onEventReceived(List<RoomEvent> roomEvents) throws IOException {
                for (RoomEvent event : roomEvents) {
                    // MatrixMinecraftBridge.LOG.debug(event.getRaw().toString());

                    if (event.getType()
                        .equals("m.room.member")
                        && event.getContent()
                            .has("membership")
                        && event.getContent()
                            .getString("membership")
                            .equals("invite")) {
                        try {
                            // make bot autojoin
                            c.joinRoom(event.getRoom_id(), null);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (event.getType()
                        .equals("m.room.message")
                        && event.getRoom_id()
                            .equals(
                                configInstance.getConfigData()
                                    .getRoomId())) {
                                        // MatrixMinecraftBridge.LOG.debug("RECEIVED MESSAGE EVENT");
                                        if (event.getSender()
                                            .equals(
                                                c.getLoginData()
                                                    .getUser_id())) {
                                            // MatrixMinecraftBridge.LOG.debug("Ignoring own message");
                                            return;
                                        }
                                        // Sends a readreceipt for every received message
                                        try {
                                            c.sendReadReceipt(event.getRoom_id(), event.getEvent_id(), "m.read", null);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        if (event.getContent()
                                            .has("body")) {
                                            String msg = RoomEvent.getBodyFromMessageEvent(event);
                                            if (msg != null && msg.trim()
                                                .length() > 0) {
                                                if (minecraftMessagerInstance == null) {
                                                    minecraftMessagerInstance = MinecraftMessager.getInstance();
                                                }
                                                MemberCallback memberCallback = new MemberCallback() {

                                                    @Override
                                                    public void onResponse(List<Member> list) throws IOException {
                                                        String message = "[" + event.getSender() + "] " + msg;
                                                        for (Member member : list) {
                                                            if (member.getId()
                                                                .equals(event.getSender())) {
                                                                message = "[" + member.getDisplay_name() + "] " + msg;
                                                                break;
                                                            }
                                                        }
                                                        minecraftMessagerInstance.sendToMinecraft(message);
                                                    }
                                                };
                                                c.getRoomMembers(event.getRoom_id(), memberCallback);
                                            }
                                        }
                                    }
                }
            }
        };
        c.registerRoomEventListener(event);
    }

    public static MatrixClient getInstance() {
        if (instance == null) {
            instance = new MatrixClient();
        }
        return instance;
    }

    public void sendToMatrix(String message) {
        if (configInstance.getConfigData() == null) {
            MatrixMinecraftBridge.LOG.warn("Incorrect or missing config file!");
            return;
        }
        // MatrixMinecraftBridge.LOG.debug("received message to send: " + message);
        if (client == null) {
            MatrixMinecraftBridge.LOG.warn("Client is null!");
            return;
        }
        if (roomId == null) {
            if (configInstance == null) {
                configInstance = Config.getInstance();
            }
            roomId = configInstance.getConfigData()
                .getRoomId();
        }
        if (roomId == null) {
            MatrixMinecraftBridge.LOG.warn("Room ID not set!");
            return;
        }
        try {
            client.sendText(roomId, message, null);
        } catch (Exception e) {
            MatrixMinecraftBridge.LOG.warn("Failed to send message to room!");
        }
    }
}
