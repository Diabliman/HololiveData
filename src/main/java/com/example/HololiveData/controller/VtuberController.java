package com.example.HololiveData.controller;

import com.example.HololiveData.model.Vtuber;
import com.example.HololiveData.service.VtuberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class VtuberController {

    @Autowired
    VtuberService vtuberService;

    @GetMapping("/")
    public String index(final Model model) {
        model.addAttribute("pageTitle", "HololiveData");
        model.addAttribute("vtuberPerGeneration", vtuberService.getVtuberOrderedPerGen());
        return "index";
    }
    @GetMapping("/vtuber/{id}")
    public String vtuber(@PathVariable("id") final String id, final Model model){
        Vtuber v = vtuberService.getVtuberByID(id);
        model.addAttribute("pageTitle", v.getName());
        model.addAttribute("vtuber", v);
        return "vtuber";
    }
}
