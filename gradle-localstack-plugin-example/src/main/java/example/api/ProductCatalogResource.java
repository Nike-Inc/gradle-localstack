/**
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package example.api;

import example.api.model.AddProductRequest;
import example.api.model.AddProductResult;
import example.api.model.GetProductResult;
import example.service.ProductCatalogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * API for accessing the product catalog.
 */
@RestController
public class ProductCatalogResource {
    private static final Logger LOG = LoggerFactory.getLogger(ProductCatalogResource.class);

    @Autowired
    private ProductCatalogService productCatalogService;

    /**
     * Creates a new product.
     *
     * @param request request to create new product
     * @return an {@link AddProductResult} containing info about the newly created product
     */
    @PostMapping(value = "/products",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AddProductResult> addProduct(@RequestBody AddProductRequest request) {
        AddProductResult addProductResult = productCatalogService.addProduct(request);
        return ResponseEntity.ok(addProductResult);
    }

    /**
     * Gets a product.
     *
     * @param id product identifier
     * @return a {@link GetProductResult} containing info about the product
     */
    @GetMapping(value = "/products/{id}",
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetProductResult> getProduct(@PathVariable("id") String id) {
        GetProductResult product = productCatalogService.getProduct(id);

        if (product != null) {
            return ResponseEntity.ok(product);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Deletes a product.
     *
     * @param id product identifier
     * @return an empty http 200 response
     */
    @DeleteMapping(value = "/products/{id}",
                   produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteProduct(@PathVariable("id") String id) {
        productCatalogService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }
}
