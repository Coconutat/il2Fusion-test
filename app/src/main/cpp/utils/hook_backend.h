#ifndef IL2FUSION_HOOK_BACKEND_H
#define IL2FUSION_HOOK_BACKEND_H

#include <cstdint>

namespace hook_backend {

enum class Backend : std::int32_t {
    kAnd64InlineHook = 0,
    kDobby = 1,
};

Backend BackendFromOrdinal(std::int32_t value);
const char* BackendName(Backend backend);

void SetPreferredBackend(Backend backend);
Backend GetPreferredBackend();
bool SupportsDestroy(Backend backend);

bool InstallInlineHook(void* target,
                       void* replacement,
                       void** original,
                       const char* label,
                       Backend backend = GetPreferredBackend());

bool DestroyInlineHook(void* target, Backend backend = GetPreferredBackend());

}  // namespace hook_backend

#endif  // IL2FUSION_HOOK_BACKEND_H
