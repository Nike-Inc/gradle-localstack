/**
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class DockerTestUtil {
    public static final String LOCALSTACK_TEST_DOCKER_CONTAINER_NAME = "/gradle-localstack-plugin-test";

    private static final Logger LOG = LoggerFactory.getLogger(DockerTestUtil.class);

    private final DockerClient dockerClient;

    public DockerTestUtil() {
        DockerClientConfig standard = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(standard.getDockerHost())
                .sslConfig(standard.getSSLConfig())
                .build();

        this.dockerClient = DockerClientImpl.getInstance(standard, httpClient);
    }

    public void killLocalStack() {
        String containerId = getContainerId(LOCALSTACK_TEST_DOCKER_CONTAINER_NAME);
        dockerClient.killContainerCmd(containerId).exec();
        dockerClient.removeContainerCmd(containerId).exec();
    }

    public String getContainerId(String containerName) {
        List<Container> result = dockerClient.listContainersCmd().exec();

        if (result != null) {
            for (Container container : result) {
                if (container.getNames() != null) {
                    if (Arrays.asList(container.getNames()).contains(containerName)) {
                        return container.getId();
                    }
                }
            }
        }

        return null;
    }
}
