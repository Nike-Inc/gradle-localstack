/**
 * Copyright 2019-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.compose;

import com.nike.pdm.localstack.boot.SpringBootExtension;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.util.ConfigureUtil;

import java.io.File;

/**
 * LocalStack Gradle Plugin extension properties.
 */
public class LocalStackExtension {
    public static final String NAME = "localstack";

    // Defaults
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 4566;
    private static final String DEFAULT_SIGNING_REGION = "us-east-1";

    private File workingDir;
    private String host = DEFAULT_HOST;
    private int port = DEFAULT_PORT;
    private String signingRegion = DEFAULT_SIGNING_REGION;
    private SpringBootExtension springboot;

    /**
     * Gets the LocalStack working directory.
     *
     * @return localstack working directory if configured via extension; otherwise <code>null</code>
     */
    public File getWorkingDir() {
        return workingDir;
    }

    /**
     * Sets the LocalStack working directory.
     *
     * @param workingDir localstack working directory
     */
    public void setWorkingDir(File workingDir) {
        this.workingDir = workingDir;
    }

    /**
     * Gets the localstack hostname.
     *
     * @return localstack hostname
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the localstack hostname.
     *
     * @param host localstack hostname
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Gets the localstack edge services port.
     *
     * @return localstack edge services port
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the localstack edge services port.
     *
     * @param port localstack edge services port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Gets the default localstack AWS signing region.
     *
     * @return default localstack AWS signing region
     */
    public String getSigningRegion() {
        return signingRegion;
    }

    /**
     * Sets the default localstack AWS signing region.
     *
     * @param signingRegion default localstack AWS signing region
     */
    public void setSigningRegion(String signingRegion) {
        this.signingRegion = signingRegion;
    }

    /**
     * Gets the nested `springboot` configuration extension.
     *
     * @return springboot configuration extension
     */
    public SpringBootExtension getSpringboot() {
        return springboot;
    }

    //
    // SpringBoot Nested Extension (required by Gradle)
    //

    void springboot(Closure c) {
        springboot = ConfigureUtil.configure(c, new SpringBootExtension());
    }

    void springboot(Action<? super SpringBootExtension> action) {
        action.execute(new SpringBootExtension());
    }
}
