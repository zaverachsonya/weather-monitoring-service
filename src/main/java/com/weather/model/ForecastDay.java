package com.weather.model;

public class ForecastDay {
    private String date;
    private double temp;
    private String description;
    private String icon;

    public ForecastDay(String date, double temp, String description, String icon) {
        this.date = date;
        this.temp = temp;
        this.description = description;
        this.icon = icon;
    }
    public String getDate() { return date; }
    public double getTemp() { return temp; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
}