package uk.davidwei.perfmock.internal.perf.network;

import java.io.Closeable;

public class JavaTense implements Closeable {
    static {
        System.load("/usr/local/lib/libtense_java.so");
    }

    private native int init();
    private native int destroy();
    private native long time();
    private native int scale(int percent);
    private native int reset();
    private native int sleep0(long nanos);
    private native int jump0(long nanos);

    public JavaTense() {
        if (this.init() != 0)
            throw new RuntimeException("failed to init tense");
    }

    @Override
    public void close() {
        this.destroy();
    }

    public long now() {
        return this.time();
    }

    public void setScale(int percent) {
        if (this.scale(percent) != 0)
            throw new RuntimeException("failed to set scaling");
    }

    public void resetScale() {
        if (this.reset() != 0)
            throw new RuntimeException("failed to reset scaling");
    }

    public void sleep(long nanos) {
        if (this.sleep0(nanos) != 0)
            throw new RuntimeException("failed to sleep");
    }

    public void jump(long nanos) {
        if (this.jump0(nanos) != 0)
            throw new RuntimeException("failed to jump");
    }
}