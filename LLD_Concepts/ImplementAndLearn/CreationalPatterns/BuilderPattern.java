import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Builder Pattern demo.
 *
 * Why Builder?
 * - Avoids telescoping constructors (too many optional params).
 * - Readable, chained configuration with sane defaults.
 * - Validation happens once at build() time.
 * - Produces an immutable object.
 *
 * How to understand it:
 * 1) Read the nested HttpRequest.Builder class below (the "builder").
 * 2) Notice each setter returns this (for fluent chaining).
 * 3) build() validates and returns an immutable HttpRequest.
 * 4) Run the main() method to see it in action.
 *
 */
public class BuilderPattern {

    // Demo: how to use the Builder
    public static void main(String[] args) {
        HttpRequest request = new HttpRequest.Builder()
                .method("GET")
                .url("https://example.com/api/v1/users")
                .header("Accept", "application/json")
                .timeout(10_000)
                .retries(2)
                .build();

        System.out.println("request1 : " + request);

        HttpRequest request2 = new HttpRequest.Builder()
                            .method("POST")
                            .url("https://example.com/v1/data")
                            .body("name", "vivek")
                            .build();
        
        System.out.println("request2 : " + request2);
    }

    /**
     * Immutable object built via nested Builder.
     */
    public static final class HttpRequest {
        private final String method;
        private final String url;
        private final Map<String, String> headers;
        private final int timeoutMillis;
        private final int retries;
        private final Map<String, String> body;

        private HttpRequest(Builder b) {
            this.method = b.method;
            this.url = b.url;
            this.headers = Collections.unmodifiableMap(new HashMap<>(b.headers));
            this.timeoutMillis = b.timeoutMillis;
            this.retries = b.retries;
            this.body = b.body;
        }

        public String method() { return method; }
        public String url() { return url; }
        public Map<String, String> headers() { return headers; }
        public int timeoutMillis() { return timeoutMillis; }
        public int retries() { return retries; }
        public Map<String, String> body() { return body; }

        @Override
        public String toString() {
            return "HttpRequest{" +
                    "method='" + method + '\'' +
                    ", url='" + url + '\'' +
                    ", headers=" + headers +
                    ", timeoutMillis=" + timeoutMillis +
                    ", retries=" + retries + 
                    ", body=" + body + 
                    '}';
        }

        /**
         * The Builder: holds mutable state while configuring, then validates and
         * produces an immutable HttpRequest.
         */
        public static final class Builder {
            // defaults
            private String method = "GET";
            private String url = "";
            private final Map<String, String> headers = new HashMap<>();
            private int timeoutMillis = 30_000;
            private int retries = 3;
            private Map<String, String> body = new HashMap<>();

            public Builder method(String method) {
                if (method == null || method.trim().isEmpty()) {
                    throw new IllegalArgumentException("method must not be blank");
                }
                this.method = method.toUpperCase();
                return this;
            }

            public Builder url(String url) {
                if (url == null || url.trim().isEmpty()) {
                    throw new IllegalArgumentException("url must not be blank");
                }
                this.url = url;
                return this;
            }
            public Builder body(String key, String value){
                if(key == null || key.trim().isEmpty()){
                    throw new IllegalStateException("key must contain a value in body");
                }
                body.put(key, value);
                return this;
            }
            
            //**********  function overloading *********************//
            public Builder header(String key, String value) {
                if (key == null || key.trim().isEmpty()) {
                    throw new IllegalArgumentException("header key must not be blank");
                }
                this.headers.put(key, value == null ? "" : value);
                return this;
            }

            public Builder headers(Map<String, String> headers) {
                if (headers != null) {
                    this.headers.putAll(headers);
                }
                return this;
            }
            //***************************************************//

            public Builder timeout(int timeoutMillis) {
                if (timeoutMillis <= 0) {
                    throw new IllegalArgumentException("timeoutMillis must be positive");
                }
                this.timeoutMillis = timeoutMillis;
                return this;
            }

            public Builder retries(int retries) {
                if (retries < 0) {
                    throw new IllegalArgumentException("retries must be >= 0");
                }
                this.retries = retries;
                return this;
            }

            public HttpRequest build() {
                if (url == null || url.trim().isEmpty()) {
                    throw new IllegalStateException("url is required");
                }
                if (method == null || method.trim().isEmpty()) {
                    throw new IllegalStateException("method is required");
                }
                return new HttpRequest(this);
            }
        }
    }
}
