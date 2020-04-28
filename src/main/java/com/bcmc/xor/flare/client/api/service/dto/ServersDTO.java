package com.bcmc.xor.flare.client.api.service.dto;


import com.bcmc.xor.flare.client.api.domain.server.TaxiiServer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@SuppressWarnings("unused")
public class ServersDTO {
    private HashMap<String, ServerDTO> byLabel = new HashMap<>();
    private HashSet<String> allIds = new HashSet<>();

    public ServersDTO(List<TaxiiServer> serversSet) {
        for (TaxiiServer server: serversSet) {
            ServerDTO serverDTO = new ServerDTO(server);
            this.byLabel.put(server.getLabel(), serverDTO);
            this.allIds.add(serverDTO.getId());
        }
    }

    public HashMap<String, ServerDTO> getByLabel() {
        return byLabel;
    }

    public void setByLabel(HashMap<String, ServerDTO> byLabel) {
        this.byLabel = byLabel;
    }

    public HashSet<String> getAllIds() {
        return allIds;
    }

    public void setAllIds(HashSet<String> allIds) {
        this.allIds = allIds;
    }
}
