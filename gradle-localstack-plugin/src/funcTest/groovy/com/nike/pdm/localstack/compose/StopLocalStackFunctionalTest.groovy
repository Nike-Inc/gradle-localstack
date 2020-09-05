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
import spock.lang.Timeout

import java.util.concurrent.TimeUnit

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static org.junit.Assert.assertTrue

@Timeout(value= 3, unit = TimeUnit.MINUTES)
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

        composeFile << ComposeFile.getContents()

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
