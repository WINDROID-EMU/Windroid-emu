#include <android/log.h>
#include <arpa/inet.h>
#include <fcntl.h>
#include <jni.h>
#include <math.h>
#include <pthread.h>
#include <signal.h>
#include <stdbool.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>

#define log(prio, ...)                                                         \
  __android_log_print(ANDROID_LOG_##prio, "ControllerNative", __VA_ARGS__)

typedef struct {
  bool isConnected;
  uint8_t buttons;
  uint8_t buttonsB;
  uint8_t dpadStatus;
  uint8_t axisLX;
  uint8_t axisLY;
  uint8_t axisRX;
  uint8_t axisRY;
  uint8_t axisLT;
  uint8_t axisRT;
} ControllerState;

static ControllerState connectedVirtualControllers[4] = {
    {false, 0, 0, 0, 127, 127, 127, 127, 0, 0},
    {false, 0, 0, 0, 127, 127, 127, 127, 0, 0},
    {false, 0, 0, 0, 127, 127, 127, 127, 0, 0},
    {false, 0, 0, 0, 127, 127, 127, 127, 0, 0}};

#define CLIENT_PORT 7941
#define BUFFER_SIZE 44

#define REQUEST_GET_CONNECTION 1
#define REQUEST_GET_CONTROLLER_STATE 2
#define REQUEST_GET_CONTROLLER_STATE_DINPUT 3

static volatile int inputServerRunning = 0;

static bool enableXInput = true;
static bool enableDInput = true;

static pthread_t controller_thread_id;
static int sockfd;

// Vibration state
static JavaVM *jvm = NULL;
static jclass controllerUtilsClass = NULL;
static jmethodID updateVibrationMethod = NULL;
static bool vibrationEnabled = true;
static int currentVibrationIntensity = 0;

static void thread_signal_handler(__unused int signum) {
  if (sockfd >= 0) {
    close(sockfd);
    sockfd = -1;
  }
  pthread_exit(NULL);
}

void *controller_update_thread(__unused void *param) {
  struct sigaction sa;
  sa.sa_handler = thread_signal_handler;
  sigemptyset(&sa.sa_mask);
  sa.sa_flags = 0;
  sigaction(SIGUSR2, &sa, NULL);

  struct sockaddr_in serverAddr, clientAddr;
  socklen_t addrLen = sizeof(clientAddr);
  uint8_t buffer[BUFFER_SIZE];

  sockfd = socket(AF_INET, SOCK_DGRAM, 0);
  if (sockfd < 0) {
    log(ERROR, "Error on creating server.");
    inputServerRunning = 0;
    pthread_exit(NULL);
  }

  fcntl(sockfd, F_SETFD, FD_CLOEXEC);

  int optval = 1;
  if (setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR, &optval, sizeof(optval)) <
      0) {
    log(ERROR, "Failed to set SO_REUSEADDR");
  }
#ifdef SO_REUSEPORT
  setsockopt(sockfd, SOL_SOCKET, SO_REUSEPORT, &optval, sizeof(optval));
#endif

  memset(&serverAddr, 0, sizeof(serverAddr));
  serverAddr.sin_family = AF_INET;
  serverAddr.sin_addr.s_addr = INADDR_ANY;
  serverAddr.sin_port = htons(CLIENT_PORT);

  if (bind(sockfd, (struct sockaddr *)&serverAddr, sizeof(serverAddr)) < 0) {
    log(ERROR, "Error on binding.");
    close(sockfd);
    sockfd = -1;
    inputServerRunning = 0;
    pthread_exit(NULL);
  }

  while (inputServerRunning) {
    memset(buffer, 0, BUFFER_SIZE);
    addrLen = sizeof(clientAddr);
    ssize_t len = recvfrom(sockfd, buffer, BUFFER_SIZE, 0,
                           (struct sockaddr *)&clientAddr, &addrLen);

    if (len < 0) {
      continue;
    }

    if (buffer[0] == REQUEST_GET_CONNECTION) {
      sendto(sockfd, buffer, BUFFER_SIZE, 0, (struct sockaddr *)&clientAddr,
             addrLen);
    } else if (buffer[0] == REQUEST_GET_CONTROLLER_STATE) {
      // Extract vibration data from Wine (buffer[1-8] contains vibration for 4 controllers)
      // buffer[1]=C0 left, [2]=C0 right, [3]=C1 left, [4]=C1 right, etc.
      // Values are 0-255 (already scaled from 0-65535 by Wine)
      if (vibrationEnabled && jvm != NULL && controllerUtilsClass != NULL && updateVibrationMethod != NULL) {
        int maxVibration = 0;
        for (int i = 0; i < 4; i++) {
          if (connectedVirtualControllers[i].isConnected) {
            int leftVibration = buffer[1 + (i * 2)];
            int rightVibration = buffer[2 + (i * 2)];
            int controllerMax = (leftVibration > rightVibration) ? leftVibration : rightVibration;
            if (controllerMax > maxVibration) {
              maxVibration = controllerMax;
            }
          }
        }

        // Only update if intensity changed
        if (maxVibration != currentVibrationIntensity) {
          currentVibrationIntensity = maxVibration;
          JNIEnv *jniEnv;
          int getEnvStat = (*jvm)->GetEnv(jvm, (void **)&jniEnv, JNI_VERSION_1_6);
          int attached = 0;
          if (getEnvStat == JNI_EDETACHED) {
            (*jvm)->AttachCurrentThread(jvm, &jniEnv, NULL);
            attached = 1;
          }
          if (jniEnv != NULL) {
            (*jniEnv)->CallStaticVoidMethod(jniEnv, controllerUtilsClass, updateVibrationMethod, (jint)maxVibration);
            if (attached) {
              (*jvm)->DetachCurrentThread(jvm);
            }
          }
        }
      }

      for (int i = 0; i < 4; i++) {
        buffer[0 + (i * 11)] = REQUEST_GET_CONTROLLER_STATE;
        buffer[1 + (i * 11)] =
            (connectedVirtualControllers[i].isConnected || i == 0) &&
            enableXInput;
        buffer[2 + (i * 11)] = connectedVirtualControllers[i].buttons;
        buffer[3 + (i * 11)] = connectedVirtualControllers[i].buttonsB;
        buffer[4 + (i * 11)] = connectedVirtualControllers[i].dpadStatus;
        buffer[5 + (i * 11)] = connectedVirtualControllers[i].axisLX;
        buffer[6 + (i * 11)] = connectedVirtualControllers[i].axisLY;
        buffer[7 + (i * 11)] = connectedVirtualControllers[i].axisRX;
        buffer[8 + (i * 11)] = connectedVirtualControllers[i].axisRY;
        buffer[9 + (i * 11)] = connectedVirtualControllers[i].axisLT;
        buffer[10 + (i * 11)] = connectedVirtualControllers[i].axisRT;
      }

      sendto(sockfd, buffer, BUFFER_SIZE, 0, (struct sockaddr *)&clientAddr,
             addrLen);
    } else if (buffer[0] == REQUEST_GET_CONTROLLER_STATE_DINPUT) {
      for (int i = 0; i < 4; i++) {
        buffer[0 + (i * 11)] = REQUEST_GET_CONTROLLER_STATE_DINPUT;
        buffer[1 + (i * 11)] =
            (connectedVirtualControllers[i].isConnected || i == 0) &&
            enableDInput;
        buffer[2 + (i * 11)] = connectedVirtualControllers[i].buttons;
        buffer[3 + (i * 11)] = connectedVirtualControllers[i].buttonsB;
        buffer[4 + (i * 11)] = connectedVirtualControllers[i].dpadStatus;
        buffer[5 + (i * 11)] = connectedVirtualControllers[i].axisLX;
        buffer[6 + (i * 11)] = connectedVirtualControllers[i].axisLY;
        buffer[7 + (i * 11)] = connectedVirtualControllers[i].axisRX;
        buffer[8 + (i * 11)] = connectedVirtualControllers[i].axisRY;
        buffer[9 + (i * 11)] = connectedVirtualControllers[i].axisLT;
        buffer[10 + (i * 11)] = connectedVirtualControllers[i].axisRT;
      }

      sendto(sockfd, buffer, BUFFER_SIZE, 0, (struct sockaddr *)&clientAddr,
             addrLen);
    }

    usleep(2000);
  }

  pthread_exit(NULL);
}

JNIEXPORT void JNICALL
Java_com_windroid_emu_controller_ControllerUtils_setEnableXInputNative(
    __unused JNIEnv *env, __unused jobject cls, jboolean enabled) {
  enableXInput = enabled;
}

JNIEXPORT void JNICALL
Java_com_windroid_emu_controller_ControllerUtils_setEnableDInputNative(
    __unused JNIEnv *env, __unused jobject cls, jboolean enabled) {
  enableDInput = enabled;
}

JNIEXPORT void JNICALL
Java_com_windroid_emu_controller_ControllerUtils_startInputServer(
    __unused JNIEnv *env, __unused jobject cls) {
  if (inputServerRunning)
    return;
  inputServerRunning = true;

  pthread_create(&controller_thread_id, NULL, controller_update_thread, NULL);
}

JNIEXPORT void JNICALL
Java_com_windroid_emu_controller_ControllerUtils_stopInputServer(
    __unused JNIEnv *env, __unused jobject cls) {
  if (!inputServerRunning)
    return;
  inputServerRunning = false;
  if (sockfd >= 0) {
    close(sockfd);
    sockfd = -1;
  }
  pthread_kill(controller_thread_id, SIGUSR2);
  pthread_join(controller_thread_id, NULL);
}

JNIEXPORT jint JNICALL
Java_com_windroid_emu_controller_ControllerUtils_connectController(
    __unused JNIEnv *env, __unused jobject cls) {
  for (int i = 0; i < 4; i++) {
    if (!connectedVirtualControllers[i].isConnected) {
      log(DEBUG, "Connected Controller on Port %i", i);
      connectedVirtualControllers[i].isConnected = true;
      return i;
    }
  }

  return -1;
}

JNIEXPORT void JNICALL
Java_com_windroid_emu_controller_ControllerUtils_disconnectController(
    __unused JNIEnv *env, __unused jobject cls, jint index) {
  if (index == -1 || index > 3)
    return;
  log(DEBUG, "Disconnected Controller on Port %i", index);
  connectedVirtualControllers[index].isConnected = false;
}

JNIEXPORT void JNICALL
Java_com_windroid_emu_controller_ControllerUtils_updateButtonsStateNative(
    __unused JNIEnv *env, __unused jobject cls, jint index, jint buttons,
    jint buttonsB) {
  if (index == -1 || index > 3)
    return;
  connectedVirtualControllers[index].buttons = buttons;
  connectedVirtualControllers[index].buttonsB = buttonsB;
}

static inline uint8_t float_to_u8_255(float f) {
  if (f < -1.0F) {
    f = -1.0F;
  } else if (f > 1.0F) {
    f = 1.0F;
  }

  if (f > -0.01f && f < 0.01f) {
    return 127;
  }

  float scaled = (f + 1.0F) * 127.5F;
  int rounded = (int)roundf(scaled);

  if (rounded < 0) {
    rounded = 0;
  } else if (rounded > 255) {
    rounded = 255;
  }

  return (uint8_t)rounded;
}

JNIEXPORT void JNICALL
Java_com_windroid_emu_controller_ControllerUtils_updateAxisStateNative(
    __unused JNIEnv *env, jobject __unused cls, jint index, jfloat lx,
    jfloat ly, jfloat rx, jfloat ry, jfloat lt, jfloat rt, jbyte dpadStatus) {
  if (index == -1 || index > 3)
    return;
  connectedVirtualControllers[index].axisLX = float_to_u8_255(lx);
  connectedVirtualControllers[index].axisLY = float_to_u8_255(-ly);
  connectedVirtualControllers[index].axisRX = float_to_u8_255(rx);
  connectedVirtualControllers[index].axisRY = float_to_u8_255(-ry);
  connectedVirtualControllers[index].axisLT = float_to_u8_255(lt * 2.F - 1.F);
  connectedVirtualControllers[index].axisRT = float_to_u8_255(rt * 2.F - 1.F);
  connectedVirtualControllers[index].dpadStatus = dpadStatus;
}

JNIEXPORT void JNICALL
Java_com_windroid_emu_controller_ControllerUtils_setVibrationEnabled(
    __unused JNIEnv *env, __unused jobject cls, jboolean enabled) {
  vibrationEnabled = enabled;
  if (!enabled) {
    // Stop current vibration when disabled
    if (jvm != NULL && controllerUtilsClass != NULL && updateVibrationMethod != NULL) {
      JNIEnv *jniEnv;
      int getEnvStat = (*jvm)->GetEnv(jvm, (void **)&jniEnv, JNI_VERSION_1_6);
      if (getEnvStat == JNI_EDETACHED) {
        (*jvm)->AttachCurrentThread(jvm, &jniEnv, NULL);
      }
      if (jniEnv != NULL) {
        (*jniEnv)->CallStaticVoidMethod(jniEnv, controllerUtilsClass, updateVibrationMethod, (jint)0);
        if (getEnvStat == JNI_EDETACHED) {
          (*jvm)->DetachCurrentThread(jvm);
        }
      }
    }
    currentVibrationIntensity = 0;
  }
}

JNIEXPORT void JNICALL
Java_com_windroid_emu_controller_ControllerUtils_nativeInitVibration(
    JNIEnv *env, jobject cls) {
  (*env)->GetJavaVM(env, &jvm);
  controllerUtilsClass = (*env)->NewGlobalRef(env, cls);
  updateVibrationMethod = (*env)->GetStaticMethodID(env, controllerUtilsClass, "updateVibration", "(I)V");
}