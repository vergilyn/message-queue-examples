package org.apache.kafka.common.requests;

/**
 * compatible spring-kafka v2.5
 *
 * @author vergilyn
 * @date 2020-05-18
 *
 * @see org.apache.kafka.common.IsolationLevel
 */
public enum IsolationLevel {
    READ_UNCOMMITTED((byte)0),
    READ_COMMITTED((byte)1);

    private final byte id;

    IsolationLevel(byte id) {
        this.id = id;
    }

    public byte id() {
        return this.id;
    }

    public static IsolationLevel forId(byte id) {
        switch(id) {
        case 0:
            return READ_UNCOMMITTED;
        case 1:
            return READ_COMMITTED;
        default:
            throw new IllegalArgumentException("Unknown isolation level " + id);
        }
    }
}
