package com.umc.connext.global.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

    //TODO 테스트용 삭제예정

    @GetMapping("/admin")
    public String admin(){
        return "admin";
    }

    @GetMapping("/")
    public String home(){
        return "home";
    }
}
