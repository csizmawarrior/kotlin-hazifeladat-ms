package hu.vanio.kotlin.hazifeladat.ms

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.datetime.LocalDateTime

val objectMapper = jacksonObjectMapper()

data class WeatherResultDto(
    val latitude: Double,
    val longitude: Double,
    @JsonProperty("generationtime_ms") val generationTime: Double,
    val utcOffsetSeconds: Long,
    val timezone: String,
    val timezoneAbbreviation: String,
    val elevation: Int,
    val hourlyUnits: HourlyUnitDto,
    val hourly: HourlyDto,
    @JsonProperty("temperature_2m") val temperatureList: MutableList<Number>)

data class HourlyUnitDto(
    val time: String,
    @JsonProperty("temperature_2m") val temperatureMeasurement: String)

data class HourlyDto(
    val time: MutableList<LocalDateTime>)