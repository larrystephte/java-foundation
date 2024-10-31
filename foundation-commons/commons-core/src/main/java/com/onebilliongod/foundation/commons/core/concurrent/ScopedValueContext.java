package com.onebilliongod.foundation.commons.core.concurrent;


import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Encapsulates  ScopedValue to simplify its usage.
 * Note: ScopedValue is a preview feature, so the --enable-preview option must be added to THE JVM.
 * This can replace traditional ThreadLocal usage in many scenarios.
 */
public final class ScopedValueContext {
    private static final ScopedValue<Map<String, String>> CONTEXT = ScopedValue.newInstance();

    private ScopedValueContext() {

    }

    public static void withContext(Map<String, String> context, Runnable runnable) {
        ScopedValue.where(CONTEXT, context).run(runnable);
    }

    public static <V> V withContextSupplier(Map<String, String> context, Supplier<V> supplier) {
        return ScopedValue.where(CONTEXT, context).get(supplier);
    }

    public static <V> V withContextCallable(Map<String, String> context, Callable<V> callable) throws Exception {
        return ScopedValue.where(CONTEXT, context).call(callable);
    }

    public static Map<String, String> get() {
        return CONTEXT.get();
    }
}
