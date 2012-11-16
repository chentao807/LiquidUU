package ro.narc.liquiduu;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;

import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

import buildcraft.api.liquids.LiquidData;
import buildcraft.api.liquids.LiquidManager;
import buildcraft.api.liquids.LiquidStack;
import buildcraft.api.recipes.RefineryRecipe;
import buildcraft.BuildCraftEnergy;
import buildcraft.BuildCraftFactory;
import buildcraft.BuildCraftTransport;

import ic2.common.Ic2Items;

@Mod(
        modid="LiquidUU",
        version="0.7.2",
        useMetadata=true,
        dependencies="required-after:IC2;required-after:BuildCraft|Transport;required-after:BuildCraft|Energy;required-after:BuildCraft|Factory;after:Forestry"
    )
@NetworkMod(
        clientSideRequired = true,
        versionBounds = "[0.7, 0.8)"
    )
public class LiquidUU {
    public static boolean DEBUG_NETWORK = false;

    public static ItemStack liquidUU;
    public static ItemStack cannedUU;
    public static Block liquidUUBlock;
    public static ItemStack accelerator;
    public static LiquidStack liquidUUStack;

    @Mod.Instance("LiquidUU")
    public static LiquidUU instance;

    public static Configuration config;

    @SidedProxy(clientSide = "ro.narc.liquiduu.ClientProxy", serverSide = "ro.narc.liquiduu.CommonProxy")
    public static CommonProxy proxy;

    @Mod.PreInit
    public void preInit(FMLPreInitializationEvent event) {
        config = new Configuration(event.getSuggestedConfigurationFile());
    }

    @Mod.Init
    public void init(FMLInitializationEvent event) {

        try {
            config.load();
        }
        catch (RuntimeException e) { /* and ignore it */ }

        Property accelBlockID = config.getBlock("accelerator", 1300);
        Property liquidItemID = config.getItem("liquid.uu", 21001);
        Property cannedItemID = config.getItem("canned.uu", 21002);
        //config.addCustomCategoryComment("general", "This might be useful later.");
        config.save();

        Item liquidUUItem = new ItemGeneric(liquidItemID.getInt(21001));
        liquidUUItem.setItemName("liquidUU");
        liquidUUItem.setIconIndex(0);
        liquidUUItem.setTextureFile("/liquiduu-gfx/items.png");
        liquidUU = new ItemStack(liquidUUItem, 1);

        Item cannedUUItem = new ItemGeneric(cannedItemID.getInt(21002));
        cannedUUItem.setItemName("cannedUU");
        cannedUUItem.setIconIndex(1);
        cannedUUItem.setTextureFile("/liquiduu-gfx/items.png");
        cannedUU = new ItemStack(cannedUUItem, 1);

        liquidUUBlock = new BlockGeneric(accelBlockID.getInt(1300));
        GameRegistry.registerBlock(liquidUUBlock, ItemGenericBlock.class);
        GameRegistry.registerTileEntity(TileEntityAccelerator.class, "Accelerator");
        accelerator = new ItemStack(liquidUUBlock, 1, BlockGeneric.DATA_ACCELERATOR);

        GameRegistry.addRecipe(accelerator, "TH", "SA", " w",
                Character.valueOf('T'), BuildCraftFactory.tankBlock,
                Character.valueOf('H'), BuildCraftFactory.hopperBlock,
                Character.valueOf('S'), BuildCraftTransport.pipeLiquidsStone,
                Character.valueOf('A'), Ic2Items.advancedCircuit,
                Character.valueOf('w'), BuildCraftTransport.pipeItemsWood
        );
        GameRegistry.addRecipe(accelerator, " ST", "wAH",
                Character.valueOf('T'), BuildCraftFactory.tankBlock,
                Character.valueOf('H'), BuildCraftFactory.hopperBlock,
                Character.valueOf('S'), BuildCraftTransport.pipeLiquidsStone,
                Character.valueOf('A'), Ic2Items.advancedCircuit,
                Character.valueOf('w'), BuildCraftTransport.pipeItemsWood
        );
        GameRegistry.addRecipe(accelerator, "wAH", " ST",
                Character.valueOf('T'), BuildCraftFactory.tankBlock,
                Character.valueOf('H'), BuildCraftFactory.hopperBlock,
                Character.valueOf('S'), BuildCraftTransport.pipeLiquidsStone,
                Character.valueOf('A'), Ic2Items.advancedCircuit,
                Character.valueOf('w'), BuildCraftTransport.pipeItemsWood
        );

        proxy.init();

        initLiquidData();
        initRefineryRecipes();

        NetworkRegistry.instance().registerGuiHandler(this, new LiquidUUGUIHandler());
    }

    @Mod.PostInit
    public void postInit(FMLPostInitializationEvent event) {
        loadIntegration("Forestry");
        loadIntegration("NEI");
    }

    @SuppressWarnings("unchecked")
    private static void initLiquidData() {
        liquidUUStack = new LiquidStack(liquidUU.getItem(), 1000);
        LiquidData liquidUUData = new LiquidData(liquidUUStack, Ic2Items.matter,
                Ic2Items.cell);
        LiquidManager.liquids.add(liquidUUData);
    }

    public static Side getSide() {
        return FMLCommonHandler.instance().getEffectiveSide();
    }

    private static void initRefineryRecipes() {
        Item liquidUUItem = liquidUU.getItem();

        RefineryRecipe.registerRefineryRecipe(
            new RefineryRecipe(
                new LiquidStack(liquidUUItem, 1),
                new LiquidStack(Block.waterStill.blockID, 1),
                new LiquidStack(Block.waterStill.blockID, 10),
                5, 1
            )
        );
        RefineryRecipe.registerRefineryRecipe(
            new RefineryRecipe(
                new LiquidStack(liquidUUItem, 1),
                new LiquidStack(Block.lavaStill.blockID, 1),
                new LiquidStack(Block.lavaStill.blockID, 5),
                5, 1
            )
        );
        RefineryRecipe.registerRefineryRecipe(
            new RefineryRecipe(
                new LiquidStack(liquidUUItem, 1),
                new LiquidStack(BuildCraftEnergy.oilStill.blockID, 1),
                new LiquidStack(BuildCraftEnergy.oilStill.blockID, 2),
                5, 1
            )
        );
        RefineryRecipe.registerRefineryRecipe(
            new RefineryRecipe(
                new LiquidStack(liquidUUItem, 2),
                new LiquidStack(BuildCraftEnergy.fuel.shiftedIndex, 1),
                new LiquidStack(BuildCraftEnergy.fuel.shiftedIndex, 2),
                5, 1
            )
        );
    }

    @SuppressWarnings("unchecked")
    private static boolean loadIntegration(String name) {
        System.out.println("LiquidUU: Loading " + name + " integration...");

        try {
            Class t = LiquidUU.class.getClassLoader().loadClass("ro.narc.liquiduu.integration." + name);
            return ((Boolean)t.getMethod("init", new Class[0]).invoke((Object)null, new Object[0])).booleanValue();
        }
        catch (Throwable e) {
            System.out.println("LiquidUU: Did not load " + name + " integration: " + e);
            return false;
        }
    }
}
