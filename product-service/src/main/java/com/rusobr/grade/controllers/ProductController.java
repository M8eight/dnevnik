package com.rusobr.grade.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductController {
    @GetMapping("/products")
    public String products() {
        return "List of products";
    }
}