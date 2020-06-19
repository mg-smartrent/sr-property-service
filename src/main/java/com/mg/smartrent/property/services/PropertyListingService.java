package com.mg.smartrent.property.services;

import com.mg.persistence.service.Repository;
import com.mg.smartrent.domain.models.BizItem;
import com.mg.smartrent.domain.models.PropertyListing;
import com.mg.smartrent.domain.validation.ModelBusinessValidationException;
import com.mg.smartrent.domain.validation.ModelValidationException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

import static com.mg.smartrent.domain.enrichment.ModelEnricher.enrich;
import static com.mg.smartrent.domain.validation.ModelValidator.validate;

@Service
@Validated
public class PropertyListingService {


    private ExternalUserService userService;
    private PropertyService propertyService;
    private Repository<PropertyListing> propertyListingRepository;

    public PropertyListingService(Repository<PropertyListing> propertyListingRepository,
                                  PropertyService propertyService,
                                  ExternalUserService userService) {
        this.propertyListingRepository = propertyListingRepository;
        this.propertyService = propertyService;
        this.userService = userService;
    }


    public PropertyListing save(@NotNull PropertyListing listing) throws ModelValidationException {

        if (propertyService.findById(listing.getPropertyId()) == null) {
            throw new ModelBusinessValidationException("Listing could not be saved. Property not found.");
        }

        if (!userService.userExists(listing.getUserId())) {
            throw new ModelBusinessValidationException("Listing could not be saved. User not found, User Id = " + listing.getUserId());
        }

        enrich(listing);
        validate(listing);
        return propertyListingRepository.save(listing);
    }

    public PropertyListing publish(String id, boolean listed) throws ModelValidationException {
        PropertyListing listing = findById(id);

        if (listing == null) {
            throw new ModelBusinessValidationException("Listing not found.");
        }
        listing.setListed(listed);
        save(listing);
        return listing;
    }


    public PropertyListing findById(@NotNull @NotBlank String id) {
        List<PropertyListing> listings = propertyListingRepository.findAllBy(BizItem.Fields.id, id, PropertyListing.class);

        return (listings != null && !listings.isEmpty()) ? listings.get(0) : null;
    }


    public List<PropertyListing> findByPropertyId(@NotNull @NotBlank String propertyId) {
        return propertyListingRepository.findAllBy(PropertyListing.Fields.propertyId, propertyId, PropertyListing.class);
    }

    public List<PropertyListing> findByUserId(@NotNull @NotBlank String userId) {
        return propertyListingRepository.findAllBy(PropertyListing.Fields.userId, userId, PropertyListing.class);
    }

}
