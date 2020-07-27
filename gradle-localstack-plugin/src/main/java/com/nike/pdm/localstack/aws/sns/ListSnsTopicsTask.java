package com.nike.pdm.localstack.aws.sns;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.ListTopicsResult;
import com.amazonaws.services.sns.model.Topic;
import com.nike.pdm.localstack.aws.AwsClientFactory;
import com.nike.pdm.localstack.compose.LocalStackModule;
import com.nike.pdm.localstack.core.ConsoleLogger;
import com.nike.pdm.localstack.core.Retry;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.util.Arrays;

/**
 * Task that lists SNS topics.
 */
public class ListSnsTopicsTask extends DefaultTask {

    public ListSnsTopicsTask() {
        setMustRunAfter(Arrays.asList(LocalStackModule.START_LOCALSTACK_TASK_NAME));
    }

    @TaskAction
    public void run() {
        Retry.execute(() -> {
            final AmazonSNS amazonSNS = AwsClientFactory.getInstance().sns(getProject());
            final SnsTaskUtil snsTaskUtil = new SnsTaskUtil(getProject());

            AsciiTable at = new AsciiTable();
            at.setTextAlignment(TextAlignment.JUSTIFIED_LEFT);
            at.getContext().setWidth(150);

            at.addRule();
            at.addRow("TopicName", "TopicArn");
            at.addRule();

            String nextToken = null;
            int topicCnt = 0;
            do {
                final ListTopicsResult listTopicsResult = amazonSNS.listTopics(nextToken);

                for (Topic topic : listTopicsResult.getTopics()) {
                    String topicName = snsTaskUtil.getTopicNameFromArn(topic.getTopicArn());

                    at.addRow(topicName, topic.getTopicArn());
                    at.addRule();

                    topicCnt++;
                }

                nextToken = listTopicsResult.getNextToken();
            } while (nextToken != null);

            if (topicCnt > 0) {
                ConsoleLogger.log(at.render());
            } else {
                ConsoleLogger.log("No Topics Found!");
            }

            return null;
        });
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
