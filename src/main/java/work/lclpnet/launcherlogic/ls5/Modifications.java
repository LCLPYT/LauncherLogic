package work.lclpnet.launcherlogic.ls5;

import com.google.gson.Gson;

import javax.annotation.Nullable;
import java.util.List;

public class Modifications {

	private String other;
	private String otherMD5;
	private List<Modification> mods;
	
	public String getOther() {
		return other;
	}
	
	public String getOtherMD5() {
		return otherMD5;
	}

	@Nullable
	public List<Modification> getMods() {
		return mods;
	}
	
	@Override
	public String toString() {
		return new Gson().toJson(this);
	}
	
}
