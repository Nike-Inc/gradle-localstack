# gradle-localstack-plugin-example
Simple example application to illustrate the usage of the [LocalStack Gradle Plugin](../gradle-localstack-plugin).

## Building the Example
Run the following command to build the example and run the integration tests:

    ./gradlew clean build integTest

## Running the Example
Run the following command to start the example application with a mock AWS environment provided by LocalStack:

    ./gradlew bootRunLocal

## Debugging the Example
Run the following command to start the example application in debug mode with a mock AWS environment provided by LocalStack:

    ./gradlew bootRunLocalDebug

This will start the LocalStack environment and then wait for an attached debugger on port `5005` before starting the example application.

## LocalStack Gradle Plugin Tasks
Run the following command to list all of the available LocalStack Gradle plugin tasks for the project:

    ./gradlew tasks