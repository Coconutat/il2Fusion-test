#ifndef IL2FUSION_UTILS_H
#define IL2FUSION_UTILS_H

#include <chrono>
#include <cstddef>
#include <cstdint>
#include <string>

#include "dobby.h"

// Forward declarations for il2cpp types
struct Il2CppDomain;
struct Il2CppAssembly;
struct Il2CppImage;
struct Il2CppClass;
struct Il2CppType;
struct MethodInfo;
struct Il2CppThread;

namespace textutils {

struct Il2CppString {
    void* klass;
    void* monitor;
    int32_t length;
    char16_t chars[1];
};

bool ShouldFilter(const std::string& text);
std::string Utf16ToUtf8(const char16_t* data, int32_t len);
std::string DescribeIl2CppString(const Il2CppString* str);
}

namespace hookutils {
struct ModuleInfo {
    uintptr_t load_bias = 0;
    std::string path;
};

bool GetModuleInfo(const char* name, ModuleInfo* out);
bool WaitForModuleInfo(const char* name, std::chrono::milliseconds timeout, ModuleInfo* out);
uintptr_t FindModuleBase(const char* name);
uintptr_t WaitForModule(const char* name, std::chrono::milliseconds timeout);
std::string FindModulePath(const char* name);
uintptr_t FindExportInElf(const char* path, const char* symbol, uintptr_t base);
bool IsExecutableAddressInModule(uintptr_t address, const char* module_name);
void* GetSecondArg(DobbyRegisterContext* ctx);
void SetSecondArg(DobbyRegisterContext* ctx, void* value);
}

namespace il2cpputils {

struct Il2CppApi {
    Il2CppDomain* (*domain_get)();
    const Il2CppAssembly** (*domain_get_assemblies)(Il2CppDomain*, size_t*);
    const Il2CppImage* (*assembly_get_image)(const Il2CppAssembly*);
    Il2CppClass* (*class_from_name)(const Il2CppImage*, const char*, const char*);
    const MethodInfo* (*class_get_methods)(Il2CppClass*, void**);
    const MethodInfo* (*class_get_method_from_name)(Il2CppClass*, const char*, int);
    const char* (*method_get_name)(const MethodInfo*);
    uint32_t (*method_get_param_count)(const MethodInfo*);
    const Il2CppType* (*method_get_param)(const MethodInfo*, uint32_t);
    const Il2CppType* (*method_get_return_type)(const MethodInfo*);
    bool (*method_is_instance)(const MethodInfo*);
    void* (*string_new)(const char*);
    bool (*is_vm_thread)(Il2CppThread*);
    void* (*thread_attach)(Il2CppDomain*);
};

struct TargetSpec {
    std::string full;
    std::string namespaze;
    std::string klass;
    std::string method;
};

struct ResolvedMethod {
    const MethodInfo* method = nullptr;
    uintptr_t entry = 0;
    bool is_instance = false;
    uint32_t param_count = 0;
    int first_param_type = -1;
    int return_type = -1;
};

// Parses "Namespace.Class.method" into TargetSpec; returns false if format invalid.
bool ParseTarget(const std::string& full, TargetSpec& out);

// Resolve required il2cpp symbols from a handle (dlopen/xdl_open result).
bool ResolveIl2cppApi(void* handle, Il2CppApi& api);

// Waits for VM ready (if is_vm_thread is available) and attaches current thread when possible.
bool EnsureVmReadyAndAttach(const Il2CppApi& api, int max_retry, std::chrono::milliseconds interval);

// Finds and validates a strict instance void set_text(string) method for the target spec.
bool FindMethodInAssemblies(
        const Il2CppApi& api,
        const TargetSpec& spec,
        ResolvedMethod& out,
        std::string* reason = nullptr);

}  // namespace il2cpputils

extern "C" {
int find_handle(const char* handle_name);
void* lookup_symbol(const char* libraryname, const char* symbolname);
void* lookup_symbol2(const char* libraryname, const char* symbolname);
const char* find_full_path(const char* libraryname);
}

#endif  // IL2FUSION_UTILS_H
