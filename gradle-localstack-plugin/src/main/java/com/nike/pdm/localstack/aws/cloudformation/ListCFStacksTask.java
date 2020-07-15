/**
 * Copyright 2019-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.aws.cloudformation;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.ListStacksRequest;
import com.amazonaws.services.cloudformation.model.ListStacksResult;
import com.amazonaws.util.StringUtils;
import com.nike.pdm.localstack.aws.AwsClientFactory;
import com.nike.pdm.localstack.compose.LocalStackModule;
import com.nike.pdm.localstack.core.ConsoleLogger;
import com.nike.pdm.localstack.core.Retry;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Task that lists the configured CloudFormation stacks.
 */
public class ListCFStacksTask extends DefaultTask {

    @Optional
    @Input
    private String statusFilter;

    public ListCFStacksTask() {
        setMustRunAfter(Arrays.asList(LocalStackModule.START_LOCALSTACK_TASK_NAME));
    }

    @TaskAction
    public void run() {
        Retry.execute(() -> {
            final AmazonCloudFormation amazonCF = AwsClientFactory.getInstance().cloudformation(getProject());

            AsciiTable at = new AsciiTable();
            at.setTextAlignment(TextAlignment.JUSTIFIED_LEFT);
            at.getContext().setWidth(150);

            at.addRule();
            at.addRow("StackId", "StackName", "StackStatus", "CreationTime");
            at.addRule();

            String token = null;
            do {
                final ListStacksRequest listStacksRequest = new ListStacksRequest();
                listStacksRequest.setNextToken(token);
                listStacksRequest.setStackStatusFilters(parseStatusFilter(statusFilter));

                final ListStacksResult listStacksResult = amazonCF.listStacks(listStacksRequest);
                if (!listStacksResult.getStackSummaries().isEmpty()) {
                    listStacksResult.getStackSummaries().forEach(stackSummary -> {
                        at.addRow(stackSummary.getStackId(), stackSummary.getStackName(), stackSummary.getStackStatus(), stackSummary.getCreationTime());
                        at.addRule();
                    });
                } else {
                    ConsoleLogger.log("No Stacks Found!");
                }

                token = listStacksResult.getNextToken();

                if (token == null && !listStacksResult.getStackSummaries().isEmpty()) {
                    ConsoleLogger.log(at.render());
                }
            } while (token != null);

            return null;
        });
    }

    private Collection<String> parseStatusFilter(String filterStr) {
        if (!StringUtils.isNullOrEmpty(statusFilter)) {
            return Arrays.stream(filterStr.split(","))
                    .map(String::trim)
                    .collect(Collectors.toSet());
        }

        return null;
    }

    @Internal
    @Override
    public String getGroup() {
        return CloudFormationModule.GROUP_NAME;
    }

    @Internal
    @Override
    public String getDescription() {
        return "Lists configured CloudFormation stacks.";
    }

    /**
     * Gets the status filter to apply when listing stacks.
     *
     * @return status filter
     */
    public String getStatusFilter() {
        return statusFilter;
    }

    /**
     * Sets the status filter to apply when listing stacks.
     *
     * @param statusFilter status filter
     * @see <a href="https://docs.aws.amazon.com/cli/latest/reference/cloudformation/list-stacks.html">https://docs.aws.amazon.com/cli/latest/reference/cloudformation/list-stacks.html</a>
     */
    @Option(option = "statusFilter", description = "Comma-delimited list of `StackStatus` you wish to list")
    public void setStatusFilter(String statusFilter) {
        this.statusFilter = statusFilter;
    }
}
