package io.katharsis.rs;

import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.katharsis.repository.ResourceRepositoryV2;
import io.katharsis.repository.information.RepositoryAction;
import io.katharsis.repository.information.RepositoryAction.RepositoryActionType;
import io.katharsis.repository.information.RepositoryInformationBuilderContext;
import io.katharsis.repository.information.ResourceRepositoryInformation;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.resource.information.AnnotationResourceInformationBuilder;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.rs.internal.JaxrsModule.JaxrsResourceRepositoryInformationBuilder;
import io.katharsis.rs.resource.model.Task;

public class JaxRsModuleTest {

	private JaxrsResourceRepositoryInformationBuilder builder;

	private RepositoryInformationBuilderContext context;

	@Before
	public void setup() {
		builder = new JaxrsResourceRepositoryInformationBuilder();
		final ResourceInformationBuilder resourceInformationBuilder = new AnnotationResourceInformationBuilder(
				new ResourceFieldNameTransformer());
		context = new RepositoryInformationBuilderContext() {

			@Override
			public ResourceInformationBuilder getResourceInformationBuilder() {
				return resourceInformationBuilder;
			}
		};
	}

	@Test
	public void testActionDetection() {
		ResourceRepositoryInformation information = (ResourceRepositoryInformation) builder.build(ScheduleRepository.class,
				context);
		Map<String, RepositoryAction> actions = information.getActions();
		Assert.assertEquals(5, actions.size());
		RepositoryAction action = actions.get("repositoryAction");
		Assert.assertNotNull(actions.get("repositoryPostAction"));
		Assert.assertNotNull(actions.get("repositoryDeleteAction"));
		Assert.assertNotNull(actions.get("repositoryPutAction"));
		Assert.assertNull(actions.get("notAnAction"));
		Assert.assertNotNull(action);
		Assert.assertEquals("repositoryAction", action.getName());
		Assert.assertEquals(RepositoryActionType.REPOSITORY, action.getActionType());
		Assert.assertEquals(RepositoryActionType.RESOURCE, actions.get("resourceAction").getActionType());
	}

	@Test(expected = IllegalStateException.class)
	public void testInvalidRootPathRepository() {
		builder.build(InvalidRootPathRepository.class, context);
	}

	@Test(expected = IllegalStateException.class)
	public void testInvalidIdPathRepository1() {
		builder.build(InvalidIdPathRepository1.class, context);
	}

	@Test(expected = IllegalStateException.class)
	public void testInvalidIdPathRepository2() {
		builder.build(InvalidIdPathRepository2.class, context);
	}

	@Path("schedules")
	public interface ScheduleRepository extends ResourceRepositoryV2<Task, Long> {

		@GET
		@Path("repositoryAction")
		public String repositoryAction(@QueryParam(value = "msg") String msg);

		@POST
		@Path("repositoryPostAction")
		public String repositoryPostAction();

		@DELETE
		@Path("repositoryDeleteAction")
		public String repositoryDeleteAction();

		@PUT
		@Path("repositoryPutAction")
		public String repositoryPutAction();

		@GET
		@Path("{id}/resourceAction")
		public String resourceAction(@PathParam("id") long id, @QueryParam(value = "msg") String msg);

	}

	@Path("schedules")
	public interface InvalidRootPathRepository extends ResourceRepositoryV2<Task, Long> {

		@GET
		@Path("")
		public String resourceAction();

	}

	@Path("schedules")
	public interface InvalidIdPathRepository1 extends ResourceRepositoryV2<Task, Long> {

		@GET
		@Path("{something}/test")
		public String resourceAction();

	}

	@Path("schedules")
	public interface InvalidIdPathRepository2 extends ResourceRepositoryV2<Task, Long> {

		@GET
		@Path("{id}")
		public String resourceAction();

	}

}