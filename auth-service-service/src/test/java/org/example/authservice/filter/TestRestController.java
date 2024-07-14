package org.example.authservice.filter;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
@Profile("test")
public class TestRestController {

    @PostMapping
    public ResponseEntity<String> testMethod(){
        return ResponseEntity.ok("ok");
    }
}
