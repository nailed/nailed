package jk_5.nailed.server.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class LoggerOutputStream extends ByteArrayOutputStream {

    private static final String separator = System.getProperty("line.separator");
    private final Logger logger;
    private final Level level;

    public LoggerOutputStream(Logger logger, Level level) {
        this.logger = logger;
        this.level = level;
    }

    @Override
    public synchronized void flush() throws IOException {
        super.flush();
        String record = this.toString();
        super.reset();
        if(record.length() > 0 && !record.equals(separator)){
            logger.log(level, record);
        }
    }
}
