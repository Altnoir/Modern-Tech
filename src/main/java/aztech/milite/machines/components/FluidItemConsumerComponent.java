package aztech.milite.machines.components;

import aztech.milite.api.datamaps.FluidFuel;
import aztech.milite.definition.FluidDefinition;
import aztech.milite.inventory.ConfigurableFluidStack;
import aztech.milite.inventory.ConfigurableItemStack;
import aztech.milite.machines.MachineComponent;
import aztech.milite.MIText;
import aztech.milite.MITooltips;
import aztech.milite.util.ItemStackHelper;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A component that turns fluids and/or item into energy.
 */
public class FluidItemConsumerComponent implements MachineComponent.ServerOnly {
    protected long feBuffer = 0;
    /**
     * The maximum FE that can be produced by one production operation, to limit the
     * maximum conversion rate of the machine.
     */
    public final long maxFeProduction;

    protected final double feMultiplier;

    public final FEProductionMap<Item> itemFEProductionMap;
    public final FEProductionMap<Fluid> fluidFEProductionMap;

    private final boolean hideEfficiencyTooltip;

    public FluidItemConsumerComponent(long maxFeProduction,
            double feMultiplier,
            FEProductionMap<Item> itemFEProductionMap,
            FEProductionMap<Fluid> fluidFEProductionMap,
            boolean hideEfficiencyTooltip) {
        this.itemFEProductionMap = itemFEProductionMap;
        this.fluidFEProductionMap = fluidFEProductionMap;
        this.maxFeProduction = maxFeProduction;
        this.feMultiplier = feMultiplier;
        this.hideEfficiencyTooltip = hideEfficiencyTooltip;
    }

    public boolean doAllowMoreThanOne() {
        return itemFEProductionMap.getNumberOfFuel() == NumberOfFuel.MANY
                || fluidFEProductionMap.getNumberOfFuel() == NumberOfFuel.MANY;
    }

    public static FluidItemConsumerComponent ofSingleFluid(long maxFeProduction,
            FluidDefinition acceptedFluid,
            long fluidFEperMb) {
        return ofFluid(maxFeProduction, new FeProductionMapBuilder<>(BuiltInRegistries.FLUID).add(acceptedFluid.getId(), fluidFEperMb).build());
    }

    public static FluidItemConsumerComponent ofFluidFuels(long maxFeProduction, double feMultiplier) {
        return new FluidItemConsumerComponent(maxFeProduction,
                feMultiplier,
                FEProductionMap.empty(),
                fluidFuels(),
                false);
    }

    public static FluidItemConsumerComponent ofFluid(long maxFeProduction, FEProductionMap<Fluid> fluidFEProductionMap) {
        return new FluidItemConsumerComponent(maxFeProduction,
                1,
                FEProductionMap.empty(),
                fluidFEProductionMap,
                true);
    }

    @Override
    public void writeNbt(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putLong("feBuffer", feBuffer);
    }

    @Override
    public void readNbt(CompoundTag tag, HolderLookup.Provider registries, boolean isUpgradingMachine) {
        feBuffer = tag.getLong("feBuffer");
    }

    public long getFeProduction(List<ConfigurableFluidStack> fluidInputs,
            List<ConfigurableItemStack> itemInputs,
            long maxEnergyInsertable) {
        long maxFeProduced = Math.min(maxEnergyInsertable, maxFeProduction);

        if (maxFeProduced == 0) {
            return 0;
        }

        if (feBuffer >= maxFeProduced) {
            feBuffer -= maxFeProduced;
            return maxFeProduced;
        }

        long feProduced = 0;

        // Consume from the buffer first
        feProduced += feBuffer;
        feBuffer = 0;

        for (ConfigurableFluidStack stack : fluidInputs) {
            Fluid fluid = stack.getResource().getFluid();
            if (fluidFEProductionMap.accept(fluid) && stack.getAmount() > 0) {
                long fuelFe = (long) (fluidFEProductionMap.getFeProduction(fluid) * feMultiplier);
                long usedDroplets = Math.min((maxFeProduced - feProduced + fuelFe - 1) / fuelFe, stack.getAmount());
                feProduced += usedDroplets * fuelFe;
                stack.decrement(usedDroplets);

                if (feProduced >= maxFeProduced) {
                    feBuffer += feProduced - maxFeProduced;
                    return maxFeProduced;
                }
            }
        }

        for (ConfigurableItemStack stack : itemInputs) {
            Item fuel = stack.getResource().getItem();
            if (itemFEProductionMap.accept(fuel) && stack.getAmount() > 0) {
                if (!itemFEProductionMap.isStandardFuels() || ItemStackHelper.consumeFuel(stack, true)) {
                    long fuelFE = (long) (itemFEProductionMap.getFeProduction(fuel) * feMultiplier);
                    long usedItem = Math.min((maxFeProduced - feProduced + fuelFE - 1) / fuelFE, stack.getAmount());
                    feProduced += fuelFE * usedItem;

                    if (itemFEProductionMap.isStandardFuels()) {
                        ItemStackHelper.consumeFuel(stack, false);
                    } else {
                        stack.decrement(usedItem);
                    }

                    if (feProduced >= maxFeProduced) {
                        feBuffer += feProduced - maxFeProduced;
                        return maxFeProduced;
                    }
                }
            }
        }

        return feProduced;
    }

    public List<Component> getTooltips() {
        List<Component> returnList = new ArrayList<>();

        returnList.add(new MITooltips.Line(MIText.MaxFeProduction).arg(
                this.maxFeProduction,
                MITooltips.FE_PER_TICK_PARSER).build());

        if (this.itemFEProductionMap.getNumberOfFuel() != NumberOfFuel.NONE) {
            if (this.itemFEProductionMap.isStandardFuels()) {
                returnList.add(new MITooltips.Line(MIText.AcceptAnyItemFuels).build());
            } else {
                var informationEntries = this.itemFEProductionMap.getAllAcceptedWithFE();
                if (informationEntries.size() == 1) {
                    var entry = informationEntries.iterator().next();
                    returnList.add(new MITooltips.Line(MIText.AcceptSingleItem)
                            .arg(entry.variant).arg(entry.fe, MITooltips.FE_PARSER).build());
                } else {
                    returnList.add(new MITooltips.Line(MIText.ConsumesTheFollowing).build());
                    for (var entry : informationEntries) {
                        returnList.add(
                                new MITooltips.Line(MIText.AcceptFollowingItemEntry)
                                        .arg(entry.variant).arg(entry.fe, MITooltips.FE_PARSER).build());
                    }
                }
            }
            if (!hideEfficiencyTooltip) {
                if (feMultiplier == 1) {
                    returnList.add(new MITooltips.Line(MIText.FuelEfficiencyItemNormal).build());
                } else {
                    returnList.add(new MITooltips.Line(MIText.FuelEfficiencyItem).arg(feMultiplier, MITooltips.MULTIPLIER_PARSER).arg(feMultiplier, MITooltips.MULTIPLIER_PARSER).build());
                }
            }
        }

        if (this.fluidFEProductionMap.getNumberOfFuel() != NumberOfFuel.NONE) {
            if (this.fluidFEProductionMap.isStandardFuels()) {
                returnList.add(new MITooltips.Line(MIText.AcceptAnyFluidFuels).build());
            } else {
                var informationEntries = this.fluidFEProductionMap.getAllAcceptedWithFE();

                if (informationEntries.size() == 1) {
                    var entry = informationEntries.iterator().next();
                    returnList.add(new MITooltips.Line(MIText.AcceptSingleFluid)
                            .arg(entry.variant).arg(entry.fe, MITooltips.FE_PARSER).build());
                } else if (informationEntries.size() > 1) {
                    returnList.add(new MITooltips.Line(MIText.ConsumesTheFollowing).build());
                    for (var entry : informationEntries) {
                        returnList.add(
                                new MITooltips.Line(MIText.AcceptFollowingFluidEntry)
                                        .arg(entry.variant).arg(entry.fe, MITooltips.FE_PARSER).build());
                    }
                }
            }
            if (!hideEfficiencyTooltip) {
                if (feMultiplier == 1) {
                    returnList.add(new MITooltips.Line(MIText.FuelEfficiencyFluidNormal).build());
                } else {
                    returnList.add(new MITooltips.Line(MIText.FuelEfficiencyFluid).arg(feMultiplier, MITooltips.MULTIPLIER_PARSER).arg(feMultiplier, MITooltips.MULTIPLIER_PARSER).build());
                }
            }
        }

        return returnList;
    }

    public interface FEProductionMap<T> {
        record InformationEntry<T>(long fe, T variant) {}

        long getFeProduction(T variant);

        default boolean isStandardFuels() {
            return false;
        }

        default boolean accept(T variant) {
            return getFeProduction(variant) != 0;
        }

        default NumberOfFuel getNumberOfFuel() {
            if (isStandardFuels()) {
                return NumberOfFuel.MANY;
            } else {
                if (getAllAccepted().size() == 1) {
                    return NumberOfFuel.SINGLE;
                } else if (getAllAccepted().size() == 0) {
                    return NumberOfFuel.NONE;
                } else {
                    return NumberOfFuel.MANY;
                }
            }
        }

        static <T> FEProductionMap<T> empty() {
            return new FEProductionMap<>() {
                @Override
                public long getFeProduction(T variant) {
                    return 0;
                }

                @Override
                public List<T> getAllAccepted() {
                    return List.of();
                }
            };
        }

        List<T> getAllAccepted();

        default List<InformationEntry<T>> getAllAcceptedWithFE() {
            return getAllAccepted().stream()
                    .map(variant -> new InformationEntry<>(getFeProduction(variant), variant)).sorted(Comparator.comparingLong(InformationEntry::fe))
                    .collect(Collectors.toList());
        }
    }

    public static class FeProductionMapBuilder<T> {
        private final Map<ResourceLocation, Long> map = new HashMap<>(); // Must Stores as string, because KubeJS could add not loader yet resource
                                                                        // location
        private final DefaultedRegistry<T> registryAccess;

        public FeProductionMapBuilder(DefaultedRegistry<T> registryAccess) {
            this.registryAccess = registryAccess;
        }

        public FeProductionMapBuilder<T> add(ResourceLocation resourceLocation,
                long fe) {
            map.put(resourceLocation, fe);
            return this;
        }

        public FEProductionMap<T> build() {
            return new FEProductionMap<>() {
                @Override
                public long getFeProduction(T variant) {
                    return map.getOrDefault(registryAccess.getKey(variant), 0L);
                }

                @Override
                public List<T> getAllAccepted() {
                    return map.keySet().stream().map(registryAccess::get).collect(Collectors.toList());
                }
            };
        }
    }

    public static FEProductionMap<Item> itemFuels() {
        return new FEProductionMap<>() {
            @Override
            public long getFeProduction(Item variant) {
                // TODO NEO NBT-aware fuels
                int burnTime = variant.getDefaultInstance().getBurnTime(null);
                return burnTime <= 0 ? 0 : burnTime * FuelBurningComponent.FE_PER_BURN_TICK;
            }

            @Override
            public boolean isStandardFuels() {
                return true;
            }

            @Override
            public List<Item> getAllAccepted() {
                throw new UnsupportedOperationException("The list of accepted items is not available for standard fuels");
            }
        };
    }

    public static FEProductionMap<Fluid> fluidFuels() {
        return new FEProductionMap<>() {
            @Override
            public long getFeProduction(Fluid variant) {
                return FluidFuel.getFe(variant);
            }

            @Override
            public boolean isStandardFuels() {
                return true;
            }

            @Override
            public List<Fluid> getAllAccepted() {
                throw new UnsupportedOperationException("The list of accepted fluids is not available for fluid fuels");
            }
        };
    }

    public enum NumberOfFuel {
        NONE(),
        SINGLE(),
        MANY(),
    }
}
