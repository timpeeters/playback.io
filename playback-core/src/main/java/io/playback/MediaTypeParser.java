package io.playback;

import io.playback.util.StringUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public final class MediaTypeParser {
    private MediaTypeParser() {
    }

    public static MediaType parse(String mediaType) {
        if (StringUtils.isEmpty(mediaType)) {
            throw new IllegalArgumentException("'mediatype' must not be empty");
        }

        String fullType = parseFullType(mediaType);
        String type = fullType.split("/")[0];
        String subtype = fullType.split("/")[1];

        return new MediaType(type, subtype, parseParameters(mediaType));
    }

    private static String parseFullType(String mediaType) {
        String fullType = mediaType.split(";")[0].trim();

        if ("*".equals(fullType)) {
            return "*/*";
        }

        return fullType;
    }

    private static Map<String, String> parseParameters(String mediaType) {
        return Arrays.stream(mediaType.split(";"))
                .skip(1)
                .filter(p -> p.contains("="))
                .map(p -> p.split("="))
                .collect(Collectors.toMap(p -> p[0].trim(), p -> p[1].trim()));
    }
}