package com.consol.citrus;

import java.util.ArrayList;

import com.consol.citrus.config.CitrusSpringConfig;
import com.consol.citrus.container.AfterSuite;
import com.consol.citrus.container.BeforeSuite;
import com.consol.citrus.context.TestContextFactory;
import com.consol.citrus.report.TestListeners;
import com.consol.citrus.report.TestSuiteListeners;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author Christoph Deppisch
 */
public class CitrusSpringContext extends CitrusContext {

    /** Basic Spring application context */
    private final ApplicationContext applicationContext;

    /**
     * Protected constructor using given builder to construct this instance.
     * @param builder
     */
    protected CitrusSpringContext(Builder builder) {
        super(builder);

        this.applicationContext = builder.applicationContext;
    }

    /**
     * Gets the basic Citrus Spring bean application context.
     * @return
     */
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * Closing Citrus and its application context.
     */
    public void close() {
        if (applicationContext instanceof ConfigurableApplicationContext) {
            if (((ConfigurableApplicationContext) applicationContext).isActive()) {
                ((ConfigurableApplicationContext) applicationContext).close();
            }
        }
    }

    /**
     * Initializing method loads Spring application context and reads bean definitions
     * such as test listeners and test context factory.
     * @return
     */
    public static CitrusSpringContext create() {
        return create(new AnnotationConfigApplicationContext(CitrusSpringConfig.class));
    }

    /**
     * Initializing method with Spring application context Java configuration class
     * that gets loaded as application context.
     * @param configClass
     * @return
     */
    public static CitrusSpringContext create(Class<? extends CitrusSpringConfig> configClass) {
        return create(new AnnotationConfigApplicationContext(configClass));
    }

    /**
     * Create new Citrus context with given Spring bean application context.
     * @param applicationContext
     * @return
     */
    public static CitrusSpringContext create(ApplicationContext applicationContext) {
        return new Builder()
                .withApplicationContext(applicationContext)
                .build();
    }

    /**
     * Spring aware Citrus context builder.
     */
    public static final class Builder extends CitrusContext.Builder {

        private ApplicationContext applicationContext;

        public Builder withApplicationContext(ApplicationContext applicationContext) {
            this.applicationContext = applicationContext;

            testListeners(applicationContext.getBean(TestListeners.class));
            testSuiteListeners(applicationContext.getBean(TestSuiteListeners.class));
            testContextFactory(applicationContext.getBean(TestContextFactory.class));
            beforeSuite(new ArrayList<>(applicationContext.getBeansOfType(BeforeSuite.class).values()));
            afterSuite(new ArrayList<>(applicationContext.getBeansOfType(AfterSuite.class).values()));

            return this;
        }

        public CitrusSpringContext build() {
            return new CitrusSpringContext(this);
        }
    }
}
