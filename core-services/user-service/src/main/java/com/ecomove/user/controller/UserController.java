package com.ecomove.user.controller;

import com.ecomove.common.dto.ApiResponse;
import com.ecomove.common.utils.ResponseHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {


    @GetMapping("/v1")
    public ResponseEntity<?> test(){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                      "OK"
                );
    }


}
