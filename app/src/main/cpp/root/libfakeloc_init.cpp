// libfakeloc_init.cpp
//
// Reconstructed, compilable source for the generic app init entry library.
// Recovered from do/complete/libfakeloc_init..c (arm64).  The 32-bit behaviour
// is identical at the JNI level, so this single portable source covers both.
//
// Flow:
//   doRun(JavaVM**, arg) -> AttachCurrentThread -> init(env)
//   init(env):
//     - verify payload MD5 and release signature
//     - build a DexClassLoader over /data/kail-loc/libfakeloc.so with an opt
//       directory of /data/kail-loc/system_dex
//     - load com.kail.location.inject.fakelocation.InjectDex
//     - call InjectDex.init(context) reflectively

#include <cstdio>
#include <cstdlib>

#include "fakeloc_common.h"

using namespace fakeloc;

static const char *kOptDir = "/data/kail-loc/system_dex";
static bool gInitLoaded = false;     // byte_7038

// ---------------------------------------------------------------------------
// init  (sub_2430)
// ---------------------------------------------------------------------------
//
// Loads the slim inject dex via InMemoryDexClassLoader (a ByteBuffer-backed
// loader) instead of DexClassLoader. This matters a lot inside system_server
// on Android 14: DexClassLoader insists on writing an optimized .vdex/.odex
// into its optimizedDirectory, but system_server is SELinux-confined and the
// write to /data/kail-loc/oat/ is denied. ART then spins for the entire
// ptrace watchdog window (~2 min) before giving up, freezing and ultimately
// killing the framework.
//
// InMemoryDexClassLoader runs the dex straight from memory (interpreter /
// in-memory JIT) with no on-disk oat, so there is nothing to deny and the
// load completes in milliseconds.
// ---------------------------------------------------------------------------
static void init(JNIEnv *env) {
  KLOGI(kLogTag, "InitApp is Executing");

  if (verifyApkMd5() != 0)
    return;
  if (!env) {
    KLOGI(kLogTag, "jni_env is NULL!!");
    return;
  }
  if (verifyReleaseSignature(env) != 0)
    return;

  jobject context = getGlobalContext(env);
  jclass ctxClass = env->FindClass("android/content/Context");
  jmethodID getCl = env->GetMethodID(ctxClass, "getClassLoader", "()Ljava/lang/ClassLoader;");
  jobject parentLoader = env->CallObjectMethod(context, getCl);

  // Read the slim dex bytes from /data/kail-loc/libfakeloc.so into a direct
  // ByteBuffer. The file is a bare .dex (RootDeployer deploys the raw dex,
  // not a zip, for the in-memory path).
  jobject loader = nullptr;
  FILE *fp = fopen(kPayloadPath, "rb");
  if (fp) {
    fseek(fp, 0, SEEK_END);
    long sz = ftell(fp);
    fseek(fp, 0, SEEK_SET);
    if (sz > 0) {
      void *buf = malloc((size_t)sz);
      if (buf && fread(buf, 1, (size_t)sz, fp) == (size_t)sz) {
        jobject byteBuffer = env->NewDirectByteBuffer(buf, sz);
        jclass imdclClass = env->FindClass("dalvik/system/InMemoryDexClassLoader");
        if (imdclClass && byteBuffer) {
          jmethodID imdclCtor = env->GetMethodID(
              imdclClass, "<init>",
              "(Ljava/nio/ByteBuffer;Ljava/lang/ClassLoader;)V");
          if (imdclCtor) {
            loader = env->NewObject(imdclClass, imdclCtor, byteBuffer, parentLoader);
            if (env->ExceptionCheck()) {
              env->ExceptionDescribe();
              env->ExceptionClear();
              loader = nullptr;
            }
          }
        }
        // NB: the direct ByteBuffer keeps referencing `buf`; ART copies the
        // dex into its own memory during construction, but to be safe we do
        // not free `buf` until after the loader is built. Freeing here is
        // fine because InMemoryDexClassLoader has already mapped the data.
        if (loader) free(buf);
        else { /* keep buf around on failure path; small one-shot leak */ }
      } else if (buf) {
        free(buf);
      }
    }
    fclose(fp);
  }

  if (!loader) {
    // Fallback: legacy DexClassLoader over the same path (works if the file
    // is actually a zip/apk and the opt dir is writable).
    KLOGW(kLogTag, "InMemoryDexClassLoader failed; falling back to DexClassLoader");
    jstring optDir  = env->NewStringUTF(kOptDir);
    jstring dexPath = env->NewStringUTF(kPayloadPath);
    jclass dclClass = env->FindClass("dalvik/system/DexClassLoader");
    jmethodID dclCtor = env->GetMethodID(
        dclClass, "<init>",
        "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/ClassLoader;)V");
    loader = env->NewObject(dclClass, dclCtor, dexPath, optDir, nullptr, parentLoader);
    env->DeleteLocalRef(optDir);
    env->DeleteLocalRef(dexPath);
    env->DeleteLocalRef(dclClass);
  }

  if (!loader) {
    KLOGE(kLogTag, "failed to build any class loader");
    return;
  }

  jclass loaderClass = env->GetObjectClass(loader);
  jmethodID dclLoad = env->GetMethodID(
      loaderClass, "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;");

  jstring injectClassName = env->NewStringUTF("com.kail.location.inject.fakelocation.InjectDex");
  jclass injectClass = (jclass)env->CallObjectMethod(loader, dclLoad, injectClassName);
  if (env->ExceptionCheck()) {
    env->ExceptionDescribe();
    env->ExceptionClear();
    KLOGE(kLogTag, "failed to load InjectDex class");
    return;
  }

  jmethodID initMethod = env->GetStaticMethodID(
      injectClass, "init", "(Ljava/lang/Object;)[Ljava/lang/Object;");
  env->CallStaticObjectMethod(injectClass, initMethod, context);
  if (env->ExceptionCheck()) {
    env->ExceptionDescribe();
    env->ExceptionClear();
  }

  KLOGI(kLogTag, "InitApp is finished.");

  env->DeleteLocalRef(context);
  env->DeleteLocalRef(ctxClass);
  env->DeleteLocalRef(parentLoader);
  env->DeleteLocalRef(injectClassName);
}

// ---------------------------------------------------------------------------
// doRun  (sub_2A38)
// ---------------------------------------------------------------------------
extern "C" __attribute__((visibility("default"))) void doRun(JavaVM **vmPtr, const char *arg) {
  (void)arg;
  if (gInitLoaded) {
    KLOGE(kLogTag, "-- Already loaded");
    return;
  }
  gInitLoaded = true;

  if (!vmPtr) {
    KLOGE(kLogTag, "JavaVM** == NULL");
    return;
  }
  JavaVM *vm = *vmPtr;
  if (!vm) {
    KLOGE(kLogTag, "JavaVM* == NULL");
    return;
  }

  JNIEnv *env = nullptr;
  if (vm->AttachCurrentThread(&env, nullptr) != JNI_OK) {
    KLOGE(kLogTag, "AttachCurrentThread (main) != JNI_OK");
    return;
  }
  init(env);
}
