package com.mg.samartrent.user.integration.resource

import com.fasterxml.jackson.core.type.TypeReference
import com.mg.samartrent.user.TestUtils
import com.mg.samartrent.user.integration.IntegrationTestsSetup
import com.mg.smartrent.domain.models.Property
import com.mg.smartrent.domain.models.User
import com.mg.smartrent.property.Application
import com.mg.smartrent.property.resource.rest.PropertyRestController
import com.mg.smartrent.property.services.PropertyService
import com.mg.smartrent.property.services.ExternalUserService
import org.mockito.InjectMocks
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Stepwise

import static org.mockito.Mockito.when

@SpringBootTest(
        classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = ["eureka.client.enabled:false"]
)
@Stepwise
class TestPropertyRestController extends IntegrationTestsSetup {

    @LocalServerPort
    private int port;
    @Autowired
    private PropertyRestController restController

    @MockBean
    private ExternalUserService userService
    @Autowired
    @InjectMocks
    private PropertyService propertyService

    static boolean initialized

    static def userId = "mockUserID"
    static def dbProperty = TestUtils.generateProperty()

    /**
     * Spring beans cannot be initialized in setupSpec : https://github.com/spockframework/spock/issues/76
     */
    def setup() {
        if (!initialized) {
            purgeCollection(User.class)
            MockitoAnnotations.initMocks(this)
            when(userService.userExists(userId)).thenReturn(true)//mock external service call


            mockMvc = MockMvcBuilders.standaloneSetup(restController).build()
            initialized = true
        }
    }

    def "test: save property"() {
        setup: "set mocked user id"
        dbProperty.setUserId(userId)

        when:
        def url = "http://localhost:$port/rest/properties"
        def response = doPost(mockMvc, url, dbProperty).getResponse()

        then:
        response.status == HttpStatus.OK.value()
        response.getContentAsString() == ""
    }

    def "test: get by userId"() {

        when:
        def url = "http://localhost:$port/rest/properties?userId=${dbProperty.getUserId()}"
        MvcResult result = doGet(mockMvc, url)

        then:
        result.getResponse().getStatus() == HttpStatus.OK.value()

        when:
        def dbProperties = (List<Property>) mvcResultToModels(result, new TypeReference<List<Property>>() {})
        then:
        dbProperties.size() == 1

        when:
        dbProperty = dbProperties.get(0)

        then:
        dbProperty.getId() != null
        dbProperty.getUserId() != null
    }

    def "test: get by in-existent userId"() {

        when:
        def url = "http://localhost:$port/rest/properties?userId=inExistent"
        MvcResult result = doGet(mockMvc, url)

        then:
        result.getResponse().getStatus() == HttpStatus.OK.value()
        result.getResponse().contentAsString == "[]"
    }

    def "test: get property by Id"() {
        when:
        def url = "http://localhost:$port/rest/properties?id=${dbProperty.getId()}"
        MvcResult result = doGet(mockMvc, url)

        then:
        result.getResponse().getStatus() == HttpStatus.OK.value()

        when:
        def user = (Property) mvcResultToModel(result, Property.class)
        then:
        user.getId() == dbProperty.getId()
    }

    def "test: get by Id for in-existent property"() {

        when:
        def url = "http://localhost:$port/rest/properties?id=testInvId"
        MvcResult result = doGet(mockMvc, url)

        then:
        result.getResponse().getStatus() == HttpStatus.OK.value()
        result.getResponse().contentAsString == ""
    }

}


