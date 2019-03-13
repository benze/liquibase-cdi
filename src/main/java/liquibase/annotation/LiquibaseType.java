package liquibase.annotation;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;

/**
 * Qualifier Annotation
 *
 *  @author Aaron Walker (http://github.com/aaronwalker)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD,METHOD,PARAMETER,TYPE})
@Qualifier
public @interface LiquibaseType {
    /**
     * If this LiquibaseType is part of the initialization process for an existing DB
     */
    boolean init() default false;

    /**
     * Inner type to define an annotation type
     */
    class Literal extends AnnotationLiteral<LiquibaseType> implements LiquibaseType {

        private final boolean init;

        public Literal(boolean init) {
            this.init = init;
        }

        public Literal() {
            this.init = false;
        }

        @Override
        public boolean init() {
            return init;
        }

    }
}
