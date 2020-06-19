package com.mg.smartrent.property.resource.rest;


import com.mg.smartrent.domain.models.RentalApplication;
import com.mg.smartrent.domain.validation.ModelValidationException;
import com.mg.smartrent.property.services.RentalApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/rest/rentalapplications")
public class RentalApplicationRestController {

    private final RentalApplicationService rentalApplicationService;

    public RentalApplicationRestController(RentalApplicationService rentalApplicationService) {
        this.rentalApplicationService = rentalApplicationService;
    }


    @PostMapping
    public ResponseEntity saveApplication(@RequestBody RentalApplication rentalApplication) throws ModelValidationException {
        rentalApplicationService.save(rentalApplication);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(params = "renterUserId")
    public ResponseEntity<List<RentalApplication>> getApplicationsByRenterUserId(@RequestParam String renterUserId) {
        return new ResponseEntity<>(rentalApplicationService.findByRenterUserId(renterUserId), HttpStatus.OK);
    }

    @GetMapping(params = "propertyId")
    public ResponseEntity<List<RentalApplication>> getApplicationsByPropertyUserId(@RequestParam String propertyId) {
        return new ResponseEntity<>(rentalApplicationService.findByPropertyId(propertyId), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RentalApplication> getRentalApplicationById(@PathVariable String id) {
        return new ResponseEntity<>(rentalApplicationService.findById(id), HttpStatus.OK);
    }

}
