package io.playback.servlet.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UrlDecoderTest {
    @Test
    void decode() {
        assertThat(UrlDecoder.decode("%2F")).isEqualTo("/");
    }

    @Test
    void decode_invalidCharset() {
        assertThatThrownBy(() -> UrlDecoder.decode("a", ""))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
