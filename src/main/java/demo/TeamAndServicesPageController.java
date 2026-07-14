package demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class TeamAndServicesPageController {

    @GetMapping("/team-and-services")
    String showTeamAndServicesPage() {
        return "team-and-services";
    }
}
