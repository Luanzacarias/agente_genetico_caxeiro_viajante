import java.util.ArrayList;
import java.util.List;

public class IsValidFeedback {
    public int distance;
    public List<Integer> visited;
    public boolean isValid;
    public boolean[][] person;

    public IsValidFeedback(boolean[][] person) {
        visited = new ArrayList<>();
        isValid = false;
        distance = 0;
        this.person = person;
    }
}
