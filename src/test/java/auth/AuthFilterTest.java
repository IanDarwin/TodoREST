package auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import org.mockito.ArgumentCaptor;
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
        int status = -1;
        ArgumentCaptor<Integer> captor = ArgumentCaptor.forClass(int.class);

        filter.doFilter(request, response, null);
        verify(response, times(1)).setStatus(captor.capture());
        assertEquals(403, captor.getValue());
    }
}
