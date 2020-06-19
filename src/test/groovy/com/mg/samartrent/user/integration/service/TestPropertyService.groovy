package com.mg.samartrent.user.integration.service

import com.mg.samartrent.user.integration.IntegrationTestsSetup
import com.mg.smartrent.domain.enums.EnBuildingType
import com.mg.smartrent.domain.enums.EnPropertyCondition
import com.mg.smartrent.domain.models.Property
import com.mg.smartrent.property.Application
import com.mg.smartrent.property.services.PropertyService
import com.mg.smartrent.property.services.ExternalUserService
import org.mockito.InjectMocks
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean

import static com.mg.samartrent.user.TestUtils.generateProperty
import static org.mockito.Mockito.when

/**
 * This tests suite is designed to ensure correctness of the model validation constraints.
 */

@SpringBootTest(classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = ["eureka.client.enabled:false"]
)
class TestPropertyService extends IntegrationTestsSetup {

    @MockBean
    private ExternalUserService userService
    @Autowired
    @InjectMocks
    private PropertyService propertyService

    static boolean initialized

    def setup() {
        if (!initialized) {
            purgeCollection(Property.class)
            MockitoAnnotations.initMocks(this)
            initialized = true
        }
    }


    static Property dbProperty

    def "test: create property"() {

        setup: "mock external REST call"
        dbProperty = generateProperty()
        when(userService.userExists(dbProperty.getUserId())).thenReturn(true)//mock external service call

        when: "saving a new property"

        dbProperty = propertyService.save(dbProperty)

        then: "successfully saved"
        dbProperty.getId() != null
        dbProperty.getCreatedDate() != null
        dbProperty.getModifiedDate() != null
        dbProperty.getUserId() == "userId1234"
        dbProperty.getBuildingType() == EnBuildingType.Condo.name()
        dbProperty.getCondition() == EnPropertyCondition.Normal.name()
        dbProperty.getTotalRooms() == 10
        dbProperty.getTotalBathRooms() == 5
        dbProperty.getTotalBalconies() == 1
        dbProperty.getThumbnail() == null
        dbProperty.isParkingAvailable()
    }

    def "test: find renter by id"() {
        when:
        def property = propertyService.findById(dbProperty.getId())

        then:
        property != null
        property.getId() == dbProperty.getId()
    }

    def "test: find renter by userId"() {
        when:
        def properties = propertyService.findByUserId(dbProperty.getUserId())

        then:
        properties.size() == 1
        properties.get(0).getUserId() == dbProperty.getUserId()
    }

    def "test: edit property"() {
        setup:
        def dbProperty = generateProperty()
        when(userService.userExists(dbProperty.getUserId())).thenReturn(true)//mock external service call
        propertyService.save(dbProperty)

        when:
        def editedProperty = propertyService.findById(dbProperty.id)
        editedProperty.setTotalRooms(10)

        then:
        propertyService.save(editedProperty).getTotalRooms() == 10
    }

}
