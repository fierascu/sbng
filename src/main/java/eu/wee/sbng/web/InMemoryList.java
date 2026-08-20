package eu.wee.sbng.web;

import java.util.ArrayList;
import java.util.List;

// backs the demo REST controllers below - no persistence, just a shared shape for
// "GET the list" / "POST an item, get the list back" so it isn't repeated in each controller.
// Spring MVC handles requests on multiple threads, so mutation is synchronized and callers get
// an immutable snapshot rather than the live list - otherwise a concurrent add() could corrupt
// a response another thread is still serializing off the same backing ArrayList.
class InMemoryList {

    private final List<String> items = new ArrayList<>();

    synchronized List<String> getAll() {
        return List.copyOf(items);
    }

    synchronized List<String> add(String item) {
        items.add(item);
        return List.copyOf(items);
    }

    synchronized void clear() {
        items.clear();
    }
}
