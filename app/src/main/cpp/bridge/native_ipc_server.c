#include "native_ipc_server.h"
#include "native_surface_bridge.h"
#include <sys/socket.h>
#include <sys/un.h>
#include <sys/stat.h>
#include <unistd.h>
#include <pthread.h>
#include <errno.h>
#include <string.h>
#include <stdlib.h>
#include <android/log.h>

#define TAG "WindroidIpcServer"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

#define MAX_CLIENTS 4

static int g_server_fd = -1;
static int g_client_fds[MAX_CLIENTS];
static pthread_mutex_t g_clients_mutex = PTHREAD_MUTEX_INITIALIZER;
static pthread_t g_server_thread;
static volatile int g_server_running = 0;
static char g_socket_path[256] = {0};

static void* ipc_server_loop(void* arg) {
    (void)arg;
    LOGI("IPC server loop started on %s", g_socket_path);

    while (g_server_running) {
        struct sockaddr_un client_addr;
        socklen_t client_len = sizeof(client_addr);
        int client_fd = accept(g_server_fd, (struct sockaddr*)&client_addr, &client_len);
        if (client_fd < 0) {
            if (!g_server_running) break;
            if (errno == EINTR) continue;
            LOGE("accept() failed: %s", strerror(errno));
            usleep(100000);
            continue;
        }

        LOGI("New Wine IPC client connected: fd=%d", client_fd);

        pthread_mutex_lock(&g_clients_mutex);
        int slot = -1;
        for (int i = 0; i < MAX_CLIENTS; i++) {
            if (g_client_fds[i] < 0) {
                g_client_fds[i] = client_fd;
                slot = i;
                break;
            }
        }
        pthread_mutex_unlock(&g_clients_mutex);

        if (slot < 0) {
            LOGW("Max IPC clients reached, closing fd=%d", client_fd);
            close(client_fd);
        } else {
            // Envia handshake inicial de boas vindas
            WindroidIpcHeader hdr;
            hdr.magic = WINDROID_IPC_MAGIC;
            hdr.type = WINDROID_MSG_HANDSHAKE;
            hdr.payload_size = 0;
            send(client_fd, &hdr, sizeof(hdr), MSG_NOSIGNAL);

            // Se a superficie ja estiver criada e valida, envia o status atual imediatamente
            if (windroid_is_surface_valid()) {
                int w = 0, h = 0;
                windroid_get_surface_dimensions(&w, &h);
                if (w > 0 && h > 0) {
                    struct {
                        WindroidIpcHeader s_hdr;
                        WindroidSurfaceStatusPayload s_payload;
                    } smsg;
                    smsg.s_hdr.magic = WINDROID_IPC_MAGIC;
                    smsg.s_hdr.type = WINDROID_MSG_SURFACE_STATUS;
                    smsg.s_hdr.payload_size = sizeof(smsg.s_payload);
                    smsg.s_payload.is_valid = 1;
                    smsg.s_payload.width = w;
                    smsg.s_payload.height = h;
                    send(client_fd, &smsg, sizeof(smsg), MSG_NOSIGNAL);
                    LOGI("Sent initial surface status %dx%d to client fd=%d", w, h, client_fd);
                }
            }
        }
    }

    LOGI("IPC server loop terminated");
    return NULL;
}

static void broadcast_message(const void* data, size_t total_size) {
    pthread_mutex_lock(&g_clients_mutex);
    for (int i = 0; i < MAX_CLIENTS; i++) {
        int fd = g_client_fds[i];
        if (fd >= 0) {
            ssize_t sent = send(fd, data, total_size, MSG_NOSIGNAL);
            if (sent < 0 && (errno == EPIPE || errno == ECONNRESET)) {
                LOGW("Client fd=%d disconnected", fd);
                close(fd);
                g_client_fds[i] = -1;
            }
        }
    }
    pthread_mutex_unlock(&g_clients_mutex);
}

int windroid_ipc_start(const char* socket_path) {
    if (g_server_running) {
        LOGW("IPC server already running");
        return 0;
    }

    pthread_mutex_lock(&g_clients_mutex);
    for (int i = 0; i < MAX_CLIENTS; i++) {
        g_client_fds[i] = -1;
    }
    pthread_mutex_unlock(&g_clients_mutex);

    strncpy(g_socket_path, socket_path, sizeof(g_socket_path) - 1);
    unlink(g_socket_path);

    g_server_fd = socket(AF_UNIX, SOCK_STREAM, 0);
    if (g_server_fd < 0) {
        LOGE("socket() failed: %s", strerror(errno));
        return -1;
    }

    struct sockaddr_un addr;
    memset(&addr, 0, sizeof(addr));
    addr.sun_family = AF_UNIX;
    strncpy(addr.sun_path, g_socket_path, sizeof(addr.sun_path) - 1);

    if (bind(g_server_fd, (struct sockaddr*)&addr, sizeof(addr)) < 0) {
        LOGE("bind() failed for %s: %s", g_socket_path, strerror(errno));
        close(g_server_fd);
        g_server_fd = -1;
        return -1;
    }

    chmod(g_socket_path, 0777);

    if (listen(g_server_fd, 4) < 0) {
        LOGE("listen() failed: %s", strerror(errno));
        close(g_server_fd);
        g_server_fd = -1;
        return -1;
    }

    g_server_running = 1;
    if (pthread_create(&g_server_thread, NULL, ipc_server_loop, NULL) != 0) {
        LOGE("pthread_create() failed for IPC server");
        g_server_running = 0;
        close(g_server_fd);
        g_server_fd = -1;
        return -1;
    }

    LOGI("Windroid IPC server successfully started on %s", g_socket_path);
    return 0;
}

void windroid_ipc_stop(void) {
    if (!g_server_running) return;

    g_server_running = 0;
    if (g_server_fd >= 0) {
        shutdown(g_server_fd, SHUT_RDWR);
        close(g_server_fd);
        g_server_fd = -1;
    }

    pthread_join(g_server_thread, NULL);

    pthread_mutex_lock(&g_clients_mutex);
    for (int i = 0; i < MAX_CLIENTS; i++) {
        if (g_client_fds[i] >= 0) {
            close(g_client_fds[i]);
            g_client_fds[i] = -1;
        }
    }
    pthread_mutex_unlock(&g_clients_mutex);

    unlink(g_socket_path);
    LOGI("Windroid IPC server stopped");
}

void windroid_ipc_notify_surface_change(int is_valid, int width, int height) {
    struct {
        WindroidIpcHeader hdr;
        WindroidSurfaceStatusPayload payload;
    } msg;

    msg.hdr.magic = WINDROID_IPC_MAGIC;
    msg.hdr.type = WINDROID_MSG_SURFACE_STATUS;
    msg.hdr.payload_size = sizeof(msg.payload);
    msg.payload.is_valid = (uint32_t)is_valid;
    msg.payload.width = width;
    msg.payload.height = height;

    broadcast_message(&msg, sizeof(msg));
}

void windroid_ipc_send_mouse(float x, float y, int button, int is_down, int is_relative, float wheel_dx, float wheel_dy) {
    struct {
        WindroidIpcHeader hdr;
        WindroidMousePayload payload;
    } msg;

    msg.hdr.magic = WINDROID_IPC_MAGIC;
    msg.hdr.type = WINDROID_MSG_INPUT_MOUSE;
    msg.hdr.payload_size = sizeof(msg.payload);
    msg.payload.x = x;
    msg.payload.y = y;
    msg.payload.button = button;
    msg.payload.is_down = (uint8_t)is_down;
    msg.payload.is_relative = (uint8_t)is_relative;
    msg.payload.wheel_delta_x = wheel_dx;
    msg.payload.wheel_delta_y = wheel_dy;

    broadcast_message(&msg, sizeof(msg));
}

void windroid_ipc_send_keyboard(int scan_code, int key_code, int is_down) {
    struct {
        WindroidIpcHeader hdr;
        WindroidKeyboardPayload payload;
    } msg;

    msg.hdr.magic = WINDROID_IPC_MAGIC;
    msg.hdr.type = WINDROID_MSG_INPUT_KEYBOARD;
    msg.hdr.payload_size = sizeof(msg.payload);
    msg.payload.scan_code = scan_code;
    msg.payload.key_code = key_code;
    msg.payload.is_down = (uint8_t)is_down;

    broadcast_message(&msg, sizeof(msg));
}

void windroid_ipc_send_touch(int action, int pointer_id, float x, float y) {
    struct {
        WindroidIpcHeader hdr;
        WindroidTouchPayload payload;
    } msg;

    msg.hdr.magic = WINDROID_IPC_MAGIC;
    msg.hdr.type = WINDROID_MSG_INPUT_TOUCH;
    msg.hdr.payload_size = sizeof(msg.payload);
    msg.payload.action = action;
    msg.payload.pointer_id = pointer_id;
    msg.payload.x = x;
    msg.payload.y = y;

    broadcast_message(&msg, sizeof(msg));
}

// JNI Bindings
JNIEXPORT jboolean JNICALL Java_com_windroid_emu_views_GameSurfaceView_nativeStartIpcServer(
    JNIEnv* env, jclass clazz, jstring socket_path) {
    (void)clazz;
    const char* path_str = (*env)->GetStringUTFChars(env, socket_path, NULL);
    int res = windroid_ipc_start(path_str);
    (*env)->ReleaseStringUTFChars(env, socket_path, path_str);
    return (jboolean)(res == 0);
}

JNIEXPORT void JNICALL Java_com_windroid_emu_views_GameSurfaceView_nativeStopIpcServer(
    JNIEnv* env, jclass clazz) {
    (void)env;
    (void)clazz;
    windroid_ipc_stop();
}

JNIEXPORT void JNICALL Java_com_windroid_emu_views_GameSurfaceView_nativeSendMouseEvent(
    JNIEnv* env, jobject thiz, jfloat x, jfloat y, jint button, jboolean down, jboolean relative) {
    (void)env;
    (void)thiz;
    windroid_ipc_send_mouse(x, y, button, down ? 1 : 0, relative ? 1 : 0, 0.0f, 0.0f);
}

JNIEXPORT void JNICALL Java_com_windroid_emu_views_GameSurfaceView_nativeSendMouseWheel(
    JNIEnv* env, jobject thiz, jfloat delta_x, jfloat delta_y) {
    (void)env;
    (void)thiz;
    windroid_ipc_send_mouse(0.0f, 0.0f, 0, 0, 0, delta_x, delta_y);
}

JNIEXPORT void JNICALL Java_com_windroid_emu_views_GameSurfaceView_nativeSendKeyEvent(
    JNIEnv* env, jobject thiz, jint scan_code, jint key_code, jboolean down) {
    (void)env;
    (void)thiz;
    windroid_ipc_send_keyboard(scan_code, key_code, down ? 1 : 0);
}

JNIEXPORT void JNICALL Java_com_windroid_emu_views_GameSurfaceView_nativeSendTouchEvent(
    JNIEnv* env, jobject thiz, jint action, jint pointer_id, jfloat x, jfloat y) {
    (void)env;
    (void)thiz;
    windroid_ipc_send_touch(action, pointer_id, x, y);
}
