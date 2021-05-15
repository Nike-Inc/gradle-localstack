/*
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.aws.cloudformation

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
class DeleteCFStackFunctionalTest extends Specification {

    @Rule TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile
    File composeFile
    File cloudformationFile

    LocalStackDockerTestUtil dockerTestUtil = new LocalStackDockerTestUtil()

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')

        testProjectDir.newFolder('localstack')
        composeFile = testProjectDir.newFile('localstack/localstack-docker-compose.yml')

        testProjectDir.newFolder('cloudformation')
        cloudformationFile = testProjectDir.newFile("cloudformation/test-stack.yml")
    }

    def cleanup() {
        dockerTestUtil.killLocalStack()
    }

    def "should list cloudformation stack"() {
        given:
        buildFile << """
            import com.nike.pdm.localstack.aws.cloudformation.CreateCFStackTask

            plugins {
                id "java"
                id "org.springframework.boot"           version "2.2.4.RELEASE"
                id "io.spring.dependency-management"    version "1.0.9.RELEASE"
            }
            
            apply plugin: "com.nike.pdm.localstack"
            
            task createStack(type: CreateCFStackTask) {
                stackName = 'test-stack'
                cfTemplate = file('cloudformation/test-stack.yml')
            }
        """

        composeFile << ComposeFile.getContents()

        cloudformationFile << """
            AWSTemplateFormatVersion: '2010-09-09'
            Resources:
              CatalogProductsTable:
                Type: "AWS::DynamoDB::Table"
                Properties:
                  TableName: "catalog.products"
                  PointInTimeRecoverySpecification:
                    PointInTimeRecoveryEnabled: true
                  BillingMode: "PAY_PER_REQUEST"
                  SSESpecification:
                    SSEEnabled: true
                  AttributeDefinitions:
                    - AttributeName: "id"
                      AttributeType: "S"
                  KeySchema:
                    - AttributeName: "id"
                      KeyType: "HASH"
              ProductChangeNotificationQueue:
                Type: "AWS::SQS::Queue"
                Properties:
                  QueueName: "catalog-product-change-notification"
                  MessageRetentionPeriod: 1209600
                  MaximumMessageSize: 262144
        """

        when:
        def createResult = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('createStack')
                .withPluginClasspath()
                .build()

        def deleteResult = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('deleteCFStack', '--stackName=test-stack')
                .withPluginClasspath()
                .build()

        def listResult = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('listCFStacks')
                .withPluginClasspath()
                .build()

        then:
        createResult.task(":createStack").outcome == SUCCESS

        deleteResult.task(":deleteCFStack").outcome == SUCCESS
        deleteResult.output.contains("Deleted CloudFormation Stack: test-stack" + System.lineSeparator())

        listResult.task(":listCFStacks").outcome == SUCCESS
        listResult.output.contains("test-stack")
        listResult.output.contains("DELETE_COMPLETE")
    }
}
