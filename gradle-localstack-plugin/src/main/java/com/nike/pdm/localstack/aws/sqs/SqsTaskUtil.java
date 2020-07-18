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
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.amazonaws.util.StringUtils;
import com.nike.pdm.localstack.aws.AwsClientFactory;
import org.gradle.api.Project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Utility methods shared amongst the SQS tasks.
 */
public class SqsTaskUtil {

    private final Project project;

    public SqsTaskUtil(Project project) {
        this.project = project;
    }

    /**
     * Checks if the SQS queue exists.
     *
     * @param queueName name of the queue
     * @return <code>true</code> if the queue exists; otherwise <code>false</code>
     */
    public boolean queueExists(String queueName) {
        final AmazonSQS amazonSQS = AwsClientFactory.getInstance().sqs(project);

        try {
            GetQueueUrlResult queueUrl = amazonSQS.getQueueUrl(queueName);
            return true;
        } catch (QueueDoesNotExistException ignored) {
            return false;
        }
    }

    /**
     * Returns a list of resolved queue urls from their queue names.
     *
     * @param queueNames list of queue names to resolve
     * @return list of resolved queue urls
     */
    public List<String> getQueueUrls(List<String> queueNames) {
        final AmazonSQS amazonSQS = AwsClientFactory.getInstance().sqs(project);

        List<String> queueUrls = new ArrayList<>();
        queueNames.forEach(queueName -> {
            GetQueueUrlResult queueUrlResult = amazonSQS.getQueueUrl(queueName);
            queueUrls.add(queueUrlResult.getQueueUrl());
        });

        return queueUrls;
    }

    /**
     * Returns the queue url for the queue name.
     *
     * @param queueName queue name
     * @return resolved queue url
     */
    public String getQueueUrl(String queueName) {
        final AmazonSQS amazonSQS = AwsClientFactory.getInstance().sqs(project);

        GetQueueUrlResult queueUrlResult = amazonSQS.getQueueUrl(queueName);
        return queueUrlResult.getQueueUrl();
    }

    /**
     * Creates an SQS queue.
     *
     * @param queueName queue name
     * @param queueAttributes queue attributes
     * @return {@link CreateQueueResult}
     */
    public CreateQueueResult createQueue(String queueName, Map<String, String> queueAttributes) {
        final AmazonSQS amazonSQS = AwsClientFactory.getInstance().sqs(project);

        final CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName);

        if (queueAttributes != null && !queueAttributes.isEmpty()) {
            createQueueRequest.setAttributes(queueAttributes);
        }

        return amazonSQS.createQueue(createQueueRequest);
    }

    /**
     * Gets the ARN of an SQS queue based on its queue url.
     *
     * @param queueUrl sqs queue url
     * @return queue arn
     */
    public String getQueueArn(String queueUrl) {
        final AmazonSQS amazonSQS = AwsClientFactory.getInstance().sqs(project);

        GetQueueAttributesResult queueAttributes = amazonSQS.getQueueAttributes(new GetQueueAttributesRequest(queueUrl));
        return queueAttributes.getAttributes().get("QueueArn");
    }

    /**
     * Gets the ARN of an SQS queue based on its queue name.
     *
     * @param queueName queue name
     * @return queue arn
     */
    public String getQueueArnFromName(String queueName) {
        final String queueUrl = getQueueUrl(queueName);
        return getQueueArn(queueUrl);
    }

    /**
     * Gets the name of a queue from its queue url.
     *
     * @param queueUrl queue url
     * @return the name of the queue or an empty string if the queue url is not valid.
     */
    public String getQueueNameFromUrl(String queueUrl) {
        if (!StringUtils.isNullOrEmpty(queueUrl)) {
            return queueUrl.substring(queueUrl.lastIndexOf("/") + 1);
        }

        return "";
    }

    /**
     * Gets the approximate number of messages on the queue.
     *
     * @param queueUrl queue url
     * @return approximate number of messaages
     */
    public Long getApproximateNumberOfMessages(String queueUrl) {
        String val = getQueueAttribute(queueUrl, "ApproximateNumberOfMessages");
        if (!StringUtils.isNullOrEmpty(val)) {
            return Long.parseLong(val);
        } else {
            return 0L;
        }
    }

    /**
     * Gets the specified queue attribute if it exists.
     *
     * @param queueUrl queue url
     * @param attributeName attribute name
     * @return attribute value if it exists; otherwise <code>null</code>
     */
    public String getQueueAttribute(String queueUrl, String attributeName) {
        final AmazonSQS amazonSQS = AwsClientFactory.getInstance().sqs(project);

        GetQueueAttributesResult queueAttributes = amazonSQS.getQueueAttributes(queueUrl, Arrays.asList(attributeName));
        return queueAttributes.getAttributes().get("attributeName");
    }

    /**
     * Sets the specified attributes on the queue.
     *
     * @param queueUrl queue url
     * @param attributes queue attributes
     */
    public void setQueueAttributes(String queueUrl, Map<String, String> attributes) {
        final AmazonSQS amazonSQS = AwsClientFactory.getInstance().sqs(project);

        amazonSQS.setQueueAttributes(queueUrl, attributes);
    }
}
