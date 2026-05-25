package com.weather.model;
import java.util.*;

public class User {
    private String username;
    private String password;
    private List<String> popularCities = new ArrayList<>(List.of("Минск", "Москва", "Токио", "Лондон", "Нью-Йорк"));
    private List<WeatherData> history = new ArrayList<>();

    public User() {}
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public List<String> getPopularCities() { return popularCities; }
    public void setPopularCities(List<String> popularCities) { this.popularCities = popularCities; }
    public List<WeatherData> getHistory() { return history; }
    public void setHistory(List<WeatherData> history) { this.history = history; }
}