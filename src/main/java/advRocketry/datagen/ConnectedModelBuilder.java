package advRocketry.datagen;

import advRocketry.client.model.ConnectedModelLoader;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

final class ConnectedModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
  private final JsonArray connectWith = new JsonArray();
  private int tint = -1;

  private ConnectedModelBuilder(T parent, ExistingFileHelper files) {
    super(ConnectedModelLoader.ID, parent, files, false);
  }

  static <T extends ModelBuilder<T>> ConnectedModelBuilder<T> begin(
      T parent, ExistingFileHelper files) {
    return new ConnectedModelBuilder<>(parent, files);
  }

  ConnectedModelBuilder<T> tint(int tint) {
    this.tint = tint;
    return this;
  }

  ConnectedModelBuilder<T> connectWith(String block) {
    connectWith.add(block);
    return this;
  }

  @Override
  public JsonObject toJson(JsonObject json) {
    json = super.toJson(json);
    if (tint >= 0) json.addProperty("tint_index", tint);
    if (!connectWith.isEmpty()) json.add("connect_with", connectWith);
    return json;
  }
}
