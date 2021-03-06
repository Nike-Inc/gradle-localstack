/*
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.aws.sqs

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
class PublishSqsFunctionalTest extends Specification {

    @Rule TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile
    File composeFile
    File messageFile

    LocalStackDockerTestUtil dockerTestUtil = new LocalStackDockerTestUtil()

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')

        testProjectDir.newFolder('localstack')
        composeFile = testProjectDir.newFile('localstack/localstack-docker-compose.yml')

        messageFile = testProjectDir.newFile('message.json')
    }

    def cleanup() {
        dockerTestUtil.killLocalStack()
    }

    def "should publish message to sqs queue"() {
        given:
        buildFile << """
            import com.nike.pdm.localstack.aws.sqs.CreateSqsQueuesTask

            plugins {
                id "java"
                id "org.springframework.boot"           version "2.2.4.RELEASE"
                id "io.spring.dependency-management"    version "1.0.9.RELEASE"
            }
            
            apply plugin: "com.nike.pdm.localstack"
            
            task setupLocalQueue(type: CreateSqsQueuesTask) {
                queueNames = [ 'catalog-product-change-notification' ]
                queueAttributes = [
                        VisibilityTimeout: '10'
                ]
            }
        """

        composeFile << ComposeFile.getContents()

        messageFile << "{ \"productId\": \"12345\" }"

        when:
        def setupResult = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('startLocalStack')
                .withPluginClasspath()
                .build()

        def publishResult = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('publishSqs', '--queueNames=catalog-product-change-notification', '--message=\"' + messageFile.path + '\"')
                .withPluginClasspath()
                .build()

        then:
        publishResult.task(":publishSqs").outcome == SUCCESS
    }
}
