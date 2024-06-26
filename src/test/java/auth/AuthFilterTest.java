package auth;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.io.IOException;
import java.util.Base64;

public class AuthFilterTest {

    public static final String FAKE_CONTENT_TYPE = "test/test";
    static EntityManagerFactory emf;
    EntityManager em;

    public static final String AUTHENTICATION = "Authentication";
    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;
    FilterChain chain = new FilterChain() {

        /** Invoked at end of AuthFilter IFF all authentication checks passed. */
        @Override
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException {
            System.out.println("In FilterChain::doFilter");
            // N.B.: Prove that the login worked:
            assertEquals(usernameGood, ((HttpServletRequest)servletRequest).getRemoteUser());
            // These seem to have no effect:
            servletResponse.setContentType(FAKE_CONTENT_TYPE);
            ((HttpServletResponse)servletResponse).setStatus(200);
        }
    };

    AuthFilter filter;

    final String authMethod = "Basic";
    final String usernameGood = "pearly";
    final String authHeaderGood = usernameGood + ":gates";
    final String authHeaderBad = "gurly:pates";
    final String encodedHeaderGood = authMethod + " " +
            new String(Base64.getEncoder().encode(authHeaderGood.getBytes()));
    final String encodedHeaderBad = authMethod + " " +
            new String(Base64.getEncoder().encode(authHeaderBad.getBytes()));

    @BeforeAll
    static void setupJPA() {
        emf = Persistence.createEntityManagerFactory("todolist");
    }

    @BeforeEach
    public void setup() {
        initMocks(this);
        filter = new AuthFilter();
        em = emf.createEntityManager();
        filter.em = this.em;
    }

    @AfterEach
    public void tearDown() {
        em.close();
    }

    @AfterAll
    static void stopEMF() {
        emf.close();
    }

    @Test
    void testMissingAuth() throws Exception {
        // Condition the Mock
        when(request.getHeader(AUTHENTICATION)).thenReturn(null);
        int status = -1;
        ArgumentCaptor<Integer> captor = ArgumentCaptor.forClass(int.class);

        filter.doFilter(request, response, null);
        verify(response, times(1)).setStatus(captor.capture());
        assertEquals(403, captor.getValue());
    }

    // Garbage in auth header should give 401
    @Test
    void testBasicAuthBadSyntax() throws Exception {

        when(request.getHeader(AUTHENTICATION)).thenReturn("gobble gobble");
        int status = -1;
        ArgumentCaptor<Integer> captor = ArgumentCaptor.forClass(int.class);

        filter.doFilter(request, response, null);
        verify(response, times(1)).setStatus(captor.capture());
        assertEquals(401, captor.getValue());
    }

    // Invalid user in auth header should give 401
    @Test
    void testInvalidUser() throws Exception {

        when(request.getHeader(AUTHENTICATION)).thenReturn(encodedHeaderBad);
        int status = -1;
        ArgumentCaptor<Integer> captor = ArgumentCaptor.forClass(int.class);

        filter.doFilter(request, response, null);
        verify(response, times(1)).setStatus(captor.capture());
        assertEquals(403, captor.getValue());
    }

    // Full(?) end-to-end test.
    @Test
    void testGoodCodeShouldLogin() throws Exception {

        when(request.getHeader(AUTHENTICATION)).thenReturn(encodedHeaderGood);
        int status = -1;

        filter.doFilter(request, response, chain);
        // These assertions fail, alas, because getContentType returns
        // null, and getStatus returns 0):
        // assertEquals(FAKE_CONTENT_TYPE, response.getContentType());
        // assertEquals(200, response.getStatus());
        // Instead, our mock FilterChain contains an assert() test.
        System.out.println("Looks like a win!");
    }
}
