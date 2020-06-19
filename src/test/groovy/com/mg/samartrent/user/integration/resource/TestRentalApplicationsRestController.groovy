package com.mg.samartrent.user.integration.resource

import com.fasterxml.jackson.core.type.TypeReference
import com.mg.samartrent.user.TestUtils
import com.mg.samartrent.user.integration.IntegrationTestsSetup
import com.mg.smartrent.domain.models.Property
import com.mg.smartrent.domain.models.RentalApplication
import com.mg.smartrent.domain.models.User
import com.mg.smartrent.property.Application
import com.mg.smartrent.property.resource.rest.RentalApplicationRestController
import com.mg.smartrent.property.services.ExternalUserService
import com.mg.smartrent.property.services.PropertyService
import com.mg.smartrent.property.services.RentalApplicationService
import org.apache.commons.lang.RandomStringUtils
import org.mockito.InjectMocks
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.setup.MockMvcBuilders

import static com.mg.smartrent.domain.enums.EnRentalApplicationStatus.PendingOwnerReview
import static org.mockito.Mockito.when

@SpringBootTest(
        classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = ["eureka.client.enabled:false"]
)
class TestRentalApplicationsRestController extends IntegrationTestsSetup {

    @LocalServerPort
    private int port

    @Autowired
    private PropertyService propertyService

    @Autowired
    private RentalApplicationRestController restController

    @MockBean
    private ExternalUserService userService
    @Autowired
    @InjectMocks
    private RentalApplicationService rentalApplicationService

    static boolean initialized
    static String rootUrl;

    /**
     * Spring beans cannot be initialized in setupSpec : https://github.com/spockframework/spock/issues/76
     */
    def setup() {
        if (!initialized) {
            rootUrl = "http://localhost:$port/rest/rentalapplications"
            purgeCollection(User.class)
            purgeCollection(Property.class)
            purgeCollection(RentalApplication.class)
            mockMvc = MockMvcBuilders.standaloneSetup(restController).build()
            initialized = true
        }
    }

    def "test: save rental application"() {

        setup: "set mocked user id"
        Property property = createProperty()

        when: "create rental application"
        def response = createRentalApplication(property)

        then: "created"
        response.status == HttpStatus.OK.value()
        response.getContentAsString() == ""
    }

    def "test: get by propertyId"() {
        setup:
        Property property = createProperty()
        createRentalApplication(property)

        when:
        def url = "$rootUrl?propertyId=${property.getId()}"
        MvcResult result = doGet(mockMvc, url)

        then:
        result.getResponse().getStatus() == HttpStatus.OK.value()

        when:
        def listOfApplications = (List<RentalApplication>) mvcResultToModels(result, new TypeReference<List<RentalApplication>>() {
        })
        then:
        listOfApplications.size() == 1

        when:
        def dbRentalApplication = listOfApplications.get(0)

        then:
        dbRentalApplication.getId() != null
        dbRentalApplication.getPropertyId() == property.getId()
        dbRentalApplication.getRenterUserId() == property.getUserId()
    }

    def "test: get by renterUserId"() {
        setup:
        purgeCollection(RentalApplication.class)
        Property property = createProperty()
        createRentalApplication(property)

        when:
        def url = "$rootUrl?renterUserId=${property.getUserId()}"
        MvcResult result = doGet(mockMvc, url)

        then:
        result.getResponse().getStatus() == HttpStatus.OK.value()

        when:
        def listOfApplications = (List<RentalApplication>) mvcResultToModels(result, new TypeReference<List<RentalApplication>>() {
        })
        then:
        listOfApplications.size() == 1

        when:
        def application = listOfApplications.get(0)

        then:
        application.getId() != null
        application.getPropertyId() == property.getId()
        application.getRenterUserId() == property.getUserId()
    }


    def "test: get by in-existent propertyId"() {

        when:
        def url = "$rootUrl?propertyId=inExistent"
        MvcResult result = doGet(mockMvc, url)

        then:
        result.getResponse().getStatus() == HttpStatus.OK.value()
        result.getResponse().contentAsString == "[]"
    }

    def "test: get by id"() {

        setup: "model"
        Property property = createProperty()
        String rentApplicationID = RandomStringUtils.randomAlphabetic(10)
        createRentalApplication(property, rentApplicationID)

        when:
        def url = "$rootUrl/${rentApplicationID}"
        MvcResult result = doGet(mockMvc, url)

        then:
        result.getResponse().getStatus() == HttpStatus.OK.value()

        when:
        def dbRentalApplication = (RentalApplication) mvcResultToModel(result, RentalApplication.class)

        then:
        dbRentalApplication != null
    }


//--------------------Private Methods-----------------------------

    private createProperty() {
        def userId = "mockUserID"

        MockitoAnnotations.initMocks(this)
        when(userService.userExists(userId)).thenReturn(true)//mock external service call

        Property p = TestUtils.generateProperty()
        p.setUserId(userId)
        p = propertyService.save(p)

        return p
    }

    private MockHttpServletResponse createRentalApplication(Property property) {
        return createRentalApplication(property, null)
    }

    private MockHttpServletResponse createRentalApplication(Property property, String rentalApplicationId) {
        RentalApplication rentalApplication = TestUtils.generateRentalApplication()
        rentalApplication.setId(rentalApplicationId)
        rentalApplication.setStatus(PendingOwnerReview)
        rentalApplication.setPropertyId(property.getId())
        rentalApplication.setRenterUserId(property.getUserId())

        def response = doPost(mockMvc, rootUrl, rentalApplication).getResponse()

        return response
    }
}


