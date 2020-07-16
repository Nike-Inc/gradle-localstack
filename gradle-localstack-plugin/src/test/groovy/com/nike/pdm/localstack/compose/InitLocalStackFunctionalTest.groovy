/*
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.compose

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.*
import static org.junit.Assert.*

class InitLocalStackFunctionalTest extends Specification {

    @Rule TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
    }

    def "task creates default localstack configuration"() {
        given:
        buildFile << """
            plugins {
                id "java"
                id "org.springframework.boot"           version "2.2.4.RELEASE"
                id "io.spring.dependency-management"    version "1.0.9.RELEASE"
                id "com.avast.gradle.docker-compose"    version "0.12.1"
            }
            
            apply plugin: "com.nike.pdm.localstack"
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('initLocalStack')
                .withPluginClasspath()
                .build()

        then:
        result.task(":initLocalStack").outcome == SUCCESS
        assertTrue(new File(testProjectDir.root.getAbsolutePath() + "/localstack/localstack-docker-compose.yml").exists())
    }

    def "should not overwrite existing localstack-docker-compose"() {
        given:
        testProjectDir.newFolder("localstack")
        testProjectDir.newFile("localstack/localstack-docker-compose.yml")

        buildFile << """
            plugins {
                id "java"
                id "org.springframework.boot"           version "2.2.4.RELEASE"
                id "io.spring.dependency-management"    version "1.0.9.RELEASE"
                id "com.avast.gradle.docker-compose"    version "0.12.1"
            }
            
            apply plugin: "com.nike.pdm.localstack"
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('initLocalStack')
                .withPluginClasspath()
                .build()

        then:
        thrown(Exception)
    }
}
