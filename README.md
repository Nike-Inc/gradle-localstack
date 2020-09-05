# gradle-localstack
[![][travis img]][travis]
[![][docs img]][docs]
[![][license img]][license]

Gradle plugin for setting up mock AWS endpoints during test and development using [LocalStack](https://github.com/localstack/localstack).

This plugin provides a number of helpful tasks for creating and working with mock AWS resources during test and development using LocalStack as well as tasks
for easily integrating Spring Boot with LocalStack using simple Gradle DSL.

Use the plugin to easily create and interact with all of the AWS resources required by your application and provide end-to-end integration tests that run on your local machine.

## Plugin Dependencies
This plugin requires the following plugins to be applied to the project in order for it to function:

| Plugin Id | Description |
| --------- | ----------- |
| [com.avast.gradle.docker-compose](https://plugins.gradle.org/plugin/com.avast.gradle.docker-compose) | Avast Docker-Compose Gradle Plugin

## Getting Started
The plugin can be applied with the `buildscript` syntax or the plugin DSL.

### Applying the Plugin with BuildScript Syntax
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

### Applying the Plugin with Plugin DSL
```
plugins {
    id "com.nike.pdm.localstack"    version "0.1.0"
}
```

### Documentation
For detailed documentation on this plugin and its features please refer to the [LocalStack Gradle Plugin User Guide](http://nike-inc.github.io/gradle-localstack).

### Example
Please refer to the included sample project, [gradle-localstack-plugin-example](gradle-localstack-plugin-example), for a simple demonstration on applying and configuring the plugin.

[travis]:https://travis-ci.org/Nike-Inc/gradle-localstack
[travis img]:https://travis-ci.org/Nike-Inc/gradle-localstack.svg?branch=master

[docs]:http://nike-inc.github.io/gradle-localstack
[docs img]:https://img.shields.io/badge/Documentation-yes-green.svg

[license]:LICENSE.txt
[license img]:https://img.shields.io/badge/License-Apache%202-blue.svg