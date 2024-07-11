package camp.nextstep.http.domain;

import camp.nextstep.http.exception.InvalidHttpHeaderException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class HttpHeadersTest {

    @Test
    void ContentType_설정시_정상적으로_저장된다() {
        final HttpHeaders headers = new HttpHeaders();

        headers.setContentType(ContentType.HTML);

        assertThat(headers.getContentType()).isEqualTo(ContentType.HTML);
    }

    @Test
    void ContentLength_설정시_정상적으로_저장된다() {
        final HttpHeaders headers = new HttpHeaders();

        headers.setContentLength(1000);

        assertThat(headers.getContentLength()).isEqualTo(1000);
    }

    @Test
    void ContentLength_설정을_안하면_0을_반환한다() {
        final HttpHeaders headers = new HttpHeaders();

        assertThat(headers.getContentLength()).isEqualTo(0);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    void ContentLength_는_0보다_커야한다(final int value) {
        final HttpHeaders headers = new HttpHeaders();

        assertThatThrownBy(() -> headers.setContentLength(value))
                .isInstanceOf(InvalidHttpHeaderException.class)
                .hasMessage("ContentLength must be grater than 0");
    }

    @Test
    void HttpHeaders_를_String_으로_변환할_수_있다() {
        final HttpHeaders headers = new HttpHeaders();

        headers.setContentType(ContentType.HTML);
        headers.setContentLength(1000);

        assertThat(headers.convertToString()).isEqualTo(
                String.join(System.lineSeparator(),
                        "Content-Type: text/html;charset=utf-8 ",
                        "Content-Length: 1000 "
                )
        );
    }

    @Test
    void HttpHeaders_에_Cookie_는_getCookie_로_반환받을_수_있다() {
        final Map<String, String> headerMap = Map.of(
                "Content-Type", "text/html;charset=utf-8",
                "Content-Length", "1000",
                "Cookie", "yummy_cookie=choco; tasty_cookie=strawberry; JSESSIONID=656cef62-e3c4-40bc-a8df-94732920ed46"
        );
        final HttpHeaders headers = new HttpHeaders(headerMap);

        assertSoftly(softly -> {
            softly.assertThat(headers.getContentLength()).isEqualTo(1000);
            softly.assertThat(headers.getContentType()).isEqualTo(ContentType.HTML);
            softly.assertThat(headers.getCookie("yummy_cookie")).isEqualTo("choco");
            softly.assertThat(headers.getCookie("tasty_cookie")).isEqualTo("strawberry");
            softly.assertThat(headers.getCookie("JSESSIONID")).isEqualTo("656cef62-e3c4-40bc-a8df-94732920ed46");
        });
    }
}
