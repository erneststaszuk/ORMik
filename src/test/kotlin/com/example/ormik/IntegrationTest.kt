package com.example.ormik

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("integration")
@Import(TestcontainersConfiguration::class)
@SpringBootTest
interface IntegrationTest
