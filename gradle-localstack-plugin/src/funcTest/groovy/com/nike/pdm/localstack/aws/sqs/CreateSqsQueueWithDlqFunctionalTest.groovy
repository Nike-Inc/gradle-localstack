/*
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.aws.sqs

import com.nike.pdm.localstack.LocalStackDockerTestUtil
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Timeout

import java.util.concurrent.TimeUnit

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

@Ignore
@Timeout(value = 3, unit = TimeUnit.MINUTES)
class CreateSqsQueueWithDlqFunctionalTest extends Specification {

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

    def "should create sqs queue with dlq"() {
        given:
        buildFile << """
            import com.nike.pdm.localstack.aws.sqs.CreateSqsQueueWithDlqTask

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
            
            task setupLocalQueue(type: CreateSqsQueueWithDlqTask) {
                queueName = 'catalog-product-change-notification'
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
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('startLocalStack', 'setupLocalQueue')
                .withPluginClasspath()
                .build()
        then:
        result.task(":setupLocalQueue").outcome == SUCCESS
    }
}