# gradle-localstack
[![][travis img]][travis]
[![][docs img]][docs]
[![][pluginportal img]][pluginportal]
[![][license img]][license]

Gradle plugin for setting up mock AWS endpoints during test and development using [LocalStack](https://github.com/localstack/localstack).

This plugin provides a number of helpful tasks for creating and working with mock AWS resources during test and development using LocalStack as well as tasks
for easily integrating Spring Boot with LocalStack using simple Gradle DSL.

Use the plugin to easily create and interact with all of the AWS resources required by your application locally and provide end-to-end integration tests that run on your local machine without the need for a live AWS environment.

## Plugin Dependencies
This plugin requires the following plugins to be applied to the project in order for it to function:

| Plugin Id | Description |
| --------- | ----------- |
| [com.avast.gradle.docker-compose](https://plugins.gradle.org/plugin/com.avast.gradle.docker-compose) | Avast Docker-Compose Gradle Plugin

## Getting Started
The plugin can be applied with either the plugin or legacy buildscript DSL. For more information on applying the plugin and available plugin versions please refer to the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/com.nike.pdm.localstack).

### Applying the Plugin with Plugin DSL
```
plugins {
    id "com.nike.pdm.localstack"    version "0.1.0"
}
```

### Applying the Plugin with Legacy Buildscript DSL
```
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "gradle.plugin.com.nike.pdm.localstack:gradle-localstack-plugin:0.1.0"
  }
}

apply plugin: "com.nike.pdm.localstack"
```

## Documentation
For detailed documentation on this plugin and its features please refer to the [LocalStack Gradle Plugin User Guide](http://nike-inc.github.io/gradle-localstack).

## Example
Please refer to the included sample project, [gradle-localstack-plugin-example](gradle-localstack-plugin-example), for a simple demonstration on applying and configuring the plugin.

## License
Copyright 2020 - Present, Nike, Inc.
All rights reserved.

This source code is licensed under the Apache-2.0 license found in
the LICENSE file in the root directory of this source tree.

[travis]:https://travis-ci.org/Nike-Inc/gradle-localstack
[travis img]:https://travis-ci.org/Nike-Inc/gradle-localstack.svg?branch=master

[docs]:http://nike-inc.github.io/gradle-localstack
[docs img]:https://img.shields.io/badge/Documentation-yes-green.svg

[pluginportal]:https://plugins.gradle.org/plugin/com.nike.pdm.localstack
[pluginportal img]:https://img.shields.io/badge/Gradle%20Plugin%20Portal-v0.1.0-blue.svg

[license]:LICENSE.txt
[license img]:https://img.shields.io/badge/License-Apache%202-blue.svg
