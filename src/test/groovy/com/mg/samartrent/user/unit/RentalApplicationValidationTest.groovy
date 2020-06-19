package com.mg.samartrent.user.unit

import com.mg.persistence.service.Repository
import com.mg.smartrent.domain.enums.EnCurrency
import com.mg.smartrent.domain.enums.EnRentalApplicationStatus
import com.mg.smartrent.domain.models.RentalApplication
import com.mg.smartrent.domain.validation.ModelValidationException
import com.mg.smartrent.property.Application
import com.mg.smartrent.property.services.ExternalUserService
import com.mg.smartrent.property.services.PropertyService
import com.mg.smartrent.property.services.RentalApplicationService
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
import static com.mg.samartrent.user.TestUtils.generateRentalApplication
import static java.lang.System.currentTimeMillis
import static org.mockito.Mockito.when

/**
 * This tests suite is designed to ensure correctness of the model validation constraints.
 */

@SpringBootTest(classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = ["eureka.client.enabled:false"]
)
class RentalApplicationValidationTest extends Specification {

    @Mock
    private Repository<RentalApplication> rentalApplicationRepository
    @MockBean
    private ExternalUserService userService
    @MockBean
    private PropertyService propertyService


    @Autowired
    @InjectMocks
    private RentalApplicationService rentalApplicationService


    @Unroll
    def "test: property rental application validation for #field = #value"() {

        setup: "mock db call and user exists call"
        MockitoAnnotations.initMocks(this)
        when(userService.userExists(model.getRenterUserId())).thenReturn(true)//mock external service call
        when(propertyService.findById(model.getPropertyId())).thenReturn(generateProperty())//mock property as it already exists
        when(rentalApplicationRepository.save(model)).thenReturn(model)//mock db call

        when: "saving listing with a new test value"
        BeanWrapperImpl beanUtilsWrapper = new BeanWrapperImpl(model)
        beanUtilsWrapper.setPropertyValue(field, value)


        then: "expectations are meet"

        try {
            RentalApplication dbModel = rentalApplicationService.save(model)
            beanUtilsWrapper = new BeanWrapperImpl(dbModel)

            Assert.assertEquals(value, beanUtilsWrapper.getPropertyValue(field))
            Assert.assertEquals(expectException, false)
            Assert.assertEquals(13, beanUtilsWrapper.getProperties().size())//13 properties

        } catch (Exception e) {
            Assert.assertEquals(errorMsg, e.getMessage().trim())
        }
        where:
        model                       | field          | value                                  | expectException | errorMsg
        generateRentalApplication() | 'renterUserId' | null                                   | true            | "Rental Application could not be saved. User not found, UserId = null"
        generateRentalApplication() | 'renterUserId' | ""                                     | true            | "Rental Application could not be saved. User not found, UserId ="
        generateRentalApplication() | 'renterUserId' | "inValidUserID"                        | true            | "Rental Application could not be saved. User not found, UserId = inValidUserID"
        generateRentalApplication() | 'renterUserId' | "mockedUserId"                         | false           | null

        generateRentalApplication() | 'propertyId'   | null                                   | true            | "Rental Application could not be saved. Property not found, Id = null"
        generateRentalApplication() | 'propertyId'   | ""                                     | true            | "Rental Application could not be saved. Property not found, Id ="
        generateRentalApplication() | 'propertyId'   | "inValidPropertyID"                    | true            | "Rental Application could not be saved. Property not found, Id = inValidPropertyID"
        generateRentalApplication() | 'propertyId'   | "mockedPropertyId"                     | false           | null

        generateRentalApplication() | 'price'        | -1                                     | true            | 'Field "price" has an invalid value value "-1". [must be greater than 0]'
        generateRentalApplication() | 'price'        | 0                                      | true            | 'Field "price" has an invalid value value "0". [must be greater than 0]'
        generateRentalApplication() | 'price'        | 1                                      | false           | null

        generateRentalApplication() | 'currency'     | EnCurrency.USD                         | false           | null
        generateRentalApplication() | 'currency'     | EnCurrency.EUR                         | false           | null
        generateRentalApplication() | 'currency'     | ''                                     | true            | 'Field "currency" has an invalid value value "null". [must not be null]'
        generateRentalApplication() | 'currency'     | null                                   | true            | 'Field "currency" has an invalid value value "null". [must not be null]'

        generateRentalApplication() | "checkInDate"  | new Date(currentTimeMillis())          | false           | null
        generateRentalApplication() | "checkInDate"  | new Date(currentTimeMillis() - 100000) | false           | null
        generateRentalApplication() | "checkInDate"  | new Date(currentTimeMillis() + 100000) | false           | null

        generateRentalApplication() | "checkOutDate" | new Date(currentTimeMillis())          | false           | null
        generateRentalApplication() | "checkOutDate" | new Date(currentTimeMillis() - 100000) | false           | null
        generateRentalApplication() | "checkOutDate" | new Date(currentTimeMillis() + 100000) | false           | null
    }


    def "test: property rental application status"() {

        setup: "mock db call and user exists call"
        RentalApplication application = generateRentalApplication();

        MockitoAnnotations.initMocks(this)
        when(userService.userExists(application.getRenterUserId())).thenReturn(true)//mock external service call
        when(propertyService.findById(application.getPropertyId())).thenReturn(generateProperty())//mock property as it already exists
        when(rentalApplicationRepository.save(application)).thenReturn(application)//mock db call

        when: "saving new application with no status"
        application.setStatus(null)
        def dbModel = rentalApplicationService.save(application)

        then: 'default value is set'
        dbModel.getStatus() == EnRentalApplicationStatus.PendingOwnerReview


        when: "updating application status"
        dbModel.setStatus(EnRentalApplicationStatus.Accepted)
        dbModel = rentalApplicationService.save(application)

        then: 'new status is set'
        dbModel.getStatus() == EnRentalApplicationStatus.Accepted


        when: "updating application status"
        dbModel.setStatus(EnRentalApplicationStatus.Rejected)
        dbModel = rentalApplicationService.save(application)

        then: 'new status is set'
        dbModel.getStatus() == EnRentalApplicationStatus.Rejected


        when: "updating application status"
        dbModel.setStatus(EnRentalApplicationStatus.PendingRenterReview)
        dbModel = rentalApplicationService.save(application)

        then: 'new status is set'
        dbModel.getStatus() == EnRentalApplicationStatus.PendingRenterReview


        when: "updating application status with and invalid status"
        dbModel.setStatus(null)
        rentalApplicationService.save(application)

        then: 'exception is thrown'
        thrown(ModelValidationException)
    }
}