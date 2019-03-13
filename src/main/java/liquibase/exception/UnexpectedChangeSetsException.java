package liquibase.exception;

import liquibase.changelog.RanChangeSet;
import liquibase.configuration.GlobalConfiguration;
import liquibase.util.StreamUtil;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Exception thrown when Liquibase detects changesets applied in the Database (in the {@link GlobalConfiguration#DATABASECHANGELOG_TABLE_NAME} that are not part
 * of the changelog(s) being applied
 */
public class UnexpectedChangeSetsException extends MigrationFailedException {
    private Set<RanChangeSet> changeSets;

    public UnexpectedChangeSetsException(Collection<RanChangeSet> changeSets) {
        this.changeSets = new HashSet<>(changeSets);
    }

    @Override
    public String getMessage() {
        StringBuffer out = new StringBuffer();
        out.append(String.valueOf(changeSets.size()));
        out.append( " change sets have been discovered in the database, but are not part of the liquibase changelog(s): ");
        out.append(StreamUtil.getLineSeparator());

        for (RanChangeSet changeSet : changeSets) {
            out.append("     ").append(changeSet.toString())
                    .append(StreamUtil.getLineSeparator());
        }

        return out.toString();
    }
}
