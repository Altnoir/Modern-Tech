package aztech.milite.client.datagen.model;

import aztech.milite.api.energy.EnergyTier;
import aztech.milite.client.machines.models.MachineBakedModel;
import aztech.milite.machines.models.MachineCasing;
import aztech.milite.machines.models.MachineCasings;
import aztech.milite.MI;
import aztech.milite.MIBlock;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class MachineCasingsProvider extends ModelProvider<BlockModelBuilder> {
    public MachineCasingsProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MI.ID, MachineBakedModel.CASING_FOLDER, BlockModelBuilder::new, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (var casing : MachineCasings.registeredCasings.values()) {
            if (casing.imitatedBlock != null) {
                imitateBlock(casing, casing.imitatedBlock.get());
            }
        }

        imitateBlock(EnergyTier.ELECTRIC.casing, MIBlock.ELECTRIC_MACHINE_HULL.asBlock());
        imitateBlock(EnergyTier.ALUMINUM.casing, MIBlock.ALUMINUM_MACHINE_HULL.asBlock());
        imitateBlock(EnergyTier.STAINLESS_STEEL.casing, MIBlock.STAINLESS_STEEL_MACHINE_HULL.asBlock());
        imitateBlock(EnergyTier.TITANIUM.casing, MIBlock.TITANIUM_MACHINE_HULL.asBlock());
        imitateBlock(EnergyTier.IRIDIUM.casing, MIBlock.IRIDIUM_MACHINE_HULL.asBlock());

        cubeBottomTop(MachineCasings.BRICKED_BRONZE, "block/casings/bricked_bronze", "block/fire_clay_bricks", "block/bronze_machine_casing");
        cubeBottomTop(MachineCasings.BRICKED_STEEL, "block/casings/bricked_steel/side", "block/casings/bricked_steel/bottom",
                "block/casings/bricked_steel/top");
        cubeAll(MachineCasings.CONFIGURABLE_TANK, "block/casings/configurable_tank");
        cubeAll(MachineCasings.STEEL_CRATE, "block/casings/steel_crate");
    }

    private void imitateBlock(MachineCasing casing, Block block) {
        getBuilder(casing.key.toString())
                .customLoader((bmb, existingFileHelper) -> new UseBlockModelModelBuilder<>(block, bmb, existingFileHelper));
    }

    private void cubeBottomTop(MachineCasing casing, String side, String bottom, String top) {
        cubeBottomTop(casing.key.toString(), MI.id(side), MI.id(bottom), MI.id(top));
    }

    private void cubeAll(MachineCasing casing, String side) {
        cubeAll(casing.key.toString(), MI.id(side));
    }

    @Override
    public String getName() {
        return "Machine Casings";
    }
}
