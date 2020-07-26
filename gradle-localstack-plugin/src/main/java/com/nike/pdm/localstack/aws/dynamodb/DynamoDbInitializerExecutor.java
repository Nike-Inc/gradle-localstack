/**
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.aws.dynamodb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.nike.pdm.localstack.aws.AwsClientFactory;
import com.nike.pdm.localstack.core.ConsoleLogger;
import org.gradle.api.Project;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Executes dynamodb table initializers defined in implementations of the {@link CreateDynamoDbTableTask}.
 */
final class DynamoDbInitializerExecutor {

    private final Project project;

    DynamoDbInitializerExecutor(Project project) {
        this.project = project;
    }

    /**
     * Invokes a dynamodb table initializer class.
     *
     * @param tableName name of table to initialize
     * @param initializerClassName fully-qualified class name of the table initializer
     */
    public void invoke(String tableName, String initializerClassName) {
        ConsoleLogger.log("Initializing '%s' table", tableName);

        // Get the initializer class
        Class<?> clazz;
        try {
            clazz = Class.forName(initializerClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(String.format("Initializer class not found: %s", initializerClassName), e);
        }

        // Get the initializer constructor
        Constructor<?> constructor;
        try {
            constructor = clazz.getConstructor(AmazonDynamoDB.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(String.format("Suitable constructor not found. Initializers require a single argument constructor of '%s'", AmazonDynamoDB.class.getCanonicalName()));
        }

        // Get the initializer run method
        Method runMethod;
        try {
            runMethod = clazz.getMethod("run");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Suitable run method not found. Initializers require a single void 'run' method with no arguments.", e);
        }

        // Get instance of initializer class
        Object initializerInstance;
        try {
            initializerInstance = constructor.newInstance(AwsClientFactory.getInstance().dynamoDb(project));
        } catch (Exception e) {
            throw new RuntimeException(String.format("Unable to create instance of initializer: %s", initializerClassName), e);
        }

        // Execute the initializer
        try {
            runMethod.invoke(initializerInstance);
        } catch (Exception e) {
            ConsoleLogger.log("Initializer failed!");
            throw new RuntimeException("Initializer failed!", e);
        }

        ConsoleLogger.log("Table '%s' initialized", tableName);
    }
}
