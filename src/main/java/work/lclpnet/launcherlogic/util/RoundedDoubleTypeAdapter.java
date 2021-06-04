package work.lclpnet.launcherlogic.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Locale;

public class RoundedDoubleTypeAdapter implements JsonSerializer<Double> {

	protected int decimalPlaces;
	
	public RoundedDoubleTypeAdapter(int decimalPlaces) {
		this.decimalPlaces = decimalPlaces;
	}
	
	@Override
	public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
		Double d = Double.valueOf(String.format(Locale.ENGLISH, String.format("%%.%sf", decimalPlaces), src));
		return new JsonPrimitive(d);
	}

}
