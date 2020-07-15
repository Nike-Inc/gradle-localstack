/**
 * Copyright 2019-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package example.notification.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Product change notification message that is sent to the change notification sqs queue.
 */
@JsonPropertyOrder({
        "changeType",
        "productId"
})
public class ProductChangeNotification {

    @JsonProperty("change_type") private String changeType;
    @JsonProperty("product_id") private String productId;

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}
