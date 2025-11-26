-- Create additional databases if needed
-- This script runs when the PostgreSQL container starts for the first time

-- The main database 'shopdb' is already created by the POSTGRES_DB environment variable
-- Additional setup can be done here if needed

-- Grant all privileges to the shopuser
GRANT ALL PRIVILEGES ON DATABASE shopdb TO shopuser;

-- You can add any additional database setup here
-- For example, creating additional schemas, users, etc.

-- Example: Create a schema for reporting
-- CREATE SCHEMA IF NOT EXISTS reporting;
-- GRANT USAGE ON SCHEMA reporting TO shopuser;
-- GRANT CREATE ON SCHEMA reporting TO shopuser;