package org.junit.runners;
import org.junit.runners.parameterized.BlockJUnit4ClassRunnerWithParametersFactory;
import org.junit.runners.parameterized.ParametersRunnerFactory;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.Inherited;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Annotation;
@Retention ( RetentionPolicy.RUNTIME )
@Inherited
@Target ( { ElementType.TYPE } )
public @interface UseParametersRunnerFactory {
Class<? extends ParametersRunnerFactory> value() default BlockJUnit4ClassRunnerWithParametersFactory.class;
}
