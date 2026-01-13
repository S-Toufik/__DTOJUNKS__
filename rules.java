import java.util.Objects;

public interface FieldAccessor<M, T> {

    T get(M model);

    void set(M model, T value);

    default boolean compareAndSet(
        M model,
        T expected,
        T update
    ) {
        if (Objects.equals(get(model), expected)) {
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
        for (ConditionalUpdate<M, ?> update : updates) {
            if (!matches(model, update)) {
                return false;
            }
        }
        updates.forEach(update -> applySingle(model, update));
        return true;
    }

    @SuppressWarnings("unchecked")
    private static <M, T> boolean applySingle(
        M model,
        ConditionalUpdate<M, ?> raw
    ) {
        ConditionalUpdate<M, T> update = (ConditionalUpdate<M, T>) raw;
        return update.getField()
            .compareAndSet(model, update.getExpected(), update.getUpdate());
    }

    @SuppressWarnings("unchecked")
    private static <M, T> boolean matches(
        M model,
        ConditionalUpdate<M, ?> raw
    ) {
        ConditionalUpdate<M, T> update = (ConditionalUpdate<M, T>) raw;
        return Objects.equals(
            update.getField().get(model),
            update.getExpected()
        );
    }
}

--------------
import java.util.function.Function;
import java.util.function.BiConsumer;

@SuppressWarnings("unchecked")
public enum UserField implements FieldAccessor<User, Object> {

    NAME(User::getName, User::setName),
    AGE(User::getAge, User::setAge),
    ACTIVE(User::isActive, User::setActive);

    private final Function<User, Object> getter;
    private final BiConsumer<User, Object> setter;

    UserField(
        Function<User, ?> getter,
        BiConsumer<User, ?> setter
    ) {
        this.getter = (Function<User, Object>) getter;
        this.setter = (BiConsumer<User, Object>) setter;
    }

    @Override
    public Object get(User user) {
        return getter.apply(user);
    }

    @Override
    public void set(User user, Object value) {
        setter.accept(user, value);
    }
}

