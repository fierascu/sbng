package eu.wee.sbng;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class TodosController {

    private static final List<String> TODOS = new ArrayList<>();

    @GetMapping("/api/todos")
    public List<String> getTodos() {
        return TODOS;
    }

    @PostMapping("/api/todos/{todo}")
    public List<String> addTodo(@PathVariable String todo) {
        TODOS.add(todo);
        return TODOS;
    }
}
