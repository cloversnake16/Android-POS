package no.susoft.mobile.pos.network;

import android.net.Uri;
import no.susoft.mobile.pos.network.Protocol.*;

import java.util.Locale;

/**
 * This class builds a client/server communication protocol.
 *
 * @author Yesod
 */
public final class Request {

    private final Uri.Builder uri;

    /**
     * new Protocol with an empty URI.
     * This remains private.
     */
    public Request() {
        this.uri = new Uri.Builder();
    }

    /**
     * new Protocol set with the given URI.
     * This remains private.
     *
     * @param uri
     */
    public Request(Uri.Builder uri) {
        this.uri = uri;
    }

    /**
     * Append the given client state.
     *
     * @param state
     * @return
     */
    public Request appendState(State state) {
        return this.appendParameter(Parameters.STATE, state.ordinal());
    }

    /**
     * Append the given operation.
     *
     * @param parameter
     * @return
     */
    public Request appendOperation(OperationCode parameter) {
        return this.appendParameter(Parameters.OPCODE, parameter.ordinal());
    }

    /**
     * Append the given parameter with the given value.
     *
     * @param parameter
     * @param value
     * @return
     */
    public Request appendParameter(Parameters parameter, String value) {
        return this.append(parameter.toString(), value);
    }

    /**
     * Append the given parameter with the given value.
     *
     * @param parameter
     * @param ordinal
     * @return
     */
    public Request appendParameter(Parameters parameter, int ordinal) {
        return this.append(parameter.toString(), Integer.toString(ordinal));
    }

    /**
     * Append the given entity with the given value.
     *
     * @param entity
     * @return
     */
    public Request appendEntity(SearchEntity entity) {
        return this.appendParameter(Parameters.ENTITY, entity.ordinal());
    }

    /**
     * Append the given constraint with the given value.
     *
     * @param constraint
     * @return
     */
    public Request appendConstraint(SearchConstraint constraint) {
        return this.appendParameter(Parameters.CONSTRAINT, constraint.ordinal());
    }

    /**
     * @param parameter
     * @param value
     * @return
     */
    private Request append(String parameter, String value) {
        uri.appendQueryParameter(parameter.toLowerCase(Locale.US), value);
        return this;
    }

    /**
     * Get the string of the current URI.
     *
     * @return
     */
    public String get() {
        return this.uri.build().toString();
    }
}