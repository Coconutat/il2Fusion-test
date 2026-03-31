#include "unity_translation_store.h"

#include "../../config/json_extract.h"

#include <algorithm>
#include <cctype>
#include <cstdlib>
#include <mutex>
#include <unordered_map>

namespace unity_translation_store {
namespace {

struct StoreState {
    std::mutex mutex;
    std::string source_json;
    std::unordered_map<std::string, std::uint64_t> target_addresses;
    std::unordered_map<std::string, TranslationEntry> translation_entries;
};

StoreState g_store;

size_t SkipJsonWhitespace(const std::string& text, size_t index) {
    while (index < text.size() && std::isspace(static_cast<unsigned char>(text[index])) != 0) {
        ++index;
    }
    return index;
}

bool ParseJsonString(const std::string& text, size_t* index, std::string* out) {
    if (index == nullptr || out == nullptr || *index >= text.size() || text[*index] != '"') {
        return false;
    }

    out->clear();
    bool escaped = false;
    for (size_t cursor = *index + 1; cursor < text.size(); ++cursor) {
        const char ch = text[cursor];
        if (escaped) {
            switch (ch) {
                case 'n':
                    out->push_back('\n');
                    break;
                case 'r':
                    out->push_back('\r');
                    break;
                case 't':
                    out->push_back('\t');
                    break;
                default:
                    out->push_back(ch);
                    break;
            }
            escaped = false;
            continue;
        }
        if (ch == '\\') {
            escaped = true;
            continue;
        }
        if (ch == '"') {
            *index = cursor + 1;
            return true;
        }
        out->push_back(ch);
    }

    return false;
}

bool CaptureBalancedJson(const std::string& text,
                         size_t* index,
                         char open_ch,
                         char close_ch,
                         std::string* out) {
    if (index == nullptr || out == nullptr || *index >= text.size() || text[*index] != open_ch) {
        return false;
    }

    int depth = 0;
    bool in_string = false;
    bool escaped = false;
    const size_t start = *index;
    for (size_t cursor = start; cursor < text.size(); ++cursor) {
        const char ch = text[cursor];
        if (in_string) {
            if (escaped) {
                escaped = false;
                continue;
            }
            if (ch == '\\') {
                escaped = true;
                continue;
            }
            if (ch == '"') {
                in_string = false;
            }
            continue;
        }
        if (ch == '"') {
            in_string = true;
            continue;
        }
        if (ch == open_ch) {
            ++depth;
            continue;
        }
        if (ch == close_ch) {
            --depth;
            if (depth == 0) {
                *out = text.substr(start, cursor - start + 1);
                *index = cursor + 1;
                return true;
            }
        }
    }

    return false;
}

bool CaptureJsonValue(const std::string& text, size_t* index, char* kind, std::string* raw) {
    if (index == nullptr || kind == nullptr || raw == nullptr) {
        return false;
    }

    *index = SkipJsonWhitespace(text, *index);
    if (*index >= text.size()) {
        return false;
    }

    const char start = text[*index];
    *kind = start;
    if (start == '"') {
        return ParseJsonString(text, index, raw);
    }
    if (start == '{') {
        return CaptureBalancedJson(text, index, '{', '}', raw);
    }
    if (start == '[') {
        return CaptureBalancedJson(text, index, '[', ']', raw);
    }

    const size_t literal_start = *index;
    size_t cursor = *index;
    while (cursor < text.size() &&
           text[cursor] != ',' &&
           text[cursor] != '}' &&
           text[cursor] != ']' &&
           std::isspace(static_cast<unsigned char>(text[cursor])) == 0) {
        ++cursor;
    }
    raw->assign(text, literal_start, cursor - literal_start);
    *index = cursor;
    return !raw->empty();
}

template <typename Callback>
bool ForEachObjectMember(const std::string& object_text, Callback callback) {
    size_t cursor = SkipJsonWhitespace(object_text, 0);
    if (cursor >= object_text.size() || object_text[cursor] != '{') {
        return false;
    }

    ++cursor;
    while (cursor < object_text.size()) {
        cursor = SkipJsonWhitespace(object_text, cursor);
        if (cursor >= object_text.size()) {
            return false;
        }
        if (object_text[cursor] == '}') {
            return true;
        }

        std::string key;
        if (!ParseJsonString(object_text, &cursor, &key)) {
            return false;
        }

        cursor = SkipJsonWhitespace(object_text, cursor);
        if (cursor >= object_text.size() || object_text[cursor] != ':') {
            return false;
        }
        ++cursor;

        char kind = 0;
        std::string value;
        if (!CaptureJsonValue(object_text, &cursor, &kind, &value)) {
            return false;
        }
        if (!callback(key, kind, value)) {
            return false;
        }

        cursor = SkipJsonWhitespace(object_text, cursor);
        if (cursor < object_text.size() && object_text[cursor] == ',') {
            ++cursor;
            continue;
        }
        if (cursor < object_text.size() && object_text[cursor] == '}') {
            return true;
        }
    }

    return false;
}

bool ShouldSkipTopLevelKey(const std::string& key) {
    return key == "sourceLanguageCode" ||
           key == "publishLanguageVersion" ||
           key == "regexData" ||
           key == "splitDelimiters" ||
           key == "splitData" ||
           key == "formatData" ||
           key == "targets" ||
           key == "target" ||
           key == "config" ||
           key == "game_framework" ||
           key == "translation_data_json_string" ||
           key == "patch_code" ||
           key == "patchCode" ||
           key == "translations" ||
           key == "data" ||
           key == "base_func_offset" ||
           key == "lib" ||
           key == "UIText_hook" ||
           key == "TextMeshProUGUI_hook" ||
           key == "UIRichText_hook" ||
           key == "UILabel_hook" ||
           key == "TextField_hook" ||
           key == "HybridCLR_hook";
}

void ResetLocked(StoreState& state) {
    state.target_addresses.clear();
    state.translation_entries.clear();
}

bool ApplyEntryObject(const std::string& original_text,
                      const std::string& object_text,
                      TranslationEntry* entry) {
    if (entry == nullptr) {
        return false;
    }

    std::string translation;
    if (!json_extract::ExtractJsonString(object_text, "translation", &translation) ||
        translation.empty()) {
        return false;
    }

    entry->display_mode = 0;
    entry->original_text = original_text;
    entry->translation = translation;
    entry->preferred_font_size = 0;
    entry->original_font_size = 0;
    entry->font_size_scale = 1.0f;
    entry->original_width = 0;
    entry->original_height = 0;
    entry->width = 0;
    entry->height = 0;

    json_extract::ExtractJsonInt(object_text, "fontSize", &entry->preferred_font_size);
    json_extract::ExtractJsonInt(object_text, "displayMode", &entry->display_mode);
    json_extract::ExtractJsonInt(object_text, "originalFontSize", &entry->original_font_size);
    json_extract::ExtractJsonInt(object_text, "originalWidth", &entry->original_width);
    json_extract::ExtractJsonInt(object_text, "originalHeight", &entry->original_height);
    json_extract::ExtractJsonInt(object_text, "width", &entry->width);
    json_extract::ExtractJsonInt(object_text, "height", &entry->height);

    std::string scale_text;
    if (json_extract::ExtractJsonString(object_text, "fontSizeScale", &scale_text) &&
        !scale_text.empty()) {
        entry->font_size_scale = std::strtof(scale_text.c_str(), nullptr);
    } else {
        const size_t pos = object_text.find("\"fontSizeScale\"");
        if (pos != std::string::npos) {
            const size_t colon = object_text.find(':', pos);
            if (colon != std::string::npos) {
                entry->font_size_scale = std::strtof(object_text.c_str() + colon + 1, nullptr);
            }
        }
    }

    if (entry->font_size_scale <= 0.0f) {
        entry->font_size_scale = 1.0f;
    }
    return true;
}

bool ApplyEntryValue(const std::string& original_text,
                     char kind,
                     const std::string& raw_value,
                     TranslationEntry* entry) {
    if (entry == nullptr || original_text.empty()) {
        return false;
    }

    if (kind == '"') {
        entry->display_mode = 0;
        entry->original_text = original_text;
        entry->translation = raw_value;
        entry->preferred_font_size = 0;
        entry->original_font_size = 0;
        entry->font_size_scale = 1.0f;
        entry->original_width = 0;
        entry->original_height = 0;
        entry->width = 0;
        entry->height = 0;
        return !entry->translation.empty();
    }

    if (kind == '{') {
        return ApplyEntryObject(original_text, raw_value, entry);
    }
    return false;
}

void InsertTranslationEntry(StoreState& state, TranslationEntry entry) {
    if (entry.original_text.empty() || entry.translation.empty()) {
        return;
    }
    state.translation_entries[entry.original_text] = std::move(entry);
}

void ParseTranslationObjectMembers(const std::string& object_text,
                                   bool skip_meta_keys,
                                   StoreState* state) {
    if (state == nullptr) {
        return;
    }

    ForEachObjectMember(object_text, [&](const std::string& key, char kind, const std::string& raw_value) {
        if (skip_meta_keys && ShouldSkipTopLevelKey(key)) {
            return true;
        }

        TranslationEntry entry;
        if (ApplyEntryValue(key, kind, raw_value, &entry)) {
            InsertTranslationEntry(*state, std::move(entry));
        }
        return true;
    });
}

void ParseTargetArray(const std::string& json_array, StoreState* state) {
    if (state == nullptr) {
        return;
    }

    for (const std::string& item : json_extract::ExtractJsonObjectArrayElements(json_array)) {
        std::string function_name;
        std::uint64_t address = 0;
        if (!json_extract::ExtractJsonString(item, "functionName", &function_name) ||
            function_name.empty() ||
            !json_extract::ExtractJsonUInt64(item, "address", &address) ||
            address == 0) {
            continue;
        }
        state->target_addresses[function_name] = address;
    }
}

void ParseTranslationSourcesFromObject(const std::string& object_text, StoreState* state) {
    if (state == nullptr || object_text.empty()) {
        return;
    }

    std::string nested_text;
    if (json_extract::ExtractJsonString(object_text, "translation_data_json_string", &nested_text) &&
        !nested_text.empty()) {
        ParseTranslationObjectMembers(nested_text, false, state);
    }

    std::string patch_code_object;
    if (json_extract::ExtractJsonString(object_text, "patch_code", &nested_text) && !nested_text.empty()) {
        ParseTranslationObjectMembers(nested_text, false, state);
    } else if (json_extract::ExtractJsonString(object_text, "patchCode", &nested_text) &&
               !nested_text.empty()) {
        ParseTranslationObjectMembers(nested_text, false, state);
    } else if (json_extract::ExtractJsonObject(object_text, "patch_code", &patch_code_object)) {
        ParseTranslationObjectMembers(patch_code_object, false, state);
    }

    std::string translations_object;
    if (json_extract::ExtractJsonObject(object_text, "translations", &translations_object)) {
        ParseTranslationObjectMembers(translations_object, false, state);
    }

    std::string data_object;
    if (json_extract::ExtractJsonObject(object_text, "data", &data_object)) {
        ParseTranslationObjectMembers(data_object, false, state);
    } else if (!object_text.empty() && object_text.front() == '{') {
        ParseTranslationObjectMembers(object_text, true, state);
    }
}

void RebuildStoreLocked(StoreState& state) {
    ResetLocked(state);
    if (state.source_json.empty()) {
        return;
    }

    std::string root_targets;
    if (json_extract::ExtractJsonArray(state.source_json, "targets", &root_targets)) {
        ParseTargetArray(root_targets, &state);
    }

    std::string config_object;
    if (json_extract::ExtractJsonObject(state.source_json, "config", &config_object)) {
        std::string config_targets;
        if (json_extract::ExtractJsonArray(config_object, "targets", &config_targets)) {
            ParseTargetArray(config_targets, &state);
        }
        ParseTranslationSourcesFromObject(config_object, &state);
    }

    ParseTranslationSourcesFromObject(state.source_json, &state);
}

}  // namespace

void UpdateConfigJson(const std::string& json) {
    std::lock_guard<std::mutex> lock(g_store.mutex);
    g_store.source_json = json;
    RebuildStoreLocked(g_store);
}

bool ResolveTargetAddress(const std::string& full_name, std::uint64_t* out_rva) {
    if (out_rva == nullptr || full_name.empty()) {
        return false;
    }

    std::lock_guard<std::mutex> lock(g_store.mutex);
    const auto it = g_store.target_addresses.find(full_name);
    if (it == g_store.target_addresses.end() || it->second == 0) {
        return false;
    }
    *out_rva = it->second;
    return true;
}

bool LookupTranslation(const std::string& original_text, TranslationEntry* out) {
    if (out == nullptr || original_text.empty()) {
        return false;
    }

    std::lock_guard<std::mutex> lock(g_store.mutex);
    const auto it = g_store.translation_entries.find(original_text);
    if (it == g_store.translation_entries.end()) {
        return false;
    }
    *out = it->second;
    return true;
}

}  // namespace unity_translation_store
