package com.nike.pdm.localstack.boot

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

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
                id "com.avast.gradle.docker-compose"    version "0.12.1"
            }
            
            apply plugin: "com.nike.pdm.localstack"
            
            dockerCompose {
                useComposeFiles = [ 'localstack/localstack-docker-compose.yml' ]
            }
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
