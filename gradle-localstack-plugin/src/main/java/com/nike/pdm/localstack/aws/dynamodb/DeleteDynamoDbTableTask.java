/**
 * Copyright 2019-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.aws.dynamodb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.DeleteTableResult;
import com.nike.pdm.localstack.aws.AwsClientFactory;
import com.nike.pdm.localstack.compose.LocalStackModule;
import com.nike.pdm.localstack.core.ConsoleLogger;
import com.nike.pdm.localstack.core.Retry;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Task that deletes a DynamoDB table.
 */
public class DeleteDynamoDbTableTask extends DefaultTask {

    @Input
    private String tableName;

    public DeleteDynamoDbTableTask() {
        setMustRunAfter(Arrays.asList(LocalStackModule.START_LOCALSTACK_TASK_NAME));
    }

    @TaskAction
    public void run() {
        final Set<Class<? extends Throwable>> expectedErrors = new HashSet<>(Arrays.asList(IllegalArgumentException.class));

        Retry.execute(() -> {
            final AmazonDynamoDB amazonDynamoDB = AwsClientFactory.getInstance().dynamoDb(getProject());
            final DynamoDBTaskUtil dynamoDBTaskUtil = new DynamoDBTaskUtil(getProject());

            ConsoleLogger.log("Deleting DynamoDB table: %s", tableName);

            if (!dynamoDBTaskUtil.tableExists(tableName)) {
                throw new IllegalArgumentException("Table does not exist: " + tableName);
            }

            DeleteTableResult deleteTableResult = amazonDynamoDB.deleteTable(tableName);

            ConsoleLogger.log("Deleted DynamoDB table: %s", tableName);

            return null;
        }, expectedErrors);
    }

    @Internal
    @Override
    public String getGroup() {
        return DynamoDbModule.GROUP_NAME;
    }

    @Internal
    @Override
    public String getDescription() {
        return "Drop a DynamoDB table.";
    }

    /**
     * Gets the name of the table to delete.
     *
     * @return table name
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Sets the name of the table to delete.
     *
     * @param tableName table name
     */
    @Option(option = "tableName", description = "Name of the table to delete")
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
