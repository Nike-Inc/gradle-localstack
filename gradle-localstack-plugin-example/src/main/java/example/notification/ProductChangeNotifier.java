/**
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package example.notification;

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
    private final String queueUrl;

    @Autowired
    public ProductChangeNotifier(AmazonSQS amazonSQS,
                                 @Value("${app.sqs.queueName}") String queueName) {
        this.amazonSQS = amazonSQS;

        final GetQueueUrlResult queueUrl = amazonSQS.getQueueUrl(queueName);
        this.queueUrl = queueUrl.getQueueUrl();
    }

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

    public void notifyDelete(String id) {
        final ProductChangeNotification notification = new ProductChangeNotification();
        notification.setChangeType("DELETE");
        notification.setProductId(id);

        try {
            SendMessageResult sendMessageResult = amazonSQS.sendMessage(queueUrl, MAPPER.writeValueAsString(notification));
        } catch (JsonProcessingException ignored) {
            LOG.error("Unable to process ADD notification for: {}", id);
        }
    }
}
