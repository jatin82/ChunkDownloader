package com.main.app.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class FileLogger {

    private BufferedWriter writer;

    private File file;

    private String clearLine;

    private int lastOutSize;

    public FileLogger(String fileName) throws IOException {
        file = new File(fileName);
        writer = new BufferedWriter(new FileWriter(file));
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<100;i++){
            sb.append(' ');
        }
        clearLine = sb.append('\r').toString();
        lastOutSize = 0;
    }


    public void write(String str,boolean isClose) throws IOException {
        writer.write(str);
        if(isClose) close();
    }

    public void log(String str,boolean... isSingleLine) throws IOException {
        if(isSingleLine.length>0 && isSingleLine[0]) {
            str+="\r";
        }
        else {
            writer.write(new Date().toString()+" :: "+str+"\n");
            str+="\n";
        }
        if(lastOutSize > str.length()) System.out.print(clearLine);
        System.out.print(new Date().toString()+" :: "+str);
        lastOutSize = str.length();
    }

    public void rawLog(String str) throws IOException {
        str = new Date().toString()+" :: "+str;
        writer.write(str);
        System.out.print(str);
    }

    public void close() throws IOException {
        writer.close();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        this.close();
    }

}