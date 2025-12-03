import java.util.UUID;


public abstract class BaseSchedule implements ISchedule {
    protected final String id;
    protected final String fromRepeatId;
    protected String todo;


    protected BaseSchedule(String todo) {
        this(todo, null);
    }


    protected BaseSchedule(String todo, String fromRepeatId) {
        this.id = UUID.randomUUID().toString();
        this.todo = todo;
        this.fromRepeatId = fromRepeatId;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getTodo() {
        return todo;
    }

    @Override
    public String getFromRepeatId() {
        return fromRepeatId;
    }


    public boolean isDerivedFromRepeat() {
        return fromRepeatId != null;
    }

    @Override
    public String toString() {
        return todo;
    }
}