#include "native_surface_bridge.h"
#include "native_ipc_server.h"
#include <android/log.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <errno.h>

#define TAG "WindroidSurfaceBridge"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

static pthread_mutex_t g_surface_mutex = PTHREAD_MUTEX_INITIALIZER;
static ANativeWindow* g_native_window = NULL;
static int g_surface_width = 0;
static int g_surface_height = 0;
static int g_surface_valid = 0;

static int g_shm_fd = -1;
static void* g_shm_buffer = NULL;
static size_t g_shm_size = 0;
static char g_shm_path[256] = {0};

static float g_cursor_x = 100.0f;
static float g_cursor_y = 100.0f;
static int g_cursor_visible = 1;

static const char* s_cursor_arrow[16] = {
    "X               ",
    "XX              ",
    "X.X             ",
    "X..X            ",
    "X...X           ",
    "X....X          ",
    "X.....X         ",
    "X......X        ",
    "X.......X       ",
    "X........X      ",
    "X.....XXXXX     ",
    "X..X..X         ",
    "X.X X..X        ",
    "XX   X..X       ",
    "X     X..X      ",
    "       XX       "
};

int windroid_shm_init(const char* socket_path) {
    pthread_mutex_lock(&g_surface_mutex);
    if (g_shm_buffer) {
        pthread_mutex_unlock(&g_surface_mutex);
        return 0;
    }

    const char* env_shm = getenv("WINDROID_SHM_PATH");
    if (env_shm && *env_shm) {
        strncpy(g_shm_path, env_shm, sizeof(g_shm_path) - 1);
    } else if (socket_path && *socket_path) {
        strncpy(g_shm_path, socket_path, sizeof(g_shm_path) - 1);
        char* ext = strstr(g_shm_path, ".sock");
        if (ext) {
            strcpy(ext, ".shm");
        } else {
            strncat(g_shm_path, ".shm", sizeof(g_shm_path) - strlen(g_shm_path) - 1);
        }
    } else {
        strcpy(g_shm_path, "/data/data/com.windroid.emu/files/ipc.shm");
    }

    size_t init_size = 2560 * 1440 * 4;
    g_shm_fd = open(g_shm_path, O_RDWR | O_CREAT, 0777);
    if (g_shm_fd < 0) {
        LOGE("Failed to open SHM file %s: %s", g_shm_path, strerror(errno));
        pthread_mutex_unlock(&g_surface_mutex);
        return -1;
    }

    chmod(g_shm_path, 0777);
    if (ftruncate(g_shm_fd, init_size) != 0) {
        LOGW("ftruncate warning for SHM: %s", strerror(errno));
    }

    g_shm_size = init_size;
    g_shm_buffer = mmap(NULL, g_shm_size, PROT_READ | PROT_WRITE, MAP_SHARED, g_shm_fd, 0);
    if (g_shm_buffer == MAP_FAILED) {
        LOGE("mmap failed for SHM: %s", strerror(errno));
        g_shm_buffer = NULL;
        close(g_shm_fd);
        g_shm_fd = -1;
        pthread_mutex_unlock(&g_surface_mutex);
        return -1;
    }

    memset(g_shm_buffer, 0, g_shm_size);
    LOGI("SHM framebuffer successfully initialized: %s (%zu bytes) at %p", g_shm_path, g_shm_size, g_shm_buffer);
    pthread_mutex_unlock(&g_surface_mutex);
    return 0;
}

void windroid_shm_cleanup(void) {
    pthread_mutex_lock(&g_surface_mutex);
    if (g_shm_buffer && g_shm_buffer != MAP_FAILED) {
        munmap(g_shm_buffer, g_shm_size);
        g_shm_buffer = NULL;
    }
    if (g_shm_fd >= 0) {
        close(g_shm_fd);
        g_shm_fd = -1;
    }
    if (g_shm_path[0]) {
        unlink(g_shm_path);
        g_shm_path[0] = '\0';
    }
    pthread_mutex_unlock(&g_surface_mutex);
    LOGI("SHM framebuffer cleaned up");
}

void windroid_update_cursor_position(float x, float y, int is_relative) {
    pthread_mutex_lock(&g_surface_mutex);
    if (is_relative) {
        g_cursor_x += x;
        g_cursor_y += y;
    } else {
        g_cursor_x = x;
        g_cursor_y = y;
    }
    if (g_surface_width > 0 && g_cursor_x >= g_surface_width) g_cursor_x = g_surface_width - 1;
    if (g_cursor_x < 0) g_cursor_x = 0;
    if (g_surface_height > 0 && g_cursor_y >= g_surface_height) g_cursor_y = g_surface_height - 1;
    if (g_cursor_y < 0) g_cursor_y = 0;
    g_cursor_visible = 1;
    pthread_mutex_unlock(&g_surface_mutex);
}

void windroid_present_frame(int left, int top, int right, int bottom) {
    pthread_mutex_lock(&g_surface_mutex);
    if (!g_native_window || !g_surface_valid || !g_shm_buffer) {
        pthread_mutex_unlock(&g_surface_mutex);
        return;
    }

    int w = g_surface_width;
    int h = g_surface_height;
    if (w <= 0 || h <= 0) {
        pthread_mutex_unlock(&g_surface_mutex);
        return;
    }

    ARect bounds;
    bounds.left = (left < 0) ? 0 : left;
    bounds.top = (top < 0) ? 0 : top;
    bounds.right = (right > w || right <= bounds.left) ? w : right;
    bounds.bottom = (bottom > h || bottom <= bounds.top) ? h : bottom;

    ANativeWindow_Buffer win_buf;
    if (ANativeWindow_lock(g_native_window, &win_buf, &bounds) == 0) {
        int copy_w = bounds.right - bounds.left;
        int copy_h = bounds.bottom - bounds.top;
        if (copy_w > 0 && copy_h > 0) {
            for (int y = bounds.top; y < bounds.bottom; y++) {
                const uint32_t* src = (const uint32_t*)g_shm_buffer + y * w + bounds.left;
                uint32_t* dst = (uint32_t*)win_buf.bits + y * win_buf.stride + bounds.left;
                memcpy(dst, src, copy_w * sizeof(uint32_t));
            }
        }

        if (g_cursor_visible) {
            for (int cy = 0; cy < 16; cy++) {
                int py = (int)g_cursor_y + cy;
                if (py < 0 || py >= h) continue;
                for (int cx = 0; cx < 16; cx++) {
                    int px = (int)g_cursor_x + cx;
                    if (px < 0 || px >= w) continue;
                    char c = s_cursor_arrow[cy][cx];
                    if (c == 'X') {
                        ((uint32_t*)win_buf.bits)[py * win_buf.stride + px] = 0xFF000000;
                    } else if (c == '.') {
                        ((uint32_t*)win_buf.bits)[py * win_buf.stride + px] = 0xFFFFFFFF;
                    }
                }
            }
        }

        ANativeWindow_unlockAndPost(g_native_window);
    }
    pthread_mutex_unlock(&g_surface_mutex);
}

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
