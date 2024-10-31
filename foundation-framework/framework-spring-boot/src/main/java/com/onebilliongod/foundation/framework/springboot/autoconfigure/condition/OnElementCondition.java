package com.onebilliongod.foundation.framework.springboot.autoconfigure.condition;

import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.lang.annotation.Annotation;

/**
 * The OnElementCondition class is a custom Spring Boot conditional class used to control whether certain Spring components or Beans should be loaded.
 * It decides whether specific features should be enabled or disabled by reading toggle properties from the configuration file.
 */
abstract class OnElementCondition extends SpringBootCondition {
    private final String prefix;

    private final Class<? extends Annotation> annotationType;

    protected OnElementCondition(String prefix, Class<? extends Annotation> annotationType) {
        this.prefix = prefix;
        this.annotationType = annotationType;
    }

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        AnnotationAttributes annotationAttributes = AnnotationAttributes
                .fromMap(metadata.getAnnotationAttributes(this.annotationType.getName()));

        String element = annotationAttributes.getString("value");

        return getElementOutcome(context, element);
    }

    protected ConditionOutcome getElementOutcome(ConditionContext context, String element) {
        Environment environment = context.getEnvironment();
        String parent = parent();

        boolean parentEnabled = environment.getProperty(parent,Boolean.class,true);

        if (!parentEnabled || element.isEmpty()) {
            return new ConditionOutcome(parentEnabled, ConditionMessage.forCondition(this.annotationType)
                    .because(parent + " is " + parentEnabled));
        }

        String current = current(element);
        boolean currentEnabled = environment.getProperty(current,Boolean.class,false);
        return new ConditionOutcome(currentEnabled, ConditionMessage.forCondition(this.annotationType)
                .because(current + " is " + current));

    }

    private String current(String element) {
        return this.prefix + element + ".enabled";
    }

    private String parent() {
        return this.prefix + "enabled";
    }
}
