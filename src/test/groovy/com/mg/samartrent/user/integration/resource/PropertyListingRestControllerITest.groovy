package com.mg.samartrent.user.integration.resource

import com.fasterxml.jackson.core.type.TypeReference
import com.mg.samartrent.user.TestUtils
import com.mg.samartrent.user.integration.IntegrationTestsSetup
import com.mg.smartrent.domain.models.Property
import com.mg.smartrent.domain.models.PropertyListing
import com.mg.smartrent.domain.models.User
import com.mg.smartrent.property.Application
import com.mg.smartrent.property.resource.rest.PropertyListingRestController
import com.mg.smartrent.property.services.PropertyService
import com.mg.smartrent.property.services.ExternalUserService
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
import spock.lang.Stepwise

import static org.mockito.Mockito.when

@SpringBootTest(
        classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = ["eureka.client.enabled:false"]
)
@Stepwise
class PropertyListingRestControllerITest extends IntegrationTestsSetup {

    @LocalServerPort
    private int port
    @Autowired
    private PropertyListingRestController restController

    @MockBean
    private ExternalUserService userService
    @Autowired
    @InjectMocks
    private PropertyService propertyService

    static boolean initialized
    static String rootUrl;

    /**
     * Spring beans cannot be initialized in setupSpec : https://github.com/spockframework/spock/issues/76
     */
    def setup() {
        if (!initialized) {
            purgeCollection(User.class)
            purgeCollection(Property.class)
            purgeCollection(PropertyListing.class)
            mockMvc = MockMvcBuilders.standaloneSetup(restController).build()
            rootUrl = "http://localhost:$port/rest/propertylistings"
            initialized = true
        }
    }

    def "test: save property listing"() {

        setup: "set mocked user id"
        Property property = createProperty()

        when: "create listing"
        def response = createListing(property)

        then: "created"
        response.status == HttpStatus.OK.value()
        response.getContentAsString() == ""
    }

    def "test: get by propertyId"() {
        setup:
        Property property = createProperty()
        createListing(property)

        when:
        def url = "$rootUrl?propertyId=${property.getId()}"
        MvcResult result = doGet(mockMvc, url)

        then:
        result.getResponse().getStatus() == HttpStatus.OK.value()

        when:
        def dbListings = (List<PropertyListing>) mvcResultToModels(result, new TypeReference<List<PropertyListing>>() {
        })
        then:
        dbListings.size() == 1

        when:
        def dbPropertyListing = dbListings.get(0)

        then:
        dbPropertyListing.getId() != null
        dbPropertyListing.getPropertyId() == property.getId()
        dbPropertyListing.getUserId() == property.getUserId()
    }

    def "test: get by in-existent propertyId"() {

        when:
        def url = "$rootUrl?propertyId=inExistent"
        MvcResult result = doGet(mockMvc, url)

        then:
        result.getResponse().getStatus() == HttpStatus.OK.value()
        result.getResponse().contentAsString == "[]"
    }

    def "test: get by Id"() {

        setup: "listing"
        Property property = createProperty()
        String listingId = RandomStringUtils.randomAlphabetic(10)
        createListing(property, listingId)

        when:
        MvcResult result = doGet(mockMvc, "$rootUrl/${listingId}")

        then:
        result.getResponse().getStatus() == HttpStatus.OK.value()

        when:
        def dbListing = (PropertyListing) mvcResultToModel(result, PropertyListing.class)

        then:
        dbListing != null
    }


    def "test: property listing - set listed=false"() {
        setup: "listing"
        Property property = createProperty()
        String listingId = RandomStringUtils.randomAlphabetic(10)
        createListing(property, listingId)


        when: "set published false"
        def url = "$rootUrl/${listingId}?publish=false"
        def response = doPost(mockMvc, url).getResponse()

        then:
        response.status == HttpStatus.OK.value()
        response.contentAsString == ""

        when: "getting listing from db"
        def result = doGet(mockMvc, "$rootUrl/${listingId}")
        def dbListing = (PropertyListing) mvcResultToModel(result, PropertyListing.class)

        then: "it is listing = false"
        !dbListing.isListed()
    }

    def "test: property listing - set listed=true"() {

        setup: "listing"
        Property property = createProperty()
        String listingId = RandomStringUtils.randomAlphabetic(10)
        createListing(property, listingId)


        when: "set published false"
        def url = "$rootUrl/${listingId}?publish=true"
        def response = doPost(mockMvc, url).getResponse()

        then:
        response.status == HttpStatus.OK.value()
        response.contentAsString == ""

        when: "getting listing from db"
        def result = doGet(mockMvc, "$rootUrl/${listingId}")
        def dbListing = (PropertyListing) mvcResultToModel(result, PropertyListing.class)

        then: "it is listing = false"
        dbListing.isListed()
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

    private MockHttpServletResponse createListing(Property property) {
        return createListing(property, null)
    }

    private MockHttpServletResponse createListing(Property property, String listingId) {
        PropertyListing listing = TestUtils.generatePropertyListing()
        listing.setId(listingId)
        listing.setUserId(property.getUserId())
        listing.setPropertyId(property.getId())

        def response = doPost(mockMvc, rootUrl, listing).getResponse()

        return response
    }
}


