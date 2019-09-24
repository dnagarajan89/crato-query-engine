package com.cratosys.query.engine;

import com.cratosys.data.model.*;

import java.util.List;

public interface QuerySchema {
    Invoice invoiceByUrn(String urn);
    Client clientById(String clientId);
    List<Invoice> invoicesByClientId(String clientId);
    List<ClientEntity> entitiesByClientId(String clientId);
    List<ClientEntityVendor> vendorsByClientAndEntityId(String clientId, String entityId);
    DownloadURL downloadUrlByClientId(String clientId);
    List<ClientAccountingSegment> accountCodingByClientAndEntityId(String clientId, String entityId);
    List<Terms> termsByClientId(String clientId);
}
