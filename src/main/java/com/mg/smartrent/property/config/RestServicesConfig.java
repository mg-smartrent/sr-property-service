package com.mg.smartrent.property.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * This class provides the external REST services URLs configures in the application properties file.
 */
@Configuration
public class RestServicesConfig {

    @Value("${rest.service.user}")
    private String usersServiceUri;


    public String getUsersServiceURI() {
        return this.usersServiceUri;
    }

    public void setUsersServiceUri(String usersServiceUri) {
        this.usersServiceUri = usersServiceUri;
    }

}
