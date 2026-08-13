package eu.wee.sbng;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class FakeCxfWebservice {

    private static final List<String> SERVICES = new ArrayList<>();

    @GetMapping("/services")
    public String getList() {
        return "CXF_API_OK";
    }

    @GetMapping("/services/")
    public List<String> getAllServices() {
        return SERVICES;
    }

    @PostMapping("/services/{service}")
    public List<String> addToService(@PathVariable String service) {
        SERVICES.add(service);
        return SERVICES;
    }

}
