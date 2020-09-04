/*
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.compose

import com.nike.pdm.localstack.LocalStackDockerTestUtil
import com.nike.pdm.localstack.util.ComposeFile
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class RestartLocalStackFunctionalTest extends Specification {

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

    def "should restart localstack"() {
        given:
        buildFile << """
            import com.nike.pdm.localstack.aws.s3.CreateS3BucketsTask
            
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
            
            task setupS3Bucket(type: CreateS3BucketsTask) {
                buckets = [ 'catalog-product-bucket' ]
            }
        """

        composeFile << ComposeFile.getContents()

        when:
        def setupResult = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('startLocalStack')
                .withPluginClasspath()
                .build()

        def restartResult = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('restartLocalStack')
                .withPluginClasspath()
                .build()

        then:
        setupResult.task(":startLocalStack").outcome == SUCCESS

        restartResult.task(":restartLocalStack").outcome == SUCCESS
        restartResult.output.contains("Task :cleanLocalStack")
        restartResult.output.contains("Task :killLocalStack")
        restartResult.output.contains("Task :composeUp")
        restartResult.output.contains("Task :setupS3Bucket")
        restartResult.output.contains("Task :startLocalStack")
    }
}
