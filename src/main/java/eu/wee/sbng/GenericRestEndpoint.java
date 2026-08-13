package eu.wee.sbng;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class GenericRestEndpoint {

    private static List<String> VALUES = new ArrayList<>(List.of("API_OK"));

    @GetMapping("/api/todos")
    public List<String> getList() {
        return VALUES;
    }

    @PostMapping("/api/todos/{newVal}")
    public List<String> addToList(@PathVariable String newVal) {
        VALUES.add(newVal);
        return VALUES;
    }
}
