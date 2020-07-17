# gradle-localstack-plugin
Gradle plugin for setting up mock AWS endpoints during test and development using [LocalStack](https://github.com/localstack/localstack).

This plugin provides a number of helpful tasks for simplifying the creation of AWS resources for use in testing and development, 
as well as tasks for easily integrating Spring Boot with LocalStack, using simple Gradle DSL.

## Development
It is recommended when developing the plugin that you open the [gradle-localstack-plugin-example](../gradle-localstack-plugin-example) project in Intellij as it will include the plugin
as a composite build and makes testing of the plugin during development easier.

### Building
Run the following command to build the plugin:

    ./gradlew clean build 

### Building Documentation
Run the following command to build the documentation for the plugin:

    ./gradlew buildDocSite

### Running Functional Tests
Run the following command to execute the functional tests:

    ./gradlew funcTests

## Usage
### Plugin Dependencies
This plugin requires the following plugins to be applied to the project in order for it to function:

1. [com.avast.gradle.docker-compose](https://plugins.gradle.org/plugin/com.avast.gradle.docker-compose) - Avast Docker-Compose Gradle Plugin

### Getting Started
The plugin can be applied with the `buildscript` syntax or the plugin DSL.

#### Applying the Plugin with BuildScript Syntax
```
buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath 'com.nike.pdm.localstack:0.1.0'
    }
}

apply plugin: 'com.nike.pdm.localstack'
```

#### Applying the Plugin with Plugin DSL
```
plugins {
    id "com.nike.pdm.localstack"    version "0.1.0"
}
```

### Documentation
For detailed documentation on this plugin and its features please refer to the [LocalStack Gradle Plugin User Guide](docsite/index.html).
