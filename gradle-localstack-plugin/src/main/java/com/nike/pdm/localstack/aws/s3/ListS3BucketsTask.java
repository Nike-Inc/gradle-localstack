/**
 * Copyright 2019-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.aws.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.nike.pdm.localstack.aws.AwsClientFactory;
import com.nike.pdm.localstack.compose.LocalStackModule;
import com.nike.pdm.localstack.core.ConsoleLogger;
import com.nike.pdm.localstack.core.Retry;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import java.util.Arrays;
import java.util.List;

/**
 * Task that lists all S3 buckets.
 */
public class ListS3BucketsTask extends DefaultTask {

    public ListS3BucketsTask() {
        setMustRunAfter(Arrays.asList(LocalStackModule.START_LOCALSTACK_TASK_NAME));
    }

    @TaskAction
    public void run() {
        Retry.execute(() -> {
            final AmazonS3 amazonS3 = AwsClientFactory.getInstance().s3(getProject());

            AsciiTable at = new AsciiTable();
            at.setTextAlignment(TextAlignment.JUSTIFIED_LEFT);

            at.addRule();
            at.addRow("BucketName", "CreationDate");
            at.addRule();

            final List<Bucket> buckets = amazonS3.listBuckets();
            if (buckets != null && !buckets.isEmpty()) {
                buckets.forEach(bucket -> {
                    at.addRow(bucket.getName(), bucket.getCreationDate());
                    at.addRule();
                });

                ConsoleLogger.log(at.render());
            } else {
                ConsoleLogger.log("No Buckets Found!");
            }

            return null;
        });
    }

    @Internal
    @Override
    public String getGroup() {
        return S3Module.GROUP_NAME;
    }

    @Internal
    @Override
    public String getDescription() {
        return "Lists S3 buckets.";
    }
}
