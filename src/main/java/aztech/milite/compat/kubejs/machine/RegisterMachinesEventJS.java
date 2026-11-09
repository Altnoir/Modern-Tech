package aztech.milite.compat.kubejs.machine;

import aztech.milite.compat.rei.machines.ReiMachineRecipes;
import aztech.milite.inventory.SlotPositions;
import aztech.milite.machines.BEP;
import aztech.milite.machines.blockentities.GeneratorMachineBlockEntity;
import aztech.milite.machines.blockentities.multiblocks.ElectricCraftingMultiblockBlockEntity;
import aztech.milite.machines.blockentities.multiblocks.GeneratorMultiblockBlockEntity;
import aztech.milite.machines.blockentities.multiblocks.SteamCraftingMultiblockBlockEntity;
import aztech.milite.machines.components.FluidItemConsumerComponent;
import aztech.milite.machines.gui.MachineGuiParameters;
import aztech.milite.machines.guicomponents.EnergyBar;
import aztech.milite.machines.guicomponents.ProgressBar;
import aztech.milite.machines.init.MachineLevel;
import aztech.milite.machines.init.MachineRegistrationHelper;
import aztech.milite.machines.init.MultiblockMachines;
import aztech.milite.machines.init.SingleBlockCraftingMachines;
import aztech.milite.machines.MachineBlockEntity;
import aztech.milite.machines.models.MachineCasings;
import aztech.milite.machines.multiblocks.ShapeTemplate;
import aztech.milite.machines.recipe.MachineRecipeType;
import dev.latvian.mods.kubejs.event.KubeEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.List;

@SuppressWarnings("unused")
public class RegisterMachinesEventJS implements KubeEvent, ShapeTemplateHelper {
    public ProgressBar.Params progressBar(int renderX, int renderY, String type) {
        return new ProgressBar.Params(renderX, renderY, type);
    }

    public ProgressBar.Params progressBar(int renderX, int renderY, String type, int width, int height) {
        return new ProgressBar.Params(renderX, renderY, type, width, height, false);
    }

    public EnergyBar.Params energyBar(int renderX, int renderY) {
        return new EnergyBar.Params(renderX, renderY);
    }

    public void craftingSingleBlock(
            // general
            String englishName, String internalName, MachineRecipeType recipeType, List<String> tiers,
            // gui
            int backgroundHeight, // can be -1 to use default
            ProgressBar.Params progressBar, EnergyBar.Params energyBar,
            // slots
            int itemInputs, int itemOutputs, int fluidInputs, int fluidOutputs, int bucketCapacity,
            Consumer<SlotPositions.Builder> itemSlotPositions, Consumer<SlotPositions.Builder> fluidSlotPositions,
            // model
            boolean frontOverlay, boolean topOverlay, boolean sideOverlay) {
        craftingSingleBlock(englishName, internalName, recipeType, tiers, backgroundHeight, progressBar, energyBar,
                itemInputs, itemOutputs, fluidInputs, fluidOutputs, bucketCapacity, itemSlotPositions, fluidSlotPositions,
                frontOverlay, topOverlay, sideOverlay, config -> {});
    }

    public void craftingSingleBlock(
            // general
            String englishName, String internalName, MachineRecipeType recipeType, List<String> tiers,
            // gui
            int backgroundHeight, // can be -1 to use default
            ProgressBar.Params progressBar, EnergyBar.Params energyBar,
            // slots
            int itemInputs, int itemOutputs, int fluidInputs, int fluidOutputs, int bucketCapacity,
            Consumer<SlotPositions.Builder> itemSlotPositions, Consumer<SlotPositions.Builder> fluidSlotPositions,
            // model
            boolean frontOverlay, boolean topOverlay, boolean sideOverlay,
            // Optional config
            Consumer<ExtraMachineConfig.CraftingSingleBlock> extraConfig) {
        var config = new SingleBlockCraftingMachines.Config();
        extraConfig.accept(new ExtraMachineConfig.CraftingSingleBlock(config));

        // The tier names are the machine level names, for example "bronze", "steel" or "electric".
        List<MachineLevel> levels = new ArrayList<>();
        for (String tier : tiers) {
            levels.add(MachineLevel.byName(tier));
        }
        Consumer<MachineGuiParameters.Builder> guiParams = backgroundHeight < 0 ? p -> {} : p -> p.backgroundHeight(backgroundHeight);

        SingleBlockCraftingMachines.registerMachineTiers(
                englishName, internalName, recipeType,
                itemInputs, itemOutputs, fluidInputs, fluidOutputs,
                guiParams, progressBar, progressBar, energyBar,
                itemSlotPositions, fluidSlotPositions,
                frontOverlay, topOverlay, sideOverlay,
                levels, bucketCapacity, config);
    }

    private void simpleMultiBlock(
            // general
            String englishName, String internalName, ShapeTemplate multiblockShape,
            // model
            String controllerCasingName, String overlayFolder, boolean frontOverlay, boolean topOverlay, boolean sideOverlay,
            // machine entity
            Function<BEP, MachineBlockEntity> factory) {
        var controllerCasing = MachineCasings.get(controllerCasingName);
        MachineRegistrationHelper.registerMachine(englishName, internalName, factory);
        MachineRegistrationHelper.addMachineModel(internalName, overlayFolder, controllerCasing, frontOverlay, topOverlay, sideOverlay);
        ReiMachineRecipes.registerMultiblockShape(internalName, multiblockShape);
    }

    public void simpleElectricCraftingMultiBlock(
            // general
            String englishName, String internalName, MachineRecipeType recipeType, ShapeTemplate multiblockShape,
            // REI parameters
            ProgressBar.Params progressBar,
            Consumer<SlotPositions.Builder> itemInputPositions, Consumer<SlotPositions.Builder> itemOutputPositions,
            Consumer<SlotPositions.Builder> fluidInputPositions, Consumer<SlotPositions.Builder> fluidOutputPositions,
            // model
            String controllerCasingName, String overlayFolder, boolean frontOverlay, boolean topOverlay, boolean sideOverlay) {
        simpleElectricCraftingMultiBlock(englishName, internalName, recipeType, multiblockShape, progressBar,
                itemInputPositions, itemOutputPositions, fluidInputPositions, fluidOutputPositions,
                controllerCasingName, overlayFolder, frontOverlay, topOverlay, sideOverlay,
                config -> {});
    }

    public void simpleElectricCraftingMultiBlock(
            // general
            String englishName, String internalName, MachineRecipeType recipeType, ShapeTemplate multiblockShape,
            // REI parameters
            ProgressBar.Params progressBar,
            Consumer<SlotPositions.Builder> itemInputPositions, Consumer<SlotPositions.Builder> itemOutputPositions,
            Consumer<SlotPositions.Builder> fluidInputPositions, Consumer<SlotPositions.Builder> fluidOutputPositions,
            // model
            String controllerCasingName, String overlayFolder, boolean frontOverlay, boolean topOverlay, boolean sideOverlay,
            // Optional config
            Consumer<ExtraMachineConfig.CraftingMultiBlock> extraConfig) {
        var config = new ExtraMachineConfig.CraftingMultiBlock();
        extraConfig.accept(config);

        simpleCraftingMultiBlock(englishName, internalName, recipeType, multiblockShape, progressBar,
                itemInputPositions, itemOutputPositions, fluidInputPositions, fluidOutputPositions,
                controllerCasingName, overlayFolder, frontOverlay, topOverlay, sideOverlay,
                bep -> new ElectricCraftingMultiblockBlockEntity(bep, internalName, multiblockShape, recipeType),
                config.reiConfigs);
        MachineRegistrationHelper.recordMachineLevel(recipeType, MachineLevel.STAINLESS_STEEL);
    }

    public void simpleSteamCraftingMultiBlock(
            // general
            String englishName, String internalName, MachineRecipeType recipeType, ShapeTemplate multiblockShape,
            // REI parameters
            ProgressBar.Params progressBar,
            Consumer<SlotPositions.Builder> itemInputPositions, Consumer<SlotPositions.Builder> itemOutputPositions,
            Consumer<SlotPositions.Builder> fluidInputPositions, Consumer<SlotPositions.Builder> fluidOutputPositions,
            // model
            String controllerCasingName, String overlayFolder, boolean frontOverlay, boolean topOverlay, boolean sideOverlay) {
        simpleSteamCraftingMultiBlock(englishName, internalName, recipeType, multiblockShape, progressBar,
                itemInputPositions, itemOutputPositions, fluidInputPositions, fluidOutputPositions,
                controllerCasingName, overlayFolder, frontOverlay, topOverlay, sideOverlay,
                config -> {});
    }

    public void simpleSteamCraftingMultiBlock(
            // general
            String englishName, String internalName, MachineRecipeType recipeType, ShapeTemplate multiblockShape,
            // REI parameters
            ProgressBar.Params progressBar,
            Consumer<SlotPositions.Builder> itemInputPositions, Consumer<SlotPositions.Builder> itemOutputPositions,
            Consumer<SlotPositions.Builder> fluidInputPositions, Consumer<SlotPositions.Builder> fluidOutputPositions,
            // model
            String controllerCasingName, String overlayFolder, boolean frontOverlay, boolean topOverlay, boolean sideOverlay,
            // optional config
            Consumer<ExtraMachineConfig.CraftingMultiBlock> extraConfig) {
        var config = new ExtraMachineConfig.CraftingMultiBlock();
        config.reiConfigs.add(rei -> rei.steam(true));
        extraConfig.accept(config);

        simpleCraftingMultiBlock(englishName, internalName, recipeType, multiblockShape, progressBar,
                itemInputPositions, itemOutputPositions, fluidInputPositions, fluidOutputPositions,
                controllerCasingName, overlayFolder, frontOverlay, topOverlay, sideOverlay,
                bep -> new SteamCraftingMultiblockBlockEntity(bep, internalName, multiblockShape, recipeType, config.steamOverclockCatalysts),
                config.reiConfigs);
        MachineRegistrationHelper.recordMachineLevel(recipeType, MachineLevel.STEEL);
    }

    private void simpleCraftingMultiBlock(
            // general
            String englishName, String internalName, MachineRecipeType recipeType, ShapeTemplate multiblockShape,
            // REI parameters
            ProgressBar.Params progressBar,
            Consumer<SlotPositions.Builder> itemInputPositions, Consumer<SlotPositions.Builder> itemOutputPositions,
            Consumer<SlotPositions.Builder> fluidInputPositions, Consumer<SlotPositions.Builder> fluidOutputPositions,
            // model
            String controllerCasingName, String overlayFolder, boolean frontOverlay, boolean topOverlay, boolean sideOverlay,
            // machine entity and rei configuration
            Function<BEP, MachineBlockEntity> factory, List<Consumer<MultiblockMachines.Rei>> reiConfigs) {
        simpleMultiBlock(englishName, internalName, multiblockShape, controllerCasingName, overlayFolder, frontOverlay,
                topOverlay, sideOverlay, factory);

        var rei = new MultiblockMachines.Rei(englishName, internalName, recipeType, progressBar)
                .items(itemInputPositions, itemOutputPositions)
                .fluids(fluidInputPositions, fluidOutputPositions);
        for (var reiConfig : reiConfigs) {
            reiConfig.accept(rei);
        }
        rei.register();
    }

    public void simpleGeneratorSingleBlock(
            // General,
            String englishName, String internalName,
            long maxEnergyProduction,
            long energyCapacity,
            long fluidStorageCapacity,
            Consumer<FluidItemConsumerBuilder> builder,
            String casingName, String overlayFolder, boolean frontOverlay, boolean topOverlay, boolean sideOverlay) {
        var componentBuilder = new FluidItemConsumerBuilder(maxEnergyProduction);
        builder.accept(componentBuilder);

        MachineRegistrationHelper.registerMachine(
                englishName, internalName,
                bep -> new GeneratorMachineBlockEntity(bep,
                        internalName,
                        true, // Assume facing
                        energyCapacity,
                        fluidStorageCapacity,
                        componentBuilder.build()),
                MachineBlockEntity::registerItemApi, MachineBlockEntity::registerFluidApi, GeneratorMachineBlockEntity::registerEnergyApi);

        MachineRegistrationHelper.addMachineModel(
                internalName, overlayFolder,
                MachineCasings.get(casingName),
                frontOverlay, topOverlay, sideOverlay);
    }

    public void simpleGeneratorSingleBlock(
            // General,
            String englishName,
            String internalName,
            long maxEnergyProduction,
            long energyCapacity,
            Consumer<FluidItemConsumerBuilder> builder,
            String casingName, String overlayFolder, boolean frontOverlay, boolean topOverlay, boolean sideOverlay) {
        simpleGeneratorSingleBlock(
                englishName, internalName,
                maxEnergyProduction,
                energyCapacity,
                0,
                builder,
                casingName, overlayFolder, frontOverlay, topOverlay, sideOverlay);
    }

    public void simpleGeneratorMultiBlock(
            // general
            String englishName, String internalName, ShapeTemplate multiblockShape,
            long maxEnergyProduction, Consumer<FluidItemConsumerBuilder> builder,
            // model
            String controllerCasingName, String overlayFolder, boolean frontOverlay, boolean topOverlay, boolean sideOverlay) {
        var componentBuilder = new FluidItemConsumerBuilder(maxEnergyProduction);
        builder.accept(componentBuilder);
        Function<BEP, MachineBlockEntity> ctor = bep -> new GeneratorMultiblockBlockEntity(bep, internalName, multiblockShape,
                componentBuilder.build());
        simpleMultiBlock(englishName, internalName, multiblockShape, controllerCasingName, overlayFolder, frontOverlay, topOverlay, sideOverlay,
                ctor);
    }

    public static class FluidItemConsumerBuilder {
        long maxEnergyProduction;

        double feMultiplier = 1;

        FluidItemConsumerComponent.FeProductionMapBuilder<Item> itemFeProductionMapBuilder = new FluidItemConsumerComponent.FeProductionMapBuilder<>(
                BuiltInRegistries.ITEM);

        FluidItemConsumerComponent.FeProductionMapBuilder<Fluid> fluidFeProductionMapBuilder = new FluidItemConsumerComponent.FeProductionMapBuilder<>(
                BuiltInRegistries.FLUID);

        boolean doesAcceptAllFluidFuels = false;
        boolean doesAcceptAllItemFuels = false;

        boolean hideEfficiencyTooltip = false;

        public FluidItemConsumerBuilder(long maxEnergyProduction) {
            this.maxEnergyProduction = maxEnergyProduction;
        }

        public FluidItemConsumerBuilder feMultiplier(double feMultiplier) {
            this.feMultiplier = feMultiplier;
            return this;
        }

        public FluidItemConsumerBuilder fluidFuels() {
            doesAcceptAllFluidFuels = true;
            return this;
        }

        public FluidItemConsumerBuilder furnaceFuels() {
            doesAcceptAllItemFuels = true;
            return this;
        }

        public FluidItemConsumerBuilder fluid(ResourceLocation fluidId, long fePerMb) {
            fluidFeProductionMapBuilder.add(fluidId, fePerMb);
            return this;
        }

        public FluidItemConsumerBuilder item(ResourceLocation itemId, long fePerItem) {
            itemFeProductionMapBuilder.add(itemId, fePerItem);
            return this;
        }

        public FluidItemConsumerBuilder hideEfficiencyTooltip() {
            hideEfficiencyTooltip = true;
            return this;
        }

        public FluidItemConsumerComponent build() {
            return new FluidItemConsumerComponent(
                    maxEnergyProduction,
                    feMultiplier,
                    doesAcceptAllItemFuels ? FluidItemConsumerComponent.itemFuels() : itemFeProductionMapBuilder.build(),
                    doesAcceptAllFluidFuels ? FluidItemConsumerComponent.fluidFuels() : fluidFeProductionMapBuilder.build(),
                    hideEfficiencyTooltip);
        }
    }
}
