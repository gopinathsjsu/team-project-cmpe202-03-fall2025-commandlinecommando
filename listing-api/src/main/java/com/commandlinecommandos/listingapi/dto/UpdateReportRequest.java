package com.commandlinecommandos.listingapi.dto;

import com.commandlinecommandos.listingapi.model.ReportType;

public class UpdateReportRequest {
    private ReportType reportType;
    private String description;

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

