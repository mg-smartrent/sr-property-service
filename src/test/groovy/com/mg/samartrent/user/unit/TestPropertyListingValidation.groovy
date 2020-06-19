package com.mg.samartrent.user.unit

import com.mg.persistence.service.Repository
import com.mg.smartrent.domain.models.PropertyListing
import com.mg.smartrent.property.Application
import com.mg.smartrent.property.services.PropertyListingService
import com.mg.smartrent.property.services.PropertyService
import com.mg.smartrent.property.services.ExternalUserService
import org.junit.Assert
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.springframework.beans.BeanWrapperImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import spock.lang.Specification
import spock.lang.Unroll

import static com.mg.samartrent.user.TestUtils.generateProperty
import static com.mg.samartrent.user.TestUtils.generatePropertyListing
import static java.lang.System.currentTimeMillis
import static org.mockito.Mockito.when

/**
 * This tests suite is designed to ensure correctness of the model validation constraints.
 */

@SpringBootTest(classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = ["eureka.client.enabled:false"]
)
class TestPropertyListingValidation extends Specification {

    @Mock
    private Repository<PropertyListing> propertyListingRepository
    @MockBean
    private ExternalUserService userService
    @MockBean
    private PropertyService propertyService


    @Autowired
    @InjectMocks
    private PropertyListingService propertyListingService


    @Unroll
    def "test: property listing validation for #field = #value"() {

        setup: "mock db call and user exists call"
        MockitoAnnotations.initMocks(this)
        when(userService.userExists(model.getUserId())).thenReturn(true)//mock external service call
        when(propertyService.findById(model.getPropertyId())).thenReturn(generateProperty())//mock property as it already exists
        when(propertyListingRepository.save(model)).thenReturn(model)//mock db call

        when: "saving listing with a new test value"
        BeanWrapperImpl beanUtilsWrapper = new BeanWrapperImpl(model)
        beanUtilsWrapper.setPropertyValue(field, value)


        then: "expectations are meet"

        try {
            PropertyListing dbModel = propertyListingService.save(model)
            beanUtilsWrapper = new BeanWrapperImpl(dbModel)

            Assert.assertEquals(value, beanUtilsWrapper.getPropertyValue(field))
            Assert.assertEquals(expectException, false)
            Assert.assertEquals(13, beanUtilsWrapper.getProperties().size())//13 properties

        } catch (Exception e) {
            def explanation = "ACTUAL: ${e.getMessage().trim()}\nShould countain\nEXPECTED: $errorMsg"
            Assert.assertTrue(explanation, e.getMessage().contains(errorMsg))
        }
        where:
        model                     | field          | value                                  | expectException | errorMsg
        generatePropertyListing() | 'userId'       | null                                   | true            | 'Listing could not be saved. User not found, User Id = null'
        generatePropertyListing() | 'userId'       | ""                                     | true            | 'Listing could not be saved. User not found, User Id = '
        generatePropertyListing() | 'userId'       | "inValidUserID"                        | true            | 'Listing could not be saved. User not found, User Id = inValidUserID'
        generatePropertyListing() | 'userId'       | "mockedUserId"                         | false           | null

        generatePropertyListing() | 'propertyId'   | null                                   | true            | "Listing could not be saved. Property not found."
        generatePropertyListing() | 'propertyId'   | ""                                     | true            | "Listing could not be saved. Property not found."
        generatePropertyListing() | 'propertyId'   | "inValidPropertyID"                    | true            | "Listing could not be saved. Property not found."
        generatePropertyListing() | 'propertyId'   | "mockedPropertyId"                     | false           | null

        generatePropertyListing() | 'price'        | -1                                     | true            | 'Field "price" has an invalid value value "-1". [must be greater than or equal to 0]'
        generatePropertyListing() | 'price'        | 0                                      | false           | null
        generatePropertyListing() | 'price'        | 1                                      | false           | null

        generatePropertyListing() | 'totalViews'   | -1                                     | true            | 'Field "totalViews" has an invalid value value "-1". [must be greater than or equal to 0]'
        generatePropertyListing() | 'totalViews'   | 0                                      | false           | null
        generatePropertyListing() | 'totalViews'   | 1                                      | false           | null

        generatePropertyListing() | "checkInDate"  | new Date(currentTimeMillis())          | false           | null
        generatePropertyListing() | "checkInDate"  | new Date(currentTimeMillis() - 100000) | false           | null
        generatePropertyListing() | "checkInDate"  | new Date(currentTimeMillis() + 100000) | false           | null

        generatePropertyListing() | "checkOutDate" | new Date(currentTimeMillis())          | false           | null
        generatePropertyListing() | "checkOutDate" | new Date(currentTimeMillis() - 100000) | true            | "CheckIn Date should not be greater than CheckOut Date"
        generatePropertyListing() | "checkOutDate" | new Date(currentTimeMillis() + 100000) | false           | null

        generatePropertyListing() | "listed"       | true                                   | false           | null
        generatePropertyListing() | "listed"       | false                                  | false           | null
    }

}
