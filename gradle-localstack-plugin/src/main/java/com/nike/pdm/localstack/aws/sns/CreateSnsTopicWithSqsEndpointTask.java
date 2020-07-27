/**
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.aws.sns;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.util.Topics;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.util.StringUtils;
import com.nike.pdm.localstack.aws.AwsClientFactory;
import com.nike.pdm.localstack.aws.sqs.SqsTaskUtil;
import com.nike.pdm.localstack.core.ConsoleLogger;
import com.nike.pdm.localstack.core.Retry;
import com.nike.pdm.localstack.core.annotation.LocalStackSetupTask;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import java.util.HashMap;

/**
 * Task that creates an SNS topic with an attached SQS queue.
 */
@LocalStackSetupTask
public class CreateSnsTopicWithSqsEndpointTask extends DefaultTask {

    private static final String DEFAULT_SUBSCRIBER_QUEUE_SUFFIX = "-subscriber";

    @Input
    private String topicName;

    @Optional
    @Input
    private String queueName;

    @TaskAction
    public void run() {
        // Create sqs endpoint
        String sqsQueueUrl = Retry.execute(() -> {
            final SqsTaskUtil sqsTaskUtil = new SqsTaskUtil(getProject());

            if (sqsTaskUtil.queueExists(queueName)) {
                ConsoleLogger.log("SQS queue endpoint already exists: %s", queueName);
                return null;
            }

            ConsoleLogger.log("Creating SQS endpoint queue: %s", queueName);

            CreateQueueResult createEndpointQueueResult = sqsTaskUtil.createQueue(queueName, new HashMap<>());

            ConsoleLogger.log("Created SQS endpoint queue: %s", queueName);

            return sqsTaskUtil.getQueueUrl(queueName);
        });

        // Create topic
        String topicArn = Retry.execute(() -> {
            final AmazonSNS amazonSns = AwsClientFactory.getInstance().sns(getProject());

            ConsoleLogger.log("Creating SNS topic: %s", topicName);

            CreateTopicResult topic = amazonSns.createTopic(topicName);

            ConsoleLogger.log("Created SNS topic: %s", topicName);

            return topic.getTopicArn();
        });

        Retry.execute(() -> {
            final AmazonSNS amazonSns = AwsClientFactory.getInstance().sns(getProject());
            final AmazonSQS amazonSqs = AwsClientFactory.getInstance().sqs(getProject());

            ConsoleLogger.log("Creating SNS Subscription");

            String subscriptionArn = Topics.subscribeQueue(amazonSns, amazonSqs, topicArn, sqsQueueUrl);

            ConsoleLogger.log("Created SNS subscription: %s", subscriptionArn);

            return null;
        });
    }

    /**
     * Gets the name of the topic to create.
     *
     * @return topic name
     */
    public String getTopicName() {
        return topicName;
    }

    /**
     * Sets the name of the topic to create.
     *
     * @param topicName topic name
     */
    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    /**
     * Gets the name of the queue to subscribe to the created topic.
     *
     * @return queue name
     */
    public String getQueueName() {
        return !StringUtils.isNullOrEmpty(queueName) ? queueName : topicName + DEFAULT_SUBSCRIBER_QUEUE_SUFFIX;
    }

    /**
     * Sets the name of the queue to subscribe to the created topic.
     *
     * @param queueName queue name
     */
    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }
}
