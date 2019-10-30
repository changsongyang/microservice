package org.study.demo.design.pattern.chain;

import java.io.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileLogger extends AbstractLogger {
    private FileOutputStream outputStream;

    public FileLogger(int level, String logName) {
        this.level = level;
        try{
            File file = new File(logName);
            if(file.exists()){
                file.delete();
            }

            outputStream = new FileOutputStream(file, true);
        }catch(Exception e){

        }
    }

    @Override
    protected void write(String message) {
        try{
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            message = format.format(new Date()) + " " + message + "\r\n";
            byte[] data = message.getBytes(Charset.forName("utf-8"));
            outputStream.write(data,0, data.length);
            outputStream.flush();
//            outputStream.close();
        }catch(Exception e){

        }
    }
}
