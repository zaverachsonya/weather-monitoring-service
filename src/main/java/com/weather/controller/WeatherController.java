package com.weather.controller;

import com.weather.model.*;
import com.weather.repository.UserRepository;
import com.weather.service.WeatherApiService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.*;

@Controller
public class WeatherController {
    private final WeatherApiService apiService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    private final List<String> tableCities = List.of(
            "Минск", "Брест", "Гродно", "Витебск", "Могилев",
            "Москва", "Санкт-Петербург", "Киев", "Варшава", "Прага",
            "Берлин", "Мадрид", "Астана", "Пекин", "Дубай", "Стамбул", "Сеул", "Ереван"
    );


    public WeatherController(WeatherApiService apiService,
                             UserRepository userRepository,
                             PasswordEncoder passwordEncoder) {
        this.apiService = apiService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @GetMapping("/")
    public String index(Model model, Principal principal,
                        @RequestParam(required = false) String sort,
                        @RequestParam(required = false) String newCity) {


        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        List<WeatherData> userHistory = user.getHistory();
        List<String> cardCities = user.getPopularCities();

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
            } catch (Exception ignored) {}
        }

        List<WeatherData> mainTable = new ArrayList<>();
        Set<String> allCitiesToDisplay = new LinkedHashSet<>();
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
        model.addAttribute("currentPopularNames", cardCities);

        return "index";
    }

    @PostMapping("/update-popular")
    public String updatePopular(@RequestParam("cities") List<String> cities, Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).get();
        List<String> filtered = cities.stream()
                .filter(c -> c != null && !c.isBlank())
                .limit(5)
                .toList();
        user.setPopularCities(filtered);
        userRepository.save(user);
        return "redirect:/";
    }

    @PostMapping("/fetch")
    public String fetch(@RequestParam String city, Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).get();
        String encodedCity = "";
        try {
            WeatherData data = apiService.getRemoteWeather(city);
            if (data != null) {
                user.getHistory().removeIf(d -> d.getCity().equalsIgnoreCase(data.getCity()));
                user.getHistory().add(data);
                userRepository.save(user);
                encodedCity = UriUtils.encode(data.getCity(), StandardCharsets.UTF_8);
            }
        } catch (Exception ignored) {}
        return "redirect:/?newCity=" + encodedCity;
    }

    @PostMapping("/favorite")
    public String toggleFavorite(@RequestParam String city, Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).get();
        boolean found = false;
        for (WeatherData d : user.getHistory()) {
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
                    user.getHistory().add(data);
                }
            } catch (Exception ignored) {}
        }
        userRepository.save(user);
        return "redirect:/";
    }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/register")
    public String registerPage() { return "register"; }
    @PostMapping("/register")
    public String registerUser(@RequestParam String username, @RequestParam String password) {

        System.out.println("=== ПОПЫТКА РЕГИСТРАЦИИ ===");
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);

        if (userRepository.findByUsername(username).isPresent()) {
            System.out.println("ОШИБКА: Юзер уже есть!");
            return "redirect:/register?error";
        }

        User newUser = new User(username, passwordEncoder.encode(password));
        userRepository.save(newUser);

        System.out.println("УСПЕХ: Юзер сохранен!");
        return "redirect:/login";
    }


    @GetMapping("/forecast/{city}")
    public String forecast(@PathVariable String city, Model model) {
        try {
            List<ForecastDay> days = apiService.getForecast(city);
            model.addAttribute("city", city);
            model.addAttribute("days", days);
        } catch (Exception e) { return "redirect:/"; }
        return "forecast";
    }
}