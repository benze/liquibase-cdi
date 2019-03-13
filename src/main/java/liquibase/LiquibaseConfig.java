package liquibase;

import liquibase.resource.ResourceAccessor;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Basic configuration class used to store all configuration parameters required for Liquibase
 *
 * @author Eric Benzacar
 */
public class LiquibaseConfig {

    private ResourceAccessor resourceAccessor;
    private DataSource dataSource;
    private String contexts;
    private String labels;
    private String changeLog;
    private Map<String,String> parameters;
    private String defaultSchema;
    private boolean verifyUnexpectedChangeSets;

    public String getContexts() {
        return contexts;
    }

    public void setContexts(String contexts) {
        this.contexts = contexts;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public String getChangeLog() {
        return changeLog;
    }

    public void setChangeLog(String changeLog) {
        this.changeLog = changeLog;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public String getDefaultSchema() {
        return defaultSchema;
    }

    public void setDefaultSchema(String defaultSchema) {
        this.defaultSchema = defaultSchema;
    }

    public ResourceAccessor getResourceAccessor() {
        return resourceAccessor;
    }

    public void setResourceAccessor(ResourceAccessor resourceAccessor) {
        this.resourceAccessor = resourceAccessor;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public boolean isVerifyUnexpectedChangeSets() {
        return verifyUnexpectedChangeSets;
    }

    public void setVerifyUnexpectedChangeSets(boolean verifyUnexpectedChangeSets) {
        this.verifyUnexpectedChangeSets = verifyUnexpectedChangeSets;
    }
}
