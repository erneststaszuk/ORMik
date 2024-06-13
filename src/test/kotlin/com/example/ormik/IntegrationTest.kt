package com.example.ormik

import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("integration")
@Import(TestcontainersConfiguration::class)
@SpringBootTest
@ExtendWith(ClearDatabaseExtension::class)
interface IntegrationTest
