#ifndef WINDROID_NATIVE_SURFACE_BRIDGE_H
#define WINDROID_NATIVE_SURFACE_BRIDGE_H

#include <jni.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <pthread.h>

#ifdef __cplusplus
extern "C" {
#endif

// Retorna a janela nativa atual (incrementa referência)
ANativeWindow* windroid_get_native_window(void);

// Retorna se a superfície nativa está ativa e válida
int windroid_is_surface_valid(void);

// Obtém as dimensões atuais da superfície
void windroid_get_surface_dimensions(int* width, int* height);

// Configura nova resolução geométrica na janela nativa
int windroid_set_buffers_geometry(int width, int height, int format);

// Inicializa a memória compartilhada (SHM Framebuffer) a partir do caminho do socket
int windroid_shm_init(const char* socket_path);

// Libera e desfaz o mapeamento da memória compartilhada
void windroid_shm_cleanup(void);

// Renderiza a área alterada da memória compartilhada para a ANativeWindow do Android
void windroid_present_frame(int left, int top, int right, int bottom);

// Atualiza a posição do cursor do mouse para renderização nativa
void windroid_update_cursor_position(float x, float y, int is_relative);

// Callbacks JNI registrados
JNIEXPORT void JNICALL Java_com_windroid_emu_views_GameSurfaceView_nativeSurfaceCreated(
    JNIEnv* env, jobject thiz, jobject surface);

JNIEXPORT void JNICALL Java_com_windroid_emu_views_GameSurfaceView_nativeSurfaceChanged(
    JNIEnv* env, jobject thiz, jobject surface, jint width, jint height);

JNIEXPORT void JNICALL Java_com_windroid_emu_views_GameSurfaceView_nativeSurfaceDestroyed(
    JNIEnv* env, jobject thiz, jobject surface);

JNIEXPORT jboolean JNICALL Java_com_windroid_emu_views_GameSurfaceView_nativeIsSurfaceAvailable(
    JNIEnv* env, jclass clazz);

#ifdef __cplusplus
}
#endif

#endif // WINDROID_NATIVE_SURFACE_BRIDGE_H
