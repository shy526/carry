package top.ccxh.common.utils;

import com.alibaba.fastjson.JSON;

public class JsonpUtil {
    public static String jsonpString(String name,Object object){
        return  name.concat("(").concat(JSON.toJSONString(object)).concat(")");
    }
    public static String jsonpString(Object object){
        return  JsonpUtil.jsonpString("callBack",object);
    }
}
