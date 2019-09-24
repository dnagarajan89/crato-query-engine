package com.cratosys.query.engine;

import com.cratosys.data.model.*;
import graphql.schema.DataFetcher;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.supplyAsync;

@Component
public class GraphQLDataFetchers {

    private RestTemplate restTemplate;

    public GraphQLDataFetchers(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public DataFetcher<CompletableFuture<Invoice>> getInvoiceByUrnFetcher() {
        return dataFetchingEnvironment -> supplyAsync(() -> {
            return restTemplate.getForObject(
                    "https://www.cratosys.com/invoiceapi/invoice/{urn}",
                    Invoice.class,
                    dataFetchingEnvironment.getArgument("urn").toString()
            );
        });
    }

    public DataFetcher<CompletableFuture<Client>> getClientById() {
        return dataFetchingEnvironment -> supplyAsync(() -> {
            return restTemplate.getForObject(
                    "https://www.cratosys.com/clientapi/{clientId}",
                    Client.class,
                    dataFetchingEnvironment.getArgument("clientId").toString()
            );
        });
    }

    public DataFetcher<CompletableFuture<List<Invoice>>> getInvoicesByClientId() {
        return dataFetchingEnvironment -> supplyAsync(() -> {
            ResponseEntity<List<Invoice>> responseEntity = restTemplate.exchange(
                    "https://www.cratosys.com/invoiceapi/invoice/getall/{clientId}?activeStatus=ACTIVE",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Invoice>>() {
                    },
                    dataFetchingEnvironment.getArgument("clientId").toString());
            return responseEntity.getBody();
        });
    }

    public DataFetcher<CompletableFuture<List<ClientEntity>>> getEntitiesByClientId() {
        return dataFetchingEnvironment -> supplyAsync(() -> {
            ResponseEntity<List<ClientEntity>> responseEntity = restTemplate.exchange(
                    "https://www.cratosys.com/entityapi/{clientId}",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<ClientEntity>>() {
                    },
                    dataFetchingEnvironment.getArgument("clientId").toString()
            );
            return responseEntity.getBody();
        });
    }

    public DataFetcher<CompletableFuture<List<ClientEntityVendor>>> getVendorsByClientIdAndEntityId() {
        return dataFetchingEnvironment -> supplyAsync(() -> {
            ResponseEntity<List<ClientEntityVendor>> responseEntity = restTemplate.exchange(
                    "https://www.cratosys.com/vendorapi/all?clientId={clientId}&entityid={entityId}",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<ClientEntityVendor>>() {
                    },
                    dataFetchingEnvironment.getArgument("clientId").toString(),
                    dataFetchingEnvironment.getArgument("entityId").toString()
            );
            return responseEntity.getBody();
        });
    }

    public DataFetcher<CompletableFuture<DownloadURL>> getDownloadURLByClientId() {
        return dataFetchingEnvironment -> supplyAsync(() -> {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("clientId", dataFetchingEnvironment.getArgument("clientId").toString());
            HttpEntity<String> entity = new HttpEntity<>("", httpHeaders);
            ResponseEntity<DownloadURL> responseEntity = restTemplate.exchange("https://www.cratosys.com/invoiceapi/invoice/getdownloadurl?fileName=Invoice.pdf",
                    HttpMethod.GET,
                    entity,
                    DownloadURL.class
            );
            return responseEntity.getBody();
        });
    }

    public DataFetcher<CompletableFuture<List<ClientAccountingSegment>>> getAccountCodingByClientAndEntityId() {
        return dataFetchingEnvironment -> supplyAsync(() -> {
            ResponseEntity<List<ClientAccountingSegment>> responseEntity = restTemplate.exchange(
                    "https://www.cratosys.com/accountcodingapi?clientEntityId={entityId}&clientId={clientId}",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<ClientAccountingSegment>>() {
                    },
                    dataFetchingEnvironment.getArgument("entityId").toString(),
                    dataFetchingEnvironment.getArgument("clientId").toString()
            );
            return responseEntity.getBody();
        });
    }

    public DataFetcher<CompletableFuture<List<Terms>>> getTermsByClientId() {
        return dataFetchingEnvironment -> supplyAsync(() -> {
            ResponseEntity<List<Terms>> responseEntity = restTemplate.exchange(
                    "https://www.cratosys.com/clientapi/{clientId}/terms",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Terms>>() {
                    },
                    dataFetchingEnvironment.getArgument("clientId").toString()
            );
            return responseEntity.getBody();
        });
    }

}
