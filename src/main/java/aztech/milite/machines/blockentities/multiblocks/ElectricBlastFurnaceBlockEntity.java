package aztech.milite.machines.blockentities.multiblocks;

import static aztech.milite.machines.multiblocks.HatchTypes.*;

import aztech.milite.api.machine.holder.EnergyListComponentHolder;
import aztech.milite.compat.kubejs.KubeJSProxy;
import aztech.milite.compat.rei.machines.ReiMachineRecipes;
import aztech.milite.machines.BEP;
import aztech.milite.machines.components.OrientationComponent;
import aztech.milite.machines.guicomponents.ShapeSelection;
import aztech.milite.machines.guicomponents.RedstoneControlGui;
import aztech.milite.machines.init.MachineLevel;
import aztech.milite.machines.init.MIMachineRecipeTypes;
import aztech.milite.machines.models.MachineCasings;
import aztech.milite.machines.multiblocks.HatchFlags;
import aztech.milite.machines.multiblocks.ShapeTemplate;
import aztech.milite.machines.multiblocks.ShapeMatcher;
import aztech.milite.machines.multiblocks.CoilGlowTracker;
import aztech.milite.machines.multiblocks.SimpleMember;
import aztech.milite.machines.recipe.MachineRecipe;
import aztech.milite.machines.recipe.MachineRecipeType;
import aztech.milite.MI;
import aztech.milite.MIBlock;
import aztech.milite.MIText;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.*;
import java.util.stream.Collectors;

public class ElectricBlastFurnaceBlockEntity extends AbstractElectricCraftingMultiblockBlockEntity implements EnergyListComponentHolder {
    private static final ShapeTemplate[] shapeTemplates;

    public static final List<Tier> tiers;
    public static final Map<ResourceLocation, Tier> tiersByCoil;

    static {
        List<Tier> registrationTiers = new ArrayList<>();
        registrationTiers.add(new Tier("electric", "Cupronickel", MI.id("cupronickel_coil"),
                MachineLevel.ELECTRIC));
        registrationTiers.add(new Tier("stainless_steel", "Kanthal", MI.id("kanthal_coil"),
                MachineLevel.STAINLESS_STEEL));
        registrationTiers.add(new Tier("iridium", "Superconductor", MI.id("superconductor_coil"),
                MachineLevel.IRIDIUM));

        KubeJSProxy.instance.fireAddEbfTiersEvent(tier -> {
            for (var t : registrationTiers) {
                if (t.id().equals(tier.id())) {
                    throw new IllegalArgumentException("An EBF with id " + tier.id() + " is already registered.");
                }
                if (t.coilBlockId().equals(tier.coilBlockId())) {
                    throw new IllegalArgumentException("An EBF with coil " + tier.coilBlockId() + " is already registered.");
                }
                if (t.level() == tier.level()) {
                    throw new IllegalArgumentException("An EBF with level " + tier.level() + " is already registered.");
                }
            }
            registrationTiers.add(tier);
        });
        registrationTiers.sort(Comparator.comparingInt(tier -> tier.level().ordinal()));

        tiers = List.copyOf(registrationTiers);
        tiersByCoil = tiers.stream().collect(Collectors.toMap(Tier::coilBlockId, t -> t));

        shapeTemplates = new ShapeTemplate[tiers.size()];
        for (int i = 0; i < tiers.size(); ++i) {
            shapeTemplates[i] = createShape(tiers.get(i).coilBlockId());
        }
    }

    public ElectricBlastFurnaceBlockEntity(BEP bep) {
        super(bep, "electric_blast_furnace", new OrientationComponent.Params(false, false, false), shapeTemplates);
        registerGuiComponent(new RedstoneControlGui(redstoneControl));
        registerGuiComponent(new ShapeSelection(new ShapeSelection.Behavior() {
            @Override
            public void handleClick(int clickedLine, int delta) {
                activeShape.incrementShape(ElectricBlastFurnaceBlockEntity.this, delta);
            }

            @Override
            public int getCurrentIndex(int line) {
                return activeShape.getActiveShapeIndex();
            }
        }, MIText.ShapeSelectionCoilTitle.text(), MIText.ShapeSelectionCoilDescription.text(), new ShapeSelection.LineInfo(
                tiers.stream().<Component>map(tier -> Component.translatable(tier.coilBlockId().toLanguageKey("block"))).toList(),
                List.of())));
    }

    /**
     * Every shape is the same four heatproof layers, with the coil of its tier in the middle two.
     */
    private static ShapeTemplate createShape(ResourceLocation coilBlockId) {
        SimpleMember heatproofCasings = SimpleMember.forBlock(MIBlock.BLOCK_DEFINITIONS.get(MI.id("heatproof_machine_casing")));
        SimpleMember coils = SimpleMember.forBlockId(coilBlockId);
        HatchFlags hatches = new HatchFlags.Builder().with(ITEM_INPUT, ITEM_OUTPUT, FLUID_INPUT, FLUID_OUTPUT, ENERGY_INPUT).build();
        return new ShapeTemplate.Builder(MachineCasings.HEATPROOF)
                .add3by3(0, heatproofCasings, false, hatches)
                .add3by3(1, coils, true, null)
                .add3by3(2, coils, true, null)
                .add3by3(3, heatproofCasings, false, hatches)
                .build();
    }

    public static void registerReiShapes() {
        for (int i = 0; i < tiers.size(); ++i) {
            ReiMachineRecipes.registerMultiblockShape("electric_blast_furnace", shapeTemplates[i],
                    tiers.get(i).coilBlockId().getPath());
        }
    }

    public Tier getTier() {
        return tiers.get(activeShape.getActiveShapeIndex());
    }

    @Override
    public void tickExtra() {
        if (!(level instanceof ServerLevel server)) return;
        Set<BlockPos> coils = new HashSet<>();
        if (isShapeValid() && getMachineModelData().isActive) {
            for (BlockPos relative : getActiveShape().simpleMembers.keySet()) {
                if (relative.getY() == 1 || relative.getY() == 2) {
                    coils.add(ShapeMatcher.toWorldPos(worldPosition, orientation.facingDirection, relative));
                }
            }
        }
        CoilGlowTracker.update(server, worldPosition, coils);
    }

    @Override
    public void setRemoved() {
        if (level instanceof ServerLevel server) CoilGlowTracker.update(server, worldPosition, Set.of());
        super.setRemoved();
    }

    @Override
    public MachineRecipeType recipeType() {
        return MIMachineRecipeTypes.BLAST_FURNACE;
    }

    /**
     * The tier is the coil of the shape the player built.
     */
    @Override
    public MachineLevel getMachineLevel() {
        return getTier().level();
    }

    /**
     * A furnace runs the recipes of its tier and of every tier below it.
     */
    @Override
    public boolean banRecipe(MachineRecipe recipe) {
        return !getMachineLevel().canCraft(recipe.level);
    }

    @Override
    public int getSpeedBonus() {
        return STANDARD_SPEED_BONUS;
    }

    /**
     * One tier of the electric blast furnace.
     *
     * @param id          the tier id, which KubeJS also uses to add tiers
     * @param englishName the tier name
     * @param coilBlockId the coil of this tier, and the only coil its shape accepts
     * @param level       the level of recipe it runs, and of every tier below it
     */
    public record Tier(String id, String englishName, ResourceLocation coilBlockId, MachineLevel level) {}
}
