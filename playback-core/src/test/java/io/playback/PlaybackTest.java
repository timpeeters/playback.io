package io.playback;

import io.playback.client.HttpClient;
import io.playback.matcher.RequestMatchers;
import io.playback.repository.InMemoryRecordingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaybackTest {
    private static final String OK_200 = "200";

    private Playback playback;

    @Mock
    private HttpClient httpClient;

    @BeforeEach
    void initialize() {
        playback = Playback.configure(Configuration.builder()
                .httpClient(httpClient)
                .matcher(RequestMatchers.DEFAULT)
                .recordingRepository(new InMemoryRecordingRepository()));
    }

    @Test
    void noMatchingRecording() {
        assertThatThrownBy(() -> playback.replay(Request.get().build())).isInstanceOf(NoMatchingRecordingFound.class);
    }

    @Test
    void noMatchingRecording_differentRequestMethod() {
        playback.record(Request.get().build(), Response.ok().build());

        assertThatThrownBy(() -> playback.replay(Request.post().build())).isInstanceOf(NoMatchingRecordingFound.class);
    }

    @Test
    void noMatchingRecording_differentUri() {
        playback.record(Request.get("/a").build(), Response.ok().build());

        assertThatThrownBy(() -> playback.replay(Request.get("/b").build()))
                .isInstanceOf(NoMatchingRecordingFound.class);
    }

    @Test
    void noMatchingRecording_differentAcceptHeader() {
        playback.record(
                Request.get().header(Headers.ACCEPT, "application/json").build(),
                Response.ok().build());

        assertThatThrownBy(() -> playback.replay(Request.get()
                .header(() -> Header.header(Headers.ACCEPT, "application/xml"))
                .build()))
                .isInstanceOf(NoMatchingRecordingFound.class);
    }

    @Test
    void noMatchingRecording_queryParam() {
        playback.record(Request.get().queryParam("id", "1").build(),
                Response.ok().build());

        assertThatThrownBy(() -> playback.replay(Request.get().queryParam("id", "2").build()))
                .isInstanceOf(NoMatchingRecordingFound.class);
    }

    @Test
    void noMatchingRecording_differentRequestBody() {
        playback.record(
                Request.post().body("hello").build(),
                Response.ok().build());

        assertThatThrownBy(() -> playback.replay(Request.post().body("world").build()))
                .isInstanceOf(NoMatchingRecordingFound.class);
    }

    @Test
    void replay_statusCode() {
        playback.record(Request.get().build(), Response.builder().statusCode(OK_200).build());

        assertThat(playback.replay(Request.get().build())).satisfies(r ->
                assertThat(r.getStatusCode()).isEqualTo(OK_200));
    }

    @Test
    void replay_body() {
        playback.record(Request.get().build(), Response.builder().body("Hello world").build());

        assertThat(playback.replay(Request.get().build())).satisfies(r ->
                assertThat(r.getBodyAsString()).isEqualTo("Hello world"));
    }

    @Test
    void replay_bodyWithEncoding() {
        playback.record(Request.get().build(), Response.builder()
                .header(Headers.CONTENT_TYPE, "text/plain;charset=iso-8859-1")
                .body("Hélicoptère".getBytes(Charset.forName("ISO-8859-1"))).build());

        assertThat(playback.replay(Request.get().build())).satisfies(r ->
                assertThat(r.getBodyAsString()).isEqualTo("Hélicoptère"));
    }

    @Test
    void replay_header() {
        playback.record(
                Request.get().build(),
                Response.builder().header(Headers.CONTENT_TYPE, "application/json").build());

        assertThat(playback.replay(Request.get().build())).satisfies(r ->
                assertThat(r.getHeader(Headers.CONTENT_TYPE)).containsExactly("application/json"));
    }

    @Test
    void play() {
        when(httpClient.execute(Request.get().build())).thenReturn(Response.ok().build());

        assertThat(playback.play(Request.get().build())).satisfies(response ->
                assertThat(response.getStatusCode()).isEqualTo("200"));
    }

    @Test
    void play_forwardOnlyOnce() {
        when(httpClient.execute(Request.get().build())).thenReturn(Response.ok().build());

        playback.play(Request.get().build());
        playback.play(Request.get().build());

        verify(httpClient, Mockito.times(1)).execute(Request.get().build());
    }
}
