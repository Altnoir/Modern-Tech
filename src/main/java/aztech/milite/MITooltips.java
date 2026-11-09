package aztech.milite;

import aztech.milite.api.datamaps.MIDataMaps;
import aztech.milite.blocks.OreBlock;
import aztech.milite.config.MIClientConfig;
import aztech.milite.definition.FluidLike;
import aztech.milite.items.PortableStorageUnit;
import aztech.milite.items.diesel_tools.DieselToolItem;
import aztech.milite.machines.MachineBlock;
import aztech.milite.machines.blockentities.multiblocks.ElectricBlastFurnaceBlockEntity;
import aztech.milite.nuclear.NuclearAbsorbable;
import aztech.milite.nuclear.NuclearFuel;
import aztech.milite.util.TextHelper;
import com.google.common.base.Preconditions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("unused")
public class MITooltips {
    public static final PriorityQueue<TooltipAttachment> TOOLTIPS = new PriorityQueue<>();

    public static final Style DEFAULT_STYLE = Style.EMPTY.withColor(TextColor.fromRgb(0xa9a9a9)).withItalic(false);
    public static final Style HIGHLIGHT_STYLE = Style.EMPTY.withColor(TextColor.fromRgb(0xffde7d)).withItalic(false);
    public static final Style NUMBER_TEXT = HIGHLIGHT_STYLE;

    private static final Map<Class<?>, Style> DEFAULT_ARGUMENT_STYLE = new HashMap<>();

    public static int colorFromProgress(double progress, boolean zeroIsGreen) {
        progress = Math.max(0, Math.min(1, progress));
        if (!zeroIsGreen) {
            progress = 1 - progress;
        }

        double r = Math.min(2 * progress, 1);
        double g = Math.min(1, 2 - 2 * progress);
        return (int) (r * 255) << 16 | (int) (g * 255) << 8;
    }

    public static Style styleFromProgress(double progress, boolean zeroIsGreen) {
        return Style.EMPTY.withColor(TextColor.fromRgb(colorFromProgress(progress, zeroIsGreen))).withItalic(false);
    }

    static {
        DEFAULT_ARGUMENT_STYLE.put(Integer.class, NUMBER_TEXT);
        DEFAULT_ARGUMENT_STYLE.put(Long.class, NUMBER_TEXT);
        DEFAULT_ARGUMENT_STYLE.put(Float.class, NUMBER_TEXT);
        DEFAULT_ARGUMENT_STYLE.put(Double.class, NUMBER_TEXT);
    }

    public static final TooltipAttachment DATA_DRIVEN = TooltipAttachment.ofMultilines((context, stack, item) -> {
        var dataMap = stack.getItemHolder().getData(MIDataMaps.ITEM_TOOLTIPS);
        if (dataMap != null) {
            return dataMap.components();
        } else {
            return List.of();
        }
    }).setPriority(-100);

    public static final Parser<Object> DEFAULT_PARSER = new Parser<>() {
        @Override
        public Component parse(Object o) {
            Style style = DEFAULT_STYLE;
            for (var entry : DEFAULT_ARGUMENT_STYLE.entrySet()) {
                if (o.getClass().isAssignableFrom(entry.getKey())) {
                    style = entry.getValue();
                    break;
                }
            }

            if (o instanceof Fluid f) {
                return FLUID_PARSER.parse(f);
            } else if (o instanceof FluidLike f) {
                return FLUID_PARSER.parse(f.asFluid());
            }

            if (o instanceof Item) {
                return ITEM_PARSER.parse((Item) o);
            } else if (o instanceof ItemLike) {
                return ITEM_PARSER.parse(((ItemLike) o).asItem());
            }

            if (o instanceof Component c) {
                return c.copy().withStyle(style);
            }

            return Component.literal(String.valueOf(o)).withStyle(style);
        }
    };

    public static final Parser<Number> FE_PER_TICK_PARSER = new Parser<>() {
        @Override
        public Component parse(Number number) {
            TextHelper.Amount amount = TextHelper.getAmountGeneric(number);
            return MIText.FeT.text(amount.digit(), amount.unit()).withStyle(NUMBER_TEXT);
        }
    };

    public static final Parser<Number> FE_PARSER = new Parser<>() {
        @Override
        public Component parse(Number number) {
            TextHelper.Amount amount = TextHelper.getAmountGeneric(number);
            return MIText.Fe.text(amount.digit(), amount.unit()).withStyle(NUMBER_TEXT);
        }
    };

    public record NumberWithMax(Number number, Number max) {
    }

    public static final Parser<NumberWithMax> FE_MAXED_PARSER = new Parser<>() {
        @Override
        public Component parse(NumberWithMax numberWithMax) {
            MutableComponent component = TextHelper.getFeTextMaxed(numberWithMax.number, numberWithMax.max);
            return component.withStyle(NUMBER_TEXT);
        }
    };

    public static final Parser<Fluid> FLUID_PARSER = new Parser<>() {
        @Override
        public Component parse(Fluid fluid) {
            return fluid.getFluidType().getDescription();
        }
    };

    public static final Parser<Double> RATIO_PERCENTAGE_PARSER = new Parser<>() {
        @Override
        public Component parse(Double ratio) {
            String percentage = String.format("%.1f", ratio * 100);
            return Component.literal(percentage + "%").withStyle(styleFromProgress(ratio, false));
        }
    };

    public static final Parser<Double> INVERTED_RATIO_PERCENTAGE_PARSER = new Parser<>() {
        @Override
        public Component parse(Double ratio) {
            String percentage = String.format("%.1f", ratio * 100);
            return Component.literal(percentage + "%").withStyle(styleFromProgress(ratio, true));
        }
    };

    public static final Parser<Double> MULTIPLIER_PARSER = new Parser<>() {
        @Override
        public Component parse(Double multiplier) {
            String multiplierText = multiplier % 1 == 0 ? String.valueOf(multiplier.intValue()) : String.format("%.1f", multiplier);
            return Component.literal(multiplierText + "x").withStyle(NUMBER_TEXT);
        }
    };

    public static final Parser<BlockState> BLOCK_STATE_PARSER = state -> {
        return state.getBlock().getName().withStyle(NUMBER_TEXT);
    };

    public static final Parser<Item> ITEM_PARSER = state -> {
        return state.getDefaultInstance().getHoverName().copy().withStyle(NUMBER_TEXT);
    };

    public static final Parser<String> KEYBIND_PARSER = keybind -> {
        return Component.keybind("key.%s".formatted(keybind)).withStyle(NUMBER_TEXT);
    };

    public static final Parser<Component> COMPONENT = state -> state;
    public static final TooltipAttachment BATTERIES = TooltipAttachment.of(
            (context, itemStack, item) -> {
                if (PortableStorageUnit.CAPACITY_PER_BATTERY.containsKey(item)) {
                    var capacity = PortableStorageUnit.CAPACITY_PER_BATTERY.get(itemStack.getItem());
                    return Optional.of(new Line(MIText.BatteryInStorageUnit).arg(capacity, FE_PARSER).build());
                } else {
                    return Optional.empty();
                }
            });
    public static final TooltipAttachment COILS = TooltipAttachment.of(
            (context, itemStack, item) -> {
                if (item instanceof BlockItem blockItem) {
                    var tier = ElectricBlastFurnaceBlockEntity.tiersByCoil.get(BuiltInRegistries.BLOCK.getKey(blockItem.getBlock()));
                    if (tier != null) {
                        return Optional.of(new Line(MIText.EbfMaxLevel).arg(tier.level().getDisplayName()).build());
                    }
                }
                return Optional.empty();
            });
    public static final TooltipAttachment ENERGY_STORED_ITEM = TooltipAttachment.of(
            (context, itemStack, item) -> {
                if (BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(MI.ID)) {
                    var energyStorage = itemStack.getCapability(Capabilities.EnergyStorage.ITEM);
                    if (energyStorage != null) {
                        // NeoForge's energy API is int-based, but MI tooltips expect a consistent number type:
                        // widen both values to long, as the old long-based MI energy API did.
                        long stored = energyStorage.getEnergyStored();
                        long capacity = energyStorage.getMaxEnergyStored();
                        if (capacity > 0) {
                            return Optional.of(new Line(MIText.EnergyStored)
                                    .arg(new NumberWithMax(stored, capacity), FE_MAXED_PARSER).build());
                        }
                    }
                }
                return Optional.empty();
            }).noShiftRequired();
    public static final TooltipAttachment MACHINE_TOOLTIPS = TooltipAttachment.ofMultilines(
            (context, itemStack, item) -> {
                if (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof MachineBlock machineBlock) {
                    return machineBlock.getBlockEntityInstance().getTooltips(context);
                } else {
                    return List.of();
                }
            });

    public static final TooltipAttachment GUNPOWDER = TooltipAttachment.of(Items.GUNPOWDER, MIText.GunpowderUpgrade);
    public static final TooltipAttachment NUCLEAR = TooltipAttachment.ofMultilines(
            (context, itemStack, item) -> {
                if (item instanceof NuclearAbsorbable) {
                    List<Component> tooltips = new ArrayList<>();
                    long remAbs = ((NuclearAbsorbable) itemStack.getItem()).getRemainingDesintegrations(itemStack);
                    tooltips.add(new MITooltips.Line(MIText.RemAbsorption).arg(remAbs)
                            .arg(((NuclearAbsorbable) itemStack.getItem()).desintegrationMax).build());
                    if (itemStack.getItem() instanceof NuclearFuel fuel) {
                        long totalFe = (long) fuel.totalFEbyDesintegration * fuel.desintegrationMax;
                        tooltips.add(new MITooltips.Line(MIText.BaseFeTotalStored).arg(totalFe, MITooltips.FE_PARSER).build());
                    }
                    return tooltips;
                } else {
                    return List.of();
                }
            });
    public static final TooltipAttachment ORES = TooltipAttachment.ofMultilines(
            (context, itemStack, item) -> {
                if (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof OreBlock) {
                    OreBlock oreBlock = (OreBlock) ((BlockItem) itemStack.getItem()).getBlock();
                    List<Component> lines = new ArrayList<>();

                    if (MIClientConfig.INSTANCE.defaultOreGenTooltips.getAsBoolean()) {
                        if (oreBlock.params.generate()) {
                            lines.add(new Line(MIText.OreGenerationTooltipY).arg(-64).arg(oreBlock.params.maxYLevel()).build());
                            lines.add(new Line(MIText.OreGenerationTooltipVeinFrequency).arg(oreBlock.params.veinsPerChunk()).build());
                            lines.add(new Line(MIText.OreGenerationTooltipVeinSize).arg(oreBlock.params.veinSize()).build());
                        } else {
                            lines.add(new Line(MIText.OreNotGenerated).build());
                        }

                        return lines;
                    }
                }

                return List.of();
            });
    public static final TooltipAttachment DIESEL_TOOLS = TooltipAttachment.ofMultilines(
            (context, itemStack, item) -> {
                if (item instanceof DieselToolItem tool) {
                    return List.of(
                            line(MIText.ToolConfiguration).build(),
                            line(MIText.DieselToolFuelHelp1).arg("use", KEYBIND_PARSER).build(),
                            line(MIText.DieselToolFuelHelp2).build(),
                            line(MIText.DieselToolToggle).arg("sneak", KEYBIND_PARSER).arg("use", KEYBIND_PARSER).build());
                } else {
                    return List.of();
                }
            });

    public static final TooltipAttachment STEAM_DRILL = TooltipAttachment.ofMultilines(MIItem.STEAM_MINING_DRILL,
            List.of(
                    line(MIText.ToolConfiguration).build(),
                    line(MIText.SteamDrillWaterHelp).arg("use", KEYBIND_PARSER).build(),
                    line(MIText.SteamDrillFuelHelp).arg("use", KEYBIND_PARSER).build(),
                    line(MIText.SteamDrillProfit).arg("moderntech.toggle_3x3", KEYBIND_PARSER).build(),
                    line(MIText.SteamDrillToggle).arg("sneak", KEYBIND_PARSER).arg("use", KEYBIND_PARSER).build()));
    public static void attachTooltip(Item.TooltipContext context, ItemStack stack, List<Component> lines) {
        boolean hasPrintRequiredShift = false;
        for (var tooltip : TOOLTIPS) {
            var maybeComponents = tooltip.tooltipLines.get(context, stack, stack.getItem());
            if (!tooltip.requiresShift || MICommonProxy.INSTANCE.hasShiftDown()) {
                lines.addAll(maybeComponents);
            } else if (tooltip.requiresShift && !hasPrintRequiredShift) {
                if (!maybeComponents.isEmpty()) {
                    lines.add(MIText.TooltipsShiftRequired.text().setStyle(DEFAULT_STYLE));
                    hasPrintRequiredShift = true;
                }
            }
        }
    }

    public static final Map<String, String> TOOLTIPS_ENGLISH_TRANSLATION = new HashMap<>();

    private static void add(Predicate<ItemLike> attachTo, String translationId, String... englishTooltipsLine) {
        int lineCount = englishTooltipsLine.length;

        Preconditions.checkArgument(lineCount > 0);

        String[] translationKey = IntStream.range(0, lineCount).mapToObj(l -> "item_tooltip.moderntech." + translationId + ".line_" + l)
                .toArray(String[]::new);

        for (int i = 0; i < lineCount; i++) {
            TOOLTIPS_ENGLISH_TRANSLATION.put(translationKey[i], englishTooltipsLine[i]);
        }

        MITooltips.TooltipAttachment.ofMultilines((context, itemStack, item) -> {
            if (attachTo.test(item)) {
                return Arrays.stream(translationKey).map((key) -> Component.translatable(key).withStyle(DEFAULT_STYLE))
                        .collect(Collectors.toList());
            } else {
                return List.of();
            }
        });
    }

    private static void add(ItemLike itemLike, String... englishTooltipsLine) {
        add((item) -> itemLike.asItem() == item, BuiltInRegistries.ITEM.getKey(itemLike.asItem()).getPath(), englishTooltipsLine);
    }

    private static void add(String itemId, String... englishTooltipsLine) {
        add(BuiltInRegistries.ITEM.get(MI.id(itemId)), englishTooltipsLine);
    }

    static {
        add(MIBlock.FORGE_HAMMER, "Use it to increase the yield of your ore blocks early game!",
                "(Use the Steam Mining Drill for an easy to get Silk Touch.)");
        add("stainless_steel_dust", "Use Slot-Locking with REI to differentiate its recipe from the invar dust");
        add("steam_blast_furnace", "Needs at least one Steel or higher tier hatch for 3 and 4 FE/t recipes");
        add(MIBlock.TRASH_CAN, "Will delete any item or fluid sent into it.",
                "Can also be used to empty a fluid slot by Right-Clicking on it with a Trash Can");

    }

    public static class TooltipAttachment implements Comparable<TooltipAttachment> {
        public final Provider tooltipLines;

        private TooltipAttachment(Provider tooltipLines) {
            this.tooltipLines = tooltipLines;
            MITooltips.TOOLTIPS.add(this);
        }

        public static TooltipAttachment of(ItemLike itemLike, Line line) {
            return new TooltipAttachment(
                    (context, itemStack, item) -> itemStack.getItem() == itemLike.asItem() ? List.of(line.build()) : List.of());
        }
        public boolean requiresShift = true;
        public int priority = 0;

        public static TooltipAttachment of(ItemLike itemLike, MIText text) {
            return of(itemLike, new Line(text));
        }

        public static TooltipAttachment of(ProviderOptional tooltipLines) {
            return new TooltipAttachment((context, itemStack, item) -> {
                var component = tooltipLines.get(context, itemStack, item);
                return component.isEmpty() ? List.of() : List.of(component.get());
            });
        }

        public static TooltipAttachment ofMultilines(Provider tooltipLines) {
            return new TooltipAttachment(tooltipLines);
        }

        public static TooltipAttachment ofMultilines(ItemLike itemLike, List<? extends Component> tooltipLines) {
            return new TooltipAttachment((context, itemStack, item) -> {
                if (itemStack.getItem() == itemLike.asItem()) {
                    return tooltipLines;
                } else {
                    return List.of();
                }
            });
        }

        public interface Provider {
            List<? extends Component> get(Item.TooltipContext context, ItemStack stack, Item item);
        }

        public static TooltipAttachment ofMultilines(ItemLike itemLike, MIText... tooltipLines) {
            return ofMultilines(itemLike,
                    Arrays.stream(tooltipLines).map((text) -> text.text().withStyle(DEFAULT_STYLE)).collect(Collectors.toList()));
        }

        public interface ProviderOptional {
            Optional<Component> get(Item.TooltipContext context, ItemStack stack, Item item);
        }

        public TooltipAttachment noShiftRequired() {
            this.requiresShift = false;
            return this;
        }

        public TooltipAttachment setPriority(int priority) {
            this.priority = priority;
            return this;
        }

        @Override
        public int compareTo(MITooltips.TooltipAttachment o) {
            return -Integer.compare(priority, o.priority);
        }
    }

    public static Line line(MIText baseText) {
        return new Line(baseText);
    }

    public static Line line(MIText baseText, Style style) {
        return new Line(baseText, style);
    }

    public static class Line {
        public final MIText baseText;
        public final Style baseStyle;

        public final List<Component> args = new ArrayList<>();

        public Line(MIText baseText, Style style) {
            this.baseText = baseText;
            this.baseStyle = style;
        }

        public Line(MIText baseText) {
            this(baseText, MITooltips.DEFAULT_STYLE);
        }

        public <T> Line arg(T arg, Parser<T> parser) {
            args.add(parser.parse(arg));
            return this;
        }

        public Line arg(Object arg) {
            arg(arg, MITooltips.DEFAULT_PARSER);
            return this;
        }

        public MutableComponent build() {
            return baseText.text(args.toArray()).withStyle(baseStyle);
        }
    }

    @FunctionalInterface
    public interface Parser<T> {
        Component parse(T t);
    }
}
