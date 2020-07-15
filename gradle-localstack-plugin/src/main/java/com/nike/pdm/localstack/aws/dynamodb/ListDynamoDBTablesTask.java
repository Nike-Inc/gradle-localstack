/**
 * Copyright 2019-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.aws.dynamodb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.nike.pdm.localstack.aws.AwsClientFactory;
import com.nike.pdm.localstack.compose.LocalStackModule;
import com.nike.pdm.localstack.core.ConsoleLogger;
import com.nike.pdm.localstack.core.Retry;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import java.util.Arrays;

/**
 * Task that lists all DynamoDB tables.
 */
public class ListDynamoDBTablesTask extends DefaultTask {

    public ListDynamoDBTablesTask() {
        setMustRunAfter(Arrays.asList(LocalStackModule.START_LOCALSTACK_TASK_NAME));
    }

    @TaskAction
    public void run() {
        Retry.execute(() -> {
            final AmazonDynamoDB amazonDynamoDB = AwsClientFactory.getInstance().dynamoDb(getProject());

            AsciiTable at = new AsciiTable();
            at.setTextAlignment(TextAlignment.JUSTIFIED_LEFT);

            at.addRule();
            at.addRow("TableName");
            at.addRule();

            String lastEvaluatedTableName = null;
            do {
                final ListTablesResult listTablesResult = amazonDynamoDB.listTables(lastEvaluatedTableName);

                if (!listTablesResult.getTableNames().isEmpty()) {
                    listTablesResult.getTableNames().forEach(tableName -> {
                        at.addRow(tableName);
                        at.addRule();
                    });
                } else {
                    ConsoleLogger.log("No Tables Found!");
                }

                lastEvaluatedTableName = listTablesResult.getLastEvaluatedTableName();

                if (lastEvaluatedTableName == null && !listTablesResult.getTableNames().isEmpty()) {
                    ConsoleLogger.log(at.render());
                }
            } while (lastEvaluatedTableName != null);

            return null;
        });
    }

    @Internal
    @Override
    public String getGroup() {
        return DynamoDbModule.GROUP_NAME;
    }

    @Internal
    @Override
    public String getDescription() {
        return "Lists all DynamoDB tables.";
    }
}
