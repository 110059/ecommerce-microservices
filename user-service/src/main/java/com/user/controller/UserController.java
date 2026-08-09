package com.user.controller;

import com.user.model.User;
import com.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService service;

    @PostMapping
    public User addUser(@RequestBody User user) {
        return service.save(user);
    }

    @GetMapping
    public List<User> getUsers() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public String getUsers(@PathVariable int id) {
        return "Sync : data from user service id " + id;
    }

    @GetMapping("/admin")
    public String admin() {

        return "Admin API";

    }

    @GetMapping("/user")
    public String user() {

        return "User API";

    }

    @GetMapping("/profile")
    public String profile() {

        return "Logged In";

    }
}