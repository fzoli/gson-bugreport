package sample;

import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.function.Consumer;

public class Validatables {

    /**
     * Validates a Validatable collection.
     * Rejects null collection and null objects.
     */
    public static <T> void validate(Collection<T> c) {
        Validate.notNull(c, "null collection"); // does not tolerate nulls!
        for (T o : c) {
            if (o instanceof Validatable) {
                ((Validatable) o).validate();
            }
            else {
                Validate.notNull(o, "null item");
            }
        }
    }

    /**
     * Validates a Validatable collection.
     * Rejects null collection and null objects.
     */
    public static <T> void validate(Collection<T> c, @Nonnull Consumer<Validatable> validator) {
        Validate.notNull(c, "null collection"); // does not tolerate nulls!
        for (T o : c) {
            if (o instanceof Validatable) {
                validator.accept((Validatable) o);
            }
            else {
                Validate.notNull(o, "null item");
            }
        }
    }

    /**
     * Validates a string collection.
     * Rejects null collection and empty values, but allows empty collection.
     */
    public static void validateNotEmpty(Collection<String> c) {
        Validate.notNull(c, "null collection"); // does not tolerate nulls!
        for (String o : c) {
            Validate.notEmpty(o, "empty string");
        }
    }

    private Validatables() {
    }

}
