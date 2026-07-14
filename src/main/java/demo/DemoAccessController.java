package demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
class DemoAccessController {

    @GetMapping("/demo-access")
    String show(@RequestParam(required = false) final String error) {
        return "demo-access";
    }
}
