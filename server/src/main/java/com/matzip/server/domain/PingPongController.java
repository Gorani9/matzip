package com.matzip.server.domain;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ping")
public class PingPongController {
    @GetMapping("/")
    public ResponseEntity<String> pingTest() {
        return new ResponseEntity<>("pong", HttpStatus.OK);
    }
}
