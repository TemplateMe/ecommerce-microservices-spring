plugins {
	kotlin("jvm") version "2.1.0"
}

group = "com.azsumtoshko"
version = "1.0.0"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation(kotlin("stdlib"))

	implementation("jakarta.validation:jakarta.validation-api:3.1.1")
	implementation("jakarta.persistence:jakarta.persistence-api:3.2.0")
	implementation("com.fasterxml.jackson.core:jackson-databind:2.19.0")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}