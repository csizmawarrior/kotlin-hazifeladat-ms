package hu.vanio.kotlin.hazifeladat.ms

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WeatherApp

fun main() {
    objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)

    runApplication<WeatherApp>()

}

