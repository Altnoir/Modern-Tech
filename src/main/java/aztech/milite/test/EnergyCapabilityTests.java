package aztech.milite.test;

import aztech.milite.MI;
import aztech.milite.machines.blockentities.GeneratorMachineBlockEntity;
import aztech.milite.machines.components.EnergyComponent;
import aztech.milite.test.framework.MIGameTest;
import aztech.milite.test.framework.MIGameTestHelper;
import aztech.milite.util.Simulation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.capabilities.Capabilities;

public class EnergyCapabilityTests {
    @MIGameTest
    public void machineAndGeneratorEnforceEnergyDirection(MIGameTestHelper helper) {
        var machinePos = new BlockPos(0, 1, 0);
        var generatorPos = new BlockPos(2, 1, 0);
        helper.setBlock(machinePos, BuiltInRegistries.BLOCK.get(MI.id("electric_macerator")));
        helper.setBlock(generatorPos, BuiltInRegistries.BLOCK.get(MI.id("electric_diesel_generator")));

        var input = helper.requireCapability(Capabilities.EnergyStorage.BLOCK, machinePos, Direction.NORTH);
        helper.assertValueEqual(input.receiveEnergy(400, true), 400, "simulated machine input");
        helper.assertValueEqual(input.getEnergyStored(), 0, "simulation preserves machine buffer");
        helper.assertValueEqual(input.receiveEnergy(400, false), 400, "machine input");
        helper.assertValueEqual(input.extractEnergy(100, true), 0, "machine rejects simulated extraction");
        helper.assertValueEqual(input.extractEnergy(100, false), 0, "machine rejects extraction");
        helper.assertValueEqual(input.getEnergyStored(), 400, "machine buffer after rejected extraction");

        var generator = (GeneratorMachineBlockEntity) helper.getBlockEntity(generatorPos);
        generator.getEnergyComponent().insertFe(400, Simulation.ACT);
        var output = helper.requireCapability(Capabilities.EnergyStorage.BLOCK, generatorPos, Direction.NORTH);
        helper.assertValueEqual(output.receiveEnergy(100, true), 0, "generator rejects simulated input");
        helper.assertValueEqual(output.receiveEnergy(100, false), 0, "generator rejects input");
        helper.assertValueEqual(output.extractEnergy(100, true), 100, "simulated generator output");
        helper.assertValueEqual(output.getEnergyStored(), 400, "simulation preserves generator buffer");
        helper.assertValueEqual(output.extractEnergy(100, false), 100, "generator output");
        helper.assertValueEqual(output.getEnergyStored(), 300, "generator buffer after extraction");
        helper.assertValueEqual(output.extractEnergy(-1, false), 0, "negative extraction");
        helper.assertValueEqual(input.receiveEnergy(-1, false), 0, "negative input");
        helper.succeed();
    }

    @MIGameTest
    public void energyCapabilityRespectsCapacityAndSavedFe(MIGameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, BuiltInRegistries.BLOCK.get(MI.id("electric_macerator")));
        long[] capacity = {1000};
        var energy = new EnergyComponent(helper.getBlockEntity(pos), () -> capacity[0]);
        var output = energy.buildExtractable();
        var registries = helper.getLevel().registryAccess();
        var saved = new CompoundTag();
        saved.putLong("storedFe", 500);
        energy.readNbt(saved, registries, false);
        helper.assertValueEqual(output.getEnergyStored(), 500, "saved energy");

        capacity[0] = 100;
        helper.assertValueEqual(output.getEnergyStored(), 100, "energy after capacity shrinks");
        helper.assertValueEqual(output.getMaxEnergyStored(), 100, "reduced capacity");
        helper.assertValueEqual(output.extractEnergy(200, true), 100, "bounded simulated output");
        helper.assertValueEqual(output.extractEnergy(200, false), 100, "bounded output");
        helper.assertValueEqual(output.getEnergyStored(), 0, "empty buffer");

        saved.putLong("storedFe", -500);
        energy.readNbt(saved, registries, false);
        helper.assertValueEqual(output.getEnergyStored(), 0, "negative save energy");
        energy.insertFe(50, Simulation.ACT);
        energy.writeNbt(saved, registries);
        helper.assertValueEqual(saved.getLong("storedFe"), 50L, "saved FE");
        helper.succeed();
    }
}
