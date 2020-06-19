package com.mg.samartrent.user.integration.service

import com.mg.samartrent.user.integration.IntegrationTestsSetup
import com.mg.smartrent.domain.enums.EnCurrency
import com.mg.smartrent.domain.models.Property
import com.mg.smartrent.domain.models.RentalApplication
import com.mg.smartrent.domain.validation.ModelBusinessValidationException
import com.mg.smartrent.domain.validation.ModelValidationException
import com.mg.smartrent.property.Application
import com.mg.smartrent.property.services.ExternalUserService
import com.mg.smartrent.property.services.PropertyService
import com.mg.smartrent.property.services.RentalApplicationService
import org.mockito.InjectMocks
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean

import javax.validation.ConstraintViolationException

import static com.mg.samartrent.user.TestUtils.generateProperty
import static com.mg.samartrent.user.TestUtils.generateRentalApplication
import static org.mockito.Mockito.when

/**
 * This tests suite is designed to ensure correctness of the model validation constraints.
 */

@SpringBootTest(
        classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = ["eureka.client.enabled:false"])
class TestRentalApplicationService extends IntegrationTestsSetup {

    @Autowired
    private PropertyService propertyService

    @MockBean
    private ExternalUserService userService
    @Autowired
    @InjectMocks
    private RentalApplicationService rentalApplicationService


    static boolean testsSetupExecuted
    static Property dbProperty = generateProperty()

    def setup() {
        if (!testsSetupExecuted) {
            purgeCollection(RentalApplication.class)

            MockitoAnnotations.initMocks(this)
            when(userService.userExists(dbProperty.getUserId())).thenReturn(true)//mock external service call

            dbProperty = propertyService.save(dbProperty)
            testsSetupExecuted = true
        }

    }

    def "test: save null rental application"() {
        when:
        rentalApplicationService.save(null)

        then: "exception is thrown"
        ConstraintViolationException e = thrown()
        e.getMessage() == "save.model: must not be null"
    }

    def "test: save rental application for in-existent Property"() {

        when: "saving listing with invalid property id"
        rentalApplicationService.save(generateRentalApplication())

        then: "exception is thrown"
        ModelBusinessValidationException e = thrown()
        e.getMessage() == "Rental Application could not be saved. User not found, UserId = mockedUserId"
    }

    def "test: save listing for in-existent User"() {
        setup: "mocking user"
        def application = generateRentalApplication()
        application.setPropertyId(dbProperty.getId())
        when(userService.userExists(application.getRenterUserId())).thenReturn(false)//mock external service call to user not found

        when: "saving listing"
        rentalApplicationService.save(application)

        then: "exception is thrown"
        ModelBusinessValidationException e = thrown()
        e.getMessage() == "Rental Application could not be saved. User not found, UserId = mockedUserId"
    }

    def "test: save rental application with same checkin/checkout date"() {
        setup: "mock user exists"
        def application = generateRentalApplication()
        application.setPropertyId(dbProperty.getId())
        when(userService.userExists(application.getRenterUserId())).thenReturn(true)//mock external service call

        when: "saving with same dates"
        def date = new Date(System.currentTimeMillis())
        application.setCheckInDate(date)
        application.setCheckOutDate(date)
        def dbApplication = rentalApplicationService.save(application)

        then: "exception is thrown"
        dbApplication != null
    }

    def "test: save rental application with checkin date after checkout date"() {
        setup:
        def application = generateRentalApplication()
        application.setPropertyId(dbProperty.getId())
        when(userService.userExists(application.getRenterUserId())).thenReturn(true)//mock external service call

        when: "checkin after checkout"
        application.setCheckInDate(new Date(System.currentTimeMillis() + 100000))
        application.setCheckOutDate(new Date(System.currentTimeMillis() - 100000))
        rentalApplicationService.save(application)

        then: "exception is thrown"
        ModelValidationException e = thrown()
        e.getMessage().contains("CheckIn Date should not be greater than CheckOut Date")
    }

    def "test: save listing for existent User and Property"() {
        setup:
        def application = generateRentalApplication()
        application.setPropertyId(dbProperty.getId())
        when(userService.userExists(application.getRenterUserId())).thenReturn(true)//mock external service call

        when: "saving listing"
        application = rentalApplicationService.save(application)

        then: "successfully saved"
        application.getId() != null
        application.getCreatedDate() != null
        application.getModifiedDate() != null
        application.getRenterUserId() == "mockedUserId"
        application.getPropertyId() == dbProperty.id
        application.getPrice() == 100
        application.getCurrency() == EnCurrency.USD
        application.getCheckInDate().before(new Date())
        application.getCheckOutDate().after(new Date())
    }

    def "test: findById then findByPropertyId then findByRenterUserId"() {
        setup:
        def property = generateProperty()
        when(userService.userExists(property.getUserId())).thenReturn(true)//mock external service call
        property = propertyService.save(property)

        def application = generateRentalApplication()
        application.setPropertyId(property.getId())
        application.setRenterUserId('mockedRenterUserIdentifier')
        when(userService.userExists('mockedRenterUserIdentifier')).thenReturn(true)//mock external service call


        when:
        application = rentalApplicationService.save(application)
        def dbListing = rentalApplicationService.findById(application.getId())

        then: 'found'
        dbListing != null


        when:
        def applications = rentalApplicationService.findByPropertyId(application.getPropertyId())

        then: 'only one instance found'
        applications.size() == 1

        when:
        applications = rentalApplicationService.findByRenterUserId(application.getRenterUserId())

        then: 'only one instance found'
        applications.size() == 1
    }


}
