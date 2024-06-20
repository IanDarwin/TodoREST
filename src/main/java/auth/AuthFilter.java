package auth;

import java.io.IOException;
import java.util.Base64;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import com.darwinsys.security.DigestUtils;
import model.User;

/**
 * This is the Servlet Filter for the login mechanism.
 */
public class AuthFilter implements Filter {

	@Inject
	EntityManager em;

	public void init(FilterConfig config) throws ServletException {
		System.out.println("AuthFilter.init()");
	}

	/** Allow the request if the user is logged in.
	 * Called before every request, so keep it light weight.
	 */
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {

		System.out.println("AuthFilter.doFilter");

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;

		var hdr = request.getHeader("Authentication");
		if (hdr == null) {
			System.out.println("Auth Header Missing");
			response.setStatus(403);	// Auth needed
			return;
		}

		var hdrs = hdr.split("\\s+"); // s/b Basic XXXXXXXXX
		if (!"Basic".equalsIgnoreCase(hdrs[0])) {
			System.out.println("Auth Decode - not BASIC AUTH");
			response.setStatus(401);	// Invalid
			return;
		}

		String decoded = 	// Should be "user:pass"
				new String(Base64.getDecoder().decode(hdrs[1]));

		var ident = decoded.split(":");

		if (ident.length != 2) {
			System.out.println("Auth Decode failed");
			response.setStatus(401);
			return;
		}
		String userName = ident[0],
			passwdClear = ident[1];
		System.out.printf("user is %s%n", userName);

		User person;
		try {
			TypedQuery<User> q =
					em.createQuery("SELECT User u FROM User WHERE u.name = ?", User.class);
			q.setParameter(1, userName);
			person = q.getSingleResult();
		} catch (Exception e) {
			System.out.println("Person lookup failed: " + e);
			response.setStatus(403);
			return;
		}
		if (!person.passwdEncrypted.equals(DigestUtils.md5(passwdClear))) {
			System.out.println("Invalid password");
			response.setStatus(403);
			return;
		}

		// User is logged in, continue processing, pass logged-in user via
		// overriding getRemoteUser with custom Request object.
		System.out.printf("Logged-in user is %s%n", userName);
		chain.doFilter(new AuthFilterHTTPServletRequest((HttpServletRequest) req, userName), resp);
	}

	public void destroy() {
		System.out.println("AuthFilter.destroy()");
	}

	/**
	 * Delegating HTTPServletRequest that just overrides a few methods such as getRemoteUser(), since
	 * we are handling our own login... and since the REST services need to know the login name
	 */
	static class AuthFilterHTTPServletRequest extends HttpServletRequestWrapper {

		private String userName;

		public AuthFilterHTTPServletRequest(HttpServletRequest request, String userName) {
			super(request);
			this.userName = userName;
		}

		@Override
		public String getRemoteUser() {
			return this.userName;
		}
	}
}
