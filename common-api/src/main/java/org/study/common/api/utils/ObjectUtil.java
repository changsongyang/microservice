package org.study.common.api.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

/**
 * 对象操作工具类
 * @author chenyf
 * @date 2018-12-15
 */
public class ObjectUtil {

    public static byte[] objectToByteArray(Object obj){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(obj);
            out.flush();
            return bos.toByteArray();
        } catch (IOException ex){
            return null;
        } finally {
            try {
                if(bos != null){
                    bos.close();
                }
            } catch (IOException ex) { }

            try {
                if(out != null){
                    out.close();
                }
            } catch (IOException ex) { }

        }
    }

}
