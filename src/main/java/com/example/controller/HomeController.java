package com.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Contrôleur pour la gestion de la page d'accueil (Controller MVC)
 * Gère les requêtes de la page d'accueil
 */
@Controller
public class HomeController {

    @GetMapping({"/", "/index", "/home"})
    public String home() {
        return "index";
    }
} 