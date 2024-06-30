package hu.vanio.kotlin.hazifeladat.ms

import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import org.springframework.stereotype.Service

@Service
class WeatherAppService(val httpClient: OkHttpClient) {

    companion object {
        const val WEATHER_APP_ENDPOINT_HOST = "api.open-meteo.com"
        const val WEATHER_APP_ENDPOINT_PATH = "/v1/forecast"
    }

    fun collectDailyTemperatureData(): WeatherResultDto? {
        val resultBody = httpCall()

        if (resultBody == null || resultBody.string().isBlank()) {
            throw Exception("Nem sikerült a honlapról kinyerni a hőmérsékleti adatokat.")
        }

        return objectMapper.readValue(resultBody.string())
    }

    private fun httpCall(): ResponseBody? {
        val url = HttpUrl.Builder().host(WEATHER_APP_ENDPOINT_HOST)
            .port(443)
            .scheme("https")
            .addPathSegment(WEATHER_APP_ENDPOINT_PATH)
            .addQueryParameter("latitude", "47.4984")
            .addQueryParameter("longitude", "19.0404")
            .addQueryParameter("hourly", "temperature_2m")
            .addQueryParameter("timezone", "auto").build()
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        val response = httpClient.newCall(request).execute()

        if (response.code != 200) {
            throw Exception("Nem sikerült a honlapról kinyerni a hőmérsékleti adatokat.")
        }
        return response.body
    }
}