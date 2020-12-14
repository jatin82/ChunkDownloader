package com.main.app;

import com.main.app.utils.FileLogger;
import com.main.app.utils.TerminalCMD;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.*;
import java.util.Properties;

public class Downloader {

    public static String BASE_PATH = "";

    static String configFile = BASE_PATH + "config.properties";

    static String headersFile = BASE_PATH + "headers.properties";

    static String fileToSavePath = "file.save.path";

    static String fileNameProp = "file.name";

    static String postCompleteCommands = "post.complete.command";

    static String downloadURL = "download.url";

    static String range = "range";

    static String START = "startByte";

    static String REPLACE_PROPERTIES = "replace.properties";

    static String SPLITTER = ";";

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

        if (properties.getProperty(START).isEmpty()) {
            File file = new File(fileName);
            if (file.exists()) {
                startByte = file.length();
            } else startByte = 0;
        } else {
            startByte = Long.parseLong(properties.getProperty(START));
        }

        updateStartByte(startByte);
        saveFileFromResource(fileName, startByte, totalByte);
    }


    private static void updateStartByte(long startByte) {
        String rangeValue = headerProperties.getProperty(range);
        rangeValue = rangeValue.replace("{startByte}", startByte + "");
        terminalCMD.log("Updating headers with startValue :" + rangeValue);
        headerProperties.setProperty(range, rangeValue);
    }

    private static void saveFileFromResource(String fileName, long startByte, long totalByte) throws Exception {
        InputStream stream = buildAndExecuteRequest2();
        FileOutputStream fos = new FileOutputStream(fileName, true);
        int data;
        long count = startByte;
        while ((data = stream.read()) != -1) {
            fos.write(data);
            count++;
            logProgress(count, totalByte);
        }
        fos.close();
    }

    private static void logResponse(InputStream stream) throws Exception {
        int data;
        while ((data = stream.read()) != -1) {
            terminalCMD.rawLog(String.valueOf(data));
        }
    }

    // TODO: headers are only passed while creating request this method fails to do that
    private static InputStream buildAndExecuteRequest2() throws Exception {
        HttpClient client = HttpClients.custom().build();
        RequestBuilder requestBuilder = RequestBuilder.get()
                .setUri(properties.getProperty(downloadURL));

        for (Object headerKey : headerProperties.keySet()) {
            requestBuilder.setHeader(headerKey.toString(), headerProperties.getProperty(headerKey.toString()));
        }
        HttpUriRequest request = requestBuilder.build();
//        for (Header header : request.getAllHeaders()) {
//            System.out.println(header.getElements()[0].toString());
//        }

        HttpResponse response = client.execute(request);

        if (response.getStatusLine().getStatusCode() >= 300) {
            throw new Exception("File download failed response code" + response.getStatusLine().getStatusCode() + " URL:" + properties.getProperty(downloadURL));
        }

        HttpEntity entity = response.getEntity();
        return entity.getContent();
    }


    private static InputStream buildAndExecuteRequest() throws Exception {
        CloseableHttpClient client = HttpClients.custom().build();
        HttpUriRequest request = RequestBuilder.get()
                .setUri(properties.getProperty(downloadURL))
                .setHeader("range", headerProperties.getProperty("range"))
                .build();

        HttpResponse response = client.execute(request);

        if (response.getStatusLine().getStatusCode() >= 300) {
            //logResponse(response.getEntity().getContent());
            throw new Exception("File download failed response code" + response.getStatusLine().getStatusCode() + " URL:" + properties.getProperty(downloadURL));
        }

        HttpEntity entity = response.getEntity();
        return entity.getContent();
    }

    private static void logProgress(long completedByte, long totalByte) {
        double progress = (Double.valueOf(completedByte) / Double.valueOf(totalByte)) * 100D;
        terminalCMD.log("[DOWNLOAD] COMPLETED : " + formatDecimal(progress, 2) + " %", true);
    }


    private static String formatDecimal(double decimalValue, int roundDigit) {
        double roundOff = Math.round(decimalValue * Math.pow(10, roundDigit)) / Math.pow(10, roundDigit);
        return roundOff + "";
    }

    private static void loadProperties() throws IOException {
        FileInputStream fis = new FileInputStream(configFile);
        properties.load(fis);
        fis = new FileInputStream(headersFile);
        headerProperties.load(fis);
        replaceDynamicProperties(properties);
        replaceDynamicProperties(headerProperties);
    }

    private static void replaceDynamicProperties(Properties properties) {
        String[] dynamicProperties = properties.getProperty(REPLACE_PROPERTIES).split(SPLITTER);
        properties.remove(REPLACE_PROPERTIES);
        for (String str : dynamicProperties) {
            String quotedStr = "{" + str + "}";
            for (Object key : properties.keySet()) {
                String value = properties.get(key).toString();
                value = value.replaceAll(quotedStr, properties.getProperty(str));
                properties.setProperty(key.toString(), value);
            }
        }
    }

    private static void initDirs() {
        String fileDirs = properties.getProperty(fileToSavePath);
        File file = new File(fileDirs);
        if (!file.isDirectory()) {
            file.mkdir();
        }
    }

}
