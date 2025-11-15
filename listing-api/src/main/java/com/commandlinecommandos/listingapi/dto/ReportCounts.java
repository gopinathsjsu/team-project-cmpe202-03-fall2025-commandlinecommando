package com.commandlinecommandos.listingapi.dto;

public class ReportCounts {
    private Long pending;
    private Long underReview;
    private Long resolved;
    private Long dismissed;

    public ReportCounts(Long pending, Long underReview, Long resolved, Long dismissed) {
        this.pending = pending;
        this.underReview = underReview;
        this.resolved = resolved;
        this.dismissed = dismissed;
    }

    public Long getPending() {
        return pending;
    }

    public void setPending(Long pending) {
        this.pending = pending;
    }

    public Long getUnderReview() {
        return underReview;
    }

    public void setUnderReview(Long underReview) {
        this.underReview = underReview;
    }

    public Long getResolved() {
        return resolved;
    }

    public void setResolved(Long resolved) {
        this.resolved = resolved;
    }

    public Long getDismissed() {
        return dismissed;
    }

    public void setDismissed(Long dismissed) {
        this.dismissed = dismissed;
    }
}

