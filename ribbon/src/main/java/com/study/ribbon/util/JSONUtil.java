package com.study.ribbon.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

/**
  * @描述 json动态解析工具类
  * @author
  *
  */
public class JSONUtil{

    /**
     * @描述 将json字符串解析为map
     * @author
     */
    public static void parseJsonString(String json){
        LinkedHashMap<String, Object> jsonMap = JSON.parseObject(json, new TypeReference<LinkedHashMap<String, Object>>() {});
        for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
            parseJsonMap(entry);
        }
    }

    /**
     * @描述 map按动态的key解析值
     * @author
     */
    public static Map parseJsonMap(Map.Entry<String, Object> entry){
        Map<String,Object> newMap = new HashMap<>();
        //如果是单个map继续遍历
        if(entry.getValue() instanceof Map){
            LinkedHashMap<String, Object> jsonMap = JSON.parseObject(entry.getValue().toString(),
                    new TypeReference<LinkedHashMap<String, Object>>() {});
            for (Map.Entry<String, Object> entry2 : jsonMap.entrySet()) {
                parseJsonMap(entry2);
            }
        }

        //如果是list就提取出来
        if(entry.getValue() instanceof List){
            List list = (List)entry.getValue();
            for (int i = 0; i < list.size(); i++) {
                //如何还有，循环提取
                parseJsonString(list.get(i).toString());
            }
        }
            //如果是String就获取它的值
        if(entry.getValue() instanceof String){
            newMap.put(entry.getKey(),entry.getValue());
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }
        return newMap;
    }
}