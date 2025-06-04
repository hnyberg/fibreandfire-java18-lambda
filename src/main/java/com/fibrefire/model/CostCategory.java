package com.fibrefire.model;

import java.util.Set;

public enum CostCategory {
    CREDIT_CARD,
    FOOD,
    RESTAURANT,
    ENTERTAINMENT,
    TRAVEL,
    RENT,
    MORTGAGE,
    LOANS,
    SAVINGS,
    CHARITY,
    UTILITIES,
    HOME,
    HEALTH,
    CLOTHES,
    TRANSFERS,
    OTHER;

    public static final Set<CostCategory> FIXED = Set.of(
            ENTERTAINMENT, TRAVEL, CHARITY, UTILITIES, HOME, HEALTH, CLOTHES, OTHER);
    public static final Set<CostCategory> FOODS = Set.of(FOOD, RESTAURANT);
}
