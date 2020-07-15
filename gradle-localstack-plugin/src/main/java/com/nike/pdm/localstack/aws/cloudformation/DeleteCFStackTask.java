/**
 * Copyright 2019-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.aws.cloudformation;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;
import com.amazonaws.services.cloudformation.model.DeleteStackResult;
import com.nike.pdm.localstack.aws.AwsClientFactory;
import com.nike.pdm.localstack.compose.LocalStackModule;
import com.nike.pdm.localstack.core.ConsoleLogger;
import com.nike.pdm.localstack.core.Retry;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

import java.util.Arrays;

/**
 * Task that deletes a CloudFormation stack.
 */
public class DeleteCFStackTask extends DefaultTask {

    @Input
    private String stackName;

    public DeleteCFStackTask() {
        setMustRunAfter(Arrays.asList(LocalStackModule.START_LOCALSTACK_TASK_NAME));
    }

    @TaskAction
    public void run() {
        Retry.execute(() -> {
            final AmazonCloudFormation amazonCF = AwsClientFactory.getInstance().cloudformation(getProject());

            ConsoleLogger.log("Deleting CloudFormation Stack: %s", stackName);

            final DeleteStackRequest deleteStackRequest = new DeleteStackRequest();
            deleteStackRequest.setStackName(stackName);

            DeleteStackResult deleteStackResult = amazonCF.deleteStack(deleteStackRequest);

            ConsoleLogger.log("Deleted CloudFormation Stack: %s", stackName);

            return null;
        });
    }

    @Internal
    @Override
    public String getGroup() {
        return CloudFormationModule.GROUP_NAME;
    }

    @Internal
    @Override
    public String getDescription() {
        return "Deletes an existing CloudFormation stack.";
    }

    /**
     * Gets the name of the stack to delete.
     *
     * @return stack name
     */
    public String getStackName() {
        return stackName;
    }

    /**
     * Sets the name of the stack to delete.
     *
     * @param stackName stack name
     */
    @Option(option = "stackName", description = "Name of the stack to be deleted")
    public void setStackName(String stackName) {
        this.stackName = stackName;
    }
}
