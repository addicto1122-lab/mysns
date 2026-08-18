package com.spring.mysns.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {
    private String email;
    private String password;
    private LocalDateTime createdAt;
}
