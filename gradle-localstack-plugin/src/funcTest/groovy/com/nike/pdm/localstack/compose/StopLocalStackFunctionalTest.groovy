/*
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.compose

import com.nike.pdm.localstack.LocalStackDockerTestUtil
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static org.junit.Assert.assertTrue

class StopLocalStackFunctionalTest extends Specification {

    @Rule TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile
    File composeFile

    LocalStackDockerTestUtil dockerTestUtil = new LocalStackDockerTestUtil()

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')

        testProjectDir.newFolder('localstack')
        composeFile = testProjectDir.newFile('localstack/localstack-docker-compose.yml')

    }

    def cleanup() {
        dockerTestUtil.killLocalStack()
    }

    def "task should stop running localstack"() {
        given:
        buildFile << """
            plugins {
                id "java"
                id "org.springframework.boot"           version "2.2.4.RELEASE"
                id "io.spring.dependency-management"    version "1.0.9.RELEASE"
                id "com.avast.gradle.docker-compose"    version "0.12.1"
            }
            
            apply plugin: "com.nike.pdm.localstack"
            
            dockerCompose {
                useComposeFiles = [ 'localstack/localstack-docker-compose.yml' ]
            }
        """

        composeFile << """
            version: '3.5'
            
            services:
              localstack:
                image: localstack/localstack:0.11.0
                container_name: gradle-localstack-plugin-test
                ports:
                  - '4566:4566'   # LocalStack Edge
                  - '8055:8080'   # LocalStack Console
                networks:
                  - gradle-localstack-plugin-test
                environment:
                  - DEBUG=1
                  - DATA_DIR=/tmp/localstack/data
                  - AWS_ACCESS_KEY_ID=dummy
                  - AWS_SECRET_ACCESS_KEY=dummy
                volumes:
                  - './.localstack:/tmp/localstack'
                  - '/var/run/docker.sock:/var/run/docker.sock'
                  
            networks:
              gradle-localstack-plugin-test:
                name: gradle-localstack-plugin-test-network
        """

        when:
        def startResult = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('startLocalStack', '--stacktrace')
                .withPluginClasspath()
                .build()

        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('stopLocalStack', '--stacktrace')
                .withPluginClasspath()
                .build()

        then:
        startResult.task(":startLocalStack").outcome == SUCCESS
        result.task(":stopLocalStack").outcome == SUCCESS

        // .localstack directory should not be deleted on "stopLocalStack"
        assertTrue(new File(testProjectDir.root.path +"/localstack/.localstack").exists())
    }
}