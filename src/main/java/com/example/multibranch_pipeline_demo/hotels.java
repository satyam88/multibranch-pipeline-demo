package com.example.multibranch_pipeline_demo;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class hotels {
    @GetMapping("/hotels")
    public String getData() {return  "Please book hotel from mmt 75% discount in all india" ; }
}