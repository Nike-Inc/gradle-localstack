/*
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.aws.s3

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.nike.pdm.localstack.LocalStackDockerTestUtil
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class DeleteS3BucketsFunctionalTest extends Specification {

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

    def "should delete s3 bucket"() {
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

        when:
        def setupResult = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('startLocalStack')
                .withPluginClasspath()
                .build()

        def deleteResult = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('deleteS3Buckets', '--buckets=catalog-product-bucket')
                .withPluginClasspath()
                .build()

        then:
        deleteResult.task(":deleteS3Buckets").outcome == SUCCESS
    }

    def "should delete multiple s3 buckets"() {
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
            
            task setupS3Buckets(type: CreateS3BucketsTask) {
                buckets = [ 
                    'catalog-product-bucket',
                    'catalog-pricing-bucket'
                ]
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

        when:
        def setupResult = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('startLocalStack')
                .withPluginClasspath()
                .build()

        def deleteResult = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('deleteS3Buckets', '--buckets=catalog-product-bucket,catalog-pricing-bucket')
                .withPluginClasspath()
                .build()

        then:
        deleteResult.task(":deleteS3Buckets").outcome == SUCCESS
    }

    def "should not delete non-empty bucket"() {
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

        def dummyFile = testProjectDir.newFile("dummy-file.txt")
        dummyFile << """
            File for testing the S3 plugin tasks.
        """

        def s3Client = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration('http://localhost:4566', 'us-east-1'))
                .withPathStyleAccessEnabled(true)
                .build()

        when:
        def setupResult = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('startLocalStack')
                .withPluginClasspath()
                .build()

        s3Client.putObject('catalog-product-bucket', dummyFile.name, dummyFile)

        def deleteResult = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('deleteS3Buckets', '--buckets=catalog-product-bucket')
                .withPluginClasspath()
                .build()

        then:
        deleteResult.task(":deleteS3Buckets").outcome == FAILED
        deleteResult.output.contains("Bucket is not empty and the 'force' parameter is set to 'false': catalog-product-bucket")
    }

    def "should force delete non-empty bucket"() {
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

        def dummyFile = testProjectDir.newFile("dummy-file.txt")
        dummyFile << """
            File for testing the S3 plugin tasks.
        """

        def s3Client = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration('http://localhost:4566', 'us-east-1'))
                .withPathStyleAccessEnabled(true)
                .build()

        when:
        def setupResult = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('startLocalStack')
                .withPluginClasspath()
                .build()

        s3Client.putObject('catalog-product-bucket', dummyFile.name, dummyFile)

        def deleteResult = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('deleteS3Buckets', '--buckets=catalog-product-bucket', '--force')
                .withPluginClasspath()
                .build()

        then:
        deleteResult.task(":deleteS3Buckets").outcome == SUCCESS
    }
}
