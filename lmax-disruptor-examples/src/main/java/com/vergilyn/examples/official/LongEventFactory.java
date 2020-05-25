package com.vergilyn.examples.official;

import com.lmax.disruptor.EventFactory;

/**
 * @author VergiLyn
 * @date 2019-06-27
 */
public class LongEventFactory implements EventFactory<LongEvent> {
    @Override
    public LongEvent newInstance() {
        return new LongEvent();
    }
}