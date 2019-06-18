package com.disdispace.hello.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class HelloController {

    @Value("${com.young1lin.age}")
    private String age;

    /**
     * 获取访问次数
     */
    /*@Autowired
    private DefaultCounterService defaultCounterService;*/

    @GetMapping("/hello")
    public String index(){
        System.out.println(age);
        //defaultCounterService.increment("didispce.hello.count");
        return "Hello World";
    }

    @GetMapping("/getJson")
    public List<String> getJsonHello(String json,Long id){
        ArrayList<String> strings = new ArrayList<>();
        strings.add("111");
        strings.add("2222");
        strings.add("33333");
        return strings;
    }

    @GetMapping("/getMap")
    public Map<String,Object> getMap(){
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("id",1);
        data.put("name","young");
        data.put("age",22L);
        map.put("message","success");
        map.put("data",data);
        return map;
    }
}
