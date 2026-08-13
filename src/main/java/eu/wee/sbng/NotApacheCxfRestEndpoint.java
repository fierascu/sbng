package eu.wee.sbng;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class NotApacheCxfRestEndpoint {

    @GetMapping("/api/services")
    public List<String> getList() {
        return List.of("one", "two");
    }

}
