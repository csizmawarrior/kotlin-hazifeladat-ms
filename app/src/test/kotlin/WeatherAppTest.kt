package hu.vanio.kotlin.hazifeladat.ms

import io.mockk.every
import io.mockk.mockk
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.test.Test

class WeatherAppTest {

    private val weatherAppService = WeatherAppService(httpClientMock)

    companion object {
        val responseBody = mockk<ResponseBody>()
        val httpClientMock = mockk<OkHttpClient>()
        val response = mockk<Response>()

        val httpErrorTestData = listOf(
            Pair(404, ""),
            Pair(200, ""),
            Pair(200, " "),
        )

        fun getWeatherData(timeList: List<LocalDateTime>, temperatureList: List<Number>): WeatherResultDto =
            WeatherResultDto(
                0, 0, 0, 0, "UTC", "UTC", 0,
                HourlyUnitDto("utc", "°C"), HourlyDto(timeList, temperatureList)
            )

        val dailyTemperatureData = listOf(
            Triple(
                listOf(
                    LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 0)),
                    LocalDateTime.of(LocalDate.now(), LocalTime.of(14, 0)),
                    LocalDateTime.of(LocalDate.now(), LocalTime.of(20, 0))
                ),
                listOf(20, 30, 25),
                mapOf(LocalDate.now() to 25.0)
            ),
            Triple(
                listOf(LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 0))),
                listOf(30),
                mapOf(LocalDate.now() to 30.0)
            ),
            Triple(
                listOf(
                    LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 0)),
                    LocalDateTime.of(LocalDate.now(), LocalTime.of(14, 0)),
                    LocalDateTime.of(LocalDate.now().plusDays(2), LocalTime.of(20, 0)),
                    LocalDateTime.of(LocalDate.now().plusDays(2), LocalTime.of(12, 0))
                ),
                listOf(25, 30, 25, 15),
                mapOf(LocalDate.now() to 27.5, LocalDate.now().plusDays(2) to 20.0)
            )
        )
    }

    @TestFactory
    fun `httpClient exception handling`() = httpErrorTestData
        .map { (code, body) ->

            // Given
            every { httpClientMock.newCall(any()).execute() } returns response
            every { response.body } returns responseBody
            every { response.code } returns code
            every { responseBody.string() } returns body

            // When + Then
            DynamicTest.dynamicTest("HTTP kliens válasza: HTTP code: $code response body: $body") {
                val exception = Assertions.assertThrows(Exception::class.java) {
                    weatherAppService.collectDailyTemperatureData()
                }
                Assertions.assertEquals("Nem sikerült a honlapról kinyerni a hőmérsékleti adatokat.", exception.message)
            }
        }

    @Test
    fun `httpClient returns null body`() {
        // Given
        every { httpClientMock.newCall(any()).execute() } returns response
        every { response.body } returns null
        every { response.code } returns 200

        // When + Then
        val exception = Assertions.assertThrows(Exception::class.java) {
            weatherAppService.collectDailyTemperatureData()
        }
        Assertions.assertEquals("Nem sikerült a honlapról kinyerni a hőmérsékleti adatokat.", exception.message)
    }

    @TestFactory
    fun `calculating temperature`() = dailyTemperatureData
        .map { (time, temperature, expectedResult) ->
            // Given
            val weatherData = getWeatherData(time, temperature)

            // When + Then
            DynamicTest.dynamicTest("Napi középhőmérséklet számítás: mérési időpontok: $time hőmérsékletek: $temperature elvárt eredmény: $expectedResult") {
                val result = calculateDailyTemperature(weatherData)

                // Then
                result.forEach {
                    Assertions.assertEquals(expectedResult[it.first], it.second)
                }
            }
        }

    @Test
    fun `calculating inconsistent temperature data`() {
        // Given
        val weatherData = getWeatherData(listOf(LocalDateTime.now(), LocalDateTime.now()), listOf(10))

        // When + Then
        val exception = Assertions.assertThrows(Exception::class.java) {
            calculateDailyTemperature(weatherData)
        }
        Assertions.assertEquals("Hibás adatok", exception.message)
    }

}