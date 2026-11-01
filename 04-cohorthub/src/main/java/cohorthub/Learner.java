package cohorthub;

import java.util.Comparator;
import java.util.Objects;

/**
 * A registered learner.
 *
 * Identity is the ID ONLY. equals() and hashCode() use just the ID, and the ID is final,
 * so a learner's hash code can never change while it sits inside a HashSet or HashMap.
 * Name and email can be updated without breaking the sets and maps that hold the learner.
 */
public final class Learner {

    /** Sort by name (ignoring case), then by ID when names match. */
    public static final Comparator<Learner> BY_NAME_THEN_ID =
            Comparator.comparing(Learner::getName, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(Learner::getId);

    private final String id;
    private String name;
    private String email;

    public Learner(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    void setName(String name) {
        this.name = name;
    }

    void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Learner other)) {
            return false;
        }
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return id + " | " + name + " | " + email;
    }
}
