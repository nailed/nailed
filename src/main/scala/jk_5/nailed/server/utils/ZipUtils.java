package jk_5.nailed.server.utils;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipUtils {

    private static final Logger logger = LogManager.getLogger();

    public static void extract(File input, File destination){
        try{
            ZipFile zipFile = new ZipFile(input);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while(entries.hasMoreElements()){
                ZipEntry entry = entries.nextElement();
                File entryDestination = new File(destination,  entry.getName());
                entryDestination.getParentFile().mkdirs();
                if(entry.isDirectory()){
                    entryDestination.mkdirs();
                }else{
                    InputStream in = zipFile.getInputStream(entry);
                    OutputStream out = new FileOutputStream(entryDestination);
                    IOUtils.copy(in, out);
                    IOUtils.closeQuietly(in);
                    IOUtils.closeQuietly(out);
                }
            }
        }catch(IOException e){
            logger.warn("Failed to extract " + input.getAbsolutePath() + " to " + destination.getAbsolutePath(), e);
        }
    }
}
