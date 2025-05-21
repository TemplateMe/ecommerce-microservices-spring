rootProject.name = "microservices-spring"

include("common")
include("infrastructure:api-gateway")
include("infrastructure:config-server")
include("infrastructure:discovery-server")
include("infrastructure:file-server")
include("infrastructure:scheduling-server")
include("infrastructure:notification-server")

include("services:task-service")