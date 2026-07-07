package demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class WelcomePageController {

    @GetMapping("/")
    String showWelcomePage() {
        return "welcome";
    }
}
