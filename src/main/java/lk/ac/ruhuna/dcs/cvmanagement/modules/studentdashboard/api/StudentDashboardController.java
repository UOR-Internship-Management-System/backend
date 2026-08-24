package lk.ac.ruhuna.dcs.cvmanagement.modules.studentdashboard.api;

import lk.ac.ruhuna.dcs.cvmanagement.modules.studentdashboard.api.dto.response.StudentDashboardMetricsResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentdashboard.application.StudentDashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/dashboard")
public class StudentDashboardController {

    private final StudentDashboardService studentDashboardService;

    public StudentDashboardController(StudentDashboardService studentDashboardService) {
        this.studentDashboardService = studentDashboardService;
    }

    @GetMapping("/metrics")
    public StudentDashboardMetricsResponse getMetrics() {
        return studentDashboardService.getMetrics();
    }
}
