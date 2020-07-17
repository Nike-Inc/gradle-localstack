package com.nike.pdm.localstack.aws.cloudformation

import com.nike.pdm.localstack.LocalStackDockerTestUtil
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

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
                id "com.avast.gradle.docker-compose"    version "0.12.1"
            }
            
            apply plugin: "com.nike.pdm.localstack"
            
            dockerCompose {
                useComposeFiles = [ 'localstack/localstack-docker-compose.yml' ]
            }
            
            task createStack(type: CreateCFStackTask) {
                stackName = 'test-stack'
                cfTemplate = file('cloudformation/test-stack.yml')
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
