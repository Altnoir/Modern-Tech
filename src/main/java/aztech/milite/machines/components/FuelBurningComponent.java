package aztech.milite.machines.components;

import aztech.milite.api.datamaps.FluidFuel;
import aztech.milite.inventory.ConfigurableFluidStack;
import aztech.milite.inventory.ConfigurableItemStack;
import aztech.milite.machines.MachineComponent;
import aztech.milite.MIFluids;
import aztech.milite.MIText;
import aztech.milite.MITooltips;
import aztech.milite.util.ItemStackHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class FuelBurningComponent implements MachineComponent {
    /**
     * How many FEs worth of heat can be produced every tick at most.
     */
    public final long maxFeProduction;
    /**
     * How many FEs one degree of heat is worth.
     */
    public final long fePerDegree;
    public final TemperatureComponent temperature;
    /**
     * Multiplier on the efficiency of the boiler, i.e. the number of FEs produced
     * per burn tick.
     */
    private final double burningItemFeMultiplier;

    /**
     * How many FEs one furnace burn tick is worth. Remembering that 1 furnace
     * recipe = 2 FE/t for 10 seconds, FE_PER_BURN_TICK/2 is the efficiency
     * multiplier.
     */
    public static final long FE_PER_BURN_TICK = 20;

    /**
     * Buffer of FE that was burnt already, and is awaiting to be turned into heat.
     */
    private long burningFeBuffer;
    /**
     * Total FE of the currently burning item. If 0 it means that we are burning fluid instead.
     */
    private long burningItemTotalFe;

    public FuelBurningComponent(TemperatureComponent temperature, long maxFeProduction, long fePerDegree, double burningItemFeMultiplier) {
        this.temperature = temperature;
        this.maxFeProduction = maxFeProduction;
        this.fePerDegree = fePerDegree;
        this.burningItemFeMultiplier = burningItemFeMultiplier;
    }

    public FuelBurningComponent(TemperatureComponent temperature, long maxFeProduction, long fePerDegree) {
        this(temperature, maxFeProduction, fePerDegree, 1);
    }

    public FuelBurningComponent(SteamHeaterComponent steamHeater, double burningItemFeMultiplier) {
        this(steamHeater, steamHeater.maxFeProduction, steamHeater.fePerDegree, burningItemFeMultiplier);
    }

    public FuelBurningComponent(SteamHeaterComponent steamHeater) {
        this(steamHeater, steamHeater.maxFeProduction, steamHeater.fePerDegree, 1);
    }

    public boolean isBurning() {
        return burningFeBuffer > 0;
    }

    public void clearActiveFuel() {
        burningFeBuffer = 0;
        burningItemTotalFe = 0;
    }

    public double getBurningProgress() {
        if (burningItemTotalFe == 0) {
            return Math.min(1.0, (double) burningFeBuffer / (5 * 20 * maxFeProduction));
        } else {
            return Math.min(1.0, (double) burningFeBuffer / burningItemTotalFe);
        }
    }

    public void tick(List<ConfigurableItemStack> itemInputs, List<ConfigurableFluidStack> fluidInputs, boolean canConsumeNewFuel) {
        // Turn buffer into heat
        long maxFeInsertion = Math.min(burningFeBuffer, maxFeProduction);

        // Use ceil here to be able to reach the max temperature exactly, even if it wastes a fraction of an FE.
        maxFeInsertion = Math.min(maxFeInsertion, (long) Math.ceil(fePerDegree * (temperature.temperatureMax - temperature.getTemperature())));
        if (maxFeInsertion > 0) {
            burningFeBuffer -= maxFeInsertion;
            temperature.increaseTemperature((double) maxFeInsertion / fePerDegree);
        } else if (burningFeBuffer == 0) {
            temperature.decreaseTemperature(1);
        }

        if (!canConsumeNewFuel) {
            return;
        }

        // Refill buffer with item fuel
        outer:
        while (burningFeBuffer < maxFeProduction) {
            // Find first item fuel
            for (ConfigurableItemStack stack : itemInputs) {
                var fuel = stack.getResource().toStack((int) stack.getAmount());
                if (ItemStackHelper.consumeFuel(stack, true)) {
                    int fuelTime = fuel.getBurnTime(null);
                    if (fuelTime > 0) {
                        long fuelTotalFe = (long) (fuelTime * FE_PER_BURN_TICK * burningItemFeMultiplier);
                        burningFeBuffer += fuelTotalFe;
                        burningItemTotalFe = fuelTotalFe;
                        ItemStackHelper.consumeFuel(stack, false);
                        continue outer;
                    }
                }
            }
            break;
        }
        // Refill buffer with fluid fuel
        outer:
        while (burningFeBuffer < 5 * 20 * maxFeProduction) {
            for (ConfigurableFluidStack stack : fluidInputs) {
                if (!stack.isEmpty()) {
                    long fePerMb = FluidFuel.getFe(stack.getResource().getFluid());
                    if (fePerMb != 0) {
                        long mbConsumedMax = (5 * 20 * maxFeProduction - burningFeBuffer) / fePerMb;
                        long mbConsumed = Math.min(mbConsumedMax, stack.getAmount());
                        if (mbConsumed > 0) {
                            stack.decrement(mbConsumed);
                            burningFeBuffer += mbConsumed * fePerMb;
                            burningItemTotalFe = 0;
                            continue outer;
                        }
                    }
                }
            }
            break;
        }
    }

    @Override
    public void writeNbt(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putLong("burningFeBuffer", burningFeBuffer);
        if (burningItemTotalFe != 0) {
            tag.putLong("burningItemTotalFe", burningItemTotalFe);
        }
    }

    @Override
    public void readNbt(CompoundTag tag, HolderLookup.Provider registries, boolean isUpgradingMachine) {
        burningFeBuffer = tag.getLong("burningFeBuffer");
        burningItemTotalFe = tag.getLong("burningItemTotalFe");
    }

    public List<Component> getTooltips(boolean acceptsFluid) {
        List<Component> returnList = new ArrayList<>();
        returnList.add(new MITooltips.Line(MIText.MaxFeProductionSteam).arg(
                this.maxFeProduction,
                MITooltips.FE_PER_TICK_PARSER)
                .arg(MIFluids.STEAM)
                .build());

        if (burningItemFeMultiplier == 1) {
            returnList.add(new MITooltips.Line(MIText.FuelEfficiencyItemNormal).build());
        } else {
            returnList.add(new MITooltips.Line(MIText.FuelEfficiencyItem).arg(burningItemFeMultiplier, MITooltips.MULTIPLIER_PARSER).arg(burningItemFeMultiplier, MITooltips.MULTIPLIER_PARSER).build());
        }
        if (acceptsFluid) {
            returnList.add(new MITooltips.Line(MIText.FuelEfficiencyFluidNormal).build());
        }

        return returnList;
    }
}
