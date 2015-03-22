package jk_5.nailed.server.map.game.script;

import java.io.IOException;
import java.io.InputStream;

public abstract class IMountedFileBinary extends InputStream implements IMountedFile {

    public abstract int read() throws IOException;
}
