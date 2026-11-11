/*
 * MIT License
 *
 * Copyright (c) 2026 Altnoir Null々
 * Copyright (c) 2020 Azercoco & Technici4n
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package aztech.milite;

import aztech.milite.api.datamaps.MIDataMaps;
import aztech.milite.blocks.storage.barrel.BarrelBlock;
import aztech.milite.blocks.WrenchableBlockEntity;
import aztech.milite.compat.ftbquests.FTBQuestsFacade;
import aztech.milite.compat.kubejs.KubeJSProxy;
import aztech.milite.config.MIServerConfig;
import aztech.milite.config.MIStartupConfig;
import aztech.milite.datagen.MIDatagenServer;
import aztech.milite.debug.DebugCommands;
import aztech.milite.definition.BlockDefinition;
import aztech.milite.definition.ItemDefinition;
import aztech.milite.guidebook.GuidebookEvents;
import aztech.milite.guidebook.MIGuide;
import aztech.milite.items.armor.MIArmorEffects;
import aztech.milite.items.armor.MIArmorMaterials;
import aztech.milite.items.armor.MIKeyMap;
import aztech.milite.items.DynamicToolItem;
import aztech.milite.items.SteamDrillHooks;
import aztech.milite.items.SteamDrillItem;
import aztech.milite.items.diesel_tools.DieselToolItem;
import aztech.milite.machines.init.*;
import aztech.milite.machines.multiblocks.world.ChunkEventListeners;
import aztech.milite.machines.multiblocks.CoilGlowTracker;
import aztech.milite.materials.MIMaterials;
import aztech.milite.misc.runtime_datagen.RuntimeDataGen;
import aztech.milite.network.MIPackets;
import aztech.milite.nuclear.FluidNuclearComponent;
import aztech.milite.resource.GeneratedPathPackResources;
import aztech.milite.sounds.MISounds;
import aztech.milite.stats.PlayerStatisticsData;
import aztech.milite.test.framework.MIGameTests;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.BuiltInPackSource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.GatherSkippedAttributeTooltipsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Mod(MI.ID)
public class MI {
    public static final String ID = "moderntech";
    public static final Logger LOGGER = LoggerFactory.getLogger("ModernTech");

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ID, path);
    }

    public MI(ModContainer modContainer, IEventBus modBus, Dist dist) {
        modContainer.registerConfig(ModConfig.Type.SERVER, MIServerConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.STARTUP, MIStartupConfig.SPEC);

        KubeJSProxy.checkThatKubeJsIsLoaded();

        MIAdvancementTriggers.init(modBus);
        MIComponents.init(modBus);
        MIFluids.init(modBus);
        MIBlock.init(modBus);
        MIItem.init(modBus);
        MIRegistries.init(modBus);
        MISounds.init(modBus);
        MIArmorMaterials.init(modBus);
        MIMaterials.init();

        MIMachineRecipeTypes.init();
        SingleBlockCraftingMachines.init();
        SingleBlockSpecialMachines.init();
        MultiblockHatches.init();
        MultiblockMachines.init();
        KubeJSProxy.instance.fireRegisterMachinesEvent();

        MICommonProxy.initEvents();
        FTBQuestsFacade.INSTANCE.init();
        ChunkEventListeners.init();
        CoilGlowTracker.init();
        DebugCommands.init();
        GuidebookEvents.init();
        MIGuide.init();
        MIArmorEffects.init();

        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerChangedDimensionEvent.class, event -> MIKeyMap.clear(event.getEntity()));
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedOutEvent.class, event -> MIKeyMap.clear(event.getEntity()));
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, event -> {
            var player = (ServerPlayer) event.getEntity();
            var server = Objects.requireNonNull(player.getServer());
            PlayerStatisticsData.get(server).get(player).onPlayerJoin(player);
        });
        NeoForge.EVENT_BUS.addListener(VillagerTradesEvent.class, MIVillager::init);

        NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.RightClickBlock.class, event -> {
            if (event.getUseBlock().isFalse()) {
                return;
            }

            var hand = event.getHand();
            var hitResult = event.getHitVec();
            var player = event.getEntity();
            var world = event.getLevel();

            if (player.isSpectator() || !event.getLevel().mayInteract(player, hitResult.getBlockPos())) {
                return;
            }

            if (player.getItemInHand(hand).is(MITags.WRENCHES)) {
                if (world.getBlockEntity(hitResult.getBlockPos()) instanceof WrenchableBlockEntity wrenchable) {
                    if (wrenchable.useWrench(player, hand, hitResult)) {
                        event.setCanceled(true);
                        event.setCancellationResult(InteractionResult.sidedSuccess(world.isClientSide()));
                    }
                }
            }
        });
        BarrelBlock.setupBarrelEvents();

        SteamDrillHooks.init();
        NeoForge.EVENT_BUS.addListener(AnvilUpdateEvent.class, event -> {
            if (event.getLeft().getItem() instanceof SteamDrillItem || event.getLeft().getItem() instanceof DieselToolItem) {
                return;
            }
            // Other dynamic tools only support renaming.
            if ((event.getLeft().getItem() instanceof DynamicToolItem && !event.getRight().isEmpty()) ||
                    (!event.getLeft().isEmpty() && event.getRight().getItem() instanceof DynamicToolItem)) {
                event.setCanceled(true);
                if (event.getPlayer().containerMenu instanceof AnvilMenu anvilMenu) {
                    anvilMenu.getSlot(2).set(ItemStack.EMPTY);
                    anvilMenu.setMaximumCost(0);
                }
            }
        });

        modBus.addListener(EntityAttributeModificationEvent.class, event -> {
            for (EntityType<? extends LivingEntity> entityType : event.getTypes()) {
                event.add(entityType, MIRegistries.QUANTUM_ARMOR);
                event.add(entityType, MIRegistries.INFINITE_DAMAGE);
            }
        });
        NeoForge.EVENT_BUS.addListener(LivingIncomingDamageEvent.class, event -> {
            if (event.getSource().getDirectEntity() instanceof LivingEntity damager
                    && damager.getAttributeValue(MIRegistries.INFINITE_DAMAGE) > Mth.EPSILON) {
                event.setAmount((float) Integer.MAX_VALUE);
            }
        });
        NeoForge.EVENT_BUS.addListener(GatherSkippedAttributeTooltipsEvent.class, event -> {
            // Hides the attack damage attribute on items when the infinite damage attribute is present
            boolean hasInfiniteDamageAttribute = event.getStack().getAttributeModifiers().modifiers().stream()
                    .anyMatch((modifier) -> modifier.attribute().is(MIRegistries.INFINITE_DAMAGE.getKey()));
            if (hasInfiniteDamageAttribute) {
                for (ItemAttributeModifiers.Entry modifier : event.getStack().getAttributeModifiers().modifiers()) {
                    if (modifier.attribute().is(Attributes.ATTACK_DAMAGE.getKey())) {
                        event.skipId(modifier.modifier().id());
                    }
                }
            }
        });

        modBus.addListener(FMLCommonSetupEvent.class, event -> {
            MIBlock.BLOCK_DEFINITIONS.values().forEach(BlockDefinition::onRegister);
            MIItem.ITEM_DEFINITIONS.values().forEach(ItemDefinition::onRegister);

            FluidNuclearComponent.init();
        });

        modBus.addListener(GatherDataEvent.class, event -> {
            MIDatagenServer.configure(
                    event.getGenerator(),
                    event.getExistingFileHelper(),
                    event.getLookupProvider(),
                    event.includeServer(),
                    false);
        });

        modBus.addListener(RegisterCapabilitiesEvent.class, MICapabilities::init);
        modBus.addListener(RegisterPayloadHandlersEvent.class, MIPackets::init);

        modBus.addListener(RegisterDataMapTypesEvent.class, event -> {
            event.register(MIDataMaps.FLUID_FUELS);
            event.register(MIDataMaps.ITEM_TOOLTIPS);
        });

        modBus.addListener(AddPackFindersEvent.class, event -> {
            if (dist == Dist.DEDICATED_SERVER && event.getPackType() == PackType.SERVER_DATA
                    && MIStartupConfig.INSTANCE.datagenOnStartup.getAsBoolean()) {
                RuntimeDataGen.run(MIDatagenServer::configure);
            }

            if (MIStartupConfig.INSTANCE.loadRuntimeGeneratedResources.getAsBoolean()) {
                event.addRepositorySource(consumer -> {
                    consumer.accept(new Pack(
                            new PackLocationInfo(
                                    "moderntech/generated",
                                    MIText.GeneratedResources.text(),
                                    PackSource.BUILT_IN,
                                    Optional.empty()),
                            BuiltInPackSource.fixedResources(new GeneratedPathPackResources(
                                    FMLPaths.GAMEDIR.get().resolve("moderntech/generated_resources"),
                                    event.getPackType())),
                            new Pack.Metadata(
                                    MIText.GeneratedResourcesDescription.text(),
                                    PackCompatibility.COMPATIBLE,
                                    FeatureFlagSet.of(),
                                    List.of(),
                                    false),
                            new PackSelectionConfig(true, Pack.Position.TOP, false)));
                });
            }
        });

        modBus.addListener(RegisterGameTestsEvent.class, event -> {
            event.register(MIGameTests.class);
        });

        LOGGER.info("ModernTech setup done!");
    }
}
