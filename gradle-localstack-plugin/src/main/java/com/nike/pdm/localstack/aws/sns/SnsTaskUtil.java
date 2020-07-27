/**
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.aws.sns;

import com.amazonaws.util.StringUtils;
import com.nike.pdm.localstack.compose.LocalStackExtension;
import org.gradle.api.Project;

/**
 * Utility methods shared amongst the SNS tasks.
 */
public class SnsTaskUtil {

    private final Project project;

    public SnsTaskUtil(Project project) {
        this.project = project;
    }

    /**
     * Gets the ARN of an SNS topic based on its name.
     *
     * @param topicName topic name
     * @return topic arn
     */
    public String getTopicArn(String topicName) {
        LocalStackExtension ext = project.getExtensions().getByType(LocalStackExtension.class);

        return String.format("arn:aws:sns:%s:000000000000:%s", ext.getSigningRegion(), topicName);
    }

    /**
     * Gets the topic name from the topic ARN.
     *
     * @param topicArn topic arn
     * @return topic name
     */
    public String getTopicNameFromArn(String topicArn) {
        if (!StringUtils.isNullOrEmpty(topicArn)) {
            return topicArn.substring(topicArn.lastIndexOf(":") + 1);
        }

        return null;
    }
}
