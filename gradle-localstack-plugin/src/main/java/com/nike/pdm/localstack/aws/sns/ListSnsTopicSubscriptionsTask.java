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
import com.amazonaws.services.sns.model.ListSubscriptionsByTopicResult;
import com.amazonaws.services.sns.model.Subscription;
import com.nike.pdm.localstack.aws.AwsClientFactory;
import com.nike.pdm.localstack.compose.LocalStackModule;
import com.nike.pdm.localstack.core.ConsoleLogger;
import com.nike.pdm.localstack.core.Retry;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestLine;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

import java.util.Arrays;

/**
 * Tasks that lists subscriptions for an SNS topic.
 */
public class ListSnsTopicSubscriptionsTask extends DefaultTask {

    @Input
    private String topicName;

    public ListSnsTopicSubscriptionsTask() {
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
            at.addRow("Arn", "Protocol", "Endpoint");
            at.addRule();

            CWC_LongestLine cwc = new CWC_LongestLine();
            at.getRenderer().setCWC(cwc);

            String nextToken = null;
            int subCnt = 0;
            do {
                try {
                    final ListSubscriptionsByTopicResult listSubscriptionsByTopicResult = amazonSNS.listSubscriptionsByTopic(snsTaskUtil.getTopicArn(topicName));

                    for (Subscription subscription : listSubscriptionsByTopicResult.getSubscriptions()) {
                        at.addRow(subscription.getSubscriptionArn(), subscription.getProtocol(), subscription.getEndpoint());
                        at.addRule();

                        subCnt++;
                    }

                    nextToken = listSubscriptionsByTopicResult.getNextToken();

                    if (nextToken == null && subCnt > 0) {
                        ConsoleLogger.log(at.render());
                    } else {
                        ConsoleLogger.log("No Subscriptions Found!");
                    }
                } catch (AmazonServiceException e) {
                    if (e.getStatusCode() == 404) {
                        ConsoleLogger.log("Topic Not Found!");
                        break;
                    }

                    throw e;
                }
            } while (nextToken != null);

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
        return "Lists subscriptions to SNS topic.";
    }

    /**
     * Gets the name of the topic for which to list subscriptions.
     *
     * @return topic name
     */
    public String getTopicName() {
        return topicName;
    }

    /**
     * Sets the name of the topic for which to list subscriptions.
     *
     * @param topicName topic name
     */
    @Option(option = "topicName", description = "Name of topic for which to list subscriptions")
    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }
}
