package com.cratosys.query.engine;

import com.cratosys.data.model.Client;
import com.cratosys.data.model.ClientEntity;
import com.cratosys.data.model.Invoice;
import com.cratosys.data.model.Vendor;
import graphql.schema.DataFetcher;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class GraphQLReactiveDataFetcher {

    private WebClient webClient;

    public GraphQLReactiveDataFetcher(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://www.cratosys.com").build();
    }

    public DataFetcher<CompletableFuture<Invoice>> getInvoiceByUrnFetcher() {
        return dataFetchingEnvironment -> {
            return webClient
                    .get()
                    .uri("/invoiceapi/invoice/{urn}",
                            dataFetchingEnvironment.getArgument("urn").toString()
                    )
                    .retrieve()
                    .bodyToMono(Invoice.class)
                    .toFuture();
        };
    }

    public DataFetcher<CompletableFuture<Client>> getClientById() {
        return dataFetchingEnvironment -> {
            return webClient
                    .get()
                    .uri("/clientapi/{clientId}",
                            dataFetchingEnvironment.getArgument("clientId").toString()
                    )
                    .retrieve()
                    .bodyToMono(Client.class)
                    .toFuture();
        };
    }

    public DataFetcher<CompletableFuture<List<Invoice>>> getInvoicesByClientId() {
        return dataFetchingEnvironment -> {
            return webClient
                    .get()
                    .uri("/invoiceapi/invoice/getall/{clientId}?activeStatus=ACTIVE",
                            dataFetchingEnvironment.getArgument("clientId").toString()
                    )
                    .retrieve()
                    .bodyToFlux(Invoice.class)
                    .collectList()
                    .toFuture();
        };
    }

    public DataFetcher<CompletableFuture<List<ClientEntity>>> getEntitiesByClientId() {
        return dataFetchingEnvironment -> {
            return webClient
                    .get()
                    .uri(
                            "/entityapi/{clientId}",
                            dataFetchingEnvironment.getArgument("clientId").toString()
                    )
                    .retrieve()
                    .bodyToFlux(ClientEntity.class)
                    .collectList()
                    .toFuture();
        };
    }

    public DataFetcher<CompletableFuture<List<Vendor>>> getVendorsByClientIdAndEntityId() {
        return dataFetchingEnvironment -> {
            return webClient
                    .get()
                    .uri("/vendorapi/all?clientId={clientId}&entityid={entityId}",
                            dataFetchingEnvironment.getArgument("clientId").toString(),
                            dataFetchingEnvironment.getArgument("entityId").toString()
                    )
                    .retrieve()
                    .bodyToFlux(Vendor.class)
                    .collectList()
                    .toFuture();
        };
    }

    public DataFetcher<CompletableFuture<DownloadURL>> getDownloadURLByClientId() {
        return dataFetchingEnvironment -> {
            return webClient
                    .get()
                    .uri("/invoiceapi/invoice/getdownloadurl?fileName=Invoice.pdf")
                    .header("clientId",  dataFetchingEnvironment.getArgument("clientId").toString())
                    .retrieve()
                    .bodyToMono(DownloadURL.class)
                    .toFuture();
        };
    }

}
