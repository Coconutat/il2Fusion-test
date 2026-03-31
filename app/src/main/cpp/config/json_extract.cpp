#include "json_extract.h"

#include <cerrno>
#include <cstdlib>
#include <limits>

namespace json_extract {
namespace {

bool IsJsonWhitespace(char ch) {
    return ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t';
}

size_t SkipJsonWhitespace(const std::string& text, size_t index) {
    while (index < text.size() && IsJsonWhitespace(text[index])) {
        ++index;
    }
    return index;
}

size_t FindValueStart(const std::string& json, const std::string& key) {
    const std::string needle = "\"" + key + "\"";
    const size_t key_pos = json.find(needle);
    if (key_pos == std::string::npos) {
        return std::string::npos;
    }

    const size_t colon_pos = json.find(':', key_pos + needle.size());
    if (colon_pos == std::string::npos) {
        return std::string::npos;
    }

    return SkipJsonWhitespace(json, colon_pos + 1);
}

bool AppendUtf8CodePoint(std::uint32_t code_point, std::string* out) {
    if (out == nullptr) {
        return false;
    }

    if (code_point <= 0x7F) {
        out->push_back(static_cast<char>(code_point));
        return true;
    }
    if (code_point <= 0x7FF) {
        out->push_back(static_cast<char>(0xC0 | (code_point >> 6)));
        out->push_back(static_cast<char>(0x80 | (code_point & 0x3F)));
        return true;
    }
    if (code_point <= 0xFFFF) {
        out->push_back(static_cast<char>(0xE0 | (code_point >> 12)));
        out->push_back(static_cast<char>(0x80 | ((code_point >> 6) & 0x3F)));
        out->push_back(static_cast<char>(0x80 | (code_point & 0x3F)));
        return true;
    }
    if (code_point <= 0x10FFFF) {
        out->push_back(static_cast<char>(0xF0 | (code_point >> 18)));
        out->push_back(static_cast<char>(0x80 | ((code_point >> 12) & 0x3F)));
        out->push_back(static_cast<char>(0x80 | ((code_point >> 6) & 0x3F)));
        out->push_back(static_cast<char>(0x80 | (code_point & 0x3F)));
        return true;
    }
    return false;
}

int HexDigitValue(char ch) {
    if (ch >= '0' && ch <= '9') {
        return ch - '0';
    }
    if (ch >= 'a' && ch <= 'f') {
        return 10 + (ch - 'a');
    }
    if (ch >= 'A' && ch <= 'F') {
        return 10 + (ch - 'A');
    }
    return -1;
}

bool DecodeJsonStringLiteral(const std::string& json, size_t value_pos, std::string* out) {
    if (out == nullptr || value_pos >= json.size() || json[value_pos] != '"') {
        return false;
    }

    out->clear();
    for (size_t index = value_pos + 1; index < json.size(); ++index) {
        const char ch = json[index];
        if (ch == '"') {
            return true;
        }
        if (ch != '\\') {
            out->push_back(ch);
            continue;
        }
        if (index + 1 >= json.size()) {
            return false;
        }

        const char esc = json[++index];
        switch (esc) {
            case '"':
            case '\\':
            case '/':
                out->push_back(esc);
                break;
            case 'b':
                out->push_back('\b');
                break;
            case 'f':
                out->push_back('\f');
                break;
            case 'n':
                out->push_back('\n');
                break;
            case 'r':
                out->push_back('\r');
                break;
            case 't':
                out->push_back('\t');
                break;
            case 'u': {
                if (index + 4 >= json.size()) {
                    return false;
                }
                std::uint32_t code_point = 0;
                for (int nibble = 0; nibble < 4; ++nibble) {
                    const int value = HexDigitValue(json[index + 1 + nibble]);
                    if (value < 0) {
                        return false;
                    }
                    code_point = (code_point << 4) | static_cast<std::uint32_t>(value);
                }
                index += 4;
                if (!AppendUtf8CodePoint(code_point, out)) {
                    return false;
                }
                break;
            }
            default:
                out->push_back(esc);
                break;
        }
    }

    return false;
}

bool ExtractBalancedJson(const std::string& json,
                         size_t value_pos,
                         char open_ch,
                         char close_ch,
                         std::string* out) {
    if (out == nullptr || value_pos >= json.size() || json[value_pos] != open_ch) {
        return false;
    }

    int depth = 0;
    bool in_string = false;
    bool escape = false;
    for (size_t index = value_pos; index < json.size(); ++index) {
        const char ch = json[index];
        if (in_string) {
            if (escape) {
                escape = false;
                continue;
            }
            if (ch == '\\') {
                escape = true;
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
                *out = json.substr(value_pos, index - value_pos + 1);
                return true;
            }
        }
    }

    return false;
}

template <typename IntType>
bool ExtractJsonInteger(const std::string& json, const std::string& key, IntType* out) {
    if (out == nullptr) {
        return false;
    }

    const size_t value_pos = FindValueStart(json, key);
    if (value_pos == std::string::npos) {
        return false;
    }

    std::string string_literal;
    const char* parse_begin = json.c_str() + value_pos;
    if (json[value_pos] == '"') {
        if (!DecodeJsonStringLiteral(json, value_pos, &string_literal) || string_literal.empty()) {
            return false;
        }
        parse_begin = string_literal.c_str();
    }

    errno = 0;
    char* end = nullptr;
    const unsigned long long value = std::strtoull(parse_begin, &end, 0);
    if (end == parse_begin || errno == ERANGE) {
        return false;
    }
    *out = static_cast<IntType>(value);
    return true;
}

}  // namespace

bool ExtractJsonString(const std::string& json, const std::string& key, std::string* out) {
    const size_t value_pos = FindValueStart(json, key);
    return value_pos != std::string::npos && DecodeJsonStringLiteral(json, value_pos, out);
}

bool ExtractJsonInt(const std::string& json, const std::string& key, int* out) {
    std::uint64_t value = 0;
    if (!ExtractJsonInteger(json, key, &value)) {
        return false;
    }
    if (value > static_cast<std::uint64_t>(std::numeric_limits<int>::max())) {
        return false;
    }
    *out = static_cast<int>(value);
    return true;
}

bool ExtractJsonUInt64(const std::string& json, const std::string& key, std::uint64_t* out) {
    return ExtractJsonInteger(json, key, out);
}

bool ExtractJsonObject(const std::string& json, const std::string& key, std::string* out) {
    const size_t value_pos = FindValueStart(json, key);
    return value_pos != std::string::npos && ExtractBalancedJson(json, value_pos, '{', '}', out);
}

bool ExtractJsonArray(const std::string& json, const std::string& key, std::string* out) {
    const size_t value_pos = FindValueStart(json, key);
    return value_pos != std::string::npos && ExtractBalancedJson(json, value_pos, '[', ']', out);
}

std::vector<std::string> ExtractJsonObjectArrayElements(const std::string& json_array) {
    std::vector<std::string> objects;
    if (json_array.size() < 2 || json_array.front() != '[' || json_array.back() != ']') {
        return objects;
    }

    size_t index = 1;
    while (index + 1 < json_array.size()) {
        index = SkipJsonWhitespace(json_array, index);
        if (index >= json_array.size() || json_array[index] == ']') {
            break;
        }
        if (json_array[index] == ',') {
            ++index;
            continue;
        }
        if (json_array[index] != '{') {
            ++index;
            continue;
        }

        std::string object;
        if (!ExtractBalancedJson(json_array, index, '{', '}', &object)) {
            break;
        }
        objects.push_back(object);
        index += object.size();
    }

    return objects;
}

}  // namespace json_extract
