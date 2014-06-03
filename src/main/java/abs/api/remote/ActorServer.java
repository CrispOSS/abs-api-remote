package abs.api.remote;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import abs.api.Context;
import abs.api.Lifecycle;

/**
 * @author Behrooz Nobakht
 */
public class ActorServer implements Lifecycle {

	static {
		LoggingConfiguration.configure();
	}

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final String host;
	private final Integer port;
	private final URI uri;
	private final ContextApplication application;
	private final Server server;

	public final Context context;

	public ActorServer(Properties properties) throws UnknownHostException {
		host = properties
				.getProperty("host", InetAddress.getLocalHost().getCanonicalHostName());
		port = Integer.valueOf(properties.getProperty("port", "7777"));
		uri = UriBuilder.fromUri("http://" + host).port(port).build();
		application = new ContextApplication(uri);
		ResourceConfig resourceConfig = ResourceConfig.forApplication(application);
		server = JettyHttpContainerFactory.createServer(uri, resourceConfig, true);
		context = application.context;
		logger.info("ABS Actor Context started on: {}", uri);
	}

	@Override
	public void start() throws Exception {
	}

	@Override
	public void stop() throws Exception {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					server.stop();
				} catch (Exception e) {
					logger.error("Failed to stop server on {}: {}", uri, e);
				}
			}
		}).start();
	}

	@Override
	public void initialize() throws Exception {
	}

	public static void main(String[] args) throws UnknownHostException {
		new ActorServer(System.getProperties());
	}

}
