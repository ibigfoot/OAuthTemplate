package uk.co.force.documenter.servlet;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.force.documenter.OAuthConstants;

@WebServlet(
		name = "OAuth",
		urlPatterns = {"/oauth*", "/oauth/*"},
		initParams = {
			//clientId is 'Consumer Key', clientSecret is 'Consumer Secret' 
			@WebInitParam(name = "clientId", value="3MVG9Rd3qC6oMalVL_ZWet4ueYzdRKSsAR2MTYVXhS7baMsQsh.xAAfs.uaeawxHxypyh7ON256A0UXkDZ4Sc"),
			@WebInitParam(name="clientSecret", value="4538587340034276718"),
			// must match exactly Callback URL
			@WebInitParam(name="redirectUri", value="http://localhost:8080/oauth/_callback"),
		}
)
public class OAuth extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private Logger logger;
	private String clientId = null;
	private String clientSecret = null;
	private String redirectUri = null;
	private String authUrl = null;
	private String tokenUrl = null;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
		super.init(config);
		
		logger = LoggerFactory.getLogger(this.getClass());
		
		clientId = System.getenv("clientId");
		clientSecret = System.getenv("clientSecret");
		redirectUri = System.getenv("redirectUri");
		
		if(clientId == null || clientSecret == null || redirectUri == null) {
			logger.warn("Using local configuration from Servlet Parameter, couldn't find necessary env variables clientId[{}] clientSecret[{}] or redirectUri[{}]", clientId, clientSecret, redirectUri);
			clientId = this.getInitParameter("clientId");
			clientSecret = this.getInitParameter("clientSecret");
			redirectUri = this.getInitParameter("redirectUri");
		}
		
		logger.info("Initialised with ID [{}] Secret [{}] Redirect [{}]", clientId, clientSecret, redirectUri);		
	}
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String accessToken = (String)request.getSession().getAttribute(OAuthConstants.ACCESS_TOKEN);
		logger.info("OAuth Servlet request URL [{}]", request.getRequestURL());
		if(accessToken == null) {
			String instanceUrl = null;
			
			if(request.getRequestURI().endsWith("_callback")) {
				// we have a successful auth?
				String code = request.getParameter(OAuthConstants.CODE);
				tokenUrl = "https://" + URLDecoder.decode(request.getParameter(OAuthConstants.STATE),"UTF-8") + "/services/oauth2/token";
				CloseableHttpClient client = HttpClients.createDefault();
				try {
					
					RequestBuilder postBuilder = RequestBuilder.create(HttpPost.METHOD_NAME);
					
					postBuilder.addParameter(OAuthConstants.CODE, code);
					postBuilder.addParameter(OAuthConstants.GRANT_TYPE, "authorization_code");
					postBuilder.addParameter(OAuthConstants.CLIENT_ID, clientId);
					postBuilder.addParameter(OAuthConstants.CLIENT_SECRET, clientSecret);
					postBuilder.addParameter(OAuthConstants.REDIRECT_URI, redirectUri);
					
					postBuilder.setUri(tokenUrl);
		
					logger.info("Send to token URL [{}]",tokenUrl);

					CloseableHttpResponse res = client.execute(postBuilder.build());
					logger.info("STATUS [{}]", res.getStatusLine());
					
					HttpEntity entity = res.getEntity();
					JSONObject jobj = new JSONObject(new JSONTokener(entity.getContent()));
					EntityUtils.consume(entity);
					
					// we would probably do something more meaningful than this... 
					logger.info(jobj.toString());
					accessToken = jobj.getString(OAuthConstants.ACCESS_TOKEN);
				} finally {
					client.close();
				}				
			} else {
				String environment =  request.getParameter("env"); // get SF env from request
				/*
				 * This is the Web Server OAuth Authentication Flow
				 * https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/intro_understanding_web_server_oauth_flow.htm
				 * 
				 * optional param State added to keep login environment for us
				 */
				authUrl = "https://" + environment + "/services/oauth2/authorize?"
							+ OAuthConstants.RESPONSE_TYPE + "=code&"
							+ OAuthConstants.CLIENT_ID + "=" + clientId 
							+ "&" + OAuthConstants.REDIRECT_URI + "=" + URLEncoder.encode(redirectUri,"UTF-8") 
							+ "&" + OAuthConstants.STATE + "=" + URLEncoder.encode(environment, "UTF-8");
				
				logger.info("Redirecting [{}]", authUrl);
				response.sendRedirect(authUrl);
				return;
			}
			request.getSession().setAttribute(OAuthConstants.ACCESS_TOKEN, accessToken);
		} else {
			// we have a sesson already
			logger.info("We have an auth token already");
		}

		response.sendRedirect("/hello");
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		throw new ServletException("POST access not supported");
	}

}
