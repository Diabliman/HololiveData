package com.example.HololiveData.controller;

import com.example.HololiveData.service.VtuberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VtuberController {

    @Autowired
    VtuberService vtuberService;

    @GetMapping("/test")
    public String index(final Model model) {
        model.addAttribute("vtubers", vtuberService.getVtubers());
        System.out.println("test");
        return "index";
    }
}
