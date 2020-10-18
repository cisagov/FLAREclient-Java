package com.bcmc.xor.flare.client;

import org.springframework.core.Ordered;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

public class CustomTestExecutionListener implements TestExecutionListener, Ordered {

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }

    public void beforeTestClass(TestContext testContext) throws Exception {
        EmbedMongoDownload.downloadDist();
    };
}
