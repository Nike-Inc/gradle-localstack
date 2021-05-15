# gradle-localstack-plugin
Gradle plugin for setting up mock AWS endpoints during test and development using [LocalStack](https://github.com/localstack/localstack).

This plugin provides a number of helpful tasks for simplifying the creation of AWS resources for use in testing and development, 
as well as tasks for easily integrating Spring Boot with LocalStack, using simple Gradle DSL.

## Getting Started
The plugin can be applied with either the plugin or legacy buildscript DSL. For more information on applying the plugin and available plugin versions please refer to the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/com.nike.pdm.localstack).

#### Applying the Plugin with Plugin DSL
```
plugins {
    id "com.nike.pdm.localstack"    version "1.0.0"
}
```

#### Applying the Plugin with Legacy Buildscript DSL
```
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "gradle.plugin.com.nike.pdm.localstack:gradle-localstack-plugin:1.0.0"
  }
}

apply plugin: "com.nike.pdm.localstack"
```

## Documentation
For detailed documentation on this plugin and its features please refer to the [LocalStack Gradle Plugin User Guide](http://nike-inc.github.io/gradle-localstack).

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

    ./gradlew funcTest
