/**
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.aws.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;
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
import java.util.Map;
import java.util.Set;

/**
 * Task that creates an SQS queue.
 */
@LocalStackSetupTask
public class CreateSqsQueuesTask extends DefaultTask {

    @Input
    private List<String> queueNames;

    @Optional
    @Input
    private Map<String, String> queueAttributes;

    @TaskAction
    public void run() {
        final Set<Class<? extends Throwable>> expectedErrors = new HashSet<>(Arrays.asList(IllegalArgumentException.class));

        Retry.execute(() -> {
            final AmazonSQS amazonSQS = AwsClientFactory.getInstance().sqs(getProject());
            final SqsTaskUtil sqsTaskUtil = new SqsTaskUtil(getProject());

            queueNames.forEach(queueName -> {
                ConsoleLogger.log("Creating SQS queue: %s", queueName);

                if (sqsTaskUtil.queueExists(queueName)) {
                    ConsoleLogger.log("Queue already exists: %s", queueName);
                } else {
                    final CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName);

                    if (queueAttributes != null && !queueAttributes.isEmpty()) {
                        createQueueRequest.setAttributes(queueAttributes);
                    }

                    CreateQueueResult queue = amazonSQS.createQueue(createQueueRequest);

                    ConsoleLogger.log("Created SQS queue: %s", queueNames);
                }
            });

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
        return "Creates SQS queues.";
    }

    /**
     * Gets the name of the queues to create.
     *
     * @return queue names
     */
    public List<String> getQueueNames() {
        return queueNames;
    }

    /**
     * Sets the names of the queues to create.
     *
     * @param queueNames queue names
     */
    public void setQueueNames(List<String> queueNames) {
        this.queueNames = queueNames;
    }

    /**
     * Gets the queue attributes to apply to the created queues.
     *
     * @return queue attributes
     */
    public Map<String, String> getQueueAttributes() {
        return queueAttributes;
    }

    /**
     * Sets the queue attributes to apply to the created queues.
     *
     * @param queueAttributes queue attributes
     */
    public void setQueueAttributes(Map<String, String> queueAttributes) {
        this.queueAttributes = queueAttributes;
    }
}
