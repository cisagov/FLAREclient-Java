package gov.dhs.cisa.ctm.flare.client.api.domain.content;

import java.util.Objects;

public class CountResult {

    private int contentCount;

    private int contentDuplicate;

    private int contentSaved;

    public CountResult(int contentCount, int contentDuplicate, int contentSaved) {
        this.contentCount = contentCount;
        this.contentDuplicate = contentDuplicate;
        this.contentSaved = contentSaved;
    }

    public CountResult() {
        this.contentCount = 0;
        this.contentDuplicate = 0;
        this.contentSaved = 0;
    }

    public void add(CountResult countResult) {
        this.contentCount += countResult.getContentCount();
        this.contentDuplicate += countResult.getContentDuplicate();
        this.contentSaved += countResult.getContentSaved();
    }

    public int getContentCount() {
        return contentCount;
    }

    public void setContentCount(int contentCount) {
        this.contentCount = contentCount;
    }

    public int getContentDuplicate() {
        return contentDuplicate;
    }

    public void setContentDuplicate(int contentDuplicate) {
        this.contentDuplicate = contentDuplicate;
    }

    public int getContentSaved() {
        return contentSaved;
    }

    public void setContentSaved(int contentSaved) {
        this.contentSaved = contentSaved;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CountResult that = (CountResult) o;
        return contentCount == that.contentCount &&
            contentDuplicate == that.contentDuplicate &&
            contentSaved == that.contentSaved;
    }

    @Override
    public int hashCode() {

        return Objects.hash(contentCount, contentDuplicate, contentSaved);
    }
}
