package net.eithon.plugin.stats.logic;

import net.eithon.library.json.JsonObject;

import org.json.simple.JSONObject;

public class BungeeTransfer extends JsonObject<BungeeTransfer> {
	private PlayerStatistics _statistics;
	private boolean _move;
	
	public BungeeTransfer(PlayerStatistics statistics, boolean move) {
		this._statistics = statistics;
		this._move = move;
	}
	
	private BungeeTransfer() {}
	
	public PlayerStatistics getStatistics() { return this._statistics; }
	public boolean getMove() { return this._move; }

	@Override
	public BungeeTransfer fromJson(Object json) {
		JSONObject jsonObject = (JSONObject) json;
		this._statistics = PlayerStatistics.getFromJson(jsonObject.get("statistics"));
		Boolean move = (Boolean)jsonObject.get("move");
		this._move = (move == null) ? false : move.booleanValue();
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object toJson() {
		JSONObject json = new JSONObject();
		json.put("statistics", this._statistics.toJson());
		json.put("move", new Boolean(this._move));
		return json;
	}

	@Override
	public BungeeTransfer factory() {
		return new BungeeTransfer();
	}

	public static BungeeTransfer getFromJson(JSONObject data) {
		return (new BungeeTransfer()).fromJsonObject(data);
	}

}
