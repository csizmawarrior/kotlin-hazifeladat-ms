package hu.vanio.kotlin.hazifeladat.ms

import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.stereotype.Service

@Service
class WeatherAppService(val httpClient: OkHttpClient) {

    companion object {
        const val WEATHER_APP_ENDPOINT_HOST = "api.open-meteo.com"
        const val WEATHER_APP_ENDPOINT_PATH1 = "v1"
        const val WEATHER_APP_ENDPOINT_PATH2 = "forecast"
    }

    fun collectDailyTemperatureData(): WeatherResultDto? {
        val resultBody = httpCall()

        return objectMapper.readValue(resultBody)
    }

    private fun httpCall(): String {
        val url = HttpUrl.Builder().host(WEATHER_APP_ENDPOINT_HOST)
            .port(443)
            .scheme("https")
            .addPathSegment(WEATHER_APP_ENDPOINT_PATH1)
            .addPathSegment(WEATHER_APP_ENDPOINT_PATH2)
            .addQueryParameter("latitude", "47.4984")
            .addQueryParameter("longitude", "19.0404")
            .addQueryParameter("hourly", "temperature_2m")
            .addQueryParameter("timezone", "auto").build()
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        val response = httpClient.newCall(request).execute()
        val responseBodyString = response.body?.string() ?: ""

        if (response.code != 200 || responseBodyString.isBlank()) {
            throw Exception("Nem sikerült a honlapról kinyerni a hőmérsékleti adatokat.")
        }
        return responseBodyString
    }
}