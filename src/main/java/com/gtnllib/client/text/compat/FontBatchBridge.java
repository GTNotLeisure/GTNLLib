package com.gtnllib.client.text.compat;

public interface FontBatchBridge {

    int gtnl$suspendBatch();

    void gtnl$resumeBatch(int depth);

    void gtnl$flushBatch();
}
