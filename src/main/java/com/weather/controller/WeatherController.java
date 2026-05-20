package com.weather.controller;

import com.weather.model.WeatherData;
import com.weather.repository.WeatherRepository;
import com.weather.service.WeatherApiService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import com.weather.model.ForecastDay;

@Controller
public class WeatherController {
    private final WeatherApiService apiService;
    private final WeatherRepository repository;


    private final List<String> tableCities = List.of(
            "Минск", "Брест", "Гродно", "Витебск", "Могилев",
            "Москва", "Санкт-Петербург", "Киев", "Варшава", "Прага",
            "Берлин", "Мадрид", "Астана",
            "Пекин", "Дубай", "Стамбул", "Сеул", "Ереван"
    );

    public WeatherController(WeatherApiService apiService, WeatherRepository repository) {
        this.apiService = apiService;
        this.repository = repository;
    }


    @GetMapping("/")
    public String index(Model model,
                        @RequestParam(required = false) String sort,
                        @RequestParam(required = false) String newCity) {

        List<WeatherData> userHistory = repository.getAll();
        if (userHistory == null) userHistory = new ArrayList<>();

        List<String> cardCities = repository.getPopularCities();

        List<WeatherData> popularCards = new ArrayList<>();
        for (String city : cardCities) {
            try {
                WeatherData data = apiService.getRemoteWeather(city);
                if (data != null) {
                    userHistory.stream()
                            .filter(h -> h.getCity().equalsIgnoreCase(data.getCity()))
                            .findFirst()
                            .ifPresent(h -> data.setFavorite(h.isFavorite()));
                    popularCards.add(data);
                }
            } catch (Exception e) {}
        }


        List<WeatherData> mainTable = new ArrayList<>();

        java.util.Set<String> allCitiesToDisplay = new java.util.LinkedHashSet<>();


        userHistory.forEach(h -> allCitiesToDisplay.add(h.getCity()));
        allCitiesToDisplay.addAll(tableCities);


        for (String cityName : allCitiesToDisplay) {
            try {
                WeatherData freshData = apiService.getRemoteWeather(cityName);
                if (freshData != null) {
                    userHistory.stream()
                            .filter(h -> h.getCity().equalsIgnoreCase(freshData.getCity()))
                            .findFirst()
                            .ifPresent(h -> freshData.setFavorite(h.isFavorite()));

                    mainTable.add(freshData);
                }
            } catch (Exception e) {
                userHistory.stream()
                        .filter(h -> h.getCity().equalsIgnoreCase(cityName))
                        .findFirst()
                        .ifPresent(mainTable::add);
            }
        }

        Comparator<WeatherData> comp = Comparator.comparing(WeatherData::isFavorite).reversed();
        if (sort != null) {
            switch (sort) {
                case "temp" -> comp = comp.thenComparingDouble(WeatherData::getTemperature);
                case "temp_desc" -> comp = comp.thenComparing(Comparator.comparingDouble(WeatherData::getTemperature).reversed());
                case "city" -> comp = comp.thenComparing(WeatherData::getCity, String.CASE_INSENSITIVE_ORDER);
                case "condition" -> comp = comp.thenComparing(WeatherData::getCondition);
            }
        } else {
            comp = comp.thenComparing(WeatherData::getCity, String.CASE_INSENSITIVE_ORDER);
        }
        mainTable.sort(comp);

        model.addAttribute("popular", popularCards);
        model.addAttribute("history", mainTable);
        model.addAttribute("highlightedCity", newCity != null ? newCity : "");
        model.addAttribute("currentPopularNames", cardCities); // Добавим, чтобы заполнить поля в модалке


        return "index";
    }
    @GetMapping("/forecast/{city}")
    public String forecast(@PathVariable String city, Model model) {
        try {
            List<ForecastDay> days = apiService.getForecast(city);
            model.addAttribute("city", city);
            model.addAttribute("days", days);
        } catch (Exception e) {
            return "redirect:/";
        }
        return "forecast";
    }
    @PostMapping("/update-popular")
    public String updatePopular(@RequestParam("cities") List<String> cities) {
        List<String> filtered = cities.stream()
                .filter(c -> c != null && !c.isBlank())
                .limit(5)
                .toList();
        repository.savePopularCities(filtered);
        return "redirect:/";
    }

    @PostMapping("/fetch")
    public String fetch(@RequestParam String city) {
        String encodedCity = "";
        try {
            WeatherData data = apiService.getRemoteWeather(city);
            if (data != null) {
                repository.save(data);
                encodedCity = UriUtils.encode(data.getCity(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
        }
        return "redirect:/?newCity=" + encodedCity;
    }

    @PostMapping("/favorite")
    public String toggleFavorite(@RequestParam String city) {
        List<WeatherData> history = repository.getAll();
        boolean found = false;

        for (WeatherData d : history) {
            if (d.getCity().equalsIgnoreCase(city)) {
                d.setFavorite(!d.isFavorite());
                found = true;
                break;
            }
        }

        if (!found) {
            try {
                WeatherData data = apiService.getRemoteWeather(city);
                if (data != null) {
                    data.setFavorite(true);
                    history.add(data);
                }
            } catch (Exception e) {
                System.err.println("Ошибка при добавлении в избранное");
            }
        }

        repository.saveAll(history);
        return "redirect:/";
    }
}