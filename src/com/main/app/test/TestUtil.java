package com.main.app.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class TestUtil {

    static String REPLACE_PROPERTIES = "replace.properties";
    static String SPLITTER = ";";


    public static void main(String[] args) throws IOException {
        FileInputStream fis = new FileInputStream("config.properties");
        Properties properties = new Properties();
        properties.load(fis);
        replaceDynamicProperties(properties);
        for (Object key : properties.keySet()) {
            System.out.println(key.toString()+"="+properties.get(key));
        }
    }

    private static void replaceDynamicProperties(Properties properties) {
        String[] dynamicProperties = properties.getProperty(REPLACE_PROPERTIES).split(SPLITTER);
        properties.remove(REPLACE_PROPERTIES);
        for (String str : dynamicProperties) {
            String quotedStr = "{" + str + "}";
            for (Object key : properties.keySet()) {
                String value = properties.get(key).toString();
                value = value.replace(quotedStr, properties.getProperty(str));
                properties.setProperty(key.toString(),value);
            }
        }
    }
}
