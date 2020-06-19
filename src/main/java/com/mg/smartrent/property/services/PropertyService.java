package com.mg.smartrent.property.services;


import com.mg.persistence.service.Repository;
import com.mg.smartrent.domain.models.BizItem;
import com.mg.smartrent.domain.models.Property;
import com.mg.smartrent.domain.validation.ModelBusinessValidationException;
import com.mg.smartrent.domain.validation.ModelValidationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

import static com.mg.smartrent.domain.enrichment.ModelEnricher.enrich;
import static com.mg.smartrent.domain.validation.ModelValidator.validate;

@Service
@Validated
public class PropertyService {

    private static final Logger log = LogManager.getLogger(PropertyService.class);

    private ExternalUserService userService;
    private Repository<Property> propertyRepository;


    public PropertyService(Repository<Property> propertyRepository, ExternalUserService userService) {
        this.propertyRepository = propertyRepository;
        this.userService = userService;
    }


    public Property save(@NotNull Property model) throws ModelValidationException {
        if (!userService.userExists(model.getUserId())) {
            throw new ModelBusinessValidationException(String.format("User with Id='%s' not found.", model.getUserId()));
        }
        enrich(model);
        validate(model);
        Property property = propertyRepository.save(model);
        log.info("Property created. Id = {}", property.getId());

        return property;
    }


    public Property findById(@NotNull @NotBlank String id) {
        List<Property> propertyList = propertyRepository.findAllBy(BizItem.Fields.id, id, Property.class);
        return (CollectionUtils.isEmpty(propertyList)) ? null : propertyList.get(0);
    }

    public List<Property> findByUserId(@NotNull @NotBlank String userId) {
        return propertyRepository.findAllBy(Property.Fields.userId, userId, Property.class);
    }


}
