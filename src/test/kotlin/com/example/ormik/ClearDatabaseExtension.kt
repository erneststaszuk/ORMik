package com.example.ormik

import com.zaxxer.hikari.HikariDataSource
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.junit.jupiter.SpringExtension


class ClearDatabaseExtension : AfterEachCallback {
  override fun afterEach(context: ExtensionContext) {
    val jdbcTemplate = SpringExtension.getApplicationContext(context).getBean(JdbcTemplate::class.java)
    val url = (jdbcTemplate.dataSource as HikariDataSource).jdbcUrl
    if (!url.startsWith("jdbc:tc")) {
      logger.warn(
        "Make sure you are using test database. Current datasource URL {}. Database cleanup not performed.",
        url
      )
      return
    }

    jdbcTemplate.execute("TRUNCATE TABLE policy CASCADE")
  }

  companion object {
    private val logger = LoggerFactory.getLogger(ClearDatabaseExtension::class.java)
  }
}