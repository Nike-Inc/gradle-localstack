/**
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.aws;

import com.nike.pdm.localstack.aws.cloudformation.CloudFormationModule;
import com.nike.pdm.localstack.aws.dynamodb.DynamoDbModule;
import com.nike.pdm.localstack.aws.s3.S3Module;
import com.nike.pdm.localstack.aws.sqs.SqsModule;
import org.gradle.api.Project;

/**
 * Loads tasks and configuration for AWS.
 */
public class AwsModule {

    /**
     * Delegates task creation and configuration to underlying AWS service specific modules.
     *
     * @param project gradle project
     */
    public static void load(Project project) {
        project.getLogger().info("Loading AWS Modules:");

        CloudFormationModule.load(project);
        DynamoDbModule.load(project);
        S3Module.load(project);
        SqsModule.load(project);
    }
}
