package io.tapedeck;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Response {
    private final String statusCode;
    private final String statusText;
    private final Map<String, List<String>> headers;
    private final byte[] body;

    private Response(Builder builder) {
        this.statusCode = builder.statusCode;
        this.statusText = builder.statusText;
        this.headers = builder.headers;
        this.body = builder.body;
    }

    public Charset getCharset() {
        List<String> values = getHeader(Headers.CONTENT_TYPE);

        if (values != null && !values.isEmpty()) {
            return MediaType.parse(values.iterator().next()).getCharset();
        }

        return Charset.forName("UTF-8");
    }

    public List<String> getHeader(String key) {
        return headers.get(key);
    }

    public String getStatusCode() {
        return statusCode;
    }

    public String getBodyAsString() {
        return new String(body, getCharset());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder ok() {
        return builder().statusCode("200").statusText("OK");
    }

    public static class Builder {
        private String statusCode;
        private String statusText;
        private Map<String, List<String>> headers = new HashMap<>();
        private byte[] body;

        public Builder statusCode(String statusCode) {
            this.statusCode = statusCode;

            return this;
        }

        public Builder statusText(String statusText) {
            this.statusText = statusText;

            return this;
        }

        public Builder body(String body) {
            this.body = body.getBytes(Charset.forName("UTF-8"));

            return this;
        }

        public Builder body(byte[] body) {
            this.body = body;

            return this;
        }

        public Builder header(String key, String value) {
            List<String> values = headers.getOrDefault(key, new ArrayList<>());
            values.add(value);

            headers.put(key, values);

            return this;
        }

        public Response build() {
            return new Response(this);
        }
    }
}