/**
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package example;

import example.api.model.GetProductResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

/**
 * Example integration test that uses LocalStack.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
@TestPropertySource("classpath:application-local.properties")
public class ExampleIntegrationTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void shouldReturnProduct() {
        ResponseEntity<GetProductResult> response = testRestTemplate.getForEntity("/products/00001", GetProductResult.class);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("00001", response.getBody().getId());
    }

    @Test
    public void shouldDeleteProduct() {
        // Validate the product exists
        ResponseEntity<GetProductResult> getResponse = testRestTemplate.getForEntity("/products/00009", GetProductResult.class);
        assertEquals(200, getResponse.getStatusCodeValue());
        assertEquals("00009", getResponse.getBody().getId());

        // Delete the product
        testRestTemplate.delete("/products/{id}", "00009");

        // Validate that the product has been deleted
        ResponseEntity<GetProductResult> getResponse2 = testRestTemplate.getForEntity("/products/00009", GetProductResult.class);
        assertEquals(404, getResponse2.getStatusCodeValue());
    }
}
