package auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.mockito.Mock;

public class AuthFilterTest {

    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;
    AuthFilter filter;

    @BeforeEach
    public void setup() {
        initMocks(this);
        filter = new AuthFilter();
    }

    @Test
    void testMissingAuth() throws Exception {
        // Condition the Mock
        when(request.getHeader("authorization")).thenReturn(null);
        filter.doFilter(request, response, null);
        assertEquals(403, response.getStatus());
    }
}
