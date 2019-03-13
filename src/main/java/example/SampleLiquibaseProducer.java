package example;

import liquibase.LiquibaseConfig;
import liquibase.annotation.LiquibaseType;
import liquibase.resource.ClassLoaderResourceAccessor;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.SQLException;

@ApplicationScoped
public class SampleLiquibaseProducer {

    /**
     * Default schema for the ADAMS database
     */
    protected final String defaultSchema = "dbo";

    @Resource(lookup = "java:datasources/Database1")
    private DataSource dataSource;

    @Resource(lookup = "java:datasources/Database2")
    private DataSource dataSource2;

    /**
     * Bean used to initialize an existing DB with liquibase changeset entries
     * @return
     * @throws NamingException
     * @throws SQLException
     */
    @Produces
    @LiquibaseType
    public LiquibaseConfig initializeConfig() throws NamingException, SQLException {
        LiquibaseConfig config = new LiquibaseConfig();
        config.setChangeLog("db/changelog/changelog-master.xml");
        config.setDataSource(dataSource);
        config.setResourceAccessor(new ClassLoaderResourceAccessor(getClass().getClassLoader()));
        config.setDefaultSchema(defaultSchema);
        config.setVerifyUnexpectedChangeSets(false);
        return config;
    }

    /**
     * Bean used to launch the liquibase configuration
     * @return
     * @throws NamingException
     * @throws SQLException
     */
    @Produces
    @LiquibaseType
    public LiquibaseConfig liveConfig() throws NamingException, SQLException {
        LiquibaseConfig config = new LiquibaseConfig();
        config.setChangeLog("db2/changelog/changelog-master.xml");
        config.setDataSource(dataSource2);
        config.setResourceAccessor(new ClassLoaderResourceAccessor(getClass().getClassLoader()));
        config.setDefaultSchema(defaultSchema);
        config.setVerifyUnexpectedChangeSets(true);
        return config;
    }
}
