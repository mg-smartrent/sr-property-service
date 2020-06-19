package com.mg.smartrent.property.resource.rest;


import com.mg.smartrent.domain.models.Property;
import com.mg.smartrent.domain.validation.ModelValidationException;
import com.mg.smartrent.property.services.PropertyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/rest/properties")
public class PropertyRestController {

    private final PropertyService propertyService;

    public PropertyRestController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }


    @PostMapping
    public ResponseEntity saveProperty(@RequestBody Property property) throws ModelValidationException {
        propertyService.save(property);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(params = "userId")
    public ResponseEntity<List<Property>> getPropertyByUserId(@RequestParam String userId) {
        return new ResponseEntity<>(propertyService.findByUserId(userId), HttpStatus.OK);
    }

    @GetMapping(params = "id")
    public ResponseEntity<Property> getPropertyById(@RequestParam String id) {
        return new ResponseEntity<>(propertyService.findById(id), HttpStatus.OK);
    }

}
