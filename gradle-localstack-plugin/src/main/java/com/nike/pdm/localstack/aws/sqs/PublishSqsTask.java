/**
 * Copyright 2019-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.aws.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.util.StringUtils;
import com.nike.pdm.localstack.aws.AwsClientFactory;
import com.nike.pdm.localstack.compose.LocalStackModule;
import com.nike.pdm.localstack.core.ConsoleLogger;
import com.nike.pdm.localstack.core.Retry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Task that publishes messages to SQS queues.
 */
public class PublishSqsTask extends DefaultTask {

    @Input
    private List<String> queueNames;

    @InputFiles
    private List<File> messages;

    public PublishSqsTask() {
        setMustRunAfter(Arrays.asList(LocalStackModule.START_LOCALSTACK_TASK_NAME));
    }

    @TaskAction
    public void run() {
        Retry.execute(() -> {
            final AmazonSQS amazonSQS = AwsClientFactory.getInstance().sqs(getProject());
            final SqsTaskUtil sqsTaskUtil = new SqsTaskUtil(getProject());

            if (queueNames != null && !queueNames.isEmpty()) {
                final List<String> queueUrls = sqsTaskUtil.getQueueUrls(queueNames);

                messages.forEach(messageFile -> {
                    try {
                        if (isSupportedArchive(messageFile)) {
                            processArchive(amazonSQS, queueUrls, messageFile);
                        } else {
                            processFile(amazonSQS, queueUrls, messageFile);
                        }
                    } catch (Exception e) {
                        ConsoleLogger.log("Error occurred while publishing messages: %s", e.getMessage());
                    }
                });
            } else {
                ConsoleLogger.log("No queues configured!");
            }

            return null;
        });
    }

    /**
     * Processes messages found in a tar.gz archive.
     *
     * @param sqs sqs client
     * @param queueUrls urls of the queues to which to publish messages
     * @param messageArchive archive file to process
     * @throws Exception
     */
    private void processArchive(AmazonSQS sqs, List<String> queueUrls, File messageArchive) throws Exception {
        ConsoleLogger.log("Processing message archive: %s", messageArchive.getAbsolutePath());

        TarArchiveInputStream tarInput = new TarArchiveInputStream(
                new GzipCompressorInputStream(
                        new BufferedInputStream(
                                new FileInputStream(messageArchive))));

        TarArchiveEntry te;
        while ((te = tarInput.getNextTarEntry ()) != null) {
            // Only process files that are not hidden in the archive
            if (!te.getName().startsWith(".")) {
                // Closing any one stream within the archive closes all streams so we prevent that from happening
                // by wrapping the input stream with this stream
                final CloseShieldInputStream closeShieldInputStream = new CloseShieldInputStream(tarInput);

                try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(closeShieldInputStream))) {
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        if (!line.isEmpty()) {
                            for (String queueUrl : queueUrls) {
                                sqs.sendMessage(queueUrl, line);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Processes messages found in a single file.
     *
     * @param sqs sqs client
     * @param queueUrls urls of the queues to which to publish messages
     * @param messageFile file to process
     * @throws Exception
     */
    private void processFile(AmazonSQS sqs, List<String> queueUrls, File messageFile) throws Exception {
        ConsoleLogger.log("Processing message file: %s", messageFile.getAbsolutePath());

        try (BufferedReader bufferedReader = Files.newBufferedReader(messageFile.toPath(), StandardCharsets.UTF_8)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (!line.isEmpty()) {
                    for (String queueUrl : queueUrls) {
                        sqs.sendMessage(queueUrl, line);
                    }
                }
            }
        }
    }

    /**
     * Checks whether or not the archive file is supported by this plugin.
     *
     * @param file file to check
     * @return <code>true</code> if the file type is supported; otherwise <code>false</code>
     */
    private boolean isSupportedArchive(File file) {
        return file.getName().endsWith("tar.gz") || file.getName().endsWith("tgz");
    }

    @Internal
    @Override
    public String getGroup() {
        return SqsModule.GROUP_NAME;
    }

    @Internal
    @Override
    public String getDescription() {
        return "Publishes messages to SQS queues.";
    }

    /**
     * Gets the names of the queues to which to publish messages.
     *
     * @return queue names
     */
    public List<String> getQueueNames() {
        return queueNames;
    }

    /**
     * Sets the names of the queues to which to publish messages.
     *
     * @param queueNames queue names
     */
    public void setQueueNames(List<String> queueNames) {
        this.queueNames = queueNames;
    }

    /**
     * Sets the names of the queues to which to publish messages. This method supports providing the queue names as
     * a comma-delimited list of names.
     *
     * @param queues queue names
     */
    @Option(option = "queueNames", description = "Comma-delimited list of the names of the queues to which to publish")
    public void setQueueNames(String queues) {
        if (!StringUtils.isNullOrEmpty(queues)) {
            setQueueNames(Arrays.stream(queues.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList()));
        }
    }

    /**
     * Gets the message files to publish. Message files may be a single file, a directory containing multiple files, or
     * a tar.gz archive of multiple files.
     *
     * @return messages files
     */
    public List<File> getMessages() {
        return messages;
    }

    /**
     * Sets the message files to publish. Message files may be a single file, a directory containing multiple files, or
     * a tar.gz archive of multiple files.
     *
     * @param messages files
     */
    public void setMessages(List<File> messages) {
        this.messages = messages;
    }

    /**
     * Sets the message files to publish. Message files may be a single file, a directory containing multiple files, or
     * a tar.gz archive of multiple files.
     *
     * @param path message file path
     */
    @Option(option = "message", description = "File path to the message file, message directory, or message archive")
    public void setMessage(String path) {
        setMessages(Arrays.asList(Paths.get(path).toFile()));
    }
}
