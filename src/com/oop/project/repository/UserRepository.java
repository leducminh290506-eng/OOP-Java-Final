package com.oop.project.repository;

import com.oop.project.model.User;
import com.oop.project.model.Role;
import java.util.ArrayList;
import java.util.Arrays; // Thêm import này
import java.util.List;
import java.util.Optional;

public class UserRepository {
    private List<User> users;

    public UserRepository() {
        // Thay List.of bằng Arrays.asList để chạy được trên Java 8
        this.users = new ArrayList<>(Arrays.asList(
            new User(1, "admin", "$2a$10$adminhashedpassword", Role.ADMIN),
            new User(2, "agent_john", "$2a$10$johnhashedpassword", Role.AGENT),
            new User(3, "agent_mary", "$2a$10$maryhashedpassword", Role.AGENT)
        ));
    }

    public Optional<User> findByUsername(String username) {
        return users.stream()
            .filter(user -> user.getUsername().equals(username))
            .findFirst();
    }

    public List<User> findAll() {
        return new ArrayList<>(users);
    }
}