package demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class LetsTalkPageController {

    @GetMapping("/lets-talk")
    String showLetsTalkPage() {
        return "lets-talk";
    }
}
