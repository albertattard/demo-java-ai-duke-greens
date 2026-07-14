package demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class CapabilitiesAndAiApproachPageController {

    @GetMapping("/capabilities-and-ai-approach")
    String showCapabilitiesAndAiApproachPage() {
        return "capabilities-and-ai-approach";
    }
}
