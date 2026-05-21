package com.myanatomy.sandboxpro.service;

import org.springframework.stereotype.Service;

@Service
public class HelloService {
    public String getMessage() {
        return "Service layer working fine!";
    }
}