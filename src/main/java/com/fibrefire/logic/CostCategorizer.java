package com.fibrefire.logic;

import com.fibrefire.model.CostCategory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CostCategorizer {
    public static CostCategory getCostCategory(String description) {
        for (Map.Entry<CostCategory, Set<String>> entry : keywords.entrySet()) {
            for (String keyWord : entry.getValue()) {
                if (description.contains(keyWord)) {
                    return entry.getKey();
                }
            }
        }
        return CostCategory.OTHER;
    }

    private final static Map<CostCategory, Set<String>> keywords = new HashMap<>() {{
        put(CostCategory.FOOD, Set.of("coop", "ica", "orien", "lidl", "olskroken", "hemkop", "willys", "frukt",
                "matsmart", "ost anders", "herbs", "saigon", "ceylon"));
        put(CostCategory.RESTAURANT, Set.of(
                "mcdonald", "mackbaren", "smakia", "4 gott", "sushi", "elite", "tgtg", "cykelfiket", "the place",
                "pallazo", "cederleufs", "oliven", "riber", "smorgas", "system", "bbq", "kaffe", "läsk", "cupcake",
                "nojds", "yili", "max burgers", "mugg tilltugg", "evolushi", "skatasmale", "fika", "ringo cafeterian",
                "elis corner", "k*tva feta g", "k*mopar cafe", "espresso house", "portens", "aldardo", "kaifo", "koizen",
                "baguetten", "yaki-da", "comptoir", "adriano", "salut", "husman", "kondit", "nobelgrillen", "beas"));
        put(CostCategory.LOANS, Set.of("csn"));
        put(CostCategory.SAVINGS, Set.of("avanza bank", "sigmastocks", "isk", "nödspar", "9025.52.710.52",
                "9060.71.378.35", "9060.13.806.81", "90299542035", "levler", "nödkonto"));
        put(CostCategory.MORTGAGE, Set.of("inbetalning"));
        put(CostCategory.RENT, Set.of("hsb"));
        put(CostCategory.CHARITY, Set.of("frizon", "wikipedia", "goclimate", "soma", "sv natur", "greenpeace", "rfsu"));
        put(CostCategory.TRAVEL, Set.of("vasttrafik", "sj", "spar resa", "spar cykel", "pressbyran", "k*zettle_*va"));
        put(CostCategory.ENTERTAINMENT, Set.of("roy", "filmstad", "aws", "pocket shop", "kjell & co", "kamaji", "fogg",
                "steam", "blizzard", "inet"));
        put(CostCategory.HEALTH, Set.of("ouraring", "lager", "stadium", "frisktand", "narhalsan",
                "spar gym", "orgryte", "nw goteborg"));
        put(CostCategory.HOME, Set.of("hornbach", "elboden", "taklampor", "teknos", "klint", "blomsterlandet",
                "ikea online", "spar renovering", "bagaren", "rosalina", "clas ohlson", "ikea"));
        put(CostCategory.UTILITIES, Set.of("hallon", "bredband", "energi", "lf sak", "bekväma vardagen", "akad.a"));
    }};
}
