package hunternif.mc.impl.atlas;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hunternif.mc.impl.atlas.client.KeyHandler;
import hunternif.mc.impl.atlas.core.AtlasDataHandler;
import hunternif.mc.impl.atlas.core.BiomeDetectorBase;
import hunternif.mc.impl.atlas.core.GlobalAtlasData;
import hunternif.mc.impl.atlas.ext.TileDataHandler;
import hunternif.mc.impl.atlas.forge.AntiqueAtlasConfigBuilder;
import hunternif.mc.impl.atlas.marker.GlobalMarkersDataHandler;
import hunternif.mc.impl.atlas.marker.MarkersDataHandler;
import hunternif.mc.impl.atlas.network.AntiqueAtlasNetworking;
import hunternif.mc.impl.atlas.structure.EndCity;
import hunternif.mc.impl.atlas.structure.NetherFortress;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

@Mod("antiqueatlas")
public class AntiqueAtlasMod
{
	public static AntiqueAtlasMod instance;


	public static final String ID = "antiqueatlas";
	public static final String NAME = "Antique Atlas";
	public static final String CHANNEL = ID;
	public static final String VERSION = "@VERSION@";
	
	public static final Logger LOG = LogManager.getLogger(ID);
	private static final String NETWORK_PROTOCOL_VERSION = "1";
	public static final SimpleChannel MOD_CHANNEL = NetworkRegistry.newSimpleChannel(id("main"), () -> NETWORK_PROTOCOL_VERSION, NETWORK_PROTOCOL_VERSION::equals, NETWORK_PROTOCOL_VERSION::equals);
	

	public static final AtlasDataHandler atlasData = new AtlasDataHandler();
	public static final MarkersDataHandler markersData = new MarkersDataHandler();
	
	public static final TileDataHandler tileData = new TileDataHandler();
	public static final GlobalMarkersDataHandler globalMarkersData = new GlobalMarkersDataHandler();
	
	private static final GlobalAtlasData clientAtlasData = new GlobalAtlasData("antiqueatlas:global_atlas_data");

	public AntiqueAtlasMod() 
	{
		instance = this;
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientRegistries);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueue);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::initializeClient);
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, AntiqueAtlasConfigBuilder.client_config, "antiqueatlas.client.toml");
		AntiqueAtlasConfigBuilder.loadConfig(AntiqueAtlasConfigBuilder.client_config, FMLPaths.CONFIGDIR.get().resolve("antiqueatlas.client.toml").toString());
		MinecraftForge.EVENT_BUS.register(this);

		BiomeDetectorBase.scanBiomeTypes();

		AntiqueAtlasNetworking.registerC2SListeners(MOD_CHANNEL);
		AntiqueAtlasNetworking.registerS2CListeners(MOD_CHANNEL);
//
//		ServerWorldEvents.LOAD.register(globalMarkersData::onWorldLoad);
//		ServerWorldEvents.LOAD.register(tileData::onWorldLoad);
//
//		RecipeCraftedCallback.EVENT.register(craftedHandler);

		NetherFortress.registerPieces();
		EndCity.registerPieces();
//		Village.registerMarkers();
	}

	private void setup(final FMLCommonSetupEvent event)
	{
	}

	public void clientRegistries(final FMLClientSetupEvent event)
	{
		if (!AntiqueAtlasConfig.itemNeeded.get()) {
            KeyHandler.registerBindings();
        }

	}
	
	/**
	 * Despite what the events name might suggest, this event can be used for more than registering 
	 * particle renders as it's called in the {@link net.minecraft.client.Minecraft} constructor. Thus here we use it to do
	 * all client stuff
	 * @param event
	 */
	public void initializeClient(ParticleFactoryRegisterEvent event) {
		AntiqueAtlasModClient.onInitializeClient();
	}

	public void enqueue(final InterModEnqueueEvent event) {
	}
	
	public static ResourceLocation id(String... path) {
		return path[0].contains(":") ? new ResourceLocation(String.join(".", path)) : new ResourceLocation(ID, String.join(".", path));
	}
	
	public static GlobalAtlasData getGlobalAtlasData(World world) {
		if (world instanceof ServerWorld) {
			return ((ServerWorld) world).getSavedData().getOrCreate(() -> new GlobalAtlasData("antiqueatlas:global_atlas_data"), "antiqueatlas:global_atlas_data");
		} else {
			return clientAtlasData;
		}
	}
}
