package com.study.eureka.server.indicator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;


@Component
public class RocketMQHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        //int erroCode = check();
        int erroCode = 1;
        if(erroCode != 0){
            return Health.down().withDetail("ERROR Code",erroCode).build();
        }
        return Health.up().build();
    }
}
