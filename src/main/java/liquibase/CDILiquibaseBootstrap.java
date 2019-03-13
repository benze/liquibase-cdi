package liquibase;

import liquibase.annotation.LiquibaseType;
import liquibase.exception.LiquibaseException;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * ApplicationScoped listener to bootstrap the Liquibase configuration
 */
@ApplicationScoped
public class CDILiquibaseBootstrap {
    // get a static slf4j logger for the class
    protected static final Logger logger = getLogger(CDILiquibaseBootstrap.class);

    /**
     * Launches the Liquibase startup process
     * @param o
     * @param beanManager
     */
    public void initializeLiquibase(@Observes @Initialized(ApplicationScoped.class) Object o, BeanManager beanManager) {
        logger.info("Application has started up, so launch Liquibase startup");

        // retrieve all instances of the liquibase configuration classes defined for DB initialization
        processLiquibaseConfigBeans(new LiquibaseType.Literal(true));
        processLiquibaseConfigBeans(new LiquibaseType.Literal());
    }


    /**
     * Process all LiquibaseType qualified beans
     * @param liquibaseType
     */
    private void processLiquibaseConfigBeans(LiquibaseType.Literal liquibaseType){
        Instance<LiquibaseConfig> configs = CDI.current().select(LiquibaseConfig.class, liquibaseType);
        if(configs.isUnsatisfied()) {
            logger.info("No matching Liquibase configurations found of type LiquibaseType(init={})", liquibaseType.init());
        } else {
            // loop over all config classes
            configs.forEach(config -> {
                // create a new Liquibase instance
                CDILiquibase instance = new CDILiquibase(config, config.getDataSource(), config.getResourceAccessor());
                instance.onStartup();
                if (!instance.isExecutionSuccessful())
                    throw new RuntimeException(new LiquibaseException("Error updating liquibase configuration"));
            });
        }
    }

}
