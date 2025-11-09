plugins {
	java
	id("org.springframework.boot") version "3.5.7"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.graalvm.buildtools.native") version "0.10.6"
}

group = "io.github.mgluizbrito"
version = "0.2"
description = "Retrieval Augmented Generation (RAG) system for answering questions based on the content of PDF files"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootBuildImage>("bootBuildImage"){}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
	maven { url = uri("https://repo.spring.io/milestone") }
	maven { url = uri("https://repo.spring.io/snapshot") }
	maven {
		name = "Central Portal Snapshots"
		url = uri("https://central.sonatype.com/repository/maven-snapshots/")
	}
}

extra["springAiVersion"] = "1.1.0-M3"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-security")
//	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("com.bucket4j:bucket4j-core:8.10.1")

	implementation(platform("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}"))
	implementation("org.springframework.ai:spring-ai-starter-vector-store-pgvector")
	implementation("org.springframework.ai:spring-ai-ollama")
	implementation("org.springframework.ai:spring-ai-google-genai")
	implementation("org.springframework.ai:spring-ai-pdf-document-reader")

	implementation("org.apache.commons:commons-lang3:3.18.0")
	implementation("com.auth0:java-jwt:4.5.0")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")

	compileOnly("org.projectlombok:lombok")

	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("org.postgresql:postgresql")

	annotationProcessor("org.projectlombok:lombok")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.springframework.ai:spring-ai-spring-boot-testcontainers")
	testImplementation("org.testcontainers:ollama")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

//dependencyManagement {
//	imports {
//		mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
//	}
//}

tasks.withType<Test> {
	useJUnitPlatform()
}
