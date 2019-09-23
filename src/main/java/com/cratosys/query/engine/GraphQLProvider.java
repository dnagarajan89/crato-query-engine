package com.cratosys.query.engine;

import java.io.IOException;
import java.net.URL;

import javax.annotation.PostConstruct;


import graphql.schema.idl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import graphql.GraphQL;
import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLSchema;
import org.springframework.stereotype.Component;

@Component
public class GraphQLProvider {

    private GraphQL graphQL;

    @Bean
    public GraphQL graphQL() {
        return graphQL;
    }

    @PostConstruct
    public void init() throws IOException {
        URL url = Resources.getResource("schema.graphqls");
        String sdl = Resources.toString(url, Charsets.UTF_8);
        GraphQLSchema graphQLSchema = buildSchema(sdl);
        this.graphQL = GraphQL.newGraphQL(graphQLSchema).build();
    }

    @Autowired
    GraphQLReactiveDataFetcher graphQLDataFetchers;

    private GraphQLSchema buildSchema(String sdl) {
        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(sdl);
        RuntimeWiring runtimeWiring = buildWiring();
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
    }

    private RuntimeWiring buildWiring() {
        return RuntimeWiring
                .newRuntimeWiring()
                .scalar(ExtendedScalars.DateTime)
                .type(
                    TypeRuntimeWiring.newTypeWiring("Query")
                        .dataFetcher("invoiceByUrn", graphQLDataFetchers.getInvoiceByUrnFetcher())
                        .dataFetcher("clientById", graphQLDataFetchers.getClientById())
                        .dataFetcher("invoicesByClientId", graphQLDataFetchers.getInvoicesByClientId())
                        .dataFetcher("entitiesByClientId", graphQLDataFetchers.getEntitiesByClientId())
                        .dataFetcher("vendorsByClientAndEntityId", graphQLDataFetchers.getVendorsByClientIdAndEntityId())
                        .dataFetcher("downloadUrlByClientId", graphQLDataFetchers.getDownloadURLByClientId())
                )
                .build();
    }

}
