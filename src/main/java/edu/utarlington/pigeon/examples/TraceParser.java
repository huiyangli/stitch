package edu.utarlington.pigeon.examples;

import edu.utarlington.pigeon.daemon.util.Utils;
import org.apache.commons.cli.*;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

public class TraceParser {

    private static final String TARGET_FILE_IN_TAR = "RequestInfoMaster.txt";
    private static HashMap<String, String> requestID2LatencyMS = new HashMap<String, String>();

    public static void main(String[] args) {
        Options options = new Options();

        Option input = new Option("f", "input-file", true, "Input file path: contains all .tar.gz trace files");
        input.setRequired(true);
        options.addOption(input);

        Option showRequestID = new Option("s", "show-id", false, "Write first column as request ID");
        showRequestID.setRequired(false);
        options.addOption(showRequestID);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);

            System.exit(1);
        }

        String inputFilePath = cmd.getOptionValue("f");
        TraceParser traceParser = new TraceParser();

        try {
            traceParser.parse(inputFilePath, TARGET_FILE_IN_TAR, cmd.hasOption("s"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final Logger LOG = Logger.getLogger(TraceParser.class);
    /**
     * Collect all information form all .tar.gz file under the path, neglecting all files other than the target in all compressed files
     * @param dir
     * @param target
     */
    public void parse(String dir, String target, boolean showRequestID) throws IOException {

        //1. read all files under dir
        File path = new File(dir);
        File[] files = path.listFiles();
        for (File file : files) {
            LOG.debug(file.getPath());
            if(!file.isFile() || !file.getPath().endsWith(".tar.gz")) continue; //currently only deal with .tar.gz file existing in level 1 depth

            String content = readGzipFile(file.getPath(), target);
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            new ByteArrayInputStream(content.getBytes(Charset.forName("utf8"))), Charset.forName("utf8")));
            String line;
            while ( (line = br.readLine()) != null ) {
               LOG.debug("read: " + line);
               String[] data = line.split(" ");
               String requestID = data[1];
               String latencyMS = data[4].substring(0, data[4].length() - 2);
               LOG.debug("Parsed requestID: " + requestID + ", latency in milliseconds: " + latencyMS);

                String updated = requestID2LatencyMS.get(requestID) == null
                        ? (showRequestID ? requestID + " : " + latencyMS : latencyMS)
                        : requestID2LatencyMS.get(requestID) + " " + latencyMS;
                requestID2LatencyMS.put(requestID, updated);
            }
        }

        //write output
        for (String out : requestID2LatencyMS.values()) {
            Utils.writeToLocalFile("perf_metrics_master_ms.txt", out);
        }
    }

    private String readGzipFile(String targzFile, String target){
        FileInputStream fis = null;
        ArchiveInputStream tarIn = null;
        BufferedInputStream bufferedInputStream = null;
        StringBuilder sb = new StringBuilder();
        try {
            fis = new FileInputStream(targzFile);
            GZIPInputStream is = new GZIPInputStream(new BufferedInputStream(fis));
            tarIn = new ArchiveStreamFactory().createArchiveInputStream("tar", is);
            bufferedInputStream = new BufferedInputStream(tarIn);

            TarArchiveEntry entry = null;
            while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
                long size = 0;
                String name = entry.getName();
                String[] names = name.split("/");
                String fileName = targzFile;
                String t = targzFile;

                for (int i = 0; i < names.length; i++) {
                    String str = names[i];
                    fileName = fileName + File.separator + str;
                    if(i != names.length -1) t = t + File.separator + str;
                }

                t = t + File.separator + target;
                if(!t.equals(fileName)) continue; //only deal with the target file

                if (!fileName.endsWith("/")) {
                    size = entry.getSize();
                }

                if (size > 0) {
                    StringBuffer line = new StringBuffer();
                    byte[] b = new byte[(int) entry.getSize()];
                    int len = 0;
                    while ((len = tarIn.read(b)) != -1) {
                        line.append(new String(b, 0, len, "utf-8"));
                    }

                    sb.append(line.toString());
                }
            }

            tarIn.close();
            fis.close();
            bufferedInputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }
}

