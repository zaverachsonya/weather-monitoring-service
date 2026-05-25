package com.weather.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.model.User;
import org.springframework.stereotype.Repository;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Repository
public class UserRepository {
    private final String FILE_PATH = "users.json";
    private final ObjectMapper mapper = new ObjectMapper();

    public List<User> findAll() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return new ArrayList<>();
        try {
            return mapper.readValue(file, new TypeReference<List<User>>() {});
        } catch (IOException e) { return new ArrayList<>(); }
    }

    public void saveAll(List<User> users) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(FILE_PATH), users);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public Optional<User> findByUsername(String username) {
        return findAll().stream().filter(u -> u.getUsername().equals(username)).findFirst();
    }

    public void save(User user) {
        List<User> users = findAll();
        users.removeIf(u -> u.getUsername().equals(user.getUsername()));
        users.add(user);
        saveAll(users);
    }
}