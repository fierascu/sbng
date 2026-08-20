package eu.wee.sbng.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TodosController {

    private final InMemoryList todos = new InMemoryList();

    @GetMapping("/api/todos")
    public List<String> getTodos() {
        return todos.getAll();
    }

    @PostMapping("/api/todos/{todo}")
    public List<String> addTodo(@PathVariable String todo) {
        return todos.add(todo);
    }
}
