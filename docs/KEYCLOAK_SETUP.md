# Keycloak Setup Guide for Microservices

## Overview
This guide explains how to configure Keycloak for JWT authentication in the microservices architecture.

## What Was Fixed

### 1. JWT Issuer URI Configuration
- **Problem**: API Gateway was trying to connect to `localhost:8180` from inside the container
- **Solution**: Changed API Gateway to use `http://keycloak:8080/realms/ms-realm` for internal communication
- **Location**: `infrastructure/config-server/src/main/resources/configurations/api-gateway.yaml`

### 2. Keycloak Hostname Configuration
- **Added**: `KC_HOSTNAME_URL` and `KC_HOSTNAME_ADMIN_URL` to support external access
- **Location**: `docker-compose.yaml` Keycloak service configuration

## Required Setup Steps

### Step 1: Update Your .env File
Update your `.env` file with the correct values:

```bash
# Keep your existing values, but ensure these are correct:
KEYCLOAK_ADMIN_USERNAME=admin
KEYCLOAK_ADMIN_PASSWORD=admin
POSTGRES_KEYCLOAK_URL=jdbc:postgresql://postgres:5432/keycloak

# You can remove OAUTH_JWT_ISSUER_URI - it's now hardcoded in the config
```

### Step 2: Start the Services
```bash
# Start the infrastructure services first
docker-compose up -d postgres keycloak

# Wait for Keycloak to be ready (check logs)
docker-compose logs -f keycloak

# Once ready, start the rest
docker-compose up -d
```

### Step 3: Create the Realm in Keycloak

1. **Access Keycloak Admin Console**:
   - URL: http://localhost:8180/admin
   - Username: admin
   - Password: admin

2. **Create the `ms-realm` Realm**:
   - Click "Create Realm"
   - Name: `ms-realm`
   - Enabled: Yes
   - Click "Create"

3. **Configure the Realm**:
   - Go to Realm Settings
   - In the "General" tab, ensure:
     - Enabled: ON
     - User-managed access: ON
   - In the "Login" tab, configure as needed
   - In the "Keys" tab, ensure RSA keys are generated

4. **Create a Client** (if needed for your application):
   - Go to Clients
   - Click "Create client"
   - Client ID: `microservices-client`
   - Client authentication: ON
   - Authorization: ON
   - Save and configure as needed

### Step 4: Verify the Configuration

#### Check Keycloak Well-Known Configuration
```bash
# From your host machine
curl http://localhost:8180/realms/ms-realm/.well-known/openid-configuration

# From inside the Docker network (should also work)
docker exec api-gateway curl http://keycloak:8080/realms/ms-realm/.well-known/openid-configuration
```

#### Check API Gateway Health
```bash
curl http://localhost:8080/actuator/health
```

## Troubleshooting

### Issue: "Unable to resolve the Configuration with the provided Issuer"
- **Cause**: The realm doesn't exist or isn't properly configured
- **Solution**: Follow Step 3 to create the realm

### Issue: Connection Refused
- **Cause**: Keycloak isn't fully started
- **Solution**: Wait for Keycloak to be healthy before starting other services

### Issue: Invalid JWT Tokens
- **Cause**: Mismatch between issuer URI and actual realm
- **Solution**: Ensure the realm name matches exactly (`ms-realm`)

## Service Startup Order

The correct startup order is:
1. `postgres` (database)
2. `keycloak` (authentication server)
3. `config-server` (configuration)
4. `discovery-server` (service registry)
5. `api-gateway` (gateway with JWT validation)
6. Other services (`file-server`, `notification-server`, etc.)

## Testing JWT Authentication

Once everything is set up, you can test JWT authentication:

1. **Get a token from Keycloak** (if you have a client configured)
2. **Use the token in requests** to the API Gateway
3. **Verify the token is validated** by checking the logs

The API Gateway will now properly validate JWT tokens using the internal Keycloak connection.

## Additional Notes

- The API Gateway now uses `http://keycloak:8080/realms/ms-realm` for internal validation
- External clients should still use `http://localhost:8180/realms/ms-realm` for token requests
- The realm name `ms-realm` must match exactly in all configurations 