package com.regalos.back_regalos.controllers;

import com.regalos.back_regalos.models.DeliveryCity;
import com.regalos.back_regalos.repositories.DeliveryCityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
public class CityController {

    private final DeliveryCityRepository cityRepository;

    @GetMapping
    public ResponseEntity<List<DeliveryCity>> getAllCities() {
        return ResponseEntity.ok(cityRepository.findAllByIsActiveTrue());
    }
}
