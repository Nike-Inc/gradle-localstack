/**
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Utility for working with local Docker agent.
 */
public class LocalStackDockerTestUtil {
    private static final Logger LOG = LoggerFactory.getLogger(LocalStackDockerTestUtil.class);
    private static final String LOCALSTACK_TEST_DOCKER_CONTAINER_NAME = "/gradle-localstack-plugin-test";
    private static final String LOCALSTACK_TEST_IMAGE_NAME = "localstack/localstack";
    private static final String LOCALSTACK_TEST_DOCKER_TAG = "0.11.0";

    private final DockerClient dockerClient;

    public LocalStackDockerTestUtil() {
        final DockerClientConfig standard = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        final DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(standard.getDockerHost())
                .sslConfig(standard.getSSLConfig())
                .build();

        this.dockerClient = DockerClientImpl.getInstance(standard, httpClient);
    }

    /**
     * Pulls the default localstack container.
     *
     * @throws InterruptedException
     */
    public void pullLocalStack() throws InterruptedException {
        pullLocalStack(LOCALSTACK_TEST_DOCKER_TAG);
    }

    /**
     * Pulls the localstack container with the supplied tag.
     *
     * @param tag docker tag
     * @throws InterruptedException
     */
    public void pullLocalStack(String tag) throws InterruptedException {
        dockerClient.pullImageCmd("localstack/localstack")
                .withTag(LOCALSTACK_TEST_DOCKER_TAG)
                .exec(new PullImageResultCallback())
                .awaitCompletion(5, TimeUnit.MINUTES);
    }

    public void startLocalStack() throws InterruptedException {
        if (!imageExists(LOCALSTACK_TEST_IMAGE_NAME, LOCALSTACK_TEST_DOCKER_TAG)) {
            LOG.info("Pulling {}:{}", LOCALSTACK_TEST_IMAGE_NAME, LOCALSTACK_TEST_DOCKER_TAG);
            pullLocalStack();
        }
    }

    /**
     * Kills the running localstack test container and removes its network.
     */
    public void killLocalStack() {
        final String containerId = getContainerId(LOCALSTACK_TEST_DOCKER_CONTAINER_NAME);

        if (containerId != null) {
            LOG.info("Killing container [name: '{}', containerId: '{}']", LOCALSTACK_TEST_DOCKER_CONTAINER_NAME, containerId);
            dockerClient.killContainerCmd(containerId).exec();
            dockerClient.removeContainerCmd(containerId).exec();
        } else {
            LOG.info("No running localstack container found!");
        }
    }

    /**
     * Gets the container id of the container with the supplied name.
     *
     * @param containerName container name
     * @return container id
     */
    public String getContainerId(String containerName) {
        final List<Container> result = dockerClient.listContainersCmd().exec();

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

    /**
     * Checks if the specified docker image exists on the local system.
     *
     * @param imageName image name
     * @param tag image tag (if not specified defaults to <code>LATEST</code>)
     * @return <code>true</code> if the image exists; otherwise <code>false</code>
     */
    public boolean imageExists(String imageName, String tag) {
        if (tag == null || tag.isEmpty()) {
            tag = "LATEST";
        }

        final List<Image> images = dockerClient.listImagesCmd().withImageNameFilter(imageName + ":" + tag).exec();
        return images.size() == 1;
    }
}
