package com.rentalservice.antivirus.config;

import com.rentalservice.antivirus.eds.properties.EdsProperties;
import com.rentalservice.antivirus.security.properties.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({JwtProperties.class, EdsProperties.class})
public class ConfigurationPropertiesConfig {
}
