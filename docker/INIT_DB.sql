-- Create the scheduling database
CREATE DATABASE scheduling;

-- Grant privileges to the current user (which will be the user from POSTGRES_USERNAME)
GRANT ALL PRIVILEGES ON DATABASE scheduling TO CURRENT_USER;

-- Also grant to the default 'postgres' superuser if it exists
DO $$
BEGIN
    IF EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'postgres') THEN
        GRANT ALL PRIVILEGES ON DATABASE scheduling TO postgres;
    END IF;
END
$$;