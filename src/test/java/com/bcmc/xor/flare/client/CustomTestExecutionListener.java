package com.bcmc.xor.flare.client;

import com.bcmc.xor.flare.client.api.config.EmbedMongoDownload;
import org.springframework.core.Ordered;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

public class CustomTestExecutionListener implements TestExecutionListener, Ordered {

    @Override
    public int getOrder() {
        return 10000;
    }

    public void beforeTestClass(TestContext testContext) throws Exception {
        EmbedMongoDownload.downloadDist();
    };
}
