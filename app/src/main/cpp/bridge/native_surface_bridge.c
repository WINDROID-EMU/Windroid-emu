#include "native_surface_bridge.h"
#include "native_ipc_server.h"
#include <android/log.h>
#include <stdlib.h>

#define TAG "WindroidSurfaceBridge"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

static pthread_mutex_t g_surface_mutex = PTHREAD_MUTEX_INITIALIZER;
static ANativeWindow* g_native_window = NULL;
static int g_surface_width = 0;
static int g_surface_height = 0;
static int g_surface_valid = 0;

ANativeWindow* windroid_get_native_window(void) {
    pthread_mutex_lock(&g_surface_mutex);
    ANativeWindow* win = g_native_window;
    if (win) {
        ANativeWindow_acquire(win);
    }
    pthread_mutex_unlock(&g_surface_mutex);
    return win;
}

int windroid_is_surface_valid(void) {
    pthread_mutex_lock(&g_surface_mutex);
    int valid = g_surface_valid && (g_native_window != NULL);
    pthread_mutex_unlock(&g_surface_mutex);
    return valid;
}

void windroid_get_surface_dimensions(int* width, int* height) {
    pthread_mutex_lock(&g_surface_mutex);
    if (width) *width = g_surface_width;
    if (height) *height = g_surface_height;
    pthread_mutex_unlock(&g_surface_mutex);
}

int windroid_set_buffers_geometry(int width, int height, int format) {
    pthread_mutex_lock(&g_surface_mutex);
    int result = -1;
    if (g_native_window) {
        result = ANativeWindow_setBuffersGeometry(g_native_window, width, height, format);
        if (result == 0) {
            g_surface_width = width;
            g_surface_height = height;
            LOGI("ANativeWindow buffers geometry set to %dx%d (format %d)", width, height, format);
        } else {
            LOGE("Failed to set ANativeWindow buffers geometry: %d", result);
        }
    }
    pthread_mutex_unlock(&g_surface_mutex);
    return result;
}

JNIEXPORT void JNICALL Java_com_windroid_emu_views_GameSurfaceView_nativeSurfaceCreated(
    JNIEnv* env, jobject thiz, jobject surface) {
    (void)thiz;
    pthread_mutex_lock(&g_surface_mutex);
    if (g_native_window) {
        ANativeWindow_release(g_native_window);
        g_native_window = NULL;
    }
    g_native_window = ANativeWindow_fromSurface(env, surface);
    if (g_native_window) {
        g_surface_valid = 1;
        g_surface_width = ANativeWindow_getWidth(g_native_window);
        g_surface_height = ANativeWindow_getHeight(g_native_window);
        LOGI("Surface created successfully: ANativeWindow=%p (%dx%d)",
             g_native_window, g_surface_width, g_surface_height);
    } else {
        g_surface_valid = 0;
        LOGE("Failed to obtain ANativeWindow from Surface object");
    }
    pthread_mutex_unlock(&g_surface_mutex);

    // Notifica o servidor IPC para informar o driver do Wine
    windroid_ipc_notify_surface_change(g_surface_valid, g_surface_width, g_surface_height);
}

JNIEXPORT void JNICALL Java_com_windroid_emu_views_GameSurfaceView_nativeSurfaceChanged(
    JNIEnv* env, jobject thiz, jobject surface, jint width, jint height) {
    (void)env;
    (void)thiz;
    (void)surface;
    pthread_mutex_lock(&g_surface_mutex);
    g_surface_width = width;
    g_surface_height = height;
    if (g_native_window) {
        // Formato padrão RGBA_8888 (WINDOW_FORMAT_RGBA_8888 = 1)
        ANativeWindow_setBuffersGeometry(g_native_window, width, height, 1);
        g_surface_valid = 1;
        LOGI("Surface changed: %dx%d", width, height);
    }
    pthread_mutex_unlock(&g_surface_mutex);

    windroid_ipc_notify_surface_change(1, width, height);
}

JNIEXPORT void JNICALL Java_com_windroid_emu_views_GameSurfaceView_nativeSurfaceDestroyed(
    JNIEnv* env, jobject thiz, jobject surface) {
    (void)env;
    (void)thiz;
    (void)surface;
    pthread_mutex_lock(&g_surface_mutex);
    LOGW("Surface destroyed: releasing ANativeWindow=%p", g_native_window);
    if (g_native_window) {
        ANativeWindow_release(g_native_window);
        g_native_window = NULL;
    }
    g_surface_valid = 0;
    pthread_mutex_unlock(&g_surface_mutex);

    windroid_ipc_notify_surface_change(0, 0, 0);
}

JNIEXPORT jboolean JNICALL Java_com_windroid_emu_views_GameSurfaceView_nativeIsSurfaceAvailable(
    JNIEnv* env, jclass clazz) {
    (void)env;
    (void)clazz;
    return (jboolean)windroid_is_surface_valid();
}
