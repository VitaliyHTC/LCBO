package com.vitaliyhtc.lcbo;

public class Config {
    public static final String LCBO_API_ACCESS_KEY = "MDo4MjI4NjU0Ni1mZGFmLTExZTYtODQzOC0yZjJhN2Y4YWNmOGQ6dEFucGdpR2xvcXR1bGloOThwNkZ0S1lKVGxNa1M2OUdvV3Bw";
    public static final String LCBO_API_BASE_URL = "https://lcboapi.com/";

    public static final int STORES_PER_PAGE = 40;

    public static final int PRODUCTS_PER_PAGE = 40;
    public static final String PRODUCTS_WHERE_NOT = "is_dead";

    public static final boolean IS_24_HOURS_FORMAT = true;
    public static final int INITIAL_MAP_ZOOM = 15;

    public static final String PRODUCT_CATEGORY_BEER = "Beer";
    public static final String PRODUCT_CATEGORY_WINE = "Wine";
    public static final String PRODUCT_CATEGORY_SPIRITS = "Spirits";

    public static final boolean IS_LOG_DEBUG = false;

}
