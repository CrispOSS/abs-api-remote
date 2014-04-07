package abs.api.remote;

import java.net.URI;
import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import abs.api.Actor;
import abs.api.Context;
import abs.api.Envelope;
import abs.api.FactoryLoader;
import abs.api.Reference;
import abs.api.SimpleEnvelope;

/**
 * @author Behrooz Nobakht
 * @since 1.0
 */
@Provider
@Path("actors")
public class ContextResource {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final Context context;
	private final URI uri;
	private final FactoryLoader factoryLoader = new FactoryLoader();

	private final Integer maxLocalActors;

	public ContextResource(Context context, URI uri, Integer maxLocalActors) {
		this.context = context;
		this.uri = uri;
		this.maxLocalActors = maxLocalActors;
	}

	@GET
	public Response get() {
		return Response.status(Status.OK).entity(context.notary().size()).build();
	}

	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	@POST
	public Response post(@FormParam("from") String from, @FormParam("to") String to,
			@FormParam("message") String message) {
		// try (ObjectInputStream ois = new ObjectInputStream(new
		// ByteArrayInputStream(message))) {
		try {
			// Object msg = ois.readObject();

			// The local context requires an actual actor.
			// We need to find the actor with this reference or it
			// should fail.
			Reference toReference = Reference.from(to);
			Actor receiver = (Actor) context.notary().identify(toReference);
			Envelope e = new SimpleEnvelope(Reference.from(from), receiver, message);
			context.router().route(e);
			// Ensure that the envelope is processed.
			e.response().get();
			return Response.status(Status.OK).build();
		} catch (Exception e) {
			logger.error("{}", e);
			return Response.status(Status.BAD_REQUEST).entity("Error: " + e.getMessage())
					.build();
		}
	}

	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	@PUT
	@Path("{name}")
	public Response create(@PathParam("name") String name, @QueryParam("class") String fqcn,
			Collection<String> params) {
		if (context.notary().size() >= maxLocalActors) {
			return Response.status(Status.NOT_ACCEPTABLE)
					.entity("Maximum local actors reached.").build();
		}
		try {
			final Object object = factoryLoader.create(fqcn, params.toArray(new String[] {}));
			final Actor actor = context.newActor(name, object);
			return Response.status(Status.CREATED).entity(actor.name().toString()).build();
		} catch (Exception e) {
			return Response
					.status(Status.BAD_REQUEST)
					.entity(e.getMessage()
							+ (e.getCause() != null ? e.getCause().getMessage() : "")).build();
		}
	}

}
