package org.example.rateforge.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class hello {
    @GetMapping("/api")
    public String func(){
//        System.out.println();
        return "hello there! hello shivansh hello again";
    }
}
