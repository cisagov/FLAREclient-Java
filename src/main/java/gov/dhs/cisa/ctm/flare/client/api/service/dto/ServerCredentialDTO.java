package gov.dhs.cisa.ctm.flare.client.api.service.dto;

public class ServerCredentialDTO {

    private String serverLabel;
    private String username;
    private String password;

    public ServerCredentialDTO() {
    }

    public ServerCredentialDTO(String serverLabel, String username, String password) {
        this.serverLabel = serverLabel;
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    @SuppressWarnings("unused")
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @SuppressWarnings("unused")
    public String getServerLabel() {
        return serverLabel;
    }

    public void setServerLabel(String serverLabel) {
        this.serverLabel = serverLabel;
    }
}
