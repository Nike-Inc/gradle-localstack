/**
 * Copyright 2019-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.boot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Extension properties for the Spring Boot module.
 */
public class SpringBootExtension {
    public static final String NAME = "springboot";

    // Defaults
    public static final boolean DEFAULT_ENABLED = true;
    public static final int DEFAULT_DEBUG_PORT = 5005;
    public static final List<String> DEFAULT_PROFILES = Arrays.asList("local");
    public static final List<String> DEFAULT_JVM_ARGS = new ArrayList<>();

    private boolean enabled = DEFAULT_ENABLED;
    private int debugPort = DEFAULT_DEBUG_PORT;
    private List<String> profiles = DEFAULT_PROFILES;
    private List<String> jvmArgs = DEFAULT_JVM_ARGS;

    /**
     * Gets whether or not auto-configuration of the spring boot module is enabled.
     *
     * @return <code>true</code> if auto-configuration is enabled; otherwise <code>false</code>
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether or not auto-configuration of the spring boot module is enabled.
     *
     * @param enabled <code>true</code> if enabled; otherwise <code>false</code> to disable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Gets the debug port that will be used by the `bootRunLocalDebug` task.
     *
     * @return debug port
     */
    public int getDebugPort() {
        return debugPort;
    }

    /**
     * Sets the debug port that will be used by the `bootRunLocalDebug` task.
     *
     * @param debugPort debug port
     */
    public void setDebugPort(int debugPort) {
        this.debugPort = debugPort;
    }

    /**
     * Gets the spring profiles with which to start the `bootRunLocal` and `bootRunLocalDebug` tasks.
     *
     * @return spring profiles
     */
    public List<String> getProfiles() {
        return profiles;
    }

    /**
     * Sets the spring profiles with which to start the `bootRunLocal` and `bootRunLocalDebug` tasks.
     *
     * @param profiles spring profiles
     */
    public void setProfiles(List<String> profiles) {
        this.profiles = profiles;
    }

    /**
     * Gets additional JVM arguments to supply to the `bootRunLocal` and `bootRunLocalDebug` tasks.
     *
     * @return jvm arguments
     */
    public List<String> getJvmArgs() {
        return jvmArgs;
    }

    /**
     * Sets additional JVM arguments to supply to the `bootRunLocal` and `bootRunLocalDebug` tasks.
     *
     * @param jvmArgs jvm arguments
     */
    public void setJvmArgs(List<String> jvmArgs) {
        this.jvmArgs = jvmArgs;
    }
}
