/**
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package example.data;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import example.data.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Repository for accessing product catalog data in the database.
 */
@Component
public class ProductCatalogRepository {
    private static final Logger LOG = LoggerFactory.getLogger(ProductCatalogRepository.class);

    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    public Product create(Product newProduct) {
        dynamoDBMapper.save(newProduct);
        return newProduct;
    }

    public Product findOne(String id) {
        return dynamoDBMapper.load(Product.class, id);
    }

    public void delete(Product product) {
        dynamoDBMapper.delete(product);
    }
}
