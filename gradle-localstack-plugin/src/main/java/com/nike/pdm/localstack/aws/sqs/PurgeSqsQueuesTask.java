/**
 * Copyright 2019-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.aws.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import com.amazonaws.services.sqs.model.PurgeQueueResult;
import com.amazonaws.util.StringUtils;
import com.nike.pdm.localstack.aws.AwsClientFactory;
import com.nike.pdm.localstack.compose.LocalStackModule;
import com.nike.pdm.localstack.core.ConsoleLogger;
import com.nike.pdm.localstack.core.Retry;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Task that purges messages from SQS queues.
 */
public class PurgeSqsQueuesTask extends DefaultTask {

    @Input
    private List<String> queueNames;

    public PurgeSqsQueuesTask() {
        setMustRunAfter(Arrays.asList(LocalStackModule.START_LOCALSTACK_TASK_NAME));
    }

    @TaskAction
    public void run() {
        final Set<Class<? extends Throwable>> expectedErrors = new HashSet<>(Arrays.asList(IllegalArgumentException.class));

        Retry.execute(() -> {
            final AmazonSQS amazonSQS = AwsClientFactory.getInstance().sqs(getProject());
            final SqsTaskUtil sqsTaskUtil = new SqsTaskUtil(getProject());

            if (queueNames != null && !queueNames.isEmpty()) {
                queueNames.forEach(queueName -> {

                    ConsoleLogger.log("Purging SQS queue: %s", queueName);

                    if (!sqsTaskUtil.queueExists(queueName)) {
                        throw new IllegalArgumentException("Queue does not exist: " + queueName);
                    }

                    GetQueueUrlResult getQueueUrlResult = amazonSQS.getQueueUrl(queueName);

                    PurgeQueueResult purgeQueueResult = amazonSQS.purgeQueue(new PurgeQueueRequest(getQueueUrlResult.getQueueUrl()));

                    ConsoleLogger.log("Purged SQS queue: %s", queueName);
                });
            } else {
                ConsoleLogger.log("No queues defined in 'queueNames' parameter");
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
        return "Purges SQS queues.";
    }

    /**
     * Gets the names of the queues to purge.
     *
     * @return queue names
     */
    public List<String> getQueueNames() {
        return queueNames;
    }

    /**
     * Sets the names of the queues to purge.
     *
     * @param queueNames queue names
     */
    public void setQueueNames(List<String> queueNames) {
        this.queueNames = queueNames;
    }

    /**
     * Sets the names of queues to purge. This method supports providing the queue names as
     * a comma-delimited list of names.
     *
     * @param queueNames queue names
     */
    @Option(option = "queueNames", description = "Comma-delimited list of the names of the queues to purge")
    public void setQueueNames(String queueNames) {
        if (!StringUtils.isNullOrEmpty(queueNames)) {
            setQueueNames(Arrays.stream(queueNames.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList()));
        }
    }
}
