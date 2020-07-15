/**
 * Copyright 2019-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.aws.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListVersionsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.S3VersionSummary;
import com.amazonaws.services.s3.model.VersionListing;
import com.amazonaws.util.StringUtils;
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
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Task that purges S3 buckets.
 */
public class PurgeS3BucketsTask extends DefaultTask {

    @Input
    private List<String> buckets;

    public PurgeS3BucketsTask() {
        setMustRunAfter(Arrays.asList(LocalStackModule.START_LOCALSTACK_TASK_NAME));
    }

    @TaskAction
    public void run() {
        Retry.execute(() -> {
            final AmazonS3 amazonS3 = AwsClientFactory.getInstance().s3(getProject());
            final S3TaskUtil s3TaskUtil = new S3TaskUtil(getProject());

            if (buckets != null) {
                buckets.forEach(bucketName -> {
                    ConsoleLogger.log("Purging S3 bucket: %s", bucketName);

                    ObjectListing objectListing = amazonS3.listObjects(bucketName);
                    long purgeCnt = 0;
                    while (true) {
                        Iterator<S3ObjectSummary> objIter = objectListing.getObjectSummaries().iterator();
                        while (objIter.hasNext()) {
                            amazonS3.deleteObject(bucketName, objIter.next().getKey());
                            purgeCnt++;
                        }

                        if (objectListing.isTruncated()) {
                            objectListing = amazonS3.listNextBatchOfObjects(objectListing);
                        } else {
                            break;
                        }
                    }

                    // Versioned S3 buckets must have their versions removed separately from the objects
                    if (s3TaskUtil.isBucketVersioned(bucketName)) {
                        ConsoleLogger.log("Bucket versioning detected. Purging versions.");

                        VersionListing versionList = amazonS3.listVersions(new ListVersionsRequest().withBucketName(bucketName));
                        while (true) {
                            Iterator<S3VersionSummary> versionIter = versionList.getVersionSummaries().iterator();
                            while (versionIter.hasNext()) {
                                S3VersionSummary vs = versionIter.next();
                                amazonS3.deleteVersion(bucketName, vs.getKey(), vs.getVersionId());
                            }

                            if (versionList.isTruncated()) {
                                versionList = amazonS3.listNextBatchOfVersions(versionList);
                            } else {
                                break;
                            }
                        }
                    }

                    ConsoleLogger.log("Purged S3 bucket: %s", bucketName);
                });
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
        return "Purges S3 buckets.";
    }

    /**
     * Gets the names of buckets to purge.
     *
     * @return bucket names
     */
    public List<String> getBuckets() {
        return buckets;
    }

    /**
     * Sets the names of buckets to purge.
     *
     * @param buckets bucket names
     */
    public void setBuckets(List<String> buckets) {
        this.buckets = buckets;
    }

    /**
     * Sets the names of buckets to purge. This method allows the bucket names to be specified as a
     * comma-delimited list of strings.
     *
     * @param buckets bucket names
     */
    @Option(option = "buckets", description = "Comma-delimited list of the names of the buckets to purge")
    public void setBuckets(String buckets) {
        if (!StringUtils.isNullOrEmpty(buckets)) {
            setBuckets(Arrays.stream(buckets.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList()));
        }
    }
}
