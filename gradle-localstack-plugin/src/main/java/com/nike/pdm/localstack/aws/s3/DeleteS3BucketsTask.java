/**
 * Copyright 2020-present, Nike, Inc.
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
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Task that deletes S3 buckets.
 */
public class DeleteS3BucketsTask extends DefaultTask {

    @Input
    private List<String> buckets;

    @Optional
    @Input
    private Boolean force = false;

    public DeleteS3BucketsTask() {
        setMustRunAfter(Arrays.asList(LocalStackModule.START_LOCALSTACK_TASK_NAME));
    }

    @TaskAction
    public void run() {
        final Set<Class<? extends Throwable>> expectedErrors = new HashSet<>(Arrays.asList(GradleException.class));

        Retry.execute(() -> {
            final AmazonS3 amazonS3 = AwsClientFactory.getInstance().s3(getProject());
            final S3TaskUtil s3TaskUtil = new S3TaskUtil(getProject());

            if (buckets != null) {
                buckets.forEach(bucketName -> {
                    ConsoleLogger.log("Deleting S3 bucket: %s", bucketName);

                    ObjectListing objectListing = amazonS3.listObjects(bucketName);
                    while (true) {
                        Iterator<S3ObjectSummary> objIter = objectListing.getObjectSummaries().iterator();
                        while (objIter.hasNext()) {
                            // Only delete buckets that contain objects/versions if the force parameter is true
                            if (!force) {
                                throw new GradleException("Bucket is not empty and the 'force' parameter is set to 'false': " + bucketName);
                            }

                            amazonS3.deleteObject(bucketName, objIter.next().getKey());
                        }

                        if (objectListing.isTruncated()) {
                            objectListing = amazonS3.listNextBatchOfObjects(objectListing);
                        } else {
                            break;
                        }
                    }

                    // Versioned S3 buckets must have their versions removed separately from the objects
                    if (s3TaskUtil.isBucketVersioned(bucketName)) {
                        VersionListing versionList = amazonS3.listVersions(new ListVersionsRequest().withBucketName(bucketName));
                        while (true) {
                            Iterator<S3VersionSummary> versionIter = versionList.getVersionSummaries().iterator();
                            while (versionIter.hasNext()) {
                                // Only delete buckets that contain objects/versions if the force parameter is true
                                if (!force) {
                                    throw new GradleException("Bucket is not empty and the 'force' parameter is set to 'false': " + bucketName);
                                }

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

                    // Once the bucket has been purged it can be deleted
                    amazonS3.deleteBucket(bucketName);

                    ConsoleLogger.log("Deleted bucket: %s", bucketName);
                });
            }

           return null;
        }, expectedErrors);
    }

    @Internal
    @Override
    public String getGroup() {
        return S3Module.GROUP_NAME;
    }

    @Internal
    @Override
    public String getDescription() {
        return "Deletes S3 buckets.";
    }

    /**
     * Gets the names of the buckets to delete.
     *
     * @return bucket names
     */
    public List<String> getBuckets() {
        return buckets;
    }

    /**
     * Sets the names of the buckets to delete.
     *
     * @param buckets bucket names
     */
    public void setBuckets(List<String> buckets) {
        this.buckets = buckets;
    }

    /**
     * Sets the names of the buckets to delete. This method allows the bucket names to be specified as a
     * comma-delimited list of strings.
     *
     * @param buckets bucket names.
     */
    @Option(option = "buckets", description = "Comma-delimited list of the names of the buckets to delete")
    public void setBuckets(String buckets) {
        if (!StringUtils.isNullOrEmpty(buckets)) {
            setBuckets(Arrays.stream(buckets.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList()));
        }
    }

    /**
     * Gets whether or not buckets containing objects should be deleted.
     *
     * @return <code>true</code> if non-empty buckets should be deleted; otherwise <code>false</code>
     */
    public Boolean getForce() {
        return force;
    }

    /**
     * Sets whether or not buckets containing objects should be deleted.
     *
     * @param force <code>true</code> if non-empty buckets should be deleted; <code>false</code> if an error should occur.
     */
    @Option(option = "force", description = "Force the deletion of buckets that contain objects")
    public void setForce(Boolean force) {
        this.force = force;
    }
}
