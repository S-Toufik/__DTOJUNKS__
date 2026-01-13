import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.Objects;

public interface FieldAccessor<M, T> {

    Function<M, T> getter();
    BiConsumer<M, T> setter();

    default T get(M model) {
        return getter().apply(model);
    }

    default void set(M model, T value) {
        setter().accept(model, value);
    }

    default boolean compareAndSet(
        M model,
        T expected,
        T update
    ) {
        T current = get(model);
        if (Objects.equals(current, expected)) {
            set(model, update);
            return true;
        }
        return false;
    }
}

--------------------
import lombok.Value;

@Value
public class ConditionalUpdate<M, T> {
    FieldAccessor<M, T> field;
    T expected;
    T update;
}
------------------
import lombok.experimental.UtilityClass;
import java.util.List;
import java.util.Objects;

@UtilityClass
public class ConditionalUpdateHandler {

    public static <M> boolean applyAll(
        M model,
        List<ConditionalUpdate<M, ?>> updates
    ) {
        boolean allApplied = true;
        for (ConditionalUpdate<M, ?> update : updates) {
            allApplied &= applySingle(model, update);
        }
        return allApplied;
    }

    public static <M> boolean applyAllOrNothing(
        M model,
        List<ConditionalUpdate<M, ?>> updates
    ) {
        for (ConditionalUpdate<M, ?> u : updates) {
            if (!matches(model, u)) {
                return false;
            }
        }
        updates.forEach(u -> applySingle(model, u));
        return true;
    }

    @SuppressWarnings("unchecked")
    private static <M, T> boolean applySingle(
        M model,
        ConditionalUpdate<M, T> update
    ) {
        return update.getField()
            .compareAndSet(model, update.getExpected(), update.getUpdate());
    }

    @SuppressWarnings("unchecked")
    private static <M, T> boolean matches(
        M model,
        ConditionalUpdate<M, T> update
    ) {
        return Objects.equals(
            update.getField().get(model),
            update.getExpected()
        );
    }
}
--------------
import java.util.function.Function;
import java.util.function.BiConsumer;

public enum UserField<T> implements FieldAccessor<User, T> {

    NAME(User::getName, User::setName),
    AGE(User::getAge, User::setAge),
    ACTIVE(User::isActive, User::setActive);

    private final Function<User, T> getter;
    private final BiConsumer<User, T> setter;

    UserField(
        Function<User, T> getter,
        BiConsumer<User, T> setter
    ) {
        this.getter = getter;
        this.setter = setter;
    }

    @Override public Function<User, T> getter() { return getter; }
    @Override public BiConsumer<User, T> setter() { return setter; }
}
