/*
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.aws.dynamodb

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
class ListDynamoDbTablesFunctionalTest extends Specification {

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

    def "should list dynamodb tables"() {
        given:
        buildFile << """
            import com.amazonaws.services.dynamodbv2.model.AttributeDefinition
            import com.amazonaws.services.dynamodbv2.model.KeySchemaElement
            import com.amazonaws.services.dynamodbv2.model.KeyType
            import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType
            import com.nike.pdm.localstack.aws.dynamodb.CreateDynamoDbTableTask

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
            
            task setupLocalTable(type: CreateDynamoDbTableTask) {
                tableName = 'catalog.products'
                keySchema = [
                        new KeySchemaElement("id", KeyType.HASH)
                ]
                attributeDefinitions = [
                        new AttributeDefinition("id", ScalarAttributeType.S)
                ]
            }
        """

        composeFile << ComposeFile.getContents()

        when:
        def setupResult = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('startLocalStack')
                .withPluginClasspath()
                .build()

        def listResult = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('listDynamoDbTables')
                .withPluginClasspath()
                .build()

        then:
        listResult.task(":listDynamoDbTables").outcome == SUCCESS
        listResult.output.contains("catalog.products")
    }
}
