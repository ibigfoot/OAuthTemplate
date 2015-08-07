package uk.co.force.documenter.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.force.documenter.OAuthConstants;

@WebServlet(
		name="HelloServlet",
		urlPatterns={"/hello/*"}
)
public class HelloServlet extends HttpServlet{

	private Logger logger;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		logger = LoggerFactory.getLogger(this.getClass());
	};
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String accessToken = (String)req.getSession().getAttribute(OAuthConstants.ACCESS_TOKEN);
		if(accessToken != null && !accessToken.isEmpty()) {
			logger.info("Have an active session");
			PrintWriter writer = resp.getWriter();
			writer.write("You have an access token ... use it wisely young jedi");
			writer.write(accessToken);
			writer.flush();
			writer.close();
		} else {
			resp.sendRedirect("/");
		}
	}

	
}
