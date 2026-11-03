
package com.payflow.model;

public class AppSettings {

    private String companyName;
    private String companyAddress;
    private String companyPhone;
    private String companyEmail;

    private String currency;
    private int workingDays;
    private double defaultTaxRate;
    private double defaultSsnitRate;

    public AppSettings() {
    }

    public AppSettings(
            String companyName,
            String companyAddress,
            String companyPhone,
            String companyEmail,
            String currency,
            int workingDays,
            double defaultTaxRate,
            double defaultSsnitRate) {

        this.companyName = companyName;
        this.companyAddress = companyAddress;
        this.companyPhone = companyPhone;
        this.companyEmail = companyEmail;
        this.currency = currency;
        this.workingDays = workingDays;
        this.defaultTaxRate = defaultTaxRate;
        this.defaultSsnitRate = defaultSsnitRate;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyAddress() {
        return companyAddress;
    }

    public void setCompanyAddress(String companyAddress) {
        this.companyAddress = companyAddress;
    }

    public String getCompanyPhone() {
        return companyPhone;
    }

    public void setCompanyPhone(String companyPhone) {
        this.companyPhone = companyPhone;
    }

    public String getCompanyEmail() {
        return companyEmail;
    }

    public void setCompanyEmail(String companyEmail) {
        this.companyEmail = companyEmail;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public int getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(int workingDays) {
        this.workingDays = workingDays;
    }

    public double getDefaultTaxRate() {
        return defaultTaxRate;
    }

    public void setDefaultTaxRate(double defaultTaxRate) {
        this.defaultTaxRate = defaultTaxRate;
    }

    public double getDefaultSsnitRate() {
        return defaultSsnitRate;
    }

    public void setDefaultSsnitRate(double defaultSsnitRate) {
        this.defaultSsnitRate = defaultSsnitRate;
    }
}

