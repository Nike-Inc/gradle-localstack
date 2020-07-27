package com.nike.pdm.localstack.aws.sns;

import com.nike.pdm.localstack.compose.LocalStackModule;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.util.Arrays;

public class ListSnsTopicsTask extends DefaultTask {

    public ListSnsTopicsTask() {
        setMustRunAfter(Arrays.asList(LocalStackModule.START_LOCALSTACK_TASK_NAME));
    }

    @TaskAction
    public void run() {

    }

    @Override
    public String getGroup() {
        return SnsModule.GROUP_NAME;
    }

    @Override
    public String getDescription() {
        return "Lists SNS topics.";
    }
}
