package work.lclpnet.launcherlogic.ls5;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

public class Modifications {

	private String other;
	private String otherMD5;
	private List<Modification> mods = new ArrayList<>();
	
	public String getOther() {
		return other;
	}
	
	public String getOtherMD5() {
		return otherMD5;
	}
	
	public List<Modification> getMods() {
		return mods;
	}
	
	@Override
	public String toString() {
		return new Gson().toJson(this);
	}
	
}
