package com.vergilyn.examples.official;

import com.lmax.disruptor.EventHandler;

/**
 * @author VergiLyn
 * @date 2019-06-27
 */
public class LongEventHandler implements EventHandler<LongEvent> {
    @Override
    public void onEvent(LongEvent event, long sequence, boolean endOfBatch) {
        System.out.println("Event: " + event);
        System.out.println();
    }
}
