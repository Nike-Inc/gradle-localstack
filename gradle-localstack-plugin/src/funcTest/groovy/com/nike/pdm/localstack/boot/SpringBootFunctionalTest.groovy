/*
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.boot

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Timeout

import java.util.concurrent.TimeUnit

@Timeout(value = 3, unit = TimeUnit.MINUTES)
class SpringBootFunctionalTest extends Specification {

    @Rule TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
    }

    def "should add bootRunLocal tasks if springboot plugin applied"() {
        given:
        buildFile << """
            plugins {
                id "java"
                id "org.springframework.boot"           version "2.2.4.RELEASE"
                id "io.spring.dependency-management"    version "1.0.9.RELEASE"
            }
            
            apply plugin: "com.nike.pdm.localstack"
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('tasks')
                .withPluginClasspath()
                .build()

        then:
        result.output.contains("bootRunLocal")
        result.output.contains("bootRunLocalDebug")
    }
}
