package com.example.multibranch_pipeline_demo;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HotelController {

    @GetMapping("/hotels")
    public String getHotels() {
        return "List of hotels";
    }
}
