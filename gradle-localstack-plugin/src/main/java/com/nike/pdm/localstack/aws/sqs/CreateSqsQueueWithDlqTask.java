/**
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.aws.sqs;

import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.util.StringUtils;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nike.pdm.localstack.compose.LocalStackModule;
import com.nike.pdm.localstack.core.ConsoleLogger;
import com.nike.pdm.localstack.core.Retry;
import com.nike.pdm.localstack.core.annotation.LocalStackSetupTask;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Task that creates an SQS queue with an attached deadletter queue.
 */
@LocalStackSetupTask
public class CreateSqsQueueWithDlqTask extends DefaultTask {

    private static final String DEFAULT_DLQ_SUFFIX = "-dlq";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Input
    private String queueName;

    @Optional
    @Input
    private Map<String, String> queueAttributes;

    @Optional
    @Input
    private String deadletterQueueName;

    @Optional
    @Input
    private Map<String, String> deadletterQueueAttributes;

    @TaskAction
    public void run() {
        final Set<Class<? extends Throwable>> expectedErrors = new HashSet<>(Arrays.asList(JsonProcessingException.class));

        // Create deadletter queue
        Retry.execute(() -> {
            final SqsTaskUtil sqsTaskUtil = new SqsTaskUtil(getProject());
            final String dlqName = getDeadletterQueueName();

            if (sqsTaskUtil.queueExists(dlqName)) {
                ConsoleLogger.log("DLQ already exists: %s", dlqName);
                return null;
            }

            ConsoleLogger.log("Creating SQS deadletter queue: %s", dlqName);

            CreateQueueResult createDlqResult = sqsTaskUtil.createQueue(dlqName, deadletterQueueAttributes);
            ConsoleLogger.log("Created SQS deadletter queue: %s", dlqName);

            return null;
        });


        // Create queue
        Retry.execute(() -> {
            final SqsTaskUtil sqsTaskUtil = new SqsTaskUtil(getProject());
            final String dlqName = getDeadletterQueueName();
            final String dlqArn = sqsTaskUtil.getQueueArnFromName(dlqName);

            if (sqsTaskUtil.queueExists(queueName)) {
                ConsoleLogger.log("Queue already exists: %s", queueName);

                final String queueUrl = sqsTaskUtil.getQueueUrl(queueName);

                if (queueAttributes == null) {
                    queueAttributes = new HashMap<>();
                }

                // Setting the redrive policy on the queue
                RedrivePolicy redrivePolicy = new RedrivePolicy();
                redrivePolicy.setDeadLetterTargetArn(dlqArn);
                redrivePolicy.setMaxReceiveCount("1");
                queueAttributes.put("RedrivePolicy", MAPPER.writeValueAsString(redrivePolicy));

                sqsTaskUtil.setQueueAttributes(queueUrl, queueAttributes);

                ConsoleLogger.log("Updated RedrivePolicy on SQS queue: %s", queueName);
            } else {
                ConsoleLogger.log("Creating SQS queue: %s", queueName);

                if (queueAttributes == null) {
                    queueAttributes = new HashMap<>();
                }

                // Setting the redrive policy on the queue
                RedrivePolicy redrivePolicy = new RedrivePolicy();
                redrivePolicy.setDeadLetterTargetArn(dlqArn);
                redrivePolicy.setMaxReceiveCount("1");
                queueAttributes.put("RedrivePolicy", MAPPER.writeValueAsString(redrivePolicy));

                CreateQueueResult queue = sqsTaskUtil.createQueue(queueName, queueAttributes);

                ConsoleLogger.log("Created SQS queue: %s", queueName);
            }

            return null;
        }, expectedErrors);
    }

    @Internal
    @Override
    public String getGroup() {
        return SqsModule.GROUP_NAME;
    }

    @Internal
    @Override
    public String getDescription() {
        return "Creates an SQS queue with attached DLQ.";
    }

    /**
     * Gets the name of the queue to create.
     *
     * @return queue name
     */
    public String getQueueName() {
        return queueName;
    }

    /**
     * Sets the name of the queue to create.
     *
     * @param queueName queue name
     */
    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    /**
     * Gets the queue attributes to apply to the created queue.
     *
     * @return queue attributes
     */
    public Map<String, String> getQueueAttributes() {
        return queueAttributes;
    }

    /**
     * Sets the queue attributes to apply to the created queue.
     *
     * @param queueAttributes queue attributes
     */
    public void setQueueAttributes(Map<String, String> queueAttributes) {
        this.queueAttributes = queueAttributes;
    }

    /**
     * Gets the name of the deadletter queue to create. If not specified, the name will default to `{queueName}-dlq`.
     *
     * @return deadletter queue name
     */
    public String getDeadletterQueueName() {
        return !StringUtils.isNullOrEmpty(deadletterQueueName) ? deadletterQueueName : queueName + DEFAULT_DLQ_SUFFIX;
    }

    /**
     * Sets the name of the deadletter queue to create. If not specified, the name will default to `{queueName}-dlq`.
     *
     * @param deadletterQueueName deadletter queue name
     */
    public void setDeadletterQueueName(String deadletterQueueName) {
        this.deadletterQueueName = deadletterQueueName;
    }

    /**
     * Gets the queue attributes to apply to the created deadletter queue.
     *
     * @return queue attributes
     */
    public Map<String, String> getDeadletterQueueAttributes() {
        return deadletterQueueAttributes;
    }

    /**
     * Sets the queue attributes to apply to the created deadletter queue.
     *
     * @param deadletterQueueAttributes queue attributes
     */
    public void setDeadletterQueueAttributes(Map<String, String> deadletterQueueAttributes) {
        this.deadletterQueueAttributes = deadletterQueueAttributes;
    }

    /**
     * Holds redrive policy that will be converted to JSON when creating the queue.
     */
    @JsonPropertyOrder({
            "deadLetterTargetArn",
            "maxReceiveCount"
    })
    static class RedrivePolicy {

        private String deadLetterTargetArn;
        private String maxReceiveCount;

        public String getDeadLetterTargetArn() {
            return deadLetterTargetArn;
        }

        public void setDeadLetterTargetArn(String deadLetterTargetArn) {
            this.deadLetterTargetArn = deadLetterTargetArn;
        }

        public String getMaxReceiveCount() {
            return maxReceiveCount;
        }

        public void setMaxReceiveCount(String maxReceiveCount) {
            this.maxReceiveCount = maxReceiveCount;
        }
    }
}
