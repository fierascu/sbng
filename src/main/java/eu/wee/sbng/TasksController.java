package eu.wee.sbng;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class TasksController {

    private static final List<String> TASKS = new ArrayList<>();

    @GetMapping("/api/tasks")
    public List<String> getTasks() {
        return TASKS;
    }

    @PostMapping("/api/tasks/{task}")
    public List<String> addTasks(@PathVariable String task) {
        TASKS.add(task);
        return TASKS;
    }
}
