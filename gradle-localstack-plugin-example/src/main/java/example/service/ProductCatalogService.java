/**
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package example.service;

import example.api.model.AddProductRequest;
import example.api.model.AddProductResult;
import example.api.model.GetProductResult;
import example.data.ProductCatalogRepository;
import example.data.model.Product;
import example.notification.ProductChangeNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Service for accessing the product catalog.
 */
@Component
public class ProductCatalogService {
    private static final Logger LOG = LoggerFactory.getLogger(ProductCatalogService.class);

    @Autowired
    private ProductCatalogRepository productCatalogRepository;

    @Autowired
    private ProductChangeNotifier productChangeNotifier;

    /**
     * Adds a product.
     *
     * @param addProductRequest request to add product
     * @return an {@link AddProductResult} containing info about newly created product
     */
    public AddProductResult addProduct(AddProductRequest addProductRequest) {
        Product newProduct = new Product();

        newProduct.setId(UUID.randomUUID().toString().replaceAll("-", ""));
        newProduct.setName(addProductRequest.getName());
        newProduct.setDescription(addProductRequest.getDescription());
        newProduct.setActive(addProductRequest.isActive());
        newProduct.setCreatedAt(System.currentTimeMillis());

        LOG.info("Adding product with id: {}", newProduct.getId());
        Product product = productCatalogRepository.create(newProduct);

        productChangeNotifier.notifyAdd(product.getId());

        AddProductResult addProductResult = new AddProductResult();
        addProductResult.setId(product.getId());

        return addProductResult;
    }

    /**
     * Gets a product.
     *
     * @param id product identifier
     * @return a {@link GetProductResult} containing info about the product
     */
    public GetProductResult getProduct(String id) {
        Product product = productCatalogRepository.findOne(id);

        if (product != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            GetProductResult getProductResult = new GetProductResult();
            getProductResult.setId(product.getId());
            getProductResult.setName(product.getName());
            getProductResult.setDescription(product.getDescription());
            getProductResult.setActive(product.isActive());
            getProductResult.setCreatedAt(sdf.format(new Date(product.getCreatedAt())));

            return getProductResult;
        } else {
            // product not found
            return null;
        }
    }

    /**
     * Deletes a product.
     *
     * @param id product identifier
     */
    public void deleteProduct(String id) {
        Product product = productCatalogRepository.findOne(id);

        if (product != null) {
            productCatalogRepository.delete(product);
            productChangeNotifier.notifyDelete(id);
        }
    }
}
