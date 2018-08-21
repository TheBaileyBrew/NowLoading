package com.thebaileybrew.nowloading.queryelements.upcquery;

public class upc {
    private final String upcCode;
    private final String upcGameName;
    private final String upcGameImage;
    private final String upcGameLowPrice;
    private final String upcGameQuantity;
    private final String upcGameSystem;
    private final String upcGameCondition;

    public upc (String upcCode, String upcGameName, String upcGameImage, String upcGameLowPrice, String upcGameQuantity, String upcGameSystem, String upcGameCondition) {
        this.upcCode = upcCode;
        this.upcGameName = upcGameName;
        this.upcGameImage = upcGameImage;
        this.upcGameLowPrice = upcGameLowPrice;
        this.upcGameQuantity = upcGameQuantity;
        this.upcGameSystem = upcGameSystem;
        this.upcGameCondition = upcGameCondition;
    }

    public String getUpcCode() {
        return upcCode;
    }
    public String getUpcGameName() {
        return upcGameName;
    }
    public String getUpcGameImage() {
        return upcGameImage;
    }
    public String getUpcGameLowPrice() {
        return upcGameLowPrice;
    }
    public String getUpcGameQuantity() {
        return upcGameQuantity;
    }
    public String getUpcGameSystem() {
        return upcGameSystem;
    }
    public String getUpcGameCondition() {
        return upcGameCondition;
    }
}
