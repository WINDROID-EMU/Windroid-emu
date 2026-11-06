#include <android/log.h>
#include <jni.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/wait.h>
#include <sys/types.h>
#include <fcntl.h>
#include <errno.h>

#define FEX_LOG(prio, ...) __android_log_print(ANDROID_LOG_##prio, "FEXCoreWrapper", __VA_ARGS__)

static char fex_interpreter_path[512] = {0};
static char rootfs_path[512] = {0};
static pid_t fex_process_pid = -1;
static int fex_stdout_pipe[2] = {-1, -1};
static int fex_stderr_pipe[2] = {-1, -1};

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_windroid_emu_core_FEXCoreWrapper_setFEXInterpreterPath(JNIEnv *env, jobject thiz, jstring path) {
  if (!path) {
    FEX_LOG(ERROR, "FEXInterpreter path is null");
    return JNI_FALSE;
  }
  
  const char *path_str = env->GetStringUTFChars(path, nullptr);
  strncpy(fex_interpreter_path, path_str, sizeof(fex_interpreter_path) - 1);
  fex_interpreter_path[sizeof(fex_interpreter_path) - 1] = '\0';
  env->ReleaseStringUTFChars(path, path_str);
  
  FEX_LOG(INFO, "FEXInterpreter path set to: %s", fex_interpreter_path);
  return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL
Java_com_windroid_emu_core_FEXCoreWrapper_setRootFSPath(JNIEnv *env, jobject thiz, jstring path) {
  if (!path) {
    FEX_LOG(ERROR, "RootFS path is null");
    return JNI_FALSE;
  }
  
  const char *path_str = env->GetStringUTFChars(path, nullptr);
  strncpy(rootfs_path, path_str, sizeof(rootfs_path) - 1);
  rootfs_path[sizeof(rootfs_path) - 1] = '\0';
  env->ReleaseStringUTFChars(path, path_str);
  
  FEX_LOG(INFO, "RootFS path set to: %s", rootfs_path);
  return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL
Java_com_windroid_emu_core_FEXCoreWrapper_isAvailable(JNIEnv *env, jobject thiz) {
  if (strlen(fex_interpreter_path) == 0) {
    return JNI_FALSE;
  }
  return (access(fex_interpreter_path, X_OK) == 0) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_windroid_emu_core_FEXCoreWrapper_initialize(JNIEnv *env, jobject thiz) {
  FEX_LOG(INFO, "Initializing FEXCore wrapper (subprocess mode)...");
  
  if (strlen(fex_interpreter_path) == 0) {
    FEX_LOG(ERROR, "FEXInterpreter path not set. Call setFEXInterpreterPath first.");
    return JNI_FALSE;
  }
  
  if (access(fex_interpreter_path, X_OK) != 0) {
    FEX_LOG(ERROR, "FEX binary not executable or not found at: %s", fex_interpreter_path);
    return JNI_FALSE;
  }
  
  if (strlen(rootfs_path) == 0) {
    FEX_LOG(ERROR, "RootFS path not set. Call setRootFSPath first.");
    return JNI_FALSE;
  }
  
  FEX_LOG(INFO, "FEXCore wrapper initialized successfully");
  return JNI_TRUE;
}

JNIEXPORT void JNICALL
Java_com_windroid_emu_core_FEXCoreWrapper_shutdown(JNIEnv *env, jobject thiz) {
  FEX_LOG(INFO, "Shutting down FEXCore wrapper...");
  
  if (fex_process_pid > 0) {
    FEX_LOG(INFO, "Killing FEX process (PID: %d)", fex_process_pid);
    kill(fex_process_pid, SIGTERM);
    
    int status;
    waitpid(fex_process_pid, &status, 0);
    fex_process_pid = -1;
  }
  
  if (fex_stdout_pipe[0] != -1) close(fex_stdout_pipe[0]);
  if (fex_stdout_pipe[1] != -1) close(fex_stdout_pipe[1]);
  if (fex_stderr_pipe[0] != -1) close(fex_stderr_pipe[0]);
  if (fex_stderr_pipe[1] != -1) close(fex_stderr_pipe[1]);
  
  fex_stdout_pipe[0] = fex_stdout_pipe[1] = -1;
  fex_stderr_pipe[0] = fex_stderr_pipe[1] = -1;
  
  FEX_LOG(INFO, "FEXCore wrapper shutdown complete");
}

JNIEXPORT jboolean JNICALL
Java_com_windroid_emu_core_FEXCoreWrapper_loadELF(JNIEnv *env, jobject thiz, jstring elf_path) {
  if (strlen(fex_interpreter_path) == 0) {
    FEX_LOG(ERROR, "FEXCore not initialized");
    return JNI_FALSE;
  }
  
  const char *path = env->GetStringUTFChars(elf_path, nullptr);
  FEX_LOG(INFO, "Checking ELF: %s", path);
  
  if (access(path, F_OK) != 0) {
    FEX_LOG(ERROR, "ELF file not found: %s", path);
    env->ReleaseStringUTFChars(elf_path, path);
    return JNI_FALSE;
  }
  
  env->ReleaseStringUTFChars(elf_path, path);
  return JNI_TRUE;
}

JNIEXPORT jint JNICALL
Java_com_windroid_emu_core_FEXCoreWrapper_run(JNIEnv *env, jobject thiz, jint argc, jobjectArray argv) {
  if (strlen(fex_interpreter_path) == 0) {
    FEX_LOG(ERROR, "FEXCore not initialized");
    return -1;
  }
  
  FEX_LOG(INFO, "Running FEXCore with %d arguments", argc);
  
  if (pipe(fex_stdout_pipe) == -1 || pipe(fex_stderr_pipe) == -1) {
    FEX_LOG(ERROR, "Failed to create pipes: %s", strerror(errno));
    return -1;
  }
  
  pid_t pid = fork();
  if (pid == -1) {
    FEX_LOG(ERROR, "Failed to fork: %s", strerror(errno));
    return -1;
  }
  
  if (pid == 0) {
    // Child process
    close(fex_stdout_pipe[0]);
    close(fex_stderr_pipe[0]);
    
    dup2(fex_stdout_pipe[1], STDOUT_FILENO);
    dup2(fex_stderr_pipe[1], STDERR_FILENO);
    close(fex_stdout_pipe[1]);
    close(fex_stderr_pipe[1]);
    
    // Set FEX_ROOTFS environment variable
    if (strlen(rootfs_path) > 0) {
      setenv("FEX_ROOTFS", rootfs_path, 1);
    }
    
    // Build arguments: FEXLoader --rootfs <path> -- <binary> [args...]
    char* args[argc + 5];
    int arg_idx = 0;
    args[arg_idx++] = fex_interpreter_path;
    
    if (strlen(rootfs_path) > 0) {
      args[arg_idx++] = (char*)"--rootfs";
      args[arg_idx++] = rootfs_path;
    }
    args[arg_idx++] = (char*)"--";
    
    for (int i = 0; i < argc; i++) {
      jstring arg = (jstring)env->GetObjectArrayElement(argv, i);
      const char* arg_str = env->GetStringUTFChars(arg, nullptr);
      args[arg_idx++] = strdup(arg_str);
      env->ReleaseStringUTFChars(arg, arg_str);
    }
    args[arg_idx] = nullptr;
    
    execv(fex_interpreter_path, args);
    
    FEX_LOG(ERROR, "Failed to exec FEX binary %s: %s", fex_interpreter_path, strerror(errno));
    _exit(1);
  } else {
    // Parent process
    fex_process_pid = pid;
    close(fex_stdout_pipe[1]);
    close(fex_stderr_pipe[1]);
    
    FEX_LOG(INFO, "FEX process started with PID: %d", pid);
    
    char buffer[4096];
    ssize_t bytes_read;
    
    while ((bytes_read = read(fex_stdout_pipe[0], buffer, sizeof(buffer) - 1)) > 0) {
      buffer[bytes_read] = '\0';
      FEX_LOG(INFO, "[FEX stdout] %s", buffer);
    }
    
    while ((bytes_read = read(fex_stderr_pipe[0], buffer, sizeof(buffer) - 1)) > 0) {
      buffer[bytes_read] = '\0';
      FEX_LOG(ERROR, "[FEX stderr] %s", buffer);
    }
    
    int status;
    waitpid(pid, &status, 0);
    
    close(fex_stdout_pipe[0]);
    close(fex_stderr_pipe[0]);
    fex_stdout_pipe[0] = fex_stdout_pipe[1] = -1;
    fex_stderr_pipe[0] = fex_stderr_pipe[1] = -1;
    fex_process_pid = -1;
    
    if (WIFEXITED(status)) {
      return WEXITSTATUS(status);
    } else if (WIFSIGNALED(status)) {
      FEX_LOG(ERROR, "FEX process killed by signal: %d", WTERMSIG(status));
      return -1;
    }
    
    return 0;
  }
}

JNIEXPORT jstring JNICALL
Java_com_windroid_emu_core_FEXCoreWrapper_getVersion(JNIEnv *env, jobject thiz) {
  return env->NewStringUTF("FEXCore-Android-Bionic-2410");
}

} // extern "C"
