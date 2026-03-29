package ch.hatbe.jbof.core.jooq;

import org.jooq.Condition;

import static org.jooq.impl.DSL.noCondition;

public final class JooqConditions {
    private JooqConditions() {
    }

    public static Condition when(boolean apply, Condition condition) {
        return apply ? condition : noCondition();
    }
}
