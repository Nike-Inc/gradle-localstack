/**
 * Copyright 2019-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.boot;

import org.gradle.api.GradleException;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaApplication;
import org.springframework.boot.loader.tools.MainClassFinder;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Callable that returns the mainClassName for the configured Spring Boot plugin.
 */
final class MainClassConvention implements Callable<Object> {
    private static final String SPRING_BOOT_APPLICATION_CLASS_NAME = "org.springframework.boot.autoconfigure.SpringBootApplication";

    private final Project project;
    private final Supplier<FileCollection> classpathSupplier;

    MainClassConvention(Project project, Supplier<FileCollection> classpathSupplier) {
        this.project = project;
        this.classpathSupplier = classpathSupplier;
    }

    @Override
    public Object call() throws Exception {
        org.springframework.boot.gradle.dsl.SpringBootExtension springBootExtension = project.getExtensions().findByType(org.springframework.boot.gradle.dsl.SpringBootExtension.class);
        if (springBootExtension != null && springBootExtension.getMainClassName() != null) {
            return springBootExtension.getMainClassName();
        }

        JavaApplication javaApplication = project.getConvention().findByType(JavaApplication.class);
        if (javaApplication != null) {
            if (javaApplication.getMainClassName() != null && !javaApplication.getMainClassName().isEmpty()) {
                return javaApplication.getMainClassName();
            }
        }

        return classpathSupplier.get()
                .filter(File::isDirectory).getFiles().stream()
                .map(file -> {
                    try {
                        return MainClassFinder.findSingleMainClass(file, SPRING_BOOT_APPLICATION_CLASS_NAME);
                    } catch (IOException ignored) {
                        return null;
                    }
                })
                .filter(Objects::nonNull).findFirst()
                .orElseThrow(() -> new GradleException("Cannot resolve main classname!"));
    }
}
