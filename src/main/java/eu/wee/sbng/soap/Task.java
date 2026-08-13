package eu.wee.sbng.soap;

import java.io.Serializable;

public class Task implements Serializable {

    private String name;
    private boolean done;

    public Task() {
    }

    public Task(String name, boolean done) {
        this.name = name;
        this.done = done;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
