package auth;

import java.io.IOException;
import java.util.Base64;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import com.darwinsys.security.DigestUtils;
import model.Person;
import org.postgresql.util.MD5Digest;

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

		String decoded =
				new String(Base64.getDecoder().decode(hdrs[1]));	// S/b user:pass

		var ident = decoded.split(":");

		if (ident.length != 2) {
			System.out.println("Auth Decode failed");
			response.setStatus(401);
			return;
		}
		String userName = ident[0],
			passwdClear = ident[1];
		System.out.printf("user is %s%n", userName);

		Person p;
		try {
			TypedQuery<Person> q = em.createQuery("SELECT Person u FROM Person WHERE u.name = ?", Person.class);
			q.setParameter(1, userName);
			p = q.getSingleResult();
		} catch (Exception e) {
			System.out.println("Person lookup failed: " + e);
			response.setStatus(403);
			return;
		}
		if (!p.passwdEncrypted.equals(DigestUtils.md5(passwdClear))) {
			System.out.println("Invalid password");
			response.setStatus(403);
			return;
		}

		// User is logged in, continue processing, override getRemoteUser in request object.
		System.out.printf("user is %s%n", userName);
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
