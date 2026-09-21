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
