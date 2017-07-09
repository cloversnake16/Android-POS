package no.susoft.mobile.pos.json;

import java.lang.reflect.Type;
import java.util.Date;

import com.google.gson.*;
import no.susoft.mobile.pos.db.DBHelper;
import no.susoft.mobile.pos.search.Searchable;
import no.susoft.mobile.pos.search.SearchableAdapter;

/**
 * This class serializes and de-serializes JSON data to and from Java objects.
 * @author Yesod
 */
public enum JSONFactory {
	INSTANCE;
	private final Gson gson;

	/**
	 * Build the Gson factories.
	 */
	private JSONFactory() {

		JsonSerializer<Date> ser = new JsonSerializer<Date>() {
			@Override
			public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
				return src == null ? null : new JsonPrimitive(DBHelper.DB_TIMESTAMP_FORMAT.format(src));
			}
		};

		JsonDeserializer<Date> deser = new JsonDeserializer<Date>() {
			@Override
			public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
				try {
					return json == null ? null : DBHelper.DB_TIMESTAMP_FORMAT.parse(json.getAsString());
				} catch (Exception e) {
				}
				return null;
			}
		};

		this.gson = new GsonBuilder()
						.registerTypeAdapter(Date.class, ser)
						.registerTypeAdapter(Date.class, deser)
						.registerTypeAdapter(Searchable.class, new SearchableAdapter())
						.create();
	}

	/**
	 * Get the default GSON factory.
	 * @return
	 */
	public Gson getFactory() {
		return this.gson;
	}

}