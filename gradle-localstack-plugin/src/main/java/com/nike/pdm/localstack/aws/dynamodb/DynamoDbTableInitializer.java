package com.nike.pdm.localstack.aws.dynamodb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;

/**
 * Interface that all dynamodb table initializers must implement if they
 * wish to be automatically executed by the localstack plugin.
 */
public interface DynamoDbTableInitializer {

    /**
     * Runs the initializer.
     *
     * @param dynamoClient dynamodb client
     */
    void run(AmazonDynamoDB dynamoClient);
}
