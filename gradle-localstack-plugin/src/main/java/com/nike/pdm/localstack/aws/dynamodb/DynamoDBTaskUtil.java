/**
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.aws.dynamodb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.nike.pdm.localstack.aws.AwsClientFactory;
import org.gradle.api.Project;

/**
 * Utility methods shared amongst the DynamoDB tasks.
 */
public class DynamoDBTaskUtil {

    private final Project project;

    public DynamoDBTaskUtil(Project project) {
        this.project = project;
    }

    /**
     * Checks if a DynamoDB table with the supplied name exists.
     *
     * @param tableName dynamodb table name
     * @return <code>true</code> if the table exists; otherwise <code>false</code>
     */
    public boolean tableExists(String tableName) {
        final AmazonDynamoDB amazonDynamoDB = AwsClientFactory.getInstance().dynamoDb(project);

        String lastEvaluatedTableName = null;
        do {
            ListTablesResult listTablesResult = amazonDynamoDB.listTables(lastEvaluatedTableName);

            if (listTablesResult.getTableNames().contains(tableName)) {
                return true;
            }

            lastEvaluatedTableName = listTablesResult.getLastEvaluatedTableName();
        } while (lastEvaluatedTableName != null);

        return false;
    }
}
