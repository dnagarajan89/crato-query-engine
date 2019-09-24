package com.cratosys.query.engine;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.zhokhov.graphql.datetime.GraphQLLocalDateTime;
import graphql.GraphQL;
import graphql.execution.AsyncExecutionStrategy;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URL;

@Component
public class GraphQLProvider {

    private GraphQL graphQL;

    @Bean
    public GraphQL graphQL() {
        return graphQL;
    }

    @PostConstruct
    public void init() throws IOException {
        URL url = Resources.getResource("schemagen.graphqls");
        String sdl = Resources.toString(url, Charsets.UTF_8);
        GraphQLSchema graphQLSchema = buildSchema(sdl);
        this.graphQL = GraphQL.newGraphQL(graphQLSchema).queryExecutionStrategy(new AsyncExecutionStrategy()).build();
    }

    @Autowired
    // private GraphQLReactiveDataFetcher graphQLDataFetchers;
    private GraphQLDataFetchers graphQLDataFetchers;
    // private GraphQLNonReactiveDataFetcher graphQLDataFetchers

    private GraphQLSchema buildSchema(String sdl) {
        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(sdl);
        RuntimeWiring runtimeWiring = buildWiring();
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
    }

    private RuntimeWiring buildWiring() {
        return RuntimeWiring
                .newRuntimeWiring()
                .scalar(new GraphQLLocalDateTime())
                .type(
                        TypeRuntimeWiring.newTypeWiring("Query")
                                .dataFetcher("invoiceByUrn", graphQLDataFetchers.getInvoiceByUrnFetcher())
                                .dataFetcher("clientById", graphQLDataFetchers.getClientById())
                                .dataFetcher("invoicesByClientId", graphQLDataFetchers.getInvoicesByClientId())
                                .dataFetcher("entitiesByClientId", graphQLDataFetchers.getEntitiesByClientId())
                                .dataFetcher("vendorsByClientAndEntityId", graphQLDataFetchers.getVendorsByClientIdAndEntityId())
                                .dataFetcher("downloadUrlByClientId", graphQLDataFetchers.getDownloadURLByClientId())
                                .dataFetcher("accountCodingByClientAndEntityId", graphQLDataFetchers.getAccountCodingByClientAndEntityId())
                                .dataFetcher("termsByClientId", graphQLDataFetchers.getTermsByClientId())
                )
                .build();
    }

}
