###### Liquibase-CDI

This "extension" to Liquibase was created in order to support multiple databases with a CDI container.  It is heavily influenced by the 
org.liquibase:liquibase-cdi module/cdi-extension, but unfortunately, the latter was designed only to support a single DB configuration.

This module is not designed as a CDI-extension, but rather leverages CDI Observers to launch the Liquibase startup process.

