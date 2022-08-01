package com.matzip.server.feature;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PingPongController {
    @GetMapping("/ping/")
    public ResponseEntity<String> pingTest() {
        return new ResponseEntity<>("pong", HttpStatus.OK);
    }
}
