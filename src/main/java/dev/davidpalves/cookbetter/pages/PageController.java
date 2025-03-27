package dev.davidpalves.cookbetter.pages;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PageController {
    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    @GetMapping("/login")
    public String login() {
        return "forward:/login.html";
    }

    @GetMapping("/signup")
    public String signup() {
        return "forward:/signup.html";
    }

    @GetMapping("/explore")
    public String explore() {
        return "forward:/explore.html";
    }

    @GetMapping("/search")
    public String search() {
        return "forward:/search.html";
    }

    @GetMapping("/profile/{path:[^\\\\.]+}")
    public String profile() {
        return "forward:/profile.html";
    }

    @GetMapping("/recipes")
    public String recipes() {
        return "forward:/recipes.html";
    }

    @GetMapping("/recipe/{path:[^\\\\.]+}")
    public String recipe() {
        return "forward:/recipe.html";
    }

    @GetMapping("/recipe/assets/{image:[^\\\\.]+}")
    public String recipeAssets(@PathVariable String image) {
        return "forward:/" + image;
    }
}
