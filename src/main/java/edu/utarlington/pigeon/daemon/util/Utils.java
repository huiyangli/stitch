package edu.utarlington.pigeon.daemon.util;

import java.io.*;

public class Utils {
    private final static int MOD = 31;
    private final static int BASE = 1000000;

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

    public static double hashCode(String requestId, String taskId) {
        if(requestId == null || taskId == null) throw new IllegalArgumentException("Input string can't be null");

        StringBuilder sb = new StringBuilder();
        sb.append(taskId);
        sb.append('.');
        for (int i = requestId.length() - 1; i >= 0; i--) {
            char c = requestId.charAt(i);
            if(!Character.isDigit(c)) break;
            sb.append(c);
        }
//        int code = hashCode(sb.toString());
        double code = Double.valueOf(sb.toString());
        return code;
    }

    public static int hashCode(String str) {
        long code = 0;
        for (int i = str.length() - 1; i >= 0; i--) {
            char c = str.charAt(i);
            if(!Character.isDigit(c)) break;

            code = (code *  MOD + c - '0') % BASE;
        }

        return (int) code;
    }

    public static boolean isFirstRequest(String requestId) {
        int i = requestId.length() - 1;
       if(requestId.charAt(i) == '0' && requestId.charAt(i - 1) == '_') return true;

       return false;
    }
}
