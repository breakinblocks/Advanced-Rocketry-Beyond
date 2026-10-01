package advRocketry.smoke;
import advRocketry.Main;
import advRocketry.orbit.OrbitalRegistry;
import advRocketry.processing.*;
import com.google.gson.GsonBuilder;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
@EventBusSubscriber(modid=Main.MODID,value=Dist.CLIENT)
public final class GuideExport {
 static boolean opening,done; static int ticks; static Path root;
 static ListTag ints(int... values){var list=new ListTag();for(int v:values)list.add(IntTag.valueOf(v));return list;}
 @SubscribeEvent public static void tick(ClientTickEvent.Post event) throws Exception {
  var client=Minecraft.getInstance();
  if(!opening && client.screen instanceof TitleScreen && client.getOverlay()==null){opening=true;client.options.pauseOnLostFocus=false;root=Path.of("").toAbsolutePath();while(!Files.exists(root.resolve("settings.gradle")))root=root.getParent();client.createWorldOpenFlows().openWorld("machine-gallery-1790792094741",()->client.setScreen(new TitleScreen()));}
  if(done || client.level==null || client.player==null || ++ticks<40)return;done=true;
  var out=root.resolve("src/main/resources/assets/adv_rocketry/guides/adv_rocketry/guide/structures");Files.createDirectories(out);
  var shapes=new LinkedHashMap<String,Object>();
  var registries=client.level.registryAccess();
  for(var key:advRocketry.multiblock.Multiblocks.ids(registries)){
   String id=key.getPath();
   var cells=ProjectorBlueprint.cells(registries,key,BlockPos.ZERO,Direction.NORTH);
   var states=new LinkedHashMap<BlockPos,BlockState>(); int flexible=0;
   for(var cell:cells){
    var state=cell.matches().test(Blocks.AIR.defaultBlockState())?Blocks.AIR.defaultBlockState():BuiltInRegistries.BLOCK.stream().filter(b->b.asItem()!=Items.AIR).map(Block::defaultBlockState).filter(cell.matches()).findFirst().orElseThrow();
    if(id.equals("solar_array") && cell.position().getZ()>0 && cell.position().getZ()<=3) state=OrbitalRegistry.SOLAR_ARRAY_PANEL.get().defaultBlockState();
    if(cell.matches().test(MachinePorts.BLOCK_ITEM_INPUT_BLOCK.get().defaultBlockState()) && cell.matches().test(MachinePorts.BLOCK_ITEM_OUTPUT_BLOCK.get().defaultBlockState())) {
     if(id.equals("black_hole_generator")) state=(flexible++==0?MachinePorts.BLOCK_ITEM_INPUT_BLOCK.get():MachinePorts.BLOCK_ENERGY_OUTPUT_BLOCK.get()).defaultBlockState();
     else state=switch(flexible++){case 0,1->MachinePorts.BLOCK_ITEM_INPUT_BLOCK.get().defaultBlockState();case 2->MachinePorts.BLOCK_ITEM_OUTPUT_BLOCK.get().defaultBlockState();case 3->MachinePorts.BLOCK_ENERGY_INPUT_BLOCK.get().defaultBlockState();default->(id.equals("electric_arc_furnace")?ProcessingRegistry.part("blast_brick"):MachinePorts.BLOCK_STRUCTURE.get()).defaultBlockState();};
    }
    if(!cell.matches().test(state)) state=BuiltInRegistries.BLOCK.stream().filter(b->b.asItem()!=Items.AIR).map(Block::defaultBlockState).filter(cell.matches()).findFirst().orElseThrow();
    if(id.equals("observatory") && cell.matches().test(MachinePorts.DATA_BUS.get().defaultBlockState()) && flexible>4)state=MachinePorts.DATA_BUS.get().defaultBlockState();
    if(state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) state=state.setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.NORTH);
    if(!cell.matches().test(state))throw new AssertionError("Invalid cell "+id+" "+cell.position()+" "+state);
    states.put(cell.position(),state);
   }
   int minX=states.keySet().stream().mapToInt(BlockPos::getX).min().orElseThrow(), minY=states.keySet().stream().mapToInt(BlockPos::getY).min().orElseThrow(),minZ=states.keySet().stream().mapToInt(BlockPos::getZ).min().orElseThrow();
   int maxX=states.keySet().stream().mapToInt(BlockPos::getX).max().orElseThrow(),maxY=states.keySet().stream().mapToInt(BlockPos::getY).max().orElseThrow(),maxZ=states.keySet().stream().mapToInt(BlockPos::getZ).max().orElseThrow();
   var palette=new ArrayList<BlockState>();var blocks=new ListTag();var jsonBlocks=new ArrayList<Object>();var counts=new TreeMap<String,Integer>();
   for(var e:states.entrySet()){var state=e.getValue();if(state.isAir())continue;int idx=palette.indexOf(state);if(idx<0){idx=palette.size();palette.add(state);}var pos=e.getKey().offset(-minX,-minY,-minZ);var block=new CompoundTag();block.put("pos",ints(pos.getX(),pos.getY(),pos.getZ()));block.putInt("state",idx);blocks.add(block);String blockId=BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();counts.merge(blockId,1,Integer::sum);jsonBlocks.add(Map.of("id",blockId,"pos",List.of(pos.getX(),pos.getY(),pos.getZ())));}
   var nbt=new CompoundTag();nbt.putInt("DataVersion",3955);nbt.put("size",ints(maxX-minX+1,maxY-minY+1,maxZ-minZ+1));var pal=new ListTag();for(var state:palette)pal.add(NbtUtils.writeBlockState(state));nbt.put("palette",pal);nbt.put("blocks",blocks);nbt.put("entities",new ListTag());NbtIo.writeCompressed(nbt,out.resolve(id+".nbt"));
   var mats=ProjectorBlueprint.materials(registries,key).stream().map(m->Map.of("count",m.count(),"optional",m.optional(),"alternatives",m.alternatives().stream().map(b->BuiltInRegistries.BLOCK.getKey(b).toString()).toList())).toList();
   shapes.put(id,Map.of("size",List.of(maxX-minX+1,maxY-minY+1,maxZ-minZ+1),"blocks",jsonBlocks,"counts",counts,"materials",mats));
  }
  var gson=new GsonBuilder().setPrettyPrinting().create();Files.writeString(root.resolve("tools/guide-structures.json"),gson.toJson(shapes));
  var items=new TreeMap<String,String>();for(var item:BuiltInRegistries.ITEM) {var id=BuiltInRegistries.ITEM.getKey(item);if(id.getNamespace().equals(Main.MODID))items.put(id.toString(),item.getDescription().getString());}Files.writeString(root.resolve("tools/guide-items.json"),gson.toJson(items));
  System.out.println("GUIDE_EXPORT: "+shapes.size()+" validated structures; "+items.size()+" items");client.stop();
 }
}
