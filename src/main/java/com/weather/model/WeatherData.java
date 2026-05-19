package com.weather.model;

public class WeatherData {
    private String city;
    private double temperature;
    private String condition;
    private int humidity;
    private double windSpeed;
    private String date;
    private String icon;
    private boolean favorite;
    private String country;

    public WeatherData() {}

    public WeatherData(String city, String country, double temperature, String condition, int humidity, double windSpeed, String date, String icon) {
        this.city = city;
        this.country = country;
        this.temperature = temperature;
        this.condition = condition;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.date = date;
        this.icon = icon;
        this.favorite = false;
    }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    public int getHumidity() { return humidity; }
    public void setHumidity(int humidity) { this.humidity = humidity; }
    public double getWindSpeed() { return windSpeed; }
    public void setWindSpeed(double windSpeed) { this.windSpeed = windSpeed; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
}