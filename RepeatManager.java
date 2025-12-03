import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RepeatManager {
    private final ArrayList<RepeatSchedule> list = new ArrayList<>();

    public void add(RepeatSchedule r) { list.add(r); }
    public void remove(RepeatSchedule r) { list.remove(r); }
    public List<RepeatSchedule> all() { return Collections.unmodifiableList(list); }
}
