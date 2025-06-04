package com.fibrefire.logic;

import com.fibrefire.model.CostCategory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CostCategorizer {
    public static CostCategory getCostCategory(String description, String paymentType, String date, float cost) {
        for (Map.Entry<CostCategory, Set<String>> entry : keywords.entrySet()) {
            for (String keyWord : entry.getValue()) {
                if (description.contains(keyWord) || paymentType.contains(keyWord)) {
                    return entry.getKey();
                }
            }
        }
        return CostCategory.OTHER;
    }

    private final static Map<CostCategory, Set<String>> keywords = new HashMap<>() {{
        put(CostCategory.CHARITY, Set.of("frizon", "wikipedia", "goclimate", "soma", "sv natur", "greenpeace", "rfsu",
                "flowy", "mensa", "aktiesparare", "ingenj", "guardian news", "världsnatur", "kristdemokraterna", "insamlingsstiftelsen"));
        put(CostCategory.FOOD, Set.of("coop", " ica", "orien", "lidl", "olskroken", "hemkop", "willys", "frukt",
                "matsmart", "ost anders", "herbs", "saigon", "ceylon", "ägg", "indian", "xtra", "falbygdens",
                "wh ", "enkla kassen", "wellnox", "kahog", "honung", "gain", "josefins", "dantonio",
                "saluhallen", "oob", "kvantum", "haggs", "ica supermarket olskro"));
        put(CostCategory.RESTAURANT, Set.of(
                "mcdonald", "mackbaren", "smakia", "4 gott", "sushi", "elite", "tgtg", "the place",
                "pallazo", "cederleufs", "oliven", "riber", "smorgas", "system", "bbq", "kaffe", "läsk", "cupcake",
                "nojds", "yili", "max burgers", "mugg tilltugg", "evolushi", "skatasmale", "fika", "ringo cafeterian",
                "elis corner", "espresso house", "portens", "aldardo", "kaifo", "koizen",
                "baguetten", "yaki-da", "comptoir", "adriano", "salut", "husman", "kondit", "nobelgrillen", "beas",
                "kastello", "subway", "mahogny", "sprakcafeet", "kaka", "rollin", "red lion", "kok och bar", "mfj",
                "burger", "masala", "penny", "soppa", "british shop", "thehuset", "condeco", "alhabesha",
                "gatukok", "bistro", "sibylla", "vestlund,tove", "evion", "2 rum bar", "bertilsson", "rancho", "sellin",
                "olkompaniet", "latterian", "cafe", "babemba", "roots", "via luna", "pizza", "snus", "shawarma",
                "toogood", "barcelon", "glass", "zigge", "gela", "caffe", "döner", "falken", "napoli", "bröd", "delik",
                "tilltugg", "falafel", "fallafel", "must", "baguette", "paradiset", "da marco", "coffee", "kanonen",
                "korv", "bodega", "ivans", "espresso", "lunch", "streetfood", "taco", "bagel", "olearys", "restau",
                "mcd", "nomi", "mugg", "elis", "waipo", "palazzo", "kampanilen", "malott", "vin", "pasta",
                "toscana", "pisa", "aero", "monterosso", "lucca", "greve", "firenze", "baren", "juventus",
                "lundbergs", "enoteca", "pallazzo", "sota", "maksi", "waernet", "skeppet", "yster", "lodose",
                "trubadren", "einar", "tullen", "pinchos", "alldes", "mean bean", "mellanrummet", "made in china",
                "bageri", "geely", "b&f", "nonna", "deli", "backwerk", "wien", "frankfurt", "wine", "taste", "brod",
                "m/s", "servicebutik", "bankomat", "affandi", "kooperativet", "bergstrom", "1000  en", "the goat", "moon thai",
                "schnitzelplatz", "clarion congress", "ramen", "fei", "jacy", "barleys", "tom tom", "intill", "maxigrillen", 
                "domkyrkan", "pizze", "ica nara", "sannegardens", "kallekerr", "flying barrel", "snyt", "loefqvist",
                "gottepojkarna", "7-eleven", "laterian", "radisson", "grisar", "banh mi", "shtisel", "kvarterskro",
                "fridas mexican", "seacup"));
        put(CostCategory.LOANS, Set.of("csn"));
        put(CostCategory.SAVINGS, Set.of("avanza bank", "sigmastocks", "isk", "nödspar", "9025.52.710.52",
                "9060.71.378.35", "9060.13.806.81", "levler", "nödkonto", "spar nödocykel", "invest",
                "lysa", "aktier", "oseghale", "tree partner"));
        put(CostCategory.MORTGAGE, Set.of("inbetalning", "90299542035", "låneinbetalning"));
        put(CostCategory.RENT, Set.of("hsb"));
        put(CostCategory.TRAVEL, Set.of("vasttrafik", "sj", "spar resa", "spar cykel", "pressbyran", "k*zettle_*va",
                "tack för att du väljer", "region halland", "stuga", "västtrafik", "florens", " pr", "cykelfiket", "shell", "sävedalens cykel",
                "ryanair", "snälltåget", "biljett"));
        put(CostCategory.ENTERTAINMENT, Set.of("roy", "filmstad", "aws", "pocket shop", "kjell & co", "kamaji", "fogg",
                "steam", "blizzard", "inet", "udemy", "youtube", "antik", "tickster", "adlibris", "akademibok",
                "biopalatset", "jönsson", "netonnet", "royal", "& falk", "las is more", "bokhan", "britt-marie",
                "film", "pocket", "fritanke", "google play", "sherlock", "escape", "samurai", "disney", "billetto",
                "beredskapsodling", "vernazza", "cdon", "butterick", "chatgpt"));
        put(CostCategory.HEALTH, Set.of("ouraring", "lager", "stadium", "frisktand", "narhalsan", "vaccin",
                "spar gym", "orgryte", "nw goteborg", "werlabs", "kry", "doz", "n w", "apotek", "missat",
                "wellness", "bulk", "nordic", "pottan", "toalett", "handtag"));
        put(CostCategory.HOME, Set.of("hornbach", "elboden", "taklampor", "teknos", "klint", "blomsterlandet",
                "ikea online", "spar renovering", "bagaren", "rosalina", "clas ohlson", "ikea", "hm se", "lamphuset",
                "monument", "tavla", "ljus", "kudd", "ahlens", "nicolas", "bauhaus", "od butik", "spanska fat",
                "loppis", "resurs bank", "colorama", "leif", "hottinen", "nehls", "f,varberg", "rusta", "jula",
                "byggmax", "bygghemma", "byggvaruhus", "bygg", "elgiganten", "elgiganten outlet", "elgiganten online", "persienner",
                "smartsten", "wennerbeck", "office depot"));
        put(CostCategory.CLOTHES, Set.of("brothers", "ecco", "cos", "lester", "freefoot", "sko kliniken", "naturkompaniet"));
        put(CostCategory.CREDIT_CARD, Set.of("k*", "klarna"));
        put(CostCategory.TRANSFERS, Set.of("aktier", "lärk"));
        put(CostCategory.UTILITIES, Set.of("hallon", "bredband", "energi", "lf sak", "bekväma vardagen", "akad.a",
                "boplats", "phone", "lf"));
    }};
}
