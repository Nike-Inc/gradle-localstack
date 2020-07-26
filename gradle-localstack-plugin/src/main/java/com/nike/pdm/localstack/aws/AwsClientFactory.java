/**
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.aws;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.nike.pdm.localstack.compose.LocalStackExtension;
import org.gradle.api.Project;

/**
 * Factory to create clients for supported AWS services.
 */
public final class AwsClientFactory {
    private static final AwsClientFactory INSTANCE = new AwsClientFactory();

    private volatile AmazonCloudFormation cfClient;
    private volatile AmazonDynamoDB dynamoDbClient;
    private volatile AmazonS3 amazonS3Client;
    private volatile AmazonSQS amazonSqsClient;
    private volatile AmazonSNS amazonSnsClient;

    private AwsClientFactory() {
        // Noop
    }

    public static AwsClientFactory getInstance() {
        return INSTANCE;
    }

    /**
     * Gets a client for the AWS CloudFormation service.
     *
     * @param project gradle project
     * @return aws client
     */
    public AmazonCloudFormation cloudformation(Project project) {
        AmazonCloudFormation ref = cfClient;
        if (ref == null) {
            synchronized (this) {
                ref = cfClient;
                if (ref == null) {
                    project.getLogger().debug("Creating new aws cloudformation client");

                    cfClient = ref = AmazonCloudFormationClientBuilder.standard()
                            .withEndpointConfiguration(endpointConfiguration(project))
                            .build();
                }
            }
        }

        return ref;
    }

    /**
     * Gets a client for the AWS DynamoDB service.
     *
     * @param project gradle project
     * @return aws client
     */
    public AmazonDynamoDB dynamoDb(Project project) {
        AmazonDynamoDB ref = dynamoDbClient;
        if (ref == null) {
            synchronized (this) {
                ref = dynamoDbClient;
                if (ref == null) {
                    project.getLogger().debug("Creating new aws dynamodb client");

                    dynamoDbClient = ref = AmazonDynamoDBClientBuilder.standard()
                            .withEndpointConfiguration(endpointConfiguration(project))
                            .build();
                }
            }
        }

        return ref;
    }

    /**
     * Gets a client for the AWS S3 service.
     *
     * @param project gradle project
     * @return aws client
     */
    public AmazonS3 s3(Project project) {
        AmazonS3 ref = amazonS3Client;
        if (ref == null) {
            synchronized (this) {
                ref = amazonS3Client;
                if (ref == null) {
                    project.getLogger().debug("Creating new aws s3 client");

                    amazonS3Client = ref = AmazonS3ClientBuilder.standard()
                            .withEndpointConfiguration(endpointConfiguration(project))
                            .withPathStyleAccessEnabled(true)
                            .build();
                }
            }
        }

        return ref;
    }

    /**
     * Gets a client for the AWS SQS service.
     *
     * @param project gradle project
     * @return aws client
     */
    public AmazonSQS sqs(Project project) {
        AmazonSQS ref = amazonSqsClient;
        if (ref == null) {
            synchronized (this) {
                ref = amazonSqsClient;
                if (ref == null) {
                    project.getLogger().debug("Creating new aws sqs client");

                    amazonSqsClient = ref = AmazonSQSClientBuilder.standard()
                            .withEndpointConfiguration(endpointConfiguration(project))
                            .build();
                }
            }
        }

        return ref;
    }

    /**
     * Gets a client for the AWS SNS service.
     *
     * @param project gradle project
     * @return aws client
     */
    public AmazonSNS sns(Project project) {
        AmazonSNS ref = amazonSnsClient;
        if (ref == null) {
            synchronized (this) {
                ref = amazonSnsClient;
                if (ref == null) {
                    project.getLogger().debug("Creating new aws sns client");

                    amazonSnsClient = ref = AmazonSNSClientBuilder.standard()
                            .withEndpointConfiguration(endpointConfiguration(project))
                            .build();
                }
            }
        }

        return ref;
    }

    private AwsClientBuilder.EndpointConfiguration endpointConfiguration(Project project) {
        LocalStackExtension ext = project.getExtensions().findByType(LocalStackExtension.class);
        return new AwsClientBuilder.EndpointConfiguration(String.format("http://%s:%s", ext.getHost(), ext.getPort()), ext.getSigningRegion());
    }
}
