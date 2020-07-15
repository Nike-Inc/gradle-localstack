/**
 * Copyright 2019-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package example.api.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Response returned when a new product is added to the catalog.
 */
@JsonPropertyOrder({
        "id"
})
public class AddProductResult {

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
