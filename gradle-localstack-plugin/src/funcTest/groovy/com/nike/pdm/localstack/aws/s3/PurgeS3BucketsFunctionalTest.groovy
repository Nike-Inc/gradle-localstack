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
import com.nike.pdm.localstack.util.ComposeFile
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Timeout

import java.util.concurrent.TimeUnit

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

@Timeout(value = 3, unit = TimeUnit.MINUTES)
class PurgeS3BucketsFunctionalTest extends Specification {

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

    def "should purge s3 bucket"() {
        given:
        buildFile << """
            import com.nike.pdm.localstack.aws.s3.CreateS3BucketsTask

            plugins {
                id "java"
                id "org.springframework.boot"           version "2.2.4.RELEASE"
                id "io.spring.dependency-management"    version "1.0.9.RELEASE"
            }
            
            apply plugin: "com.nike.pdm.localstack"
            
            task setupS3Bucket(type: CreateS3BucketsTask) {
                buckets = [ 'catalog-product-bucket' ]
            }
        """

        composeFile << ComposeFile.getContents()

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

        def purgeResult = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('purgeS3Buckets', '--buckets=catalog-product-bucket')
                .withPluginClasspath()
                .build()

        then:
        purgeResult.task(":purgeS3Buckets").outcome == SUCCESS
    }
}
