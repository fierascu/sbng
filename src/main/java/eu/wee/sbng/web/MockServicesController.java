package eu.wee.sbng.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// plain REST mock under /api2/services - unrelated to the real Apache CXF SOAP endpoint
// in eu.wee.sbng.soap, which is published separately at /services.
@RestController
public class MockServicesController {

    private final InMemoryList services = new InMemoryList();

    @GetMapping("/api2/services")
    public String getList() {
        return "CXF_API_OK";
    }

    @GetMapping("/api2/services/")
    public List<String> getAllServices() {
        return services.getAll();
    }

    @PostMapping("/api2/services/{service}")
    public List<String> addToService(@PathVariable String service) {
        return services.add(service);
    }

}
