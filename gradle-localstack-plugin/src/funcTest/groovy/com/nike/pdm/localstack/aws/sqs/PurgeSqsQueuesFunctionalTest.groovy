/*
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.aws.sqs

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.nike.pdm.localstack.LocalStackDockerTestUtil
import com.nike.pdm.localstack.util.ComposeFile
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Timeout

import java.util.concurrent.TimeUnit

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

@Timeout(value = 3, unit = TimeUnit.MINUTES)
class PurgeSqsQueuesFunctionalTest extends Specification {

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

    def "should purge sqs queue"() {
        given:
        buildFile << """
            import com.nike.pdm.localstack.aws.sqs.CreateSqsQueuesTask

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
            
            task setupLocalQueue(type: CreateSqsQueuesTask) {
                queueNames = [ 'catalog-product-change-notification' ]
                queueAttributes = [
                        VisibilityTimeout: '10'
                ]
            }
        """

        composeFile << ComposeFile.getContents()

        def sqsClient = AmazonSQSClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration('http://localhost:4566', 'us-east-1'))
                .build()

        when:
        def setupResult = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('startLocalStack')
                .withPluginClasspath()
                .build()

        def queueUrl = sqsClient.getQueueUrl('catalog-product-change-notification')
        sqsClient.sendMessage(queueUrl.getQueueUrl(), 'This is a test message')

        def purgeResult = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('purgeSqsQueues', '--queueNames=catalog-product-change-notification')
                .withPluginClasspath()
                .build()

        then:
        purgeResult.task(":purgeSqsQueues").outcome == SUCCESS
    }
}
