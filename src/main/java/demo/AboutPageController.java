package demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class AboutPageController {

    @GetMapping("/about")
    String page() {
        return "about";
    }
}
