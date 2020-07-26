/**
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.aws.sns;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sqs.AmazonSQS;
import com.nike.pdm.localstack.aws.AwsClientFactory;
import com.nike.pdm.localstack.aws.sqs.SqsTaskUtil;
import com.nike.pdm.localstack.core.Retry;
import com.nike.pdm.localstack.core.annotation.LocalStackSetupTask;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

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
        Retry.execute(() -> {
            final AmazonSNS amazonSns = AwsClientFactory.getInstance().sns(getProject());
            final SqsTaskUtil sqsTaskUtil = new SqsTaskUtil(getProject());

            return null;
        });
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }
}
