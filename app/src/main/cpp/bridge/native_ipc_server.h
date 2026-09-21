#ifndef WINDROID_NATIVE_IPC_SERVER_H
#define WINDROID_NATIVE_IPC_SERVER_H

#include <stdint.h>
#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

#define WINDROID_IPC_MAGIC 0x574E4452 // "WNDR"

enum WindroidMessageType {
    WINDROID_MSG_HANDSHAKE = 1,
    WINDROID_MSG_SURFACE_STATUS = 2,
    WINDROID_MSG_INPUT_MOUSE = 3,
    WINDROID_MSG_INPUT_KEYBOARD = 4,
    WINDROID_MSG_INPUT_TOUCH = 5,
    WINDROID_MSG_SET_RESOLUTION = 6,
    WINDROID_MSG_CLIPBOARD = 7
};

#pragma pack(push, 1)

typedef struct {
    uint32_t magic;
    uint32_t type;
    uint32_t payload_size;
} WindroidIpcHeader;

typedef struct {
    uint32_t is_valid;
    int32_t width;
    int32_t height;
} WindroidSurfaceStatusPayload;

typedef struct {
    float x;
    float y;
    int32_t button;
    uint8_t is_down;
    uint8_t is_relative;
    float wheel_delta_x;
    float wheel_delta_y;
} WindroidMousePayload;

typedef struct {
    int32_t scan_code;
    int32_t key_code;
    uint8_t is_down;
} WindroidKeyboardPayload;

typedef struct {
    int32_t action;
    int32_t pointer_id;
    float x;
    float y;
} WindroidTouchPayload;

typedef struct {
    int32_t width;
    int32_t height;
} WindroidResolutionPayload;

#pragma pack(pop)

// Inicia o servidor IPC no caminho especificado (ex: socket_path)
int windroid_ipc_start(const char* socket_path);

// Encerra o servidor IPC
void windroid_ipc_stop(void);

// Notifica os clientes conectados sobre alteração na superfície
void windroid_ipc_notify_surface_change(int is_valid, int width, int height);

// Envia evento de mouse
void windroid_ipc_send_mouse(float x, float y, int button, int is_down, int is_relative, float wheel_dx, float wheel_dy);

// Envia evento de teclado
void windroid_ipc_send_keyboard(int scan_code, int key_code, int is_down);

// Envia evento de toque
void windroid_ipc_send_touch(int action, int pointer_id, float x, float y);

// JNI bindings
JNIEXPORT jboolean JNICALL Java_com_windroid_emu_views_GameSurfaceView_nativeStartIpcServer(
    JNIEnv* env, jclass clazz, jstring socket_path);

JNIEXPORT void JNICALL Java_com_windroid_emu_views_GameSurfaceView_nativeStopIpcServer(
    JNIEnv* env, jclass clazz);

JNIEXPORT void JNICALL Java_com_windroid_emu_views_GameSurfaceView_nativeSendMouseEvent(
    JNIEnv* env, jobject thiz, jfloat x, jfloat y, jint button, jboolean down, jboolean relative);

JNIEXPORT void JNICALL Java_com_windroid_emu_views_GameSurfaceView_nativeSendMouseWheel(
    JNIEnv* env, jobject thiz, jfloat delta_x, jfloat delta_y);

JNIEXPORT void JNICALL Java_com_windroid_emu_views_GameSurfaceView_nativeSendKeyEvent(
    JNIEnv* env, jobject thiz, jint scan_code, jint key_code, jboolean down);

JNIEXPORT void JNICALL Java_com_windroid_emu_views_GameSurfaceView_nativeSendTouchEvent(
    JNIEnv* env, jobject thiz, jint action, jint pointer_id, jfloat x, jfloat y);

#ifdef __cplusplus
}
#endif

#endif // WINDROID_NATIVE_IPC_SERVER_H
