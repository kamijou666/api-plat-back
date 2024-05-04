package com.kamijou.interfaceinfo.controller;


import com.kamijou.clientsdk.model.User;
import org.springframework.web.bind.annotation.*;

/**
 * 名称API
 */
@RestController
@RequestMapping("/name")
public class NameController {
    @GetMapping("/get")
    public String getNameByGet(String name) {
        return "GET Name Is " + name;
    }

    @PostMapping("/post")
    public String getNameByPost(@RequestParam String name) {
        return "POST Name Is " + name;
    }

    @PostMapping("/user")
    public String getUsernameByPost(@RequestBody User user) {
        return "POST Name Is " + user.getUsername();
    }
}
