package net.quarkify;

import graphql.GraphQL;
import graphql.schema.*;
import graphql.schema.idl.*;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.graphql.*;
import net.quarkify.data.*;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class GraphQlConfig {
    public static final List<Team> TEAMS = new ArrayList<>() {{
        add(new Team(1L, "Programmers",
                new User(1L, "Dmytro"),
                new User(2L, "Alex")
        ));
        add(new Team(2L, "Machine Learning",
                new User(3L, "Andrew NG")
        ));
    }};

    public void init(@Observes Router router) throws Exception {
        router.route("/graphql").blockingHandler(GraphQLHandler.create(createGraphQL()));
    }

    private GraphQL createGraphQL() throws Exception {
        TypeDefinitionRegistry teamsSchema = getTeamSchema();

        RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring()
                .type("Query",
                        builder -> builder.dataFetcher("allTeams", new VertxDataFetcher<>(this::getAllTeams))
                ).build();

        SchemaGenerator schemaGenerator = new SchemaGenerator();
        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(teamsSchema, runtimeWiring);

        return GraphQL.newGraphQL(graphQLSchema).build();
    }

    private TypeDefinitionRegistry getTeamSchema() throws Exception {
        final URL resource = this.getClass().getClassLoader().getResource("teams.graphql");
        String schema = String.join("\n", Files.readAllLines(Paths.get(resource.toURI())));
        return new SchemaParser().parse(schema);
    }

    private void getAllTeams(DataFetchingEnvironment env, Promise<List<Team>> future) {
        final String excluding = env.getArgument("excluding");
        future.complete(
                TEAMS.stream()
                        .filter(it -> !it.name.equals(excluding))
                        .collect(Collectors.toList())
        );
    }

}