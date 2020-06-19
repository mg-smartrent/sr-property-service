package com.mg.samartrent.user.integration

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import de.flapdoodle.embed.mongo.MongodExecutable
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.IMongodConfig
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.process.runtime.Network
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import spock.lang.Specification

@TestPropertySource(locations = "classpath:test-application.yml")
class IntegrationTestsSetup extends Specification {

    protected static MockMvc mockMvc
    private static MongodExecutable mongoExecutable = null

    @Autowired
    private MongoTemplate mongoTemplate


    def setupSpec() {
        startEmbeddedMongo()
    }

    def cleanupSpec() {
        stopEmbeddedMongo()
    }

    void purgeCollection(Class entityClazz) {
        mongoTemplate.dropCollection(entityClazz.simpleName)
        println "Collection ${entityClazz.simpleName} dropped."
    }

    MvcResult doPost(MockMvc mockMvc, String restUri) {
        return mockMvc.perform(MockMvcRequestBuilders.post(restUri).contentType(MediaType.APPLICATION_JSON)).andReturn()
    }


    MvcResult doPost(MockMvc mockMvc, String restUri, Object model) {
        return mockMvc
                .perform(MockMvcRequestBuilders.post(restUri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(model)))
                .andReturn()
    }

    MvcResult doPost(MockMvc mockMvc, String restUri, List models) {
        return mockMvc
                .perform(MockMvcRequestBuilders.post(restUri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(models)))
                .andReturn()
    }


    MvcResult doGet(MockMvc mockMvc, String restUri) {
        return mockMvc.perform(MockMvcRequestBuilders.get(restUri)).andReturn()
    }

    Object mvcResultToModel(MvcResult mvcResult, Class modelClass) {
        return new ObjectMapper().readValue(mvcResult.getResponse().getContentAsString(), modelClass)
    }


    List mvcResultToModels(MvcResult mvcResult, TypeReference typeReference) {
        return new ObjectMapper().readValue(mvcResult.getResponse().getContentAsString(), typeReference)
    }

    private static void startEmbeddedMongo() {
        MongodStarter starter = MongodStarter.getDefaultInstance()
        String bindIp = "localhost"
        int port = 12345
        IMongodConfig mongoConfig = new MongodConfigBuilder().version(Version.Main.PRODUCTION).net(new Net(bindIp, port, Network.localhostIsIPv6())).build()

        mongoExecutable = null
        try {
            mongoExecutable = starter.prepare(mongoConfig)
            mongoExecutable.start()
        } catch (Exception ignore) {
            if (mongoExecutable != null)
                mongoExecutable.stop()
        }

    }

    private static void stopEmbeddedMongo() {
        if (mongoExecutable != null) {
            mongoExecutable.stop()
        }
    }
}
