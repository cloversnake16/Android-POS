package no.susoft.mobile.pos.account;

import no.susoft.mobile.pos.data.Chain;
import no.susoft.mobile.pos.data.Shop;
import no.susoft.mobile.pos.json.JSONSerializable;

import java.util.List;

/**
 * This class is the response to an authorization request.
 *
 * @author Yesod
 */
public final class AuthorizationResponse implements JSONSerializable {

    // The account token.
    private final String token;
    // The account shops.
    private final List<Shop> shops;
    // The account chain.
    private final Chain chain;

    /**
     * @param token
     * @param shops
     */
    public AuthorizationResponse(final String token, final List<Shop> shops, final Chain chain) {
        this.token = token;
        this.shops = shops;
        this.chain = chain;
    }

    /**
     * Get the server authorization token response.
     *
     * @return
     */
    public String getToken() {
        return this.token;
    }

    /**
     * Get the shops permitted by this account.
     *
     * @return
     */
    public List<Shop> getShops() {
        return this.shops;
    }

    /**
     * Get the chain for this account.
     *
     * @return
     */
    public Chain getChain() {
        return this.chain;
    }
}