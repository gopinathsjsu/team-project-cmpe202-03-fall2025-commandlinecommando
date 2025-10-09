package com.commandlinecommandos.listingapi.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/")
public class HomeController {
    
    @GetMapping
    public String home() {
        log.info("Home endpoint accessed");
        return "Hello CMPE-202";
    }
}