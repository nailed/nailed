package jk_5.nailed.server.map.game.script;

import java.io.IOException;

public interface IMountedFileText extends IMountedFile {

    public String readLine() throws IOException;
}
