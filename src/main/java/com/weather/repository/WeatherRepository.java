package com.weather.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.model.WeatherData;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class WeatherRepository {
    private final String FILE_PATH = "weather_history.json";
    private final ObjectMapper mapper = new ObjectMapper();

    public List<WeatherData> getAll() {
        File file = new File(FILE_PATH);
        if (!file.exists() || file.length() == 0) return new ArrayList<>();
        try {
            return mapper.readValue(file, new TypeReference<List<WeatherData>>() {});
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public void saveAll(List<WeatherData> list) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(FILE_PATH), list);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void save(WeatherData data) {
        List<WeatherData> list = getAll();
        list.removeIf(d -> d.getCity().equalsIgnoreCase(data.getCity()));
        list.add(data);
        saveAll(list);
    }
    public void toggleFavorite(String city) {
        List<WeatherData> list = getAll();
        for (WeatherData d : list) {
            if (d.getCity().equalsIgnoreCase(city)) {
                d.setFavorite(!d.isFavorite());
            }
        }
        try {
            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(new File(FILE_PATH), list);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private final String POPULAR_FILE_PATH = "popular_settings.json";

    public List<String> getPopularCities() {
        File file = new File(POPULAR_FILE_PATH);
        if (!file.exists()) {
            return List.of("Минск", "Москва", "Токио", "Лондон", "Нью-Йорк");
        }
        try {
            return mapper.readValue(file, new TypeReference<List<String>>() {});
        } catch (IOException e) {
            return List.of("Минск", "Москва", "Токио", "Лондон", "Нью-Йорк");
        }
    }

    public void savePopularCities(List<String> cities) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(POPULAR_FILE_PATH), cities);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}