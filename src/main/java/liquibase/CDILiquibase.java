package liquibase;

import liquibase.annotation.LiquibaseType;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;
import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnappliedChangeSetsException;
import liquibase.exception.UnexpectedChangeSetsException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.resource.ResourceAccessor;
import liquibase.util.LiquibaseUtil;
import liquibase.util.NetUtil;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * A CDI wrapper for Liquibase.
 *
 * This class is heavily influenced by the liquibase-cdi module:liquibase.integration.cdi.CDILiquibase class, but has been modified to support multiple configuration instances.
 *
 *
 * @author Eric Benzacar
 */
public class CDILiquibase{
    // get a static slf4j logger for the class
    protected static final org.slf4j.Logger logger = getLogger(CDILiquibase.class);

    ResourceAccessor resourceAccessor;
    private LiquibaseConfig config;
    private boolean initialized;
    private boolean executionSuccessful;

    @Inject
    public CDILiquibase(@LiquibaseType LiquibaseConfig config, @LiquibaseType DataSource dataSource, @LiquibaseType ResourceAccessor resourceAccessor){
        this.resourceAccessor = resourceAccessor;
        this.config = config;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isExecutionSuccessful() {
        return executionSuccessful;
    }

    @PostConstruct
    public void onStartup() {
        logger.info("Booting Liquibase {}.", LiquibaseUtil.getBuildVersion());
        String hostName;
        try {
            hostName = NetUtil.getLocalHostName();
        } catch (Exception e) {
            logger.warn( "Cannot find hostname: " + e.getMessage());
            logger.debug("{}", e);
            return;
        }

        LiquibaseConfiguration liquibaseConfiguration = LiquibaseConfiguration.getInstance();
        if (!liquibaseConfiguration.getConfiguration(GlobalConfiguration.class).getShouldRun()) {
            logger.info( "Liquibase did not run on {} because {} was set to false.",
                    hostName,
                    liquibaseConfiguration.describeValueLookupLogic(
                            GlobalConfiguration.class, GlobalConfiguration.SHOULD_RUN)
            );
            return;
        }
        initialized = true;

        try{
            logger.info("Running Liquibase in {} mode", liquibaseConfiguration.getConfiguration(CDIBootstrapConfiguration.class).getExecutionMode());
            // check to see which mode to run liquibase in
            switch(liquibaseConfiguration.getConfiguration(CDIBootstrapConfiguration.class).getExecutionMode()){
                case UPDATE:
                    // run the full automated liquibase updates
                    performUpdate();
                    break;
                case VALIDATE:
                    // validate that all changesets have been applied
                    List<ChangeSet> unrun = getUnrunChangeSets();
                    if( !unrun.isEmpty()){
                        throw new UnappliedChangeSetsException(unrun);
                    }
                    executionSuccessful = true;
                    break;
                case DISABLE:
                    // do nothing
                    logger.info("Skipping liquibase execution");
                    return;
            }

            // load any unexpected changesets from the database
            if( config.isVerifyUnexpectedChangeSets() ) {
                logger.info("Verifying if any unexpected changesets are detected by Liquibase in the database compared to the changelog({})", config.getChangeLog());
                Collection<RanChangeSet> ranChangeSets = getUnexpectedChangeSets();
                if (!ranChangeSets.isEmpty()) {
                    UnexpectedChangeSetsException exception = new UnexpectedChangeSetsException(ranChangeSets);
                    if (liquibaseConfiguration.getConfiguration(CDIBootstrapConfiguration.class).getHaltOnUnexpectedChangesets()) {
                        throw exception;
                    } else {
                        logger.warn(exception.getMessage());
                    }
                }
            } else {
                // display a warning if the halt on unexpected changesets is enabled but verification is disabled
                if( liquibaseConfiguration.getConfiguration(CDIBootstrapConfiguration.class).getHaltOnUnexpectedChangesets() ){
                    logger.warn("Configuration warning: This changelog configuration ({}) skips UnexpectedChangeset Verification, so application will not halt if unexpected changesets are encountered", config.getChangeLog());
                }
            }
        } catch (LiquibaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }


    /**
     * Validates that all changesets in the changelog have been applied
     * @return list of any changeSets that haven't been applied in the DB
     * @throws LiquibaseException
     */
    private List<ChangeSet> getUnrunChangeSets() throws LiquibaseException {
        Liquibase liquibase = null;
        try {
            Database database = createDatabase(config.getDataSource().getConnection());
            liquibase = createLiquibase(database);
            return liquibase.listUnrunChangeSets(new Contexts(config.getContexts()), new LabelExpression(config.getLabels()));
        } catch (SQLException e) {
            executionSuccessful = false;
            throw new LiquibaseException(e);
        } catch (LiquibaseException ex) {
            executionSuccessful = false;
            throw ex;
        } finally {
            if ((liquibase != null) && (liquibase.getDatabase() != null)) {
                liquibase.getDatabase().close();
            }
        }

    }

    /**
     * Checks the DB to see if there are any changesets in the DB that have already been run which are not part of the changelog(s)
     * @return Collection of any changeSets that have been applied in the DB but are not part of the changelog
     * @throws LiquibaseException
     */
    private Collection<RanChangeSet> getUnexpectedChangeSets() throws LiquibaseException {
        Liquibase liquibase = null;
        try {
            Database database = createDatabase(config.getDataSource().getConnection());
            liquibase = createLiquibase(database);
            return liquibase.listUnexpectedChangeSets(new Contexts(config.getContexts()), new LabelExpression(config.getLabels()));
        } catch (LiquibaseException ex) {
            executionSuccessful = false;
            throw ex;
        } catch (SQLException e) {
            executionSuccessful = false;
            throw new LiquibaseException(e);
        } finally {
            if ((liquibase != null) && (liquibase.getDatabase() != null)) {
                liquibase.getDatabase().close();
            }
        }
    }

    private void performUpdate() throws LiquibaseException {
        Liquibase liquibase = null;
        try {
            Database database = createDatabase(config.getDataSource().getConnection());
            liquibase = createLiquibase(database);
            liquibase.update(new Contexts(config.getContexts()), new LabelExpression(config.getLabels()));
            executionSuccessful = true;
        } catch (LiquibaseException ex) {
            executionSuccessful = false;
            throw ex;
        } catch (SQLException e) {
            executionSuccessful = false;
            throw new LiquibaseException(e);
        } finally {
            if ((liquibase != null) && (liquibase.getDatabase() != null)) {
                liquibase.getDatabase().close();
            }
        }
    }

    protected Liquibase createLiquibase(Database database) throws LiquibaseException {
        logger.info("Using Changelog {}", config.getChangeLog());
        Liquibase liquibase = new Liquibase(config.getChangeLog(), resourceAccessor, database);
        if (config.getParameters() != null) {
            for(Map.Entry<String, String> entry: config.getParameters().entrySet()) {
                liquibase.setChangeLogParameter(entry.getKey(), entry.getValue());
            }
        }

        return liquibase;
    }

    /**
     * Subclasses may override this method add change some database settings such as
     * default schema before returning the database object.
     * @param c
     * @return a Database implementation retrieved from the {@link DatabaseFactory}.
     * @throws DatabaseException
     */
    protected Database createDatabase(Connection c) throws DatabaseException {
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c));
        if (config.getDefaultSchema() != null) {
            database.setDefaultSchemaName(config.getDefaultSchema());
        }
        return database;
    }
}
