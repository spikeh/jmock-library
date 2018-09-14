#define _GNU_SOURCE

#include <fcntl.h>
#include <stdint.h>
#include <stdio.h>
#include <sys/types.h>
#include <unistd.h>

#include "uk_davidwei_perfmock_internal_perf_network_JavaTense.h"

#define TENSE_FILE "/sys/kernel/debug/tense"
#define FASTER 0
#define SLOWER 1
#define ONE_BILLION 1000000000L
#define NS_IN_MS 1000000

static __thread int tense_fd;
static __thread uint32_t tense[2];

JNIEXPORT jint JNICALL Java_uk_davidwei_perfmock_internal_perf_network_JavaTense_init
  (JNIEnv *env, jclass thisObj)
{
    tense[FASTER] = 1;
    tense[SLOWER] = 1;

    tense_fd = open(TENSE_FILE, O_RDWR);
    if (tense_fd < 0)
        goto bad_tense_open;

    if (write(tense_fd, (const void *)tense, 2 * sizeof(uint32_t)) < 0)
        goto bad_tense_write;

    goto success;

bad_tense_write:
    close(tense_fd);

bad_tense_open:
    return -1;

success:
    return 0;
}

JNIEXPORT jint JNICALL Java_uk_davidwei_perfmock_internal_perf_network_JavaTense_destroy
  (JNIEnv *env, jclass thisObj)
{
    return close(tense_fd);
}

JNIEXPORT jlong JNICALL Java_uk_davidwei_perfmock_internal_perf_network_JavaTense_time
  (JNIEnv *env, jclass thisObj)
{
    struct timespec time;
    read(tense_fd, &time, 0);
    long long time_ns = time.tv_sec * ONE_BILLION + time.tv_nsec;
    return time_ns;
}

JNIEXPORT jint JNICALL Java_uk_davidwei_perfmock_internal_perf_network_JavaTense_scale
  (JNIEnv *env, jclass thisObj, jint percent)
{
    tense[FASTER] *= percent;
    tense[SLOWER] *= 100;
    return write(tense_fd, (const void *)tense, 2 * sizeof(uint32_t));
}

JNIEXPORT jint JNICALL Java_uk_davidwei_perfmock_internal_perf_network_JavaTense_reset
  (JNIEnv *env, jclass thisObj)
{
    tense[FASTER] = 1;
    tense[SLOWER] = 1;
    return write(tense_fd, (const void *)tense, 2 * sizeof(uint32_t));
}

JNIEXPORT jint JNICALL Java_uk_davidwei_perfmock_internal_perf_network_JavaTense_sleep0
  (JNIEnv *env, jclass thisObj, jlong nanos)
{
    off64_t ret = lseek64(tense_fd, (off64_t)nanos, SEEK_HOLE);
    return ret < 0 ? ret : 0;
}

JNIEXPORT jint JNICALL Java_uk_davidwei_perfmock_internal_perf_network_JavaTense_jump0
  (JNIEnv *env, jclass thisObj, jlong nanos)
{
    off64_t ret = lseek64(tense_fd, (off64_t)nanos, SEEK_CUR);
    return ret < 0 ? ret : 0;
}
