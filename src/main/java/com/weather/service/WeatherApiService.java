package com.weather.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.weather.model.WeatherData;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.weather.model.ForecastDay;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;

@Service
public class WeatherApiService {

    // 1. Создаем переменную, в которую Spring положит ключ из application.properties
    @Value("${weather.api.key}")
    private String apiKey;

    public List<ForecastDay> getForecast(String city) {
        RestTemplate restTemplate = new RestTemplate();
        // 2. Используем apiKey здесь (вместо старого API_KEY)
        String url = String.format("http://api.openweathermap.org/data/2.5/forecast?q=%s&appid=%s&units=metric&lang=ru", city, apiKey);

        JsonNode root = restTemplate.getForObject(url, JsonNode.class);
        List<ForecastDay> forecast = new ArrayList<>();

        if (root != null && root.has("list")) {
            for (JsonNode node : root.path("list")) {
                String dtTxt = node.path("dt_txt").asText();

                if (dtTxt.contains("12:00:00")) {
                    forecast.add(new ForecastDay(
                            dtTxt.substring(0, 10),
                            node.path("main").path("temp").asDouble(),
                            node.path("weather").get(0).path("description").asText(),
                            node.path("weather").get(0).path("icon").asText()
                    ));
                }
            }
        }
        return forecast;
    }

    public WeatherData getRemoteWeather(String city) {
        RestTemplate restTemplate = new RestTemplate();
        // 3. И здесь тоже используем apiKey
        String url = String.format("http://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=metric&lang=ru", city, apiKey);

        JsonNode root = restTemplate.getForObject(url, JsonNode.class);

        return new WeatherData(
                root.path("name").asText(),
                root.path("sys").path("country").asText(),
                root.path("main").path("temp").asDouble(),
                root.path("weather").get(0).path("description").asText(),
                root.path("main").path("humidity").asInt(),
                root.path("wind").path("speed").asDouble(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                root.path("weather").get(0).path("icon").asText()
        );
    }
}