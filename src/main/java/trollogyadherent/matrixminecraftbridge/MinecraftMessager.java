package trollogyadherent.matrixminecraftbridge;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

public class MinecraftMessager {

    private static MinecraftMessager instance;
    private Util utilInstance;

    public MinecraftMessager() {

    }

    public void sendToMinecraft(String message) {
        if (utilInstance == null) {
            utilInstance = Util.getInstance();
        }
        // MatrixMinecraftBridge.LOG.debug("GOT MATRIX MESSAGE: " + message);
        ChatComponentText componentMessage = utilInstance.getChatCompWithLinks(message);
        MinecraftServer minecraftServer = MinecraftServer.getServer();
        if (minecraftServer == null) {
            return;
        }
        final List<EntityPlayerMP> players = new ArrayList<>();
        minecraftServer.getConfigurationManager().playerEntityList.forEach(playerEntity -> {
            if (playerEntity instanceof EntityPlayerMP) {
                players.add((EntityPlayerMP) playerEntity);
            }
        });
        for (EntityPlayerMP player : players) {
            player.addChatMessage(componentMessage);// new ChatComponentText(message.getFormattedTextMinecraft()));
        }
    }

    public static MinecraftMessager getInstance() {
        if (instance == null) {
            instance = new MinecraftMessager();
        }
        return instance;
    }
}
