package hu.vanio.kotlin.hazifeladat.ms

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.LocalDateTime

val objectMapper = jacksonObjectMapper()

data class WeatherResultDto(
    val latitude: Number,
    val longitude: Number,
    @JsonProperty("generationtime_ms") val generationTime: Number,
    val utcOffsetSeconds: Number,
    val timezone: String,
    val timezoneAbbreviation: String,
    val elevation: Int,
    val hourlyUnits: HourlyUnitDto,
    val hourly: HourlyDto
)

data class HourlyUnitDto(
    val time: String,
    @JsonProperty("temperature_2m") val temperatureMeasurement: String)

data class HourlyDto(
    val time: List<LocalDateTime>,
    @JsonProperty("temperature_2m") val temperatureList: List<Number>
)