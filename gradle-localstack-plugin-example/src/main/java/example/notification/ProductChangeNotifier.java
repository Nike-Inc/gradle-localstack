/**
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package example.notification;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import example.notification.model.ProductChangeNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Sends product change notifications to an SQS queue.
 */
@Component
public class ProductChangeNotifier {
    private static final Logger LOG = LoggerFactory.getLogger(ProductChangeNotifier.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final AmazonSQS amazonSQS;
    private final AmazonSNS amazonSNS;
    private final String queueUrl;
    private final String topicArn;

    @Autowired
    public ProductChangeNotifier(AmazonSQS amazonSQS,
                                 AmazonSNS amazonSNS,
                                 @Value("${app.sqs.queueName}") String queueName,
                                 @Value("${app.sns.topicArn}") String topicArn) {
        this.amazonSQS = amazonSQS;
        this.amazonSNS = amazonSNS;
        this.queueUrl = amazonSQS.getQueueUrl(queueName).getQueueUrl();
        this.topicArn = topicArn;
    }

    /**
     * Sends a notification for newly added product.
     *
     * @param id product identifier
     */
    public void notifyAdd(String id) {
        final ProductChangeNotification notification = new ProductChangeNotification();
        notification.setChangeType("ADD");
        notification.setProductId(id);

        try {
            SendMessageResult sendMessageResult = amazonSQS.sendMessage(queueUrl, MAPPER.writeValueAsString(notification));
        } catch (JsonProcessingException ignored) {
            LOG.error("Unable to process ADD notification for: {}", id);
        }
    }

    /**
     * Sends a notificatioen for newly deleted product.
     *
     * @param id product identifier
     */
    public void notifyDelete(String id) {
        final ProductChangeNotification notification = new ProductChangeNotification();
        notification.setChangeType("DELETE");
        notification.setProductId(id);

        // Send notification to change notification queue
        try {
            SendMessageResult sendMessageResult = amazonSQS.sendMessage(queueUrl, MAPPER.writeValueAsString(notification));
        } catch (JsonProcessingException ignored) {
            LOG.error("Unable to process DELETE notification to queue for: {}", id);
        }

        // Send notification to drop notification topic
        try {
            amazonSNS.publish(topicArn, MAPPER.writeValueAsString(notification));
        } catch (JsonProcessingException ignored) {
            LOG.error("Unable to process DELETE notification to topic for: {}", id);
        }
    }
}
