/**
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.aws.sns;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.ListTopicsResult;
import com.amazonaws.services.sns.model.Subscription;
import com.amazonaws.services.sns.model.Topic;
import com.nike.pdm.localstack.aws.AwsClientFactory;
import com.nike.pdm.localstack.compose.LocalStackModule;
import com.nike.pdm.localstack.core.ConsoleLogger;
import com.nike.pdm.localstack.core.Retry;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import java.util.Arrays;
import java.util.List;

/**
 * Task that lists SNS topics.
 */
public class ListSnsTopicsTask extends DefaultTask {

    public ListSnsTopicsTask() {
        setMustRunAfter(Arrays.asList(LocalStackModule.START_LOCALSTACK_TASK_NAME));
    }

    @TaskAction
    public void run() {
        Retry.execute(() -> {
            final AmazonSNS amazonSNS = AwsClientFactory.getInstance().sns(getProject());
            final SnsTaskUtil snsTaskUtil = new SnsTaskUtil(getProject());

            AsciiTable at = new AsciiTable();
            at.setTextAlignment(TextAlignment.JUSTIFIED_LEFT);
            at.getContext().setWidth(150);

            at.addRule();
            at.addRow("TopicName", "TopicArn", "Subscriptions");
            at.addRule();

            String nextToken = null;
            int topicCnt = 0;
            do {
                final ListTopicsResult listTopicsResult = amazonSNS.listTopics(nextToken);

                for (Topic topic : listTopicsResult.getTopics()) {
                    final String topicName = snsTaskUtil.getTopicName(topic.getTopicArn());
                    final List<Subscription> subscriptions = snsTaskUtil.getSubscriptions(topic.getTopicArn());

                    final StringBuilder subsBuilder = new StringBuilder();
                    subscriptions.forEach(subscription -> subsBuilder.append(subscription.getSubscriptionArn())
                            .append(System.lineSeparator()));

                    at.addRow(topicName, topic.getTopicArn(), subsBuilder.toString());
                    at.addRule();

                    topicCnt++;
                }

                nextToken = listTopicsResult.getNextToken();
            } while (nextToken != null);

            if (topicCnt > 0) {
                ConsoleLogger.log(at.render());
            } else {
                ConsoleLogger.log("No Topics Found!");
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
        return "Lists SNS topics.";
    }
}
