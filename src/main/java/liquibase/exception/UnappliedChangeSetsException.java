package liquibase.exception;

import liquibase.changelog.ChangeSet;
import liquibase.configuration.GlobalConfiguration;
import liquibase.util.StreamUtil;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Exception thrown when Liquibase detects changesets in the changelog(s) that have not already been applied to the database and recorded
 * (in the {@link GlobalConfiguration#DATABASECHANGELOG_TABLE_NAME}
 */
public class UnappliedChangeSetsException extends MigrationFailedException {

    private Set<ChangeSet> changeSets;

    public UnappliedChangeSetsException(Collection<ChangeSet> changeSets) {
        this.changeSets = new HashSet<>(changeSets);
    }

    @Override
    public String getMessage() {
        StringBuffer out = new StringBuffer();
        out.append(String.valueOf(changeSets.size()));
        out.append( " change sets are part of the codebase but have not already been applied: ");
        out.append(StreamUtil.getLineSeparator());

        for (ChangeSet changeSet : changeSets) {
            out.append("     ").append(changeSet.toString(false))
                    .append(StreamUtil.getLineSeparator());
        }

        return out.toString();
    }
}
