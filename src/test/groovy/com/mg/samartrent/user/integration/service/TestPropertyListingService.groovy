package com.mg.samartrent.user.integration.service

import com.mg.samartrent.user.integration.IntegrationTestsSetup
import com.mg.smartrent.domain.models.Property
import com.mg.smartrent.domain.models.PropertyListing
import com.mg.smartrent.domain.validation.ModelBusinessValidationException
import com.mg.smartrent.domain.validation.ModelValidationException
import com.mg.smartrent.property.Application
import com.mg.smartrent.property.services.PropertyListingService
import com.mg.smartrent.property.services.PropertyService
import com.mg.smartrent.property.services.ExternalUserService
import org.mockito.InjectMocks
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean

import javax.validation.ConstraintViolationException

import static com.mg.samartrent.user.TestUtils.generateProperty
import static com.mg.samartrent.user.TestUtils.generatePropertyListing
import static org.mockito.Mockito.when

/**
 * This tests suite is designed to ensure correctness of the model validation constraints.
 */

@SpringBootTest(
        classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = ["eureka.client.enabled:false"])
class TestPropertyListingService extends IntegrationTestsSetup {

    @Autowired
    private PropertyService propertyService

    @MockBean
    private ExternalUserService userService
    @Autowired
    @InjectMocks
    private PropertyListingService listingService


    static boolean testsSetupExecuted
    static Property dbProperty = generateProperty()

    def setup() {
        if (!testsSetupExecuted) {
            purgeCollection(PropertyListing.class)

            MockitoAnnotations.initMocks(this)
            when(userService.userExists(dbProperty.getUserId())).thenReturn(true)//mock external service call

            dbProperty = propertyService.save(dbProperty)
            testsSetupExecuted = true
        }

    }

    def "test: save null listing"() {
        when:
        listingService.save(null)

        then: "exception is thrown"
        ConstraintViolationException e = thrown()
        e.getMessage() == "save.listing: must not be null"
    }

    def "test: save listing for in-existent Property"() {

        when: "saving listing with invalid property id"
        listingService.save(generatePropertyListing())

        then: "exception is thrown"
        ModelBusinessValidationException e = thrown()
        e.getMessage() == "Listing could not be saved. Property not found."
    }

    def "test: save listing for in-existent User"() {
        setup: "mocking user"
        def listing = generatePropertyListing()
        listing.setPropertyId(dbProperty.getId())
        when(userService.userExists(listing.getUserId())).thenReturn(false)//mock external service call to user not found

        when: "saving listing"
        listingService.save(listing)

        then: "exception is thrown"
        ModelBusinessValidationException e = thrown()
        e.getMessage() == "Listing could not be saved. User not found, User Id = ${listing.userId}"
    }

    def "test: save listing with same checkin/checkout date"() {
        setup: "mock user exists"
        def listing = generatePropertyListing()
        listing.setPropertyId(dbProperty.getId())
        when(userService.userExists(listing.getUserId())).thenReturn(true)//mock external service call

        when: "saving with past dates"
        def date = new Date(System.currentTimeMillis())
        listing.setCheckInDate(date)
        listing.setCheckOutDate(date)
        def dbListing = listingService.save(listing)

        then:
        dbListing != null
    }

    def "test: save listing for with checkin after checkout date"() {
        setup:
        def listing = generatePropertyListing()
        listing.setPropertyId(dbProperty.getId())
        when(userService.userExists(listing.getUserId())).thenReturn(true)//mock external service call

        when: "checkin after checkout"
        listing.setCheckInDate(new Date(System.currentTimeMillis() + 100000))
        listing.setCheckOutDate(new Date(System.currentTimeMillis() - 100000))
        listingService.save(listing)

        then: "exception is thrown"
        ModelValidationException e = thrown()
        e.getMessage().contains("CheckIn Date should not be greater than CheckOut Date")
    }

    def "test: save listing for existent User and Property"() {
        setup:
        def listing = generatePropertyListing()
        listing.setPropertyId(dbProperty.getId())
        when(userService.userExists(listing.getUserId())).thenReturn(true)//mock external service call

        when: "saving listing"
        listing = listingService.save(listing)

        then: "successfully saved"
        listing.getId() != null
        listing.getCreatedDate() != null
        listing.getModifiedDate() != null
        listing.getUserId() == "mockedUserId"
        listing.getPropertyId() == dbProperty.id
        listing.getPrice() == 100
        listing.getTotalViews() == 3
        listing.getCheckInDate().before(new Date())
        listing.getCheckOutDate().after(new Date())
    }

    def "test: findById then findByPropertyId then findByUserId"() {
        setup:
        def property = generateProperty()
        when(userService.userExists(property.getUserId())).thenReturn(true)//mock external service call

        def listing = generatePropertyListing()
        listing.setPropertyId(propertyService.save(property).getId())
        when(userService.userExists(listing.getUserId())).thenReturn(true)//mock external service call

        listing = listingService.save(listing)

        when:
        def dbListing = listingService.findById(listing.getId())

        then: 'found'
        dbListing != null


        when:
        def listings = listingService.findByPropertyId(listing.getPropertyId())

        then: 'only one instance found'
        listings.size() == 1

        when:
        listings = listingService.findByPropertyId(listing.getPropertyId())

        then: 'only one instance found'
        listings.size() == 1
    }

    def "test: publish listing"() {
        setup:
        def property = generateProperty()
        when(userService.userExists(property.getUserId())).thenReturn(true)//mock external service call

        def listing = generatePropertyListing()
        listing.setPropertyId(propertyService.save(property).getId())
        when(userService.userExists(listing.getUserId())).thenReturn(true)//mock external service call

        listing = listingService.save(listing)

        when:
        def dbListing = listingService.publish(listing.getId(), true)

        then:
        dbListing.isListed()


        when:
        dbListing = listingService.publish(listing.getId(), false)

        then:
        !dbListing.isListed()
    }


}
