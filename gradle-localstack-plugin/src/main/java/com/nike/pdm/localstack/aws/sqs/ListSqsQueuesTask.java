/**
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.aws.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.ListQueuesRequest;
import com.amazonaws.services.sqs.model.ListQueuesResult;
import com.amazonaws.util.StringUtils;
import com.nike.pdm.localstack.aws.AwsClientFactory;
import com.nike.pdm.localstack.compose.LocalStackModule;
import com.nike.pdm.localstack.core.ConsoleLogger;
import com.nike.pdm.localstack.core.Retry;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

import java.util.Arrays;

/**
 * Task that lists SQS queues.
 */
public class ListSqsQueuesTask extends DefaultTask {

    @Optional
    @Input
    private String prefix;

    public ListSqsQueuesTask() {
        setMustRunAfter(Arrays.asList(LocalStackModule.START_LOCALSTACK_TASK_NAME));
    }

    @TaskAction
    public void run() {
        Retry.execute(() -> {
            final AmazonSQS amazonSQS = AwsClientFactory.getInstance().sqs(getProject());
            final SqsTaskUtil sqsTaskUtil = new SqsTaskUtil(getProject());

            AsciiTable at = new AsciiTable();
            at.setTextAlignment(TextAlignment.JUSTIFIED_LEFT);
            at.getContext().setWidth(150);

            at.addRule();
            at.addRow("QueueName", "QueueUrl", "ApproximateNumberOfMessages");
            at.addRule();

            final ListQueuesRequest listQueuesRequest = StringUtils.isNullOrEmpty(prefix) ? new ListQueuesRequest() : new ListQueuesRequest(prefix);
            ListQueuesResult listQueuesResult = amazonSQS.listQueues(listQueuesRequest);

            if (listQueuesResult.getQueueUrls() != null && !listQueuesResult.getQueueUrls().isEmpty()) {
                listQueuesResult.getQueueUrls().forEach(queueUrl -> {
                    Long approximateNumberOfMessages = sqsTaskUtil.getApproximateNumberOfMessages(queueUrl);

                    at.addRow(sqsTaskUtil.getQueueNameFromUrl(queueUrl), queueUrl, approximateNumberOfMessages);
                    at.addRule();
                });

                ConsoleLogger.log(at.render());
            } else {
                ConsoleLogger.log("No Queues Found!");
            }

            return null;
        });
    }

    @Internal
    @Override
    public String getGroup() {
        return SqsModule.GROUP_NAME;
    }

    @Internal
    @Override
    public String getDescription() {
        return "Lists SQS queues.";
    }

    /**
     * Gets the queue name prefix to use when searching for queues. Only queues whose names start with this prefix will
     * be returned.
     *
     * @return queue name prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Sets the queue name prefix to use when searching for queues. Only queues whose names start with this prefix will
     * be returned.
     *
     * @param prefix queue name prefix.
     */
    @Option(option = "prefix", description = "Filter sqs queues to list by names that start with this prefix")
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
