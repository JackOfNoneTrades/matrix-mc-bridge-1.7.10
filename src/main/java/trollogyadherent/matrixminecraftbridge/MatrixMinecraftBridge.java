package trollogyadherent.matrixminecraftbridge;

import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;

@Mod(
    modid = MatrixMinecraftBridge.MODID,
    name = MatrixMinecraftBridge.NAME,
    version = Tags.VERSION,
    acceptableRemoteVersions = "*")
public class MatrixMinecraftBridge {

    public static final String MODID = "matrixminecraftbridge";
    public static final String NAME = "Matrix Minecraft Bridge 1.7.10";
    public static final String VERSION = Tags.VERSION;
    public static final String CONFIGFILELOCATION = "config/matrix-bridge.json";
    public static final Logger LOG = LogManager.getLogger(MODID);

    private Config configInstance;
    private MatrixClient matrixClientInstance;

    private MinecraftListener minecraftListener = new MinecraftListener();

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(minecraftListener);
        FMLCommonHandler.instance()
            .bus()
            .register(minecraftListener);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        configInstance = Config.getInstance();
        if (configInstance.getConfigData() == null) {
            LOG.warn("Incorrect or missing config file!");
            return;
        }
        MatrixMinecraftBridge.LOG.debug(
            configInstance.getConfigData()
                .getHost());
        matrixClientInstance = MatrixClient.getInstance();
        matrixClientInstance.connect();
    }

    @Mod.EventHandler
    public void onServerStarted(FMLServerStartedEvent event) {
        if (configInstance == null) {
            configInstance = Config.getInstance();
        }
        if (configInstance.getConfigData() == null) {
            LOG.warn("Incorrect or missing config file!");
            return;
        }
        MatrixClient.getInstance()
            .sendToMatrix(
                configInstance.getConfigData()
                    .getStartupMessage());
    }

    @Mod.EventHandler
    public void onServerStopped(FMLServerStoppedEvent event) {
        if (configInstance == null) {
            configInstance = Config.getInstance();
        }
        if (configInstance.getConfigData() == null) {
            LOG.warn("Incorrect or missing config file!");
            return;
        }
        MatrixClient.getInstance()
            .sendToMatrix(
                configInstance.getConfigData()
                    .getStopMessage());
    }
}
