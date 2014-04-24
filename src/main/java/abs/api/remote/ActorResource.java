package abs.api.remote;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import abs.api.Actor;
import abs.api.Context;
import abs.api.Envelope;
import abs.api.Reference;
import abs.api.SimpleEnvelope;

/**
 * @author Behrooz Nobakht
 */
@Provider
public class ActorResource {

	private final Context context;
	private final Actor actor;

	public ActorResource(Context context, Actor actor) {
		this.context = context;
		this.actor = actor;
	}

	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("{from}")
	@PUT
	public Response send(@PathParam("from") String from, InputStream msg) {
		try {
			Reference sender = Reference.decode(from);
			Object message = readObject(msg);
			Envelope e = new SimpleEnvelope(sender, actor, message);
			context.router().route(e);
			return Response.ok(sender.toString(), MediaType.TEXT_PLAIN).build();
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}

	protected Object readObject(InputStream is) throws IOException, ClassNotFoundException {
		try (ObjectInputStream ois = new ObjectInputStream(is)) {
			return ois.readObject();
		}
	}

}
