package no.susoft.mobile.pos.search;

import com.google.gson.*;
import no.susoft.mobile.pos.data.Product;
import no.susoft.mobile.pos.network.Protocol.SearchEntity;

import java.lang.reflect.Type;
import java.util.Locale;

/**
 * This accountAdapter handles the JSON serilization and deserilization of a Searchable.class instance.
 *
 * @author Yesod
 */
public class SearchableAdapter implements JsonSerializer<Searchable>, JsonDeserializer<Searchable> {

    // Key.
    private static final String ENTITY = "ENTITY";
    // Key.
    private static final String INSTANCE = "INSTANCE";

    /*
     * (non-Javadoc)
     * @see com.google.gson.JsonSerializer#serialize(java.lang.Object, java.lang.reflect.Type, com.google.gson.JsonSerializationContext)
     */
    @Override
    public JsonElement serialize(Searchable searchable, Type typeOfSrc, JsonSerializationContext context) {
        // Create a new JSON object.
        JsonObject object = new JsonObject();
        // Attach the 'searchable' SearchEntity enum type as a property named ENTITY.
        object.addProperty(SearchableAdapter.ENTITY, searchable.getSearchEntityType().toString());
        // Serialize the 'searchable' instance.
        JsonElement instance = context.serialize(searchable);
        // Attach.
        object.add(SearchableAdapter.INSTANCE, instance);
        // Return the object in place of the instance.
        return object;
    }

    /*
     * (non-Javadoc)
     * @see com.google.gson.JsonDeserializer#deserialize(com.google.gson.JsonElement, java.lang.reflect.Type, com.google.gson.JsonDeserializationContext)
     */
    @Override
    public Searchable deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            // This is the Searchable JSON object
            JsonObject searchable = json.getAsJsonObject();
            // This is its ENTITY property.
            JsonPrimitive entity = (JsonPrimitive) searchable.get(SearchableAdapter.ENTITY);

		    /*
             * The JSON will contain the instance information, but by default GSON will have no idea which subclass
		     * of Searchable it belongs to. Therefore, during serialization, we inscribed the instance type via
		     * the ENTITY property of the Searchable object. We can now determine the proper subclass to use
		     * for deserializing the Searchable object when we know its SearchEntity enum type.
		     */
            switch (SearchEntity.valueOf(entity.getAsString().toUpperCase(Locale.US))) {
                case PRODUCT: {
                    return context.deserialize(searchable.get(INSTANCE), Product.class);
                }
                default: {
                    return null;
                }
            }
        } catch (Exception x) {
            throw new JsonParseException(x.getMessage());
        }
    }
}