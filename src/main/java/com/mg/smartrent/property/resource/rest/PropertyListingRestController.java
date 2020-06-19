package com.mg.smartrent.property.resource.rest;


import com.mg.smartrent.domain.models.PropertyListing;
import com.mg.smartrent.domain.validation.ModelValidationException;
import com.mg.smartrent.property.services.PropertyListingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/rest/propertylistings")
public class PropertyListingRestController {

    private final PropertyListingService listingService;

    public PropertyListingRestController(PropertyListingService listingService) {
        this.listingService = listingService;
    }


    @PostMapping
    public ResponseEntity saveListing(@RequestBody PropertyListing listing) throws ModelValidationException {
        listingService.save(listing);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(value = "/{id}", params = "publish")
    public ResponseEntity publishListing(@PathVariable String id, @RequestParam boolean publish) throws ModelValidationException {
        listingService.publish(id, publish);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyListing> getListingById(@PathVariable String id) {
        return new ResponseEntity<>(listingService.findById(id), HttpStatus.OK);
    }

    @GetMapping(params = "propertyId")
    public ResponseEntity<List<PropertyListing>> getListingsByPropertyId(@RequestParam String propertyId) {
        return new ResponseEntity<>(listingService.findByPropertyId(propertyId), HttpStatus.OK);
    }

}
