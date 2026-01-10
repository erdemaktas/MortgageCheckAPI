package org.ing.mortgage.config;

import lombok.Getter;
import lombok.Setter;
import org.ing.mortgage.domain.Rate;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "mortgage.startup")
public class RateProperties {
    private List<Rate> rates = new ArrayList<>();
}
