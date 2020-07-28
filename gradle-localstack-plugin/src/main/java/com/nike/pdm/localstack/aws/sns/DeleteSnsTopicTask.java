/**
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.aws.sns;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.DeleteTopicResult;
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

/**
 * Task that deletes an SNS topic.
 */
public class DeleteSnsTopicTask extends DefaultTask {

    @Input
    private String topicName;

    public DeleteSnsTopicTask() {
        setMustRunAfter(Arrays.asList(LocalStackModule.START_LOCALSTACK_TASK_NAME));
    }

    @TaskAction
    public void run() {
        Retry.execute(() -> {
            final AmazonSNS amazonSNS = AwsClientFactory.getInstance().sns(getProject());
            final SnsTaskUtil snsTaskUtil = new SnsTaskUtil(getProject());

            try {
                ConsoleLogger.log("Deleting SNS topic: %s", topicName);

                DeleteTopicResult deleteTopicResult = amazonSNS.deleteTopic(snsTaskUtil.getTopicArn(topicName));

                ConsoleLogger.log("Deleted SNS topic: %s", topicName);
            } catch (AmazonServiceException e) {
                if (e.getStatusCode() == 404 || e.getStatusCode() == 500) {
                    ConsoleLogger.log("Topic Not Found!");
                } else {
                    throw e;
                }
            }

            return null;
        });
    }

    @Internal
    @Override
    public String getGroup() {
        return SnsModule.GROUP_NAME;
    }

    @Internal
    @Override
    public String getDescription() {
        return "Deletes an SNS topic.";
    }

    /**
     * Gets the name of the topic to delete.
     *
     * @return topic name
     */
    public String getTopicName() {
        return topicName;
    }

    /**
     * Sets the name of the topic to delete.
     *
     * @param topicName topic name
     */
    @Option(option = "topicName", description = "Name of topic to delete")
    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }
}
