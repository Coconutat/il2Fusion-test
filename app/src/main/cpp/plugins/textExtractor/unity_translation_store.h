#ifndef IL2FUSION_UNITY_TRANSLATION_STORE_H
#define IL2FUSION_UNITY_TRANSLATION_STORE_H

#include <cstdint>
#include <string>

namespace unity_translation_store {

struct TranslationEntry {
    int32_t display_mode = 0;
    std::string original_text;
    std::string translation;
    int32_t preferred_font_size = 0;
    int32_t original_font_size = 0;
    float font_size_scale = 1.0f;
    int32_t original_width = 0;
    int32_t original_height = 0;
    int32_t width = 0;
    int32_t height = 0;
};

void UpdateConfigJson(const std::string& json);
bool ResolveTargetAddress(const std::string& full_name, std::uint64_t* out_rva);
bool LookupTranslation(const std::string& original_text, TranslationEntry* out);

}  // namespace unity_translation_store

#endif  // IL2FUSION_UNITY_TRANSLATION_STORE_H
