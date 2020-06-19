package com.mg.smartrent.property.services;


import com.mg.persistence.service.Repository;
import com.mg.smartrent.domain.enrichment.ModelEnricher;
import com.mg.smartrent.domain.enums.EnRentalApplicationStatus;
import com.mg.smartrent.domain.models.BizItem;
import com.mg.smartrent.domain.models.RentalApplication;
import com.mg.smartrent.domain.validation.ModelBusinessValidationException;
import com.mg.smartrent.domain.validation.ModelValidationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.valid4j.Validation;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

import static com.mg.smartrent.domain.validation.ModelValidator.validate;

@Service
@Validated
public class RentalApplicationService {

    private static final Logger log = LogManager.getLogger(RentalApplicationService.class);

    private ExternalUserService userService;
    private Repository<RentalApplication> rentalApplicationRepository;
    private PropertyService propertyService;


    public RentalApplicationService(final Repository<RentalApplication> rentalApplicationRepository,
                                    final ExternalUserService userService,
                                    final PropertyService propertyService) {
        this.rentalApplicationRepository = rentalApplicationRepository;
        this.userService = userService;
        this.propertyService = propertyService;
    }


    public RentalApplication save(@NotNull RentalApplication model) throws ModelValidationException {
        if (!userService.userExists(model.getRenterUserId())) {
            throw new ModelBusinessValidationException(
                    "Rental Application could not be saved. User not found, UserId = " + model.getRenterUserId());
        }
        if (propertyService.findById(model.getPropertyId()) != null) {
            throw new ModelBusinessValidationException(
                    "Rental Application could not be saved. Property not found, Id = " + model.getPropertyId());
        }

        enrich(model);
        validate(model);
        RentalApplication dbModel = rentalApplicationRepository.save(model);
        log.debug("RentalApplication created: {}", dbModel);

        return dbModel;
    }


    public RentalApplication findById(@NotNull @NotBlank String id) {
        List<RentalApplication> propertyList = rentalApplicationRepository
                .findAllBy(BizItem.Fields.id, id, RentalApplication.class);
        return (CollectionUtils.isEmpty(propertyList)) ? null : propertyList.get(0);
    }

    public List<RentalApplication> findByRenterUserId(@NotNull @NotBlank String renterUserId) {
        return rentalApplicationRepository
                .findAllBy(RentalApplication.Fields.renterUserId, renterUserId, RentalApplication.class);
    }

    public List<RentalApplication> findByPropertyId(@NotNull @NotBlank String propertyId) {
        return rentalApplicationRepository
                .findAllBy(RentalApplication.Fields.propertyId, propertyId, RentalApplication.class);
    }


    //-----------------Private Methods-------------------

    public void enrich(RentalApplication model) {

        if (model.getId() == null) {
            model.setStatus(EnRentalApplicationStatus.PendingOwnerReview);
        }
        ModelEnricher.enrich(model);
    }

}
