package com.disdispace.hello.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
