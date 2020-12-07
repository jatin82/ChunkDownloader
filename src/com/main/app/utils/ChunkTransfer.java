package com.main.app.utils;

import java.io.*;

public class ChunkTransfer {

    public static void main(String[] args) throws IOException {
        String src = "src-file-path";
        String dest = "dest-file-path";
        int limit = 342252662;
        transfer(src,dest,limit);
    }

    public static void transfer(String src, String dest, int limit) throws IOException {
        File srcFile = new File(src);
        byte [] bytes = new byte[limit];

        FileInputStream fis = new FileInputStream(src);
        FileOutputStream fos = new FileOutputStream(dest);

        fis.read(bytes,0,bytes.length);
        fos.write(bytes);

    }

    private static String formatDecimal(double decimalValue, int roundDigit) {
        double roundOff = Math.round(decimalValue * Math.pow(10, roundDigit)) / Math.pow(10, roundDigit);
        return roundOff+"";
    }
}
