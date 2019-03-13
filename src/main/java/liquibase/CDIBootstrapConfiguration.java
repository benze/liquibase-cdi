package liquibase;

import liquibase.configuration.AbstractConfigurationContainer;

/**
 * Custom Configuration class to support extended configuration parameters for the CDI Bootstrapped integration of liquibase
 */
public class CDIBootstrapConfiguration extends AbstractConfigurationContainer {

    /**
     * Different modes available for Liquibase execution
     */
    public enum ExecutionMode{
        /**
         * Updates the schema with Liquibase changesets
         */
        UPDATE,
        /**
         * Validates that all changesets have been applied.  Does not perform any UPDATE
         */
        VALIDATE,

        /**
         * Disables Liquibase altogether
         */
        DISABLE
    }

    public static final String EXECUTION_MODE = "executionMode";
    public static final String HALT_ON_UNEXPECTED_CHANGESETS = "haltUnexpectedChangeSets";

    public CDIBootstrapConfiguration() {
        super("liquibase");
        getContainer().addProperty(EXECUTION_MODE, String.class)
                .setDescription("In which mode should Liquibase run; valid options are [VALIDATE, UPDATE, DISABLE]?  ")
                .setDefaultValue("UPDATE");

        getContainer().addProperty(HALT_ON_UNEXPECTED_CHANGESETS, Boolean.class)
                .setDescription("Should Liquibase halt deployment if unexpected changesets are detected?  (Defaults to false)")
                .setDefaultValue(false);
    }

    /**
     * Mode in which liquibase should be triggered (see {@link ExecutionMode}
     */
    public ExecutionMode getExecutionMode() {
        return ExecutionMode.valueOf(getContainer().getValue(EXECUTION_MODE, String.class).toUpperCase());
    }

    public CDIBootstrapConfiguration setExecutionMode(ExecutionMode mode) {
        getContainer().setValue(EXECUTION_MODE, mode.name());
        return this;
    }

    public boolean getHaltOnUnexpectedChangesets(){
        return getContainer().getValue(HALT_ON_UNEXPECTED_CHANGESETS, Boolean.class);
    }

    public CDIBootstrapConfiguration setHaltOnUnexpectedChangesets( boolean halt){
        getContainer().setValue(HALT_ON_UNEXPECTED_CHANGESETS, halt);
        return this;
    }

}
