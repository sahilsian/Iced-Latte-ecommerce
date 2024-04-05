package com.zufar.icedlatte.security.configuration;

public final class SecurityConstants {
    public static final String SHOPPING_CART_URL = "/api/v1/cart/**";
    public static final String PAYMENT_URL = "/api/v1/payment/**";
    public static final String USERS_URL = "/api/v1/users/**";
    public static final String FAVOURITES_URL = "/api/v1/favorites/**";
    public static final String AUTH_REFRESH_URL = "/api/v1/auth/refresh";
    public static final String ORDERS_URL = "/api/v1/orders/**";
    public static final String REVIEWS_URL = "/api/v1/products/*/reviews/**";
    public static final String REVIEW_URL = "/api/v1/products/*/review";
    public static final String RATING_URL = "/api/v1/products/*/ratings/**";
    public static final String SHIPPING_URL = "/api/v1/shipping/**";

  public static final String AUTH_3PART_URL = "/api/v1/3part-auth/**";

    private SecurityConstants() {}
}
