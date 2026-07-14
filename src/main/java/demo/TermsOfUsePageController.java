package demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class TermsOfUsePageController {

    @GetMapping("/terms")
    String showTermsOfUsePage() {
        return "terms";
    }
}
