# 🛠️ Microservice Setup Instructions

This guide provides detailed steps for setting up a new Kotlin-based Spring Boot microservice in our architecture.

---

## 1. 🧱 Generate New Spring Boot Project

* Go to [https://start.spring.io](https://start.spring.io)
* **Project**: `Gradle - Kotlin`
* **Language**: `Kotlin`
* **Spring Boot Version**: `3.4.5`

### Project Metadata:

* **Group**: `com.services`

* **Artifact/Name**: `{insert name}`

* **Description**: `{insert description}`

* **Packaging**: `Jar`

* **Java Version**: `21`

> 📥 Download the project and move it into the services folder.

---

## 2. ⚙️ Modify Gradle Build File

### Replace the `plugins` block in `build.gradle.kts` with:

```kotlin
plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.spring") version "2.1.0"
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
}
```

---

## 3. 🔧 Link Project in Root `settings.gradle.kts`

Add the following line:

```kotlin
include("services:{project-name}")
```

*Replace **`{project-name}`** with the actual folder/artifact name.*

---

## 4. 📦 Add Dependencies

Append the following to the `dependencies` block in `build.gradle.kts`:

```kotlin
implementation("org.jetbrains.kotlin:kotlin-reflect")
implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
implementation("org.springframework.cloud:spring-cloud-starter-config")
implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
implementation("org.springframework.boot:spring-boot-starter-actuator")
implementation("io.micrometer:micrometer-tracing-bridge-brave")
implementation("io.micrometer:micrometer-registry-prometheus")
implementation("io.zipkin.reporter2:zipkin-reporter-brave")
```

This is how the build.gradle.kts file should look like in the end (you can directly copy it):

```kotlin
plugins {
	kotlin("jvm") version "2.1.0"
	kotlin("plugin.spring") version "2.1.0"
	id("org.springframework.boot") version "3.4.5"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.services"
version = "1.0.0"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

extra["springCloudVersion"] = "2024.0.1"

dependencies {
	implementation(project(":common"))

	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.springframework.cloud:spring-cloud-starter-config")
	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")

	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("io.micrometer:micrometer-tracing-bridge-brave")
	implementation("io.micrometer:micrometer-registry-prometheus")
	implementation("io.zipkin.reporter2:zipkin-reporter-brave")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
	}
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

```

---

## 5. 📄 Convert `application.properties` to YAML

* Rename `src/main/resources/application.properties` to `application.yaml`
* Paste the following inside:

```yaml
spring:
  config:
    import: optional:configserver:http://localhost:8888/
  cloud:
    discovery:
      enabled: true
  application:
    name: {project-name}
```

> Replace `{project-name}` with your actual project folder name

---

## 6. 🗂️ Create Config File in Config Server

Go to `infrastructure/config-server/src/resources/configurations/`

* Create a file named `{project-name}.yaml`

Example content:

```yaml
server:
  port: 3434
```

> Port must be unique and not used by other services.

---

## 7. 🌐 Register Route in API Gateway

Go to `infrastructure/api-gateway/src/resources/configurations/api-gateway.yaml`

* Add the following under `routes`:

```yaml
  - id: {project-name}
    uri: lb://{PROJECT-NAME-IN-UPPERCASE}
    predicates:
      - Path=/api/v1/bucket/**
```

> Replace `{project-name}` and `{PROJECT-NAME-IN-UPPERCASE}` accordingly.

---

✅ Your microservice is now integrated into the ecosystem!
