package cohorthub;

import java.util.Comparator;
import java.util.Objects;


public final class Learner {


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
