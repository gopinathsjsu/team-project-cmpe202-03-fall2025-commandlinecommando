package com.commandlinecommandos.listingapi.dto;

import com.commandlinecommandos.listingapi.model.Category;
import com.commandlinecommandos.listingapi.model.ItemCondition;
import java.math.BigDecimal;

public class CreateListingRequest {
    private String title;
    private String description;
    private BigDecimal price;
    private Category category;
    private ItemCondition condition;
    private String location;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
    
    public ItemCondition getCondition() {
        return condition;
    }

    public void setCondition(ItemCondition condition) {
        this.condition = condition;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}

