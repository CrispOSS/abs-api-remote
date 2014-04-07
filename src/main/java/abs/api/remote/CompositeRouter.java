package abs.api.remote;

import java.net.URI;

import abs.api.Context;
import abs.api.Envelope;
import abs.api.Reference;
import abs.api.Router;

/**
 * @author Behrooz Nobakht
 * @since 1.0
 */
public class CompositeRouter implements Router {

	private final URI uri;
	private final Router localRouter;
	private final Router remoteRouter;

	public CompositeRouter(URI uri, Router localRouter, Router remoteRouter) {
		this.uri = uri;
		this.localRouter = localRouter;
		this.remoteRouter = remoteRouter;
	}

	@Override
	public void route(Envelope envelope) {
		if (isLocal(envelope.to())) {
			localRouter.route(envelope);
		} else {
			remoteRouter.route(envelope);
		}
	}

	@Override
	public void bind(Context context) {
	}

	protected boolean isLocal(Reference ref) {
		String uri = ref.name().toASCIIString();
		return uri.indexOf('@') == -1 || uri.contains(this.uri.toASCIIString());
	}
}
