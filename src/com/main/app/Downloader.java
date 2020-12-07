package com.main.app;

import com.main.app.utils.FileLogger;
import com.main.app.utils.TerminalCMD;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.*;
import java.util.Properties;

public class Downloader {

    static String configFile = "config.properties";

    static String headersFile = "headers.properties";

    static String fileToSavePath = "file.save.path";

    static String fileNameProp = "file.name";

    static String postCompleteCommands = "post.complete.command";

    static String downloadURL = "download.url";

    static String range = "range";

    static Properties properties = new Properties();
    static Properties headerProperties = new Properties();

    static TerminalCMD terminalCMD;

    public static void main(String[] args) {

        if (args.length > 0) {
            configFile = args[0];
            headersFile = args[1];
        }

        try {
            loadProperties();
            initDirs();
            terminalCMD = new TerminalCMD(true, new FileLogger(properties.getProperty(fileToSavePath) + "/log.txt"));
            terminalCMD.log("Download Started");
            download();
            terminalCMD.log("Download Complete");
            terminalCMD.run(properties.getProperty(postCompleteCommands));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void download() throws Exception {
        String baseDir = properties.getProperty(fileToSavePath);
        String fileName = baseDir + "/" + properties.getProperty(fileNameProp);

        long startByte = 0L;
        long totalByte = Long.parseLong(headerProperties.getProperty(range).split("-")[1]);

        File file = new File(fileName);
        if (file.exists()) {
            startByte = file.length() + 1;
        } else startByte = 0;
        saveFileFromResource(fileName,startByte,totalByte);
    }


    private static void saveFileFromResource(String fileName, long startByte, long totalByte) throws Exception {
        InputStream stream = buildAndExecuteRequest();
        FileOutputStream fos = new FileOutputStream(fileName, true);
        int data;
        long count = startByte;
        while ((data = stream.read()) != -1) {
            fos.write(data);
            count++;
            logProgress(count,totalByte);
        }
        fos.close();
    }

    private static InputStream buildAndExecuteRequest() throws Exception {
        CloseableHttpClient client = HttpClients.custom().build();
        HttpUriRequest request = RequestBuilder.get()
                .setUri(properties.getProperty(downloadURL))
                .build();

        for (Object headerKey : headerProperties.keySet()) {
            request.setHeader(headerKey.toString(), headerProperties.getProperty(headerKey.toString()));
        }

        HttpResponse response = client.execute(request);

        if (response.getStatusLine().getStatusCode() >=300) {
            throw new Exception("File download failed response code" + response.getStatusLine().getStatusCode() + " URL:" + properties.getProperty(downloadURL));
        }

        HttpEntity entity = response.getEntity();
        return entity.getContent();
    }

    private static void logProgress(long completedByte, long totalByte){
        double progress = (Double.valueOf(completedByte)/Double.valueOf(totalByte))  * 100D;
        terminalCMD.log("[DOWNLOAD] COMPLETED : "+formatDecimal(progress,2)+" %",true);
    }


    private static String formatDecimal(double decimalValue, int roundDigit) {
        double roundOff = Math.round(decimalValue * Math.pow(10, roundDigit)) / Math.pow(10, roundDigit);
        return roundOff+"";
    }

    private static void loadProperties() throws IOException {
        FileInputStream fis = new FileInputStream(configFile);
        properties.load(fis);
        fis = new FileInputStream(headersFile);
        headerProperties.load(fis);
    }

    private static void initDirs(){
        String fileDirs = properties.getProperty(fileToSavePath);
        File file = new File(fileDirs);
        if(!file.isDirectory()){
            file.mkdir();
        }
    }


}
