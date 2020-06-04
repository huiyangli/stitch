package edu.utarlington.pigeon.daemon.util;

import java.io.*;

public class Utils {
    /*Output txt file*/
    public static void writeToLocalFile(String path, String content){
        BufferedWriter output = null;
        try {
            File file = new File(path);
            output = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file, true), "utf-8"));
            output.write(content+"\r\n");
            output.close();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }
}
