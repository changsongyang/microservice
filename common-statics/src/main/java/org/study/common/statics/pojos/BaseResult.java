package org.study.common.statics.pojos;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

/**
 * @Description:
 * @author: chenyf
 * @Date: 2018/2/3
 */
public class BaseResult<T> implements Serializable {
    private static final long serialVersionUID = -1894984868783727889L;

    /**
     * 返回的数据
     */
    private T data;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    /**
     * 计算当前 data 的长度
     * @return
     */
    public int length(){
        return BaseResult.calcDataLength(this.data).intValue();
    }

    /**
     * 计算 data 的长度
     * @param data
     */
    public static <T> Integer calcDataLength(T data){
        if(data == null){
            return 0;
        }else if(data instanceof Collection){
            return ((Collection) data).size();
        }else if(data instanceof Map){
            return ((Map) data).size();
        }else if(data.getClass().isArray()){
            return Array.getLength(data);
        }else{//其他的 String、Integer、Date、Object等对象则统一计算为1，表示一个对象
            return 1;
        }
    }
}
