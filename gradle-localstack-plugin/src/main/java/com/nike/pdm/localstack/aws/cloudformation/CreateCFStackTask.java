/**
 * Copyright 2019-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.aws.cloudformation;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.CreateStackResult;
import com.amazonaws.services.cloudformation.model.OnFailure;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.RollbackConfiguration;
import com.amazonaws.services.cloudformation.model.Tag;
import com.amazonaws.util.StringUtils;
import com.nike.pdm.localstack.aws.AwsClientFactory;
import com.nike.pdm.localstack.compose.LocalStackModule;
import com.nike.pdm.localstack.core.ConsoleLogger;
import com.nike.pdm.localstack.core.Retry;
import org.apache.commons.io.IOUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Task that creates a CloudFormation stack.
 */
public class CreateCFStackTask extends DefaultTask {

    @Input
    private String stackName;

    @InputFile
    private File cfTemplate;

    @Optional
    @Input
    private Collection<String> capabilities;

    @Optional
    @Input
    private String clientRequestToken;

    @Optional
    @Input
    private Boolean disableRollback;

    @Optional
    @Input
    private Boolean enableTerminationProtection;

    @Optional
    @Input
    private Collection<String> notificationArns;

    @Optional
    @Input
    private OnFailure onFailure;

    @Optional
    @Input
    private Collection<Parameter> parameters;

    @Optional
    @Input
    private Collection<String> resourceTypes;

    @Optional
    @Input
    private String roleArn;

    @Optional
    @Input
    private RollbackConfiguration rollbackConfiguration;

    @Optional
    @InputFile
    private File stackPolicy;

    @Optional
    @Input
    private Integer timeoutInMinutes;

    @Optional
    @Input
    private Collection<Tag> tags;

    public CreateCFStackTask() {
        setDependsOn(Arrays.asList(LocalStackModule.START_LOCALSTACK_TASK_NAME));
        setMustRunAfter(Arrays.asList(LocalStackModule.START_LOCALSTACK_TASK_NAME));
    }

    @TaskAction
    public void run() {
        final Set<Class<? extends Throwable>> expectedErrors = new HashSet<>(Arrays.asList(
                IllegalArgumentException.class,
                FileNotFoundException.class));

        Retry.execute(() -> {
            final AmazonCloudFormation amazonCF = AwsClientFactory.getInstance().cloudformation(getProject());

            ConsoleLogger.log("Creating CloudFormation Stack: %s", stackName);

            CreateStackRequest createStackRequest = new CreateStackRequest();
            createStackRequest.setStackName(stackName);
            createStackRequest.setTemplateBody(IOUtils.toString(new FileInputStream(cfTemplate), Charset.defaultCharset()));

            if (capabilities != null && !capabilities.isEmpty()) {
                createStackRequest.setCapabilities(capabilities);
            }

            if (!StringUtils.isNullOrEmpty(clientRequestToken)) {
                createStackRequest.setClientRequestToken(clientRequestToken);
            }

            if (disableRollback != null) {
                createStackRequest.setDisableRollback(disableRollback);
            }

            if (enableTerminationProtection != null) {
                createStackRequest.setEnableTerminationProtection(enableTerminationProtection);
            }

            if (notificationArns != null && !notificationArns.isEmpty()) {
                createStackRequest.setNotificationARNs(notificationArns);
            }

            if (onFailure != null) {
                createStackRequest.setOnFailure(onFailure);
            } else {
                createStackRequest.setOnFailure(OnFailure.ROLLBACK);
            }

            if (parameters != null && !parameters.isEmpty()) {
                createStackRequest.setParameters(parameters);
            }

            if (resourceTypes != null && !resourceTypes.isEmpty()) {
                createStackRequest.setResourceTypes(resourceTypes);
            }

            if (!StringUtils.isNullOrEmpty(roleArn)) {
                createStackRequest.setRoleARN(roleArn);
            }

            if (rollbackConfiguration != null) {
                createStackRequest.setRollbackConfiguration(rollbackConfiguration);
            }

            if (stackPolicy != null) {
                createStackRequest.setStackPolicyBody(IOUtils.toString(new FileInputStream(stackPolicy), Charset.defaultCharset()));
            }

            if (timeoutInMinutes != null) {
                createStackRequest.setTimeoutInMinutes(timeoutInMinutes);
            }

            CreateStackResult createStackResult = amazonCF.createStack(createStackRequest);

            ConsoleLogger.log("Created CloudFormation Stack: %s", stackName);

            return null;
        }, expectedErrors);
    }

    @Internal
    @Override
    public String getGroup() {
        return CloudFormationModule.GROUP_NAME;
    }

    @Internal
    @Override
    public String getDescription() {
        return "Creates a new CloudFormation stack.";
    }

    /**
     * Gets the name of the stack to create.
     *
     * @return stack name
     * @see <a href="https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html">https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html</a>
     */
    public String getStackName() {
        return stackName;
    }

    /**
     * Sets the name of the stack to create.
     *
     * @param stackName stack name
     * @see <a href="https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html">https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html</a>
     */
    public void setStackName(String stackName) {
        this.stackName = stackName;
    }

    /**
     * Gets the CloudFormation template to run.
     *
     * @return cloudformation template
     * @see <a href="https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html">https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html</a>
     */
    public File getCfTemplate() {
        return cfTemplate;
    }

    /**
     * Sets the CloudFormation template to run.
     *
     * @param cfTemplate cloudformation template
     * @see <a href="https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html">https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html</a>
     */
    public void setCfTemplate(File cfTemplate) {
        this.cfTemplate = cfTemplate;
    }

    /**
     * Gets the capabilities.
     *
     * @return capabilities
     * @see <a href="https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html">https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html</a>
     */
    public Collection<String> getCapabilities() {
        return capabilities;
    }

    /**
     * Sets the capabilities
     *
     * @param capabilities capabilities
     * @see <a href="https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html">https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html</a>
     */
    public void setCapabilities(Collection<String> capabilities) {
        this.capabilities = capabilities;
    }

    /**
     * Gets the client request token.
     *
     * @return client request token
     * @see <a href="https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html">https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html</a>
     */
    public String getClientRequestToken() {
        return clientRequestToken;
    }

    /**
     * Sets the client request token.
     *
     * @param clientRequestToken client request token
     * @see <a href="https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html">https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html</a>
     */
    public void setClientRequestToken(String clientRequestToken) {
        this.clientRequestToken = clientRequestToken;
    }

    /**
     * Gets the disable rollback setting.
     *
     * @return disable rollback setting
     * @see <a href="https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html">https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html</a>
     */
    public Boolean getDisableRollback() {
        return disableRollback;
    }

    /**
     * Sets the disable rollback setting.
     *
     * @param disableRollback disable rollback setting
     * @see <a href="https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html">https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html</a>
     */
    public void setDisableRollback(Boolean disableRollback) {
        this.disableRollback = disableRollback;
    }

    /**
     * Gets the termination protection setting.
     *
     * @return termination protection setting
     * @see <a href="https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html">https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html</a>
     */
    public Boolean getEnableTerminationProtection() {
        return enableTerminationProtection;
    }

    /**
     * Sets the termination protection setting.
     *
     * @param enableTerminationProtection termination protection setting
     * @see <a href="https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html">https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html</a>
     */
    public void setEnableTerminationProtection(Boolean enableTerminationProtection) {
        this.enableTerminationProtection = enableTerminationProtection;
    }

    /**
     * Gets notification arns.
     *
     * @return notification arns
     * @see <a href="https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html">https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html</a>
     */
    public Collection<String> getNotificationArns() {
        return notificationArns;
    }

    /**
     * Sets notification arns
     *
     * @param notificationArns notification arns
     * @see <a href="https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html">https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html</a>
     */
    public void setNotificationArns(Collection<String> notificationArns) {
        this.notificationArns = notificationArns;
    }

    /**
     * Gets the on failure setting.
     *
     * @return on failure setting
     * @see <a href="https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html">https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html</a>
     */
    public OnFailure getOnFailure() {
        return onFailure;
    }

    /**
     * Sets the on failure setting.
     *
     * @param onFailure on failure setting
     * @see <a href="https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html">https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html</a>
     */
    public void setOnFailure(OnFailure onFailure) {
        this.onFailure = onFailure;
    }

    /**
     * Gets the template parameters.
     *
     * @return template parameters
     * @see <a href="https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html">https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html</a>
     */
    public Collection<Parameter> getParameters() {
        return parameters;
    }

    /**
     * Sets the template parameters.
     *
     * @param parameters template parameters
     * @see <a href="https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html">https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html</a>
     */
    public void setParameters(Collection<Parameter> parameters) {
        this.parameters = parameters;
    }

    /**
     * Gets the templatae resource types.
     *
     * @return template resource types
     * @see <a href="https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html">https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html</a>
     */
    public Collection<String> getResourceTypes() {
        return resourceTypes;
    }

    /**
     * Sets the template resource types.
     *
     * @param resourceTypes template resource types
     * @see <a href="https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html">https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html</a>
     */
    public void setResourceTypes(Collection<String> resourceTypes) {
        this.resourceTypes = resourceTypes;
    }

    /**
     * Gets the role arn.
     *
     * @return role arn
     * @see <a href="https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html">https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html</a>
     */
    public String getRoleArn() {
        return roleArn;
    }

    /**
     * Sets the role arn
     *
     * @param roleArn role arn
     * @see <a href="https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html">https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html</a>
     */
    public void setRoleArn(String roleArn) {
        this.roleArn = roleArn;
    }

    /**
     * Gets the rollback configuration.
     *
     * @return rollback configuration
     * @see <a href="https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html">https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html</a>
     */
    public RollbackConfiguration getRollbackConfiguration() {
        return rollbackConfiguration;
    }

    /**
     * Sets the rollback configuration
     *
     * @param rollbackConfiguration rollback configuration
     * @see <a href="https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html">https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html</a>
     */
    public void setRollbackConfiguration(RollbackConfiguration rollbackConfiguration) {
        this.rollbackConfiguration = rollbackConfiguration;
    }

    /**
     * Gets the stack policy.
     *
     * @return stack policy
     * @see <a href="https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html">https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html</a>
     */
    public File getStackPolicy() {
        return stackPolicy;
    }

    /**
     * Sets the stack policy.
     *
     * @param stackPolicy stack policy
     * @see <a href="https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html">https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html</a>
     */
    public void setStackPolicy(File stackPolicy) {
        this.stackPolicy = stackPolicy;
    }

    /**
     * Gets the timeout.
     *
     * @return timeout
     * @see <a href="https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html">https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html</a>
     */
    public Integer getTimeoutInMinutes() {
        return timeoutInMinutes;
    }

    /**
     * Sets the timeout.
     *
     * @param timeoutInMinutes timeout
     * @see <a href="https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html">https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html</a>
     */
    public void setTimeoutInMinutes(Integer timeoutInMinutes) {
        this.timeoutInMinutes = timeoutInMinutes;
    }

    /**
     * Gets the tags to apply to the stack.
     *
     * @return tags
     * @see <a href="https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html">https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html</a>
     */
    public Collection<Tag> getTags() {
        return tags;
    }

    /**
     * Sets the tags to apply to the stack.
     *
     * @param tags tags
     * @see <a href="https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html">https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudformation/model/CreateStackRequest.html</a>
     */
    public void setTags(Collection<Tag> tags) {
        this.tags = tags;
    }
}
