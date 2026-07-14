package demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class DemoNoticeAndResponsibleAiPageController {

    @GetMapping("/about-this-demonstration")
    String showDemoNoticeAndResponsibleAiPage() {
        return "demo-notice-and-responsible-ai";
    }
}
