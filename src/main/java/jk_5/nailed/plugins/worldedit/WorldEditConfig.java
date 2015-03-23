package jk_5.nailed.plugins.worldedit;

import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.session.SessionManager;
import com.sk89q.worldedit.world.snapshot.SnapshotRepository;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.HashSet;

public class WorldEditConfig extends LocalConfiguration {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public void load() {
        File file = new File("settings.conf");
        logger.info("Loading config");
        if(!file.exists() || file.length() == 0){
            ReadableByteChannel in = null;
            FileChannel out = null;
            try{
                in = Channels.newChannel(WorldEditConfig.class.getResourceAsStream("/reference.conf"));
                out = new FileOutputStream(file).getChannel();
                out.transferFrom(in, 0, Long.MAX_VALUE);
            }catch(Exception e){
                logger.error("Was not able to load WorldEdit config", e);
            }finally{
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
        }
        Config defaults = ConfigFactory.defaultReference().withOnlyPath("worldedit");
        Config conf = ConfigFactory.parseFile(file).withFallback(defaults);
        Config config;
        try{
            config = conf.getConfig("worldedit");
        }catch(Exception e){
            logger.warn("Failed to load config, using defaults", e);
            config = defaults.getConfig("worldedit");
        }

        profile = config.getBoolean("profile");
        wandItem = config.getInt("wand-item");
        defaultChangeLimit = Math.max(-1, config.getInt("limits.max-blocks-changed.default"));
        maxChangeLimit = Math.max(-1, config.getInt("limits.max-blocks-changed.maximum"));
        defaultMaxPolygonalPoints = Math.max(-1, config.getInt("limits.max-polygonal-points.default"));
        maxPolygonalPoints = Math.max(-1,config.getInt("limits.max-polygonal-points.maximum"));
        defaultMaxPolyhedronPoints = Math.max(-1, config.getInt("limits.max-polyhedron-points.default"));
        maxPolyhedronPoints = Math.max(-1, config.getInt("limits.max-polyhedron-points.maximum"));
        maxRadius = Math.max(-1, config.getInt("limits.max-radius"));
        maxBrushRadius = config.getInt("limits.max-brush-radius");
        maxSuperPickaxeSize = Math.max(1, config.getInt("limits.max-super-pickaxe-size"));
        butcherDefaultRadius = Math.max(-1, config.getInt("limits.butcher-radius.default"));
        butcherMaxRadius = Math.max(-1, config.getInt("limits.butcher-radius.maximum"));
        disallowedBlocks = new HashSet<Integer>(config.getIntList("limits.disallowed-blocks"));
        allowedDataCycleBlocks = new HashSet<Integer>(config.getIntList("limits.allowed-data-cycle-blocks"));
        registerHelp = config.getBoolean("register-help");
        logCommands = config.getBoolean("logging.log-commands");
        logFile = config.getString("logging.file");
        superPickaxeDrop = config.getBoolean("super-pickaxe.drop-items");
        superPickaxeManyDrop = config.getBoolean("super-pickaxe.many-drop-items");
        noDoubleSlash = config.getBoolean("no-double-slash");
        useInventory = config.getBoolean("use-inventory.enable");
        useInventoryOverride = config.getBoolean("use-inventory.allow-override");
        useInventoryCreativeOverride = config.getBoolean("use-inventory.creative-mode-overrides");
        navigationWand = config.getInt("navigation-wand.item");
        navigationWandMaxDistance = config.getInt("navigation-wand.max-distance");
        navigationUseGlass = config.getBoolean("navigation.use-glass");
        scriptTimeout = config.getInt("scripting.timeout");
        scriptsDir = config.getString("scripting.dir");
        saveDir = config.getString("saving.dir");
        allowSymlinks = config.getBoolean("files.allow-symbolic-links");

        LocalSession.MAX_HISTORY_SIZE = Math.max(0, config.getInt("history.size"));
        SessionManager.EXPIRATION_GRACE = config.getInt("history.expiration") * 60 * 1000;

        showHelpInfo = config.getBoolean("show-help-on-first-use");

        String snapshotsDir = config.getString("snapshots.directory");
        if(!snapshotsDir.isEmpty()){
            snapshotRepo = new SnapshotRepository(snapshotsDir);
        }

        String typ = config.getString("shell-save-type").trim();
        shellSaveType = typ.isEmpty() ? null : typ;
    }
}
