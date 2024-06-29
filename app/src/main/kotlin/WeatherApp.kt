package hu.vanio.kotlin.hazifeladat.ms

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.rybalkinsd.kohttp.client.client
import io.github.rybalkinsd.kohttp.dsl.httpGet
import io.github.rybalkinsd.kohttp.ext.url
import okhttp3.OkHttpClient
import okhttp3.Response
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootApplication
class WeatherApp

val WEATHER_APP_ENDPOINT = "https://api.open-meteo.com/v1/forecast"
val BEGINNING_TEXT = "Budapest napi középhőmérsékletének számítása a következő hét napra..."
val BEGINNING_CLARIFICATTION_TEXT =
    "A napi középhőmérséklet számítását az adott napi összes mérési eredmény összegét azok számával elosztva végeztem el."

fun main() {
    objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    objectMapper.registerModules(JavaTimeModule())

    runApplication<WeatherApp>()
    println(BEGINNING_CLARIFICATTION_TEXT)
    println(BEGINNING_TEXT)

    try {
        val weatherResult = collectDailyTemperatureData()

        calculateDailyTemperature(weatherResult!!).forEach { (day, calculatedResult) ->
            println(
                "$day napi középhőmérséklet: ${
                    String.format(
                        "%.3f",
                        calculatedResult
                    )
                }${weatherResult.hourlyUnits.temperatureMeasurement}"
            )
        }
    } catch (e: Exception) {
        println("Hiba a napi középhőmérséklet számítása közben: ${e.localizedMessage}")
    }

}

fun collectDailyTemperatureData(): WeatherResultDto? {
    val response: Response = httpGet {
        client { OkHttpClient.Builder().build() }
        url(WEATHER_APP_ENDPOINT)
        param {
            "latitude" to 47.4984
            "longitude" to 19.0404
            "hourly" to "temperature_2m"
            "timezone" to "auto"
        }
    }
    if (response.body == null || response.code != 200) {
        throw Exception("Nem sikerült a honlapról kinyerni")
    }
    val result = response.body!!.string()
    return objectMapper.readValue(result)
}

fun calculateDailyTemperature(weatherData: WeatherResultDto): MutableList<Pair<LocalDate, Double>> {
    val timeList = weatherData.hourly.time
    if (timeList.size != weatherData.hourly.temperatureList.size) {
        throw Exception("Hibás adatok")
    }

    val days = timeList.groupBy { LocalDate.of(it.year, it.monthValue, it.dayOfMonth) }.keys.toList()
    val resultMap: Map<LocalDateTime, Number> =
        timeList.map { it to weatherData.hourly.temperatureList[timeList.indexOf(it)] }.toMap()

    return days.map { day ->
        val dailyTemperatureList = timeList.filter {
            day.isEqual(LocalDate.of(it.year, it.monthValue, it.dayOfMonth))
        }.toList()
        val calculatedForDay =
            dailyTemperatureList.map { resultMap[it] ?: 0 }.sumOf { it.toDouble() }.div(dailyTemperatureList.size)

        day to calculatedForDay
    }.toMutableList()
}