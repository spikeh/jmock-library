package uk.davidwei.perfmock.internal.perf.network;

public class JavaTense {
    static {
        System.load("/usr/local/lib/libtense_java.so");
    }

    public static native int init();
    public static native int destroy();
    public static native long time();
    public static native int scale(int percent);
    public static native int reset();
    public static native int sleep0(long nanos);
    public static native int jump0(long nanos);
}