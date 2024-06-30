package hu.vanio.kotlin.hazifeladat.ms

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import okhttp3.OkHttpClient
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootApplication
class WeatherApp

const val BEGINNING_TEXT = "Budapest napi középhőmérsékletének számítása a következő hét napra..."
const val BEGINNING_CLARIFICATTION_TEXT =
    "A napi középhőmérséklet számítását az adott napi összes mérési eredmény összegét azok számával elosztva végeztem el."

fun main() {
    objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    objectMapper.registerModules(JavaTimeModule())

    runApplication<WeatherApp>()
    println(BEGINNING_CLARIFICATTION_TEXT)
    println(BEGINNING_TEXT)

    try {
        dailyTemperatureCount()
    } catch (e: Exception) {
        println("Hiba a napi középhőmérséklet számítása közben: ${e.localizedMessage}")
    }

}

private fun dailyTemperatureCount() {
    val weatherAppService = WeatherAppService(OkHttpClient.Builder().build())
    val weatherResult = weatherAppService.collectDailyTemperatureData()

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