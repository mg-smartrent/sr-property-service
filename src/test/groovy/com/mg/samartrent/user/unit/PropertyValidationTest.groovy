package com.mg.samartrent.user.unit

import com.mg.persistence.service.Repository
import com.mg.smartrent.domain.enums.EnBuildingType
import com.mg.smartrent.domain.enums.EnPropertyCondition
import com.mg.smartrent.domain.models.Property
import com.mg.smartrent.property.Application
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
import static org.mockito.Mockito.when

/**
 * This tests suite is designed to ensure correctness of the model validation constraints.
 */

@SpringBootTest(classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = ["eureka.client.enabled:false"]
)
class PropertyValidationTest extends Specification {

    @MockBean
    private ExternalUserService userService
    @Mock
    private Repository<Property> propertyRepository
    @Autowired
    @InjectMocks
    private PropertyService propertyService


    @Unroll
    def "test: property constraint for #field = #value"() {

        setup: "mock db call and user exists call"
        MockitoAnnotations.initMocks(this)
        when(userService.userExists(model.getUserId())).thenReturn(true)//mock external service call
        when(propertyRepository.save(model)).thenReturn(model)//mock db call

        when: "saving property with a new test value"
        BeanWrapperImpl beanUtilsWrapper = new BeanWrapperImpl(model)
        beanUtilsWrapper.setPropertyValue(field, value)

        then: "expectations are meet"
        try {
            Property dbModel = propertyService.save(model)
            beanUtilsWrapper = new BeanWrapperImpl(dbModel)

            Assert.assertEquals(value, beanUtilsWrapper.getPropertyValue(field))

            Assert.assertEquals(expectException, false)
            Assert.assertEquals(13, beanUtilsWrapper.getProperties().size())//13 properties

        } catch (Exception e) {
            Assert.assertEquals(errorMsg, e.getMessage().trim())
        }

        where:
        model              | field              | value                                  | expectException | errorMsg
        generateProperty() | 'userId'           | null                                   | true            | "User with Id=null not found."
        generateProperty() | 'userId'           | ""                                     | true            | "User with Id= not found."
        generateProperty() | 'userId'           | "inValidUserID"                        | true            | "User with Id=inValidUserID not found."
        generateProperty() | 'userId'           | "userId1234"                           | false           | null

        generateProperty() | 'buildingType'     | EnBuildingType.Apartment               | false           | null
        generateProperty() | 'buildingType'     | EnBuildingType.Condo                   | false           | null
        generateProperty() | 'buildingType'     | EnBuildingType.House                   | false           | null
        generateProperty() | 'buildingType'     | null                                   | true            | 'Field "buildingType" has an invalid value value "null". [must not be null]'
        generateProperty() | 'buildingType'     | ""                                     | true            | 'Field "buildingType" has an invalid value value "". [must be any of enum class com.mg.smartrent.domain.enums.EnBuildingType]'
        generateProperty() | 'buildingType'     | "invalid"                              | true            | 'Field "buildingType" has an invalid value value "invalid". [must be any of enum class com.mg.smartrent.domain.enums.EnBuildingType]'

        generateProperty() | 'condition'        | EnPropertyCondition.Normal             | false           | null
        generateProperty() | 'condition'        | EnPropertyCondition.RequiresReparation | false           | null
        generateProperty() | 'condition'        | EnPropertyCondition.AfterReparation    | false           | null
        generateProperty() | 'condition'        | "invalid"                              | true            | 'Field "condition" has an invalid value value "invalid". [must be any of enum class com.mg.smartrent.domain.enums.EnPropertyCondition]'
        generateProperty() | 'condition'        | ""                                     | true            | 'Field "condition" has an invalid value value "". [must be any of enum class com.mg.smartrent.domain.enums.EnPropertyCondition]'
        generateProperty() | 'condition'        | null                                   | true            | 'Field "condition" has an invalid value value "null". [must not be null]'

        generateProperty() | 'totalRooms'       | -1                                     | true            | 'Field "totalRooms" has an invalid value value "-1". [must be greater than or equal to 0]'
        generateProperty() | 'totalRooms'       | 1                                      | false           | null
        generateProperty() | 'totalRooms'       | 10000                                  | false           | null

        generateProperty() | 'totalBathRooms'   | -1                                     | true            | 'Field "totalBathRooms" has an invalid value value "-1". [must be greater than or equal to 0]'
        generateProperty() | 'totalBathRooms'   | 1                                      | false           | null
        generateProperty() | 'totalBathRooms'   | 10000                                  | false           | null

        generateProperty() | 'totalBalconies'   | -1                                     | true            | 'Field "totalBalconies" has an invalid value value "-1". [must be greater than or equal to 0]'
        generateProperty() | 'totalBalconies'   | 1                                      | false           | null
        generateProperty() | 'totalBalconies'   | 10000                                  | false           | null

//        generateProperty() | 'thumbnail'        | null                                          | false           | null
        generateProperty() | 'thumbnail'        | new byte[1]                            | false           | null

        generateProperty() | 'parkingAvailable' | true                                   | false           | null
        generateProperty() | 'parkingAvailable' | false                                  | false           | null
    }

}
