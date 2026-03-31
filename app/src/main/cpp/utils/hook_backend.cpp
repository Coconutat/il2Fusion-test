#include "hook_backend.h"

#include <atomic>

#include "dobby.h"
#include "log.h"

#if defined(__aarch64__)
#include "../third_party/And64InlineHook/And64InlineHook.hpp"
#endif

namespace hook_backend {
namespace {

std::atomic<std::int32_t> g_backend{static_cast<std::int32_t>(Backend::kAnd64InlineHook)};

}  // namespace

Backend BackendFromOrdinal(std::int32_t value) {
    if (value == static_cast<std::int32_t>(Backend::kDobby)) {
        return Backend::kDobby;
    }
    return Backend::kAnd64InlineHook;
}

const char* BackendName(Backend backend) {
    switch (backend) {
        case Backend::kDobby:
            return "Dobby";
        case Backend::kAnd64InlineHook:
        default:
            return "And64InlineHook";
    }
}

void SetPreferredBackend(Backend backend) {
    g_backend.store(static_cast<std::int32_t>(backend));
    LOGI("Hook backend set to %s", BackendName(backend));
}

Backend GetPreferredBackend() {
    return BackendFromOrdinal(g_backend.load());
}

bool SupportsDestroy(Backend backend) {
    return backend == Backend::kDobby;
}

bool InstallInlineHook(void* target,
                       void* replacement,
                       void** original,
                       const char* label,
                       Backend backend) {
    const char* hook_label = label != nullptr ? label : "<unknown>";
    if (target == nullptr || replacement == nullptr || original == nullptr) {
        LOGW("Skip hook install for %s: invalid args target=%p replacement=%p original=%p",
             hook_label,
             target,
             replacement,
             original);
        return false;
    }

    if (backend == Backend::kDobby) {
        const int ret = DobbyHook(
            target,
            reinterpret_cast<dobby_dummy_func_t>(replacement),
            reinterpret_cast<dobby_dummy_func_t*>(original));
        if (ret != 0) {
            LOGE("Dobby hook failed for %s: ret=%d target=%p", hook_label, ret, target);
            return false;
        }
        LOGI("Installed Dobby hook %s target=%p replacement=%p original=%p",
             hook_label,
             target,
             replacement,
             *original);
        return true;
    }

#if defined(__aarch64__)
    A64HookFunction(target, replacement, original);
    if (*original == nullptr) {
        LOGE("And64InlineHook failed for %s target=%p", hook_label, target);
        return false;
    }
    LOGI("Installed And64InlineHook %s target=%p replacement=%p original=%p",
         hook_label,
         target,
         replacement,
         *original);
    return true;
#else
    LOGW("And64InlineHook unavailable on current arch, fallback to Dobby for %s", hook_label);
    return InstallInlineHook(target, replacement, original, label, Backend::kDobby);
#endif
}

bool DestroyInlineHook(void* target, Backend backend) {
    if (target == nullptr) {
        return false;
    }
    if (backend != Backend::kDobby) {
        LOGW("DestroyInlineHook skipped: %s backend does not support uninstall for target=%p",
             BackendName(backend),
             target);
        return false;
    }

    const int ret = DobbyDestroy(target);
    if (ret != 0) {
        LOGE("Dobby destroy failed target=%p ret=%d", target, ret);
        return false;
    }
    return true;
}

}  // namespace hook_backend
