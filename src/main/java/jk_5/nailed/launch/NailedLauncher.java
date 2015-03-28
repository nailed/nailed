package jk_5.nailed.launch;

import javax.annotation.Nullable;
import java.io.File;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

public final class NailedLauncher {

    @Nullable
    private static File gameDir;

    private NailedLauncher() {
    }

    public static void initialize(File gameDir) {
        NailedLauncher.gameDir = requireNonNull(gameDir, "gameDir");
    }

    public static File getGameDirectory() {
        checkState(gameDir != null, "Nailed was not initialized");
        return gameDir;
    }
}
