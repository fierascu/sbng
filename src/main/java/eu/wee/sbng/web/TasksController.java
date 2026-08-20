package eu.wee.sbng.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TasksController {

    private final InMemoryList tasks = new InMemoryList();

    @GetMapping("/api/tasks")
    public List<String> getTasks() {
        return tasks.getAll();
    }

    @PostMapping("/api/tasks/{task}")
    public List<String> addTasks(@PathVariable String task) {
        return tasks.add(task);
    }
}
