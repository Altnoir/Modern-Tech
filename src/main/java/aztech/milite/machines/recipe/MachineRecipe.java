package aztech.milite.machines.recipe;

import aztech.milite.machines.init.MachineLevel;
import aztech.milite.machines.recipe.condition.MachineProcessCondition;
import aztech.milite.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.milite.util.DefaultedListWrapper;
import aztech.milite.util.MIExtraCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MachineRecipe implements Recipe<RecipeInput> {
    public static MapCodec<MachineRecipe> codec(MachineRecipeType type) {
        return RecordCodecBuilder.mapCodec(
                g -> g
                        .group(
                                ExtraCodecs.POSITIVE_INT.fieldOf("energy").forGetter(recipe -> recipe.energy),
                                ExtraCodecs.POSITIVE_INT.fieldOf("duration").forGetter(recipe -> recipe.duration),
                                MachineLevel.CODEC.optionalFieldOf("level", MachineLevel.BRONZE).forGetter(recipe -> recipe.level),
                                MIExtraCodecs.maybeList(ItemInput.CODEC, "item_inputs").forGetter(recipe -> recipe.itemInputs),
                                MIExtraCodecs.maybeList(FluidInput.CODEC, "fluid_inputs").forGetter(recipe -> recipe.fluidInputs),
                                MIExtraCodecs.maybeList(ItemOutput.CODEC, "item_outputs").forGetter(recipe -> recipe.itemOutputs),
                                MIExtraCodecs.maybeList(FluidOutput.CODEC, "fluid_outputs").forGetter(recipe -> recipe.fluidOutputs),
                                MIExtraCodecs.maybeList(MachineProcessCondition.CODEC, "process_conditions").forGetter(recipe -> recipe.conditions))
                        .apply(g, (energy, duration, level, itemInputs, fluidInputs, itemOutputs, fluidOutputs, conditions) -> {
                            var ret = new MachineRecipe(type);
                            ret.energy = energy;
                            ret.duration = duration;
                            ret.level = level;
                            ret.itemInputs = itemInputs;
                            ret.fluidInputs = fluidInputs;
                            ret.itemOutputs = itemOutputs;
                            ret.fluidOutputs = fluidOutputs;
                            ret.conditions = conditions;
                            return ret;
                        }));
    }

    /**
     * The two scalar fields that are not a list, packed into one so that the stream codec stays within the seven
     * fields {@code NeoForgeStreamCodecs.composite} supports.
     */
    private record Timing(int duration, MachineLevel level) {
        static final StreamCodec<ByteBuf, Timing> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Timing::duration,
                ByteBufCodecs.fromCodec(MachineLevel.CODEC), Timing::level,
                Timing::new);
    }

    public static StreamCodec<RegistryFriendlyByteBuf, MachineRecipe> streamCodec(MachineRecipeType type) {
        return NeoForgeStreamCodecs.composite(
                ByteBufCodecs.VAR_INT,
                r -> r.energy,
                Timing.STREAM_CODEC,
                r -> new Timing(r.duration, r.level),
                ItemInput.STREAM_CODEC.apply(ByteBufCodecs.list()),
                r -> r.itemInputs,
                FluidInput.STREAM_CODEC.apply(ByteBufCodecs.list()),
                r -> r.fluidInputs,
                ItemOutput.STREAM_CODEC.apply(ByteBufCodecs.list()),
                r -> r.itemOutputs,
                FluidOutput.STREAM_CODEC.apply(ByteBufCodecs.list()),
                r -> r.fluidOutputs,
                MachineProcessCondition.STREAM_CODEC.apply(ByteBufCodecs.list()),
                r -> r.conditions,
                (energy, timing, itemInputs, fluidInputs, itemOutputs, fluidOutputs, conditions) -> {
                    var ret = new MachineRecipe(type);
                    ret.energy = energy;
                    ret.duration = timing.duration();
                    ret.level = timing.level();
                    ret.itemInputs = itemInputs;
                    ret.fluidInputs = fluidInputs;
                    ret.itemOutputs = itemOutputs;
                    ret.fluidOutputs = fluidOutputs;
                    ret.conditions = conditions;
                    return ret;
                });
    }

    final MachineRecipeType type;

    public int energy;
    /** Ticks at the recipe's required level without additional speed bonuses. */
    public int duration;
    public MachineLevel level = MachineLevel.BRONZE;
    public List<ItemInput> itemInputs = new ArrayList<>();
    public List<FluidInput> fluidInputs = new ArrayList<>();
    public List<ItemOutput> itemOutputs = new ArrayList<>();
    public List<FluidOutput> fluidOutputs = new ArrayList<>();
    public List<MachineProcessCondition> conditions = new ArrayList<>();

    MachineRecipe(MachineRecipeType type) {
        this.type = type;
    }

    public long getTotalFe() {
        return (long) energy * duration;
    }

    public long getFePerTick(int speed) {
        return (long) energy * Math.min(speed, duration);
    }

    public int getProcessingTicks(MachineLevel machineLevel, int speedBonus) {
        long speed = (long) machineLevel.speedMultiplierFrom(level) * speedBonus;
        return (int) Math.ceilDiv(duration, speed);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean matches(RecipeInput recipeInput, Level world) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemStack assemble(RecipeInput recipeInput, HolderLookup.Provider registryAccess) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        // This function is implemented for AE2 pattern shift-clicking compat.
        // The item stacks in the ingredient are copied with the correct count
        // instead of being modified in place, because some mods (e.g. AllTheLeaks)
        // cache and lock the item stacks of ingredients.
        return new DefaultedListWrapper<>(itemInputs.stream().filter(i -> !i.reusable && i.probability == 1).map(i -> Ingredient.of(
                Arrays.stream(i.ingredient.getItems()).map(stack -> stack.copyWithCount(i.amount)))).collect(Collectors.toList()));
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registryAccess) {
        for (ItemOutput o : itemOutputs) {
            if (o.probability == 1) {
                return o.getStack();
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return type;
    }

    @Override
    public RecipeType<?> getType() {
        return type;
    }

    public boolean conditionsMatch(MachineProcessCondition.Context context) {
        for (var condition : conditions) {
            if (!condition.canProcessRecipe(context, this)) {
                return false;
            }
        }
        return true;
    }

    private static final MapCodec<Integer> AMOUNT_CODEC = NeoForgeExtraCodecs
            .mapWithAlternative(
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("amount"),
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("count"))
            .xmap(
                    deserialized -> deserialized.orElse(1),
                    Optional::of);

    public record ItemInput(Ingredient ingredient, int amount, float probability, boolean reusable) {
        public static final Codec<ItemInput> CODEC = RecordCodecBuilder.create(
                g -> g.group(
                        Ingredient.MAP_CODEC_NONEMPTY.forGetter(ItemInput::ingredient),
                        AMOUNT_CODEC.forGetter(ItemInput::amount),
                        MIExtraCodecs.FLOAT_01.optionalFieldOf("probability").forGetter(input ->
                                input.reusable || input.probability == 1f ? Optional.empty() : Optional.of(input.probability)),
                        Codec.BOOL.optionalFieldOf("reusable", false).forGetter(ItemInput::reusable))
                        .apply(g, (ingredient, amount, probability, reusable) ->
                                new ItemInput(ingredient, amount, probability.orElse(1f), reusable)));
        public static final StreamCodec<RegistryFriendlyByteBuf, ItemInput> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC,
                ItemInput::ingredient,
                ByteBufCodecs.VAR_INT,
                ItemInput::amount,
                ByteBufCodecs.FLOAT,
                ItemInput::probability,
                ByteBufCodecs.BOOL,
                ItemInput::reusable,
                ItemInput::new);

        public ItemInput {
            if (reusable) {
                probability = 1f;
            }
        }

        public ItemInput(Ingredient ingredient, int amount, float probability) {
            this(ingredient, amount, probability, false);
        }

        public boolean matches(ItemStack otherStack) {
            return ingredient.test(otherStack);
        }

        public List<Item> getInputItems() {
            return Arrays.stream(ingredient.getItems()).map(ItemStack::getItem).distinct().collect(Collectors.toList());
        }
    }

    public record FluidInput(FluidIngredient fluid, long amount, float probability) {
        @Deprecated(forRemoval = true)
        public FluidInput(Fluid fluid, long amount, float probability) {
            this(FluidIngredient.of(fluid), amount, probability);
        }

        public static final Codec<FluidInput> CODEC = RecordCodecBuilder.create(
                g -> g.group(
                        FluidIngredient.MAP_CODEC_NONEMPTY.forGetter(FluidInput::fluid),
                        NeoForgeExtraCodecs.optionalFieldAlwaysWrite(MIExtraCodecs.POSITIVE_LONG, "amount", 1L).forGetter(FluidInput::amount),
                        MIExtraCodecs.FLOAT_01.optionalFieldOf("probability", 1f).forGetter(FluidInput::probability))
                        .apply(g, FluidInput::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, FluidInput> STREAM_CODEC = StreamCodec.composite(
                FluidIngredient.STREAM_CODEC,
                FluidInput::fluid,
                ByteBufCodecs.VAR_LONG,
                FluidInput::amount,
                ByteBufCodecs.FLOAT,
                FluidInput::probability,
                FluidInput::new);

        public List<Fluid> getInputFluids() {
            return Arrays.stream(fluid.getStacks()).map(FluidStack::getFluid).distinct().collect(Collectors.toList());
        }
    }

    public record ItemOutput(ItemVariant variant, int amount, float probability) {
        public static final Codec<ItemOutput> CODEC = RecordCodecBuilder.create(
                g -> g.group(
                        ItemStack.ITEM_NON_AIR_CODEC.fieldOf("item")
                                .forGetter(itemOutput -> itemOutput.variant.getItem().builtInRegistryHolder()),
                        AMOUNT_CODEC.forGetter(itemOutput -> itemOutput.amount),
                        DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY)
                                .forGetter(itemOutput -> itemOutput.variant.getComponentsPatch()),
                        MIExtraCodecs.FLOAT_01.optionalFieldOf("probability", 1f)
                                .forGetter(itemOutput -> itemOutput.probability))
                        .apply(g, (item, count, components, probability) -> new ItemOutput(ItemVariant.of(new ItemStack(item, 1, components)), count,
                                probability)));

        public static final StreamCodec<RegistryFriendlyByteBuf, ItemOutput> STREAM_CODEC = StreamCodec.composite(
                ItemVariant.STREAM_CODEC,
                ItemOutput::variant,
                ByteBufCodecs.VAR_INT,
                ItemOutput::amount,
                ByteBufCodecs.FLOAT,
                ItemOutput::probability,
                ItemOutput::new);

        public ItemStack getStack() {
            return variant.toStack(amount);
        }
    }

    public record FluidOutput(Fluid fluid, long amount, float probability) {
        public static final Codec<FluidOutput> CODEC = RecordCodecBuilder.create(
                g -> g.group(
                        BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid").forGetter(fluidOutput -> fluidOutput.fluid),
                        NeoForgeExtraCodecs.optionalFieldAlwaysWrite(MIExtraCodecs.POSITIVE_LONG, "amount", 1L)
                                .forGetter(fluidOutput -> fluidOutput.amount),
                        MIExtraCodecs.FLOAT_01.optionalFieldOf("probability", 1f).forGetter(fluidOutput -> fluidOutput.probability))
                        .apply(g, FluidOutput::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, FluidOutput> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.registry(Registries.FLUID),
                FluidOutput::fluid,
                ByteBufCodecs.VAR_LONG,
                FluidOutput::amount,
                ByteBufCodecs.FLOAT,
                FluidOutput::probability,
                FluidOutput::new);
    }
}
