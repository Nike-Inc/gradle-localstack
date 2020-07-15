/**
 * Copyright 2019-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package example.config;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class AwsConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(AwsConfiguration.class);

    @Profile("local")
    @Bean
    public AmazonDynamoDB localAmazonDynamoDb() {
        LOG.info("Creating LocalStack DynamoDB client");

        return AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:4566", "us-east-1"))
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .build();
    }

    @Profile("!local")
    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        return AmazonDynamoDBClientBuilder.defaultClient();
    }

    @Bean
    public DynamoDBMapper dynamoDBMapper(AmazonDynamoDB amazonDynamoDB) {
        return new DynamoDBMapper(amazonDynamoDB);
    }

    @Profile("local")
    @Bean
    public AmazonSQS localAmazonSqs() {
        LOG.info("Creating LocalStack SQS client");

        return AmazonSQSClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:4566", "us-east-1"))
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .build();
    }

    @Profile("!local")
    @Bean
    public AmazonSQS amazonSQS() {
        return AmazonSQSClientBuilder.defaultClient();
    }
}
