package demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class DemoGuidePageController {

    @GetMapping("/how-duke-greens-creates-value")
    String showDemoGuidePage() {
        return "demo-guide";
    }
}
