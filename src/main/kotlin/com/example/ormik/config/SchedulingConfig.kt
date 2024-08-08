package com.example.ormik.config

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.EnableScheduling

@Profile("!integration")
@Configuration
@EnableScheduling
class SchedulingConfig