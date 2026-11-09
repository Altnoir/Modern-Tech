package aztech.milite.compat.jade.server;

import aztech.milite.api.machine.component.FluidAccess;
import aztech.milite.api.machine.holder.CrafterComponentHolder;
import aztech.milite.api.machine.holder.EnergyComponentHolder;
import aztech.milite.api.machine.holder.EnergyListComponentHolder;
import aztech.milite.api.machine.holder.FluidStorageComponentHolder;
import aztech.milite.api.machine.holder.MultiblockInventoryComponentHolder;
import aztech.milite.machines.MachineBlockEntity;
import aztech.milite.MI;
import aztech.milite.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import com.google.common.math.LongMath;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.addon.universal.ItemCollector;
import snownee.jade.api.Accessor;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.EnergyView;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ItemView;
import snownee.jade.api.view.ProgressView;
import snownee.jade.api.view.ViewGroup;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract sealed class MachineComponentProvider<S, C>
        implements IServerExtensionProvider<S>, IClientExtensionProvider<S, C> {
    @Override
    public ResourceLocation getUid() {
        return MI.id("machine");
    }

    public static final class Energy extends MachineComponentProvider<CompoundTag, EnergyView> {
        @Override
        public List<ViewGroup<CompoundTag>> getGroups(Accessor<?> accessor) {
            if (accessor.getTarget() instanceof EnergyListComponentHolder holder) {
                var components = holder.getEnergyComponents();

                if (!components.isEmpty()) {
                    long stored = 0;
                    long capacity = 0;

                    for (var component : components) {
                        stored = LongMath.saturatedAdd(stored, component.getFe());
                        capacity = LongMath.saturatedAdd(capacity, component.getCapacity());
                    }

                    return List.of(new ViewGroup<>(List.of(EnergyView.of(stored, capacity))));
                }
            } else if (accessor.getTarget() instanceof EnergyComponentHolder holder) {
                var component = holder.getEnergyComponent();
                return List.of(new ViewGroup<>(List.of(EnergyView.of(component.getFe(), component.getCapacity()))));
            }

            return List.of();
        }

        @Override
        public List<ClientViewGroup<EnergyView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<CompoundTag>> list) {
            // ModernTech exposes machine energy through NeoForge's FE-compatible energy API.
            return ClientViewGroup.map(list, tag -> EnergyView.read(tag, " FE"), null);
        }
    }

    public static final class Fluids extends MachineComponentProvider<CompoundTag, FluidView> {
        @Override
        public List<ViewGroup<CompoundTag>> getGroups(Accessor<?> accessor) {
            var machine = (MachineBlockEntity) accessor.getTarget();
            var fluidContents = new LinkedHashMap<FluidVariant, FluidContents>();
            if (machine instanceof MultiblockInventoryComponentHolder holder) {
                var component = holder.getMultiblockInventoryComponent();
                var inputs = component.getFluidInputs();
                var outputs = component.getFluidOutputs();

                if (!inputs.isEmpty() || !outputs.isEmpty()) {
                    addFluids(fluidContents, inputs);
                    addFluids(fluidContents, outputs);
                }
            } else if (machine instanceof FluidStorageComponentHolder holder) {
                var component = holder.getFluidStorageComponent();

                if (component != null) {
                    var fluid = component.getVariant();
                    var contents = new FluidContents();
                    contents.amount = component.getAmount();
                    contents.capacity = component.getCapacity();
                    fluidContents.put(fluid, contents);
                }
            } else {
                var stacks = machine.getInventory().getFluidStacks();
                addFluids(fluidContents, stacks);
            }
            if (fluidContents.isEmpty()) {
                return List.of();
            }
            var fluidData = new ViewGroup<CompoundTag>(new ArrayList<>());
            for (var entry : fluidContents.entrySet()) {
                fluidData.views.add(FluidView.writeDefault(MIJadeCommonPlugin.fluidStack(entry.getKey(), entry.getValue().amount), entry.getValue().capacity));
            }
            return List.of(fluidData);
        }

        public static final class FluidContents {
            private long amount;
            private long capacity;
        }

        private void addFluids(Map<FluidVariant, FluidContents> fluidContents, List<? extends FluidAccess> stacks) {
            for (var stack : stacks) {
                fluidContents.compute(stack.getVariant(), (key, contents) -> {
                    if (contents == null) {
                        contents = new FluidContents();
                    }
                    contents.amount += stack.getAmount();
                    contents.capacity += stack.getCapacity();
                    return contents;
                });
            }
        }

        @Override
        public List<ClientViewGroup<FluidView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<CompoundTag>> list) {
            return ClientViewGroup.map(list, FluidView::readDefault, null);
        }
    }

    public static final class Items extends MachineComponentProvider<ItemStack, ItemView> {
        @Override
        public List<ViewGroup<ItemStack>> getGroups(Accessor<?> accessor) {
            var itemCollector = new ItemCollector<>(new MachineItemIterator());
            return itemCollector.update(accessor);
        }

        @Override
        public List<ClientViewGroup<ItemView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<ItemStack>> list) {
            return ClientViewGroup.map(list, ItemView::new, null);
        }
    }

    // TODO: probably needs to be rewritten to be standalone for a better display
    public static final class Progress extends MachineComponentProvider<CompoundTag, ProgressView> {
        @Override
        public List<ViewGroup<CompoundTag>> getGroups(Accessor<?> accessor) {
            if (accessor.getTarget() instanceof CrafterComponentHolder holder) {
                var component = holder.getCrafterComponent();
                var progress = component.getProgress();

                if (progress > 0.0f) {
                    var inventory = holder.getCrafterComponent().getInventory();
                    var progressData = new ViewGroup<CompoundTag>(new ArrayList<>());

                    // TODO potentially add inventory information if the Jade API ever supports it

                    progressData.views.add(ProgressView.create(progress));
                    return List.of(progressData);
                }
            }
            return List.of();
        }

        @Override
        public List<ClientViewGroup<ProgressView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<CompoundTag>> list) {
            return ClientViewGroup.map(list, ProgressView::read, null);
        }
    }
}
