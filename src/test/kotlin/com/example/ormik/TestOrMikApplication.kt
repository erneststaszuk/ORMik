package com.example.ormik

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
  fromApplication<OrMikApplication>().with(TestcontainersConfiguration::class).run(*args)
}
