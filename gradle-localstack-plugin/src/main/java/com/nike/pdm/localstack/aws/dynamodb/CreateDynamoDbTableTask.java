/**
 * Copyright 2019-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.aws.dynamodb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.LocalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.SSESpecification;
import com.amazonaws.services.dynamodbv2.model.StreamSpecification;
import com.nike.pdm.localstack.aws.AwsClientFactory;
import com.nike.pdm.localstack.core.ConsoleLogger;
import com.nike.pdm.localstack.core.Retry;
import com.nike.pdm.localstack.core.annotation.LocalStackSetupTask;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Task that creates a DynamoDB table.
 */
@LocalStackSetupTask
public class CreateDynamoDbTableTask extends DefaultTask {

    private final SSESpecification DEFAULT_SSE_SPECIFICATION = new SSESpecification().withEnabled(true);
    private final ProvisionedThroughput DEFAULT_PROVISIONED_THROUGHPUT = new ProvisionedThroughput(100L, 100L);

    @Input
    private String tableName;

    @Input
    private List<KeySchemaElement> keySchema;

    @Input
    private List<AttributeDefinition> attributeDefinitions;

    @Optional
    @Input
    private List<GlobalSecondaryIndex> globalSecondaryIndexes;

    @Optional
    @Input
    private List<LocalSecondaryIndex> localSecondaryIndexes;

    @Optional
    @Input
    private ProvisionedThroughput provisionedThroughput;

    @Optional
    @Input
    private SSESpecification sseSpecification;

    @Optional
    @Input
    private StreamSpecification streamSpecification;

    @TaskAction
    public void run() {
        final Set<Class<? extends Throwable>> expectedErrors = new HashSet<>(Arrays.asList(IllegalArgumentException.class));

        Retry.execute(() -> {
            final AmazonDynamoDB amazonDynamoDB = AwsClientFactory.getInstance().dynamoDb(getProject());
            final DynamoDBTaskUtil dynamoDBTaskUtil = new DynamoDBTaskUtil(getProject());

            ConsoleLogger.log("Creating DynamoDB table: %s", tableName);

            if (dynamoDBTaskUtil.tableExists(tableName)) {
                ConsoleLogger.log("Table already exists: %s", tableName);
                return null;
            }

            final CreateTableRequest createTableRequest = new CreateTableRequest(tableName, keySchema);
            createTableRequest.setAttributeDefinitions(attributeDefinitions);

            if (globalSecondaryIndexes != null && !globalSecondaryIndexes.isEmpty()) {
                createTableRequest.setGlobalSecondaryIndexes(globalSecondaryIndexes);
            }

            if (localSecondaryIndexes != null && !localSecondaryIndexes.isEmpty()) {
                createTableRequest.setLocalSecondaryIndexes(localSecondaryIndexes);
            }

            if (provisionedThroughput != null) {
                createTableRequest.setProvisionedThroughput(provisionedThroughput);
            } else {
                createTableRequest.setProvisionedThroughput(DEFAULT_PROVISIONED_THROUGHPUT);
            }

            if (sseSpecification != null) {
                createTableRequest.setSSESpecification(sseSpecification);
            } else {
                createTableRequest.setSSESpecification(DEFAULT_SSE_SPECIFICATION);
            }

            if (streamSpecification != null) {
                createTableRequest.setStreamSpecification(streamSpecification);
            }

            CreateTableResult createTableResult = amazonDynamoDB.createTable(createTableRequest);

            ConsoleLogger.log("Created DynamoDB table: %s", tableName);

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
        return "Create a DynamoDB table.";
    }

    /**
     * Gets the name of the table to create.
     *
     * @return table name
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Sets the name of the table to create.
     *
     * @param tableName table name
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * Gets the table key schema elements.
     *
     * @return table key schema elements
     */
    public List<KeySchemaElement> getKeySchema() {
        return keySchema;
    }

    /**
     * Sets the table key schema elements.
     *
     * @param keySchema table key schema elements
     */
    public void setKeySchema(List<KeySchemaElement> keySchema) {
        this.keySchema = keySchema;
    }

    /**
     * Gets the table attribute definitions.
     *
     * @return table attribute definitions
     */
    public List<AttributeDefinition> getAttributeDefinitions() {
        return attributeDefinitions;
    }

    /**
     * Sets the table attribute definitions.
     *
     * @param attributeDefinitions table attribute definitions
     */
    public void setAttributeDefinitions(List<AttributeDefinition> attributeDefinitions) {
        this.attributeDefinitions = attributeDefinitions;
    }

    /**
     * Gets the global secondary indexes defined for the table.
     *
     * @return table global secondary indexes
     */
    public List<GlobalSecondaryIndex> getGlobalSecondaryIndexes() {
        return globalSecondaryIndexes;
    }

    /**
     * Sets the global secondary indexes to apply to the table.
     *
     * @param globalSecondaryIndexes table global secondary indexes
     */
    public void setGlobalSecondaryIndexes(List<GlobalSecondaryIndex> globalSecondaryIndexes) {
        this.globalSecondaryIndexes = globalSecondaryIndexes;
    }

    /**
     * Gets the local secondary indexes defined for the table.
     *
     * @return table local secondary indexes
     */
    public List<LocalSecondaryIndex> getLocalSecondaryIndexes() {
        return localSecondaryIndexes;
    }

    /**
     * Sets the local secondary indexes defined for the table.
     *
     * @param localSecondaryIndexes table local secondary indexes
     */
    public void setLocalSecondaryIndexes(List<LocalSecondaryIndex> localSecondaryIndexes) {
        this.localSecondaryIndexes = localSecondaryIndexes;
    }

    /**
     * Sets the provisioned throughput for the table.
     *
     * @return table provisioned throughput
     */
    public ProvisionedThroughput getProvisionedThroughput() {
        return provisionedThroughput;
    }

    /**
     * Sets the provisioned throughput for the table.
     *
     * @param provisionedThroughput table provisioned throughput
     */
    public void setProvisionedThroughput(ProvisionedThroughput provisionedThroughput) {
        this.provisionedThroughput = provisionedThroughput;
    }

    /**
     * Gets the table SSE specification.
     *
     * @return table sse specification
     */
    public SSESpecification getSseSpecification() {
        return sseSpecification;
    }

    /**
     * Sets the table SSE specification
     *
     * @param sseSpecification table sse specification
     */
    public void setSseSpecification(SSESpecification sseSpecification) {
        this.sseSpecification = sseSpecification;
    }

    /**
     * Gets the table stream specification.
     *
     * @return table stream specification
     */
    public StreamSpecification getStreamSpecification() {
        return streamSpecification;
    }

    /**
     * Sets the table stream specification
     *
     * @param streamSpecification table stream specification
     */
    public void setStreamSpecification(StreamSpecification streamSpecification) {
        this.streamSpecification = streamSpecification;
    }
}
