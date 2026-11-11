package aztech.milite.datagen.advancement;

import aztech.milite.datagen.translation.TranslationProvider;
import aztech.milite.MI;
import aztech.milite.MIAdvancementTriggers;
import aztech.milite.MIItem;
import aztech.milite.MIText;
import aztech.milite.advancement.BuiltMultiblockTrigger;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.Optional;

public record MIAdvancementsProvider(TranslationProvider translations) implements AdvancementProvider.AdvancementGenerator {
    private static Criterion<?> createSimpleCriterion(Supplier<PlayerTrigger> trigger) {
        return trigger.get().createCriterion(new PlayerTrigger.TriggerInstance(Optional.empty()));
    }

    private Component translateTitle(String path, String englishName) {
        var titleKey = "advancements.moderntech." + path;
        translations.addTranslation(titleKey, englishName);
        return Component.translatable(titleKey);
    }

    private Component translateDescription(String path, String englishDescription) {
        var descKey = "advancements.moderntech." + path + ".description";
        translations.addTranslation(descKey, englishDescription);
        return Component.translatable(descKey);
    }

    private class Builder extends Advancement.Builder {
        private final String path;

        private Builder(String path) {
            this.path = path;
        }

        @Override
        public Builder sendsTelemetryEvent() {
            return (Builder) super.sendsTelemetryEvent();
        }

        @Override
        public Builder display(ItemLike pIcon, Component pTitle, Component pDescription, @Nullable ResourceLocation pBackground,
                AdvancementType pType, boolean pShowToast, boolean pAnnounceChat, boolean pHidden) {
            return (Builder) super.display(pIcon, pTitle, pDescription, pBackground, pType, pShowToast, pAnnounceChat, pHidden);
        }

        public Builder display(
                ItemLike icon,
                String titleEnglishName,
                String englishDescription,
                @Nullable ResourceLocation pBackground,
                AdvancementType pType,
                boolean pShowToast,
                boolean pAnnounceChat,
                boolean pHidden) {
            return this.display(
                    icon,
                    translateTitle(path, titleEnglishName),
                    translateDescription(path, englishDescription),
                    pBackground,
                    pType,
                    pShowToast,
                    pAnnounceChat,
                    pHidden);
        }

        @Override
        public Builder parent(AdvancementHolder pParent) {
            return (Builder) super.parent(pParent);
        }

        @Override
        public Builder addCriterion(String pKey, Criterion<?> pCriterion) {
            return (Builder) super.addCriterion(pKey, pCriterion);
        }

        public AdvancementHolder save(Consumer<AdvancementHolder> consumer, ExistingFileHelper existingFileHelper) {
            return save(consumer, MI.id(path), existingFileHelper);
        }
    }

    private Builder newBuilder(String path) {
        return new Builder(path).sendsTelemetryEvent();
    }

    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> consumer, ExistingFileHelper existingFileHelper) {
        var guidebook = newBuilder("guidebook")
                .display(MIItem.GUIDE_BOOK,
                        MIText.ModernIndustrialization.text(),
                        translateDescription("guidebook", "Welcome to ModernTech! Make sure to check out the guidebook."),
                        MI.id("textures/block/fire_clay_bricks.png"),
                        AdvancementType.TASK,
                        false,
                        false,
                        false)
                .addCriterion("logged_in", createSimpleCriterion(MIAdvancementTriggers.PLAYER_LOGGED_IN))
                .save(consumer, existingFileHelper);

        // @formatter:off
        var forgeHammer = createBasic(consumer, "forge_hammer", guidebook, AdvancementType.GOAL, "Is This A Forge Mod?", "Craft a Forge Hammer and start exploring the mod.", existingFileHelper);
        var steamMiningDrill = createBasic(consumer, "steam_mining_drill", forgeHammer, AdvancementType.GOAL, "Getting That 3x3 Going", "Craft a Steam Mining Drill", existingFileHelper);
        var fireClayBricks = createBasic(consumer, "fire_clay_bricks", forgeHammer, "Almost Steam?", "Craft Fire Clay Bricks", existingFileHelper);
        var bronzeFurnace = createBasic(consumer, "bronze_furnace", fireClayBricks, "Ten Times More Fuel Efficient", "Craft a Bronze Furnace", existingFileHelper);
        var bronzeCompressor = createBasic(consumer, "bronze_compressor", forgeHammer, "An Automatic Forge Mod", "Craft a Bronze Compressor", existingFileHelper);
        var bronzeMixer = createBasic(consumer, "bronze_mixer", forgeHammer, "Mixing Without Mixins", "Craft a Bronze Mixer", existingFileHelper);
        var bronzeMacerator = createBasic(consumer, "bronze_macerator", forgeHammer, "Ore Hemiquadrupling", "Craft a Bronze Macerator", existingFileHelper);
        var cokeOven = createBasic(consumer, "coke_oven", forgeHammer, AdvancementType.GOAL, "Coke-A Cola", "Craft a Coke Oven to start Steel Production", existingFileHelper);
        var steamBlastFurnace = createBasic(consumer, "steam_blast_furnace", fireClayBricks, "Almost Steel!", "Craft a Steam Blast Furnace", existingFileHelper);
        var steelMachineCasing = createBasic(consumer, "steel_machine_casing", steamBlastFurnace, AdvancementType.GOAL, "Cooked Uncooked Steel!", "Use a Steam Blast Furnace to produce Steel and craft a Steel Machine Casing", existingFileHelper);
        // @formatter:on

        // @formatter:off
        var steelWiremill = createBasic(consumer, "steel_wiremill", steelMachineCasing, "Neither a Wire nor a Mill", "Craft a Steel Wiremill", existingFileHelper);
        var steamQuarry = createMultiblock(consumer, "steam_quarry", steelMachineCasing, AdvancementType.GOAL, "From Minecraft to Craft", "Build a Steam Quarry and say goodbye to mining", existingFileHelper);
        var steelPacket = createBasic(consumer, "steel_packer", steelMachineCasing, "To Pack Or Not To Pack", "Craft a Steel Packer", existingFileHelper);
        var inductor = createBasic(consumer, "inductor", steelWiremill, "The L in RLC", "Craft an Inductor", existingFileHelper);
        var resistor = createBasic(consumer, "resistor", steelWiremill, "The R in RLC", "Craft a Resistor", existingFileHelper);
        var capacitor = createBasic(consumer, "capacitor", steelWiremill, "The C in RLC", "Craft a Capacitor", existingFileHelper);
        var analogCircuit = createBasic(consumer, "analog_circuit", resistor, AdvancementType.GOAL, "RLC Circuits", "Craft an Analog Circuit and start the Electric Age", existingFileHelper);
        var lvSteamTurbine = createBasic(consumer, "electric_steam_turbine", analogCircuit, "Better Than Solar Panels", "Craft a Steam Turbine", existingFileHelper);
        var polarizer = createBasic(consumer, "electric_polarizer", "aluminum_alloy_polarizer", lvSteamTurbine, AdvancementType.TASK, "One Recipe (+2) To Rule Them All", "Craft a Polarizer", existingFileHelper);
        var largeSteamBoiler = createMultiblock(consumer, "large_steam_boiler", analogCircuit, "Kiss Your Fuel Goodbye!", "Build a Large Steam Boiler", existingFileHelper);
        var assembler = createBasic(consumer, "electric_assembler", analogCircuit, AdvancementType.GOAL, "Avengers, Assemble!", "Craft an Assembler", existingFileHelper);
        var electricBlastFurnace = createMultiblock(consumer, "electric_blast_furnace", lvSteamTurbine, AdvancementType.GOAL, "Electric Best Friend", "Build an Electric Blast Furnace to start producing Aluminum", existingFileHelper);
        var electronicCircuit = createBasic(consumer, "electronic_circuit", electricBlastFurnace, "The Power of Silicon", "Craft a Electronic Circuit", existingFileHelper);
        var dieselJetpack = createBasic(consumer, "diesel_jetpack", electricBlastFurnace, AdvancementType.CHALLENGE, "Ely... We Meant Jetpack!", "Craft a Diesel Jetpack", existingFileHelper);
        var dieselChainsaw = createBasic(consumer, "diesel_chainsaw", electricBlastFurnace, AdvancementType.CHALLENGE, "The Texas Chain Saw Massacre", "Craft a Diesel Chainsaw", existingFileHelper);
        var dieselMiningDrill = createBasic(consumer, "diesel_mining_drill", electricBlastFurnace, AdvancementType.CHALLENGE, "Through The Walls Of Ba Sing Se", "Craft a Diesel Mining Drill", existingFileHelper);
        var centrifuge = createBasic(consumer, "electric_centrifuge", "aluminum_alloy_centrifuge", electronicCircuit, AdvancementType.TASK, "Actually It's The Centripetal Force", "Craft a Centrifuge", existingFileHelper);
        createMultiblock(consumer, "water_barrel", centrifuge, AdvancementType.GOAL, "Mad Waterworld", "Build a solid 3×3×3 cube of Water Barrels", existingFileHelper);
        var electrolyzer = createBasic(consumer, "electric_electrolyzer", "aluminum_alloy_electrolyzer", electronicCircuit, AdvancementType.TASK, "It's Got What Plants Crave", "Craft an Electrolyzer", existingFileHelper);
        var chemicalReactor = createBasic(consumer, "electric_chemical_reactor", "aluminum_alloy_chemical_reactor", electronicCircuit, AdvancementType.TASK, "Walter White Approves", "Craft a Chemical Reactor", existingFileHelper);
        var distillery = createBasic(consumer, "electric_distillery", "aluminum_alloy_distillery", electronicCircuit, AdvancementType.TASK, "Al Capone Approves", "Craft a Distillery", existingFileHelper);
        var electricQuarry = createMultiblock(consumer, "electric_quarry", electronicCircuit, AdvancementType.CHALLENGE, "Resource Goes BRRRRRR!!!", "Build an Electric Quarry", existingFileHelper);
        var oilDrillingRig = createMultiblock(consumer, "oil_drilling_rig", electronicCircuit, AdvancementType.GOAL, "Bringing Freedom To Your Country", "Build an Oil Drilling Rig", existingFileHelper);
        var vacuumFreezer = createMultiblock(consumer, "vacuum_freezer", electronicCircuit, AdvancementType.GOAL, "Enslaved Winter", "Build a Vacuum Freezer", existingFileHelper);
        var mvSteamTurbine = createBasic(consumer, "aluminum_alloy_steam_turbine", electronicCircuit, AdvancementType.GOAL, "Better Than Wind Mills", "Craft an Aluminum Alloy Steam Turbine", existingFileHelper);
        var dieselGenerator = createBasic(consumer, "aluminum_alloy_diesel_generator", distillery, "Fast and Furious", "Craft an Aluminum Alloy Diesel Generator", existingFileHelper);
        var digitalCircuit = createBasic(consumer, "digital_circuit", "digital_circuit", distillery, AdvancementType.TASK, "Digital Logic", "Craft a Digital Circuit", existingFileHelper);
        var turboDieselGenerator = createBasic(consumer, "stainless_steel_diesel_generator", dieselGenerator, "Fast and Furious 2: Revenge", "Craft a Stainless Steel Diesel Generator", existingFileHelper);
        var largeDieselGenerator = createMultiblock(consumer, "large_diesel_generator", turboDieselGenerator, "Fast and Furious 42: Armageddon", "Build a Large Diesel Generator", existingFileHelper);
        var hvSteamTurbine = createBasic(consumer, "stainless_steel_steam_turbine", mvSteamTurbine, "Better than Water Wheel", "Craft a Stainless Steel Steam Turbine", existingFileHelper);
        var largeSteamTurbine = createMultiblock(consumer, "large_steam_turbine", hvSteamTurbine, "Enslaved Hurricane", "Build a Large Steam Turbine", existingFileHelper);
        var distillationTower = createMultiblock(consumer, "distillation_tower", digitalCircuit, AdvancementType.GOAL, "TOTAL™ Distillation", "Build a Distillation Tower to unlock to full potential of oil processing", existingFileHelper);
        var heatExchanger = createMultiblock(consumer, "heat_exchanger", digitalCircuit, "Lava Power but Balanced",  "Build a Heat Exchanger to avoid losing high pressure (and cheese easy energy from lava production)", existingFileHelper);
        var stainlessSteel = createBasic(consumer, "stainless_steel_ingot", vacuumFreezer, AdvancementType.GOAL, "Invar with Slot Locking", "Craft a Stainless Steel Ingot", existingFileHelper);
        var kanthalCoil = createMultiblock(consumer, "kanthal_coil", "electric_blast_furnace", "kanthal_coil", stainlessSteel, "Electric Better Furnace", "Build an EBF with Kanthal Coil to unlock new recipes", existingFileHelper);
        var processor = createBasic(consumer, "processing_unit", "processor", kanthalCoil, AdvancementType.TASK, "More Processing Power", "Produce a Processor in a Stainless Steel Lithography Machine", existingFileHelper);
        var titaniumIngot = createBasic(consumer, "titanium_ingot", kanthalCoil, "Steel but Pink", "Craft a Titanium Ingot", existingFileHelper);
        var blastProofAlloyPlate = createBasic(consumer, "blastproof_alloy_plate", titaniumIngot, "Creeper Nightmare", "Craft a Blastproof Alloy Plate in the compressor", existingFileHelper);
        var implosionCompressor = createMultiblock(consumer, "implosion_compressor", blastProofAlloyPlate, AdvancementType.GOAL, "Automated Creeper", "Build an Implosion Compressor", existingFileHelper);
        var pressurizer = createMultiblock(consumer, "pressurizer", titaniumIngot, "Under Pressure", "Build a Pressurizer to unlock for more efficient steam processing", existingFileHelper);
        var rawIridium = createBasic(consumer, "raw_iridium", titaniumIngot, "Diamond 2.0: Electric Boogaloo", "Obtain a Raw Piece of Iridium", existingFileHelper);
        var gravichestplate = createBasic(consumer, "gravichestplate", rawIridium, AdvancementType.CHALLENGE, "Gravichestplate™", "Craft a Gravichestplate to unlock creative flight", existingFileHelper);
        var nuclearReactor = createMultiblock(consumer, "nuclear_reactor", implosionCompressor, AdvancementType.CHALLENGE, "3.6 Roentgen", "Build a Nuclear Reactor and discover its overengineered mechanisms", existingFileHelper);
        var nuke = createBasic(consumer, "nuke", nuclearReactor, "I've become Death, Destroyer of Worlds", "Craft a Nuke", existingFileHelper);
        var singularity = createBasic(consumer, "singularity", nuke, "Pocket Black Hole", "Craft a Singularity", existingFileHelper);
        var mixedIngotIridium = createBasic(consumer, "mixed_ingot_iridium", implosionCompressor, "Oreo Ingot", "Craft a Mixed Ingot Iridium to craft Iridium Plates", existingFileHelper);
        var quantumProcessor = createBasic(consumer, "quantum_circuit", "quantum_processor", rawIridium, AdvancementType.GOAL, "Quantum Computing", "Produce a Quantum Processor in a Titanium Lithography Machine", existingFileHelper);
        var fusionReactor = createMultiblock(consumer, "fusion_reactor", quantumProcessor, AdvancementType.CHALLENGE, "Enslaved Star Core", "Build a Fusion Reactor to produce an insane amount of energy", existingFileHelper);
        var plasmaTurbine = createMultiblock(consumer, "plasma_turbine", fusionReactor, "Better than Nuclear Fission", "Build a Plasma Turbine to transform Helium Plasma into energy", existingFileHelper);
        var quantumUpgrade = createBasic(consumer, "quantum_upgrade", quantumProcessor, AdvancementType.GOAL, "Time Dilation", "Craft a Quantum Upgrade to unlock unlimited recipe speed", existingFileHelper);
        var replicator = createBasic(consumer, "replicator", quantumUpgrade, AdvancementType.CHALLENGE, "Legal Duping", "Craft a Replicator and replicate any item you want using UU Matter", existingFileHelper);
        var uuMatter = createBasic(consumer, "uu_matter_bucket", singularity, AdvancementType.GOAL, "Liquid Creative Mode", "Produce a bucket of UU Matter to start duplication in the replicator", existingFileHelper);
        var quantumSword = createBasic(consumer, "quantum_sword", quantumUpgrade, AdvancementType.CHALLENGE, "Annihilation Operator", "Craft a Quantum Sword and disintegrate your foes (or the Wandering Trader's llamas)", existingFileHelper);
        var quantumChestplate = createBasic(consumer, "quantum_chestplate", quantumUpgrade, AdvancementType.CHALLENGE, "Quantum Immortality™", "Craft a Quantum Chestplate to reduce the probability of taking any damage by 25% for each piece of the Quantum Armor Set", existingFileHelper);
        // @formatter:on

        newBuilder("matches_multiple_recipes")
                .parent(forgeHammer)
                .display(MIItem.WRENCH,
                        "Error 418 - I'm a teapot",
                        "Have a machine fail to start running because it matched multiple recipes.",
                        null,
                        AdvancementType.GOAL,
                        true,
                        true,
                        true)
                .addCriterion("matches_multiple_recipes", createSimpleCriterion(MIAdvancementTriggers.OVERLAPPING_RECIPES))
                .save(consumer, existingFileHelper);
    }

    private AdvancementHolder createBasic(Consumer<AdvancementHolder> consumer, String item, AdvancementHolder parent,
            String titleEnglishName, String englishDescription, ExistingFileHelper existingFileHelper) {
        return createBasic(consumer, item, parent, AdvancementType.TASK, titleEnglishName, englishDescription, existingFileHelper);
    }

    private AdvancementHolder createBasic(Consumer<AdvancementHolder> consumer, String itemId, AdvancementHolder parent,
            AdvancementType frame, String titleEnglishName, String englishDescription, ExistingFileHelper existingFileHelper) {
        return createBasic(consumer, itemId, itemId, parent, frame, titleEnglishName, englishDescription, existingFileHelper);
    }

    private AdvancementHolder createBasic(Consumer<AdvancementHolder> consumer, String advancementId, String itemId, AdvancementHolder parent,
            AdvancementType frame, String titleEnglishName, String englishDescription, ExistingFileHelper existingFileHelper) {
        var item = BuiltInRegistries.ITEM.get(MI.id(itemId));

        var advancementTask = newBuilder(advancementId);

        advancementTask.parent(parent);
        advancementTask.display(
                item,
                titleEnglishName,
                englishDescription,
                null,
                frame,
                true,
                true,
                false);
        advancementTask.addCriterion("checkInv", InventoryChangeTrigger.TriggerInstance.hasItems(item));

        return advancementTask.save(consumer, existingFileHelper);
    }

    private AdvancementHolder createMultiblock(Consumer<AdvancementHolder> consumer, String machineId, AdvancementHolder parent,
            AdvancementType frame, String titleEnglishName, String englishDescription, ExistingFileHelper existingFileHelper) {
        return createMultiblock(consumer, machineId, machineId, null, parent, frame, titleEnglishName, englishDescription, existingFileHelper);
    }

    private AdvancementHolder createMultiblock(Consumer<AdvancementHolder> consumer, String machineId, AdvancementHolder parent,
            String titleEnglishName, String englishDescription, ExistingFileHelper existingFileHelper) {
        return createMultiblock(consumer, machineId, parent, AdvancementType.TASK, titleEnglishName, englishDescription, existingFileHelper);
    }

    private AdvancementHolder createMultiblock(Consumer<AdvancementHolder> consumer, String icon, String machineId, @Nullable String shapeId, AdvancementHolder parent,
            String titleEnglishName, String englishDescription, ExistingFileHelper existingFileHelper) {
        return createMultiblock(consumer, icon, machineId, shapeId, parent, AdvancementType.TASK, titleEnglishName, englishDescription, existingFileHelper);
    }

    private AdvancementHolder createMultiblock(Consumer<AdvancementHolder> consumer, String icon, String machineId, @Nullable String shapeId, AdvancementHolder parent,
            AdvancementType frame, String titleEnglishName, String englishDescription, ExistingFileHelper existingFileHelper) {
        var item = BuiltInRegistries.ITEM.get(MI.id(icon));

        var advancementTask = newBuilder(shapeId != null ? shapeId : machineId);

        advancementTask.parent(parent);
        advancementTask.display(
                item,
                titleEnglishName,
                englishDescription,
                null,
                frame,
                true,
                true,
                false);
        advancementTask.addCriterion("buildMultiblock", BuiltMultiblockTrigger.builtMultiblock(MI.id(machineId), shapeId));

        return advancementTask.save(consumer, existingFileHelper);
    }
}
