package lk.ac.ruhuna.dcs.cvmanagement.modules.admindashboard.api;

import lk.ac.ruhuna.dcs.cvmanagement.modules.admindashboard.api.dto.response.AdminDashboardMetricsResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.admindashboard.application.AdminDashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("/metrics")
    public AdminDashboardMetricsResponse getMetrics() {
        return adminDashboardService.getMetrics();
    }
}
