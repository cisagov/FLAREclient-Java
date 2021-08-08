package gov.dhs.cisa.ctm.flare.client.api.domain.parameters;

/**
 * A simple contract for uploaded content
 */
public class UploadedFile {
    private String filename;
    private String content;
    private int hash;

    public UploadedFile() {
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getHash() {
        return hash;
    }

    public void setHash(int hash) {
        this.hash = hash;
    }
}
