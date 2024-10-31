package com.onebilliongod.foundation.commons.core;

import com.onebilliongod.foundation.commons.core.concurrent.ScopedValueContext;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class ScopedValueContextTest {

    /**
     * test the ScopedValueContext class
     */
    @Test
    public void testScopedValue() {
        Map<String, String> context = new HashMap<>();
        context.put("requestId", "12345");

        ScopedValueContext.withContext(context, () -> {
            System.out.println("Main thread context: " + ScopedValueContext.get());

            //test the Runnable in the ScopedValue
            ScopedValueContext.withContext(context, () -> {
                System.out.println("New thread context: " + ScopedValueContext.get().get("requestId"));
            });

            //test the Supplier in the ScopedValue
            String rs = ScopedValueContext.withContextSupplier(context, () -> {
                System.out.println("New Suppler context: " + ScopedValueContext.get().get("requestId"));
                return ScopedValueContext.get().get("requestId");
            });
            System.out.println("rs:" + rs);

            try {
                //test the Callable in the ScopedValue
                String rs1 = ScopedValueContext.withContextCallable(context, () -> {
                    System.out.println("New Callable context: " + ScopedValueContext.get().get("requestId"));
                    return ScopedValueContext.get().get("requestId");
                });
                System.out.println("rs1:" + rs1);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
