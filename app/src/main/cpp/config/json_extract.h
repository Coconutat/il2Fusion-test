#ifndef IL2FUSION_JSON_EXTRACT_H
#define IL2FUSION_JSON_EXTRACT_H

#include <cstdint>
#include <string>
#include <vector>

namespace json_extract {

bool ExtractJsonString(const std::string& json, const std::string& key, std::string* out);
bool ExtractJsonInt(const std::string& json, const std::string& key, int* out);
bool ExtractJsonUInt64(const std::string& json, const std::string& key, std::uint64_t* out);
bool ExtractJsonObject(const std::string& json, const std::string& key, std::string* out);
bool ExtractJsonArray(const std::string& json, const std::string& key, std::string* out);
std::vector<std::string> ExtractJsonObjectArrayElements(const std::string& json_array);

}  // namespace json_extract

#endif  // IL2FUSION_JSON_EXTRACT_H
