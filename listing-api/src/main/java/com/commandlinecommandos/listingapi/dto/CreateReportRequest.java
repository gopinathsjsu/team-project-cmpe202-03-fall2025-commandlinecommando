package com.commandlinecommandos.listingapi.dto;

import com.commandlinecommandos.listingapi.model.ReportType;

public class CreateReportRequest {
    private Long listingId;
    private ReportType reportType;
    private String description;

    public Long getListingId() {
        return listingId;
    }

    public void setListingId(Long listingId) {
        this.listingId = listingId;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

