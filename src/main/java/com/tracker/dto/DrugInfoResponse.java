package com.tracker.dto;

import lombok.Data;

@Data
public class DrugInfoResponse {
    private String brandName;
    private String genericName;
    private String activeIngredients;
    private String warnings;
    private String indicationsAndUsage;
}
