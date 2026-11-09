package aztech.milite.datagen.recipe;

import aztech.milite.MIItem;

import aztech.milite.machines.init.MIMachineRecipeTypes;
import aztech.milite.machines.recipe.MachineRecipeBuilder;
import aztech.milite.recipe.json.ShapedRecipeJson;
import aztech.milite.recipe.json.ShapelessRecipeBuilder;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;

public class ElectricAgeRecipesProvider extends FixedRecipesProvider {
    public ElectricAgeRecipesProvider(PackOutput output) {
        super(output);
    }

    @Override
    public void buildRecipes(RecipeOutput consumer) {
        new ShapelessRecipeBuilder("moderntech:cupronickel_wire_magnetic", 1)
                .requires("moderntech:cupronickel_wire").requires("minecraft:redstone")
                .offerTo(consumer, "electric_age/component/craft/cupronickel_wire_magnetic");
        shaped(consumer, "electric_age/battery/cadmium_battery_asbl",
                new ShapedRecipeJson("moderntech:cadmium_battery", 1, "cac", "BrB", "BrB")
                        .addInput('a', "#c:plates/battery_alloy")
                        .addInput('c', "moderntech:annealed_copper_wire")
                        .addInput('B', "moderntech:battery_alloy_plate")
                        .addInput('r', "#c:dusts/cadmium"));

        shaped(consumer, "electric_age/battery/lv_battery_asbl",
                new ShapedRecipeJson("moderntech:redstone_battery", 1, "cac", "BrB", "BrB")
                        .addInput('a', "#c:plates/battery_alloy")
                        .addInput('c', "moderntech:tin_wire")
                        .addInput('B', "moderntech:battery_alloy_plate")
                        .addInput('r', "minecraft:redstone"));

        shaped(consumer, "electric_age/battery/plutonium_battery_asbl",
                new ShapedRecipeJson("moderntech:plutonium_battery", 1, "cac", "BrB", "BrB")
                        .addInput('a', "#c:plates/battery_alloy")
                        .addInput('c', "moderntech:superconductor_wire")
                        .addInput('B', "moderntech:battery_alloy_plate")
                        .addInput('r', "#c:dusts/plutonium"));

        shaped(consumer, "electric_age/battery/portable_storage_unit_asbl",
                new ShapedRecipeJson("moderntech:portable_storage_unit", 1, "aCa", "crc", "aCa")
                        .addInput('a', "#c:plates/battery_alloy")
                        .addInput('c', "moderntech:battery_alloy_plate")
                        .addInput('C', "moderntech:tin_wire")
                        .addInput('r', "moderntech:analog_circuit"));

        shaped(consumer, "electric_age/battery/silicon_battery_asbl",
                new ShapedRecipeJson("moderntech:silicon_battery", 1, "cac", "BrB", "BrB")
                        .addInput('a', "#c:plates/battery_alloy")
                        .addInput('c', "moderntech:electrum_wire")
                        .addInput('B', "moderntech:battery_alloy_plate")
                        .addInput('r', "#c:dusts/silicon"));

        shaped(consumer, "electric_age/battery/sodium_battery_asbl",
                new ShapedRecipeJson("moderntech:sodium_battery", 1, "cac", "BrB", "BrB")
                        .addInput('a', "#c:plates/battery_alloy")
                        .addInput('c', "moderntech:aluminum_wire")
                        .addInput('B', "moderntech:battery_alloy_plate")
                        .addInput('r', "#c:dusts/sodium"));

        new MachineRecipeBuilder(MIMachineRecipeTypes.ASSEMBLER, 32, 1000)
                .level("bronze")
                .addItemInput("moderntech:iridium_machine_hull", 1, 1.0f)
                .addItemInput("moderntech:singularity", 1, 1.0f)
                .addItemInput(MIItem.QUANTUM_PROCESSOR, 2, 1.0f)
                .addItemInput("#c:plates/iridium", 16, 1.0f)
                .addItemInput("moderntech:large_motor", 8, 1.0f)
                .addItemInput("moderntech:titanium_barrel", 1, 1.0f)
                .addItemOutput("moderntech:quantum_barrel", 1, 1.0f)
                .offerTo(consumer, "electric_age/blocks/assembler/quantum_barrel");

        new MachineRecipeBuilder(MIMachineRecipeTypes.ASSEMBLER, 32, 1000)
                .level("bronze")
                .addItemInput("moderntech:iridium_machine_hull", 1, 1.0f)
                .addItemInput("moderntech:singularity", 1, 1.0f)
                .addItemInput(MIItem.QUANTUM_PROCESSOR, 2, 1.0f)
                .addItemInput("#c:plates/iridium", 16, 1.0f)
                .addItemInput("moderntech:large_pump", 8, 1.0f)
                .addItemInput("moderntech:titanium_tank", 1, 1.0f)
                .addItemOutput("moderntech:quantum_tank", 1, 1.0f)
                .offerTo(consumer, "electric_age/blocks/assembler/quantum_tank");

        shaped(consumer, "electric_age/blocks/fusion_chamber_asbl",
                new ShapedRecipeJson("moderntech:fusion_chamber", 1, "ChC", "LHL", "ChC")
                        .addInput('C', "moderntech:superconductor_coil")
                        .addInput('h', "moderntech:large_heat_exchanger")
                        .addInput('L', "moderntech:nuclear_alloy_large_plate")
                        .addInput('H', "moderntech:plasma_handling_iridium_machine_casing"));

        shaped(consumer, "electric_age/casing/aluminum_alloy_machine_casing_asbl",
                new ShapedRecipeJson("moderntech:aluminum_alloy_machine_casing", 1, "PPP", "PGP", "PPP")
                        .addInput('P', "#c:plates/aluminum_alloy")
                        .addInput('G', "#c:gears/aluminum_alloy"));

        shaped(consumer, "electric_age/casing/blastproof_casing_asbl",
                new ShapedRecipeJson("moderntech:blastproof_casing", 1, "AAA", "A A", "AAA")
                        .addInput('A', "#c:plates/blastproof_alloy"));

        shaped(consumer, "electric_age/casing/bronze_plated_bricks_asbl",
                new ShapedRecipeJson("moderntech:bronze_plated_bricks", 1, "AAA", "AHA", "AAA")
                        .addInput('A', "#c:plates/bronze")
                        .addInput('H', "moderntech:fire_clay_bricks"));

        shaped(consumer, "electric_age/casing/clean_stainless_steel_machine_casing_asbl",
                new ShapedRecipeJson("moderntech:clean_stainless_steel_machine_casing", 1, "AAA", "AGA", "AAA")
                        .addInput('A', "#c:plates/stainless_steel")
                        .addInput('G', "#c:gears/stainless_steel"));

        shaped(consumer, "electric_age/casing/frostproof_machine_casing_asbl",
                new ShapedRecipeJson("moderntech:frostproof_machine_casing", 1, "AAA", "AGA", "AAA")
                        .addInput('A', "#c:plates/aluminum")
                        .addInput('G', "#c:gears/aluminum"));

        shaped(consumer, "electric_age/casing/heatproof_machine_casing_asbl",
                new ShapedRecipeJson("moderntech:heatproof_machine_casing", 1, "PPP", "PGP", "PPP")
                        .addInput('P', "#c:plates/invar")
                        .addInput('G', "#c:gears/invar"));

        shaped(consumer, "electric_age/casing/iridium_machine_casing_asbl",
                new ShapedRecipeJson("moderntech:iridium_machine_casing", 1, "AAA", "AHA", "AAA")
                        .addInput('A', "#c:plates/iridium")
                        .addInput('H', "moderntech:titanium_machine_hull"));

        shaped(consumer, "electric_age/casing/nuclear_casing_asbl",
                new ShapedRecipeJson("moderntech:nuclear_casing", 1, "AAA", "ACA", "AAA")
                        .addInput('A', "#c:plates/nuclear_alloy")
                        .addInput('C', "moderntech:stainless_steel_machine_hull"));

        shaped(consumer, "electric_age/casing/tungsten_casing_asbl",
                new ShapedRecipeJson("moderntech:tungsten_casing", 1, "AAA", "ACA", "AAA")
                        .addInput('A', "#c:plates/tungsten")
                        .addInput('C', "moderntech:titanium_machine_hull"));

        shaped(consumer, "electric_age/casing/nuclear_fluid_hatch_asbl",
                new ShapedRecipeJson("moderntech:nuclear_fluid_hatch", 1, "AAA", "ACA", "AAA")
                        .addInput('A', "#c:plates/nuclear_alloy")
                        .addInput('C', "moderntech:titanium_fluid_input_hatch"));

        shaped(consumer, "electric_age/casing/nuclear_item_hatch_asbl",
                new ShapedRecipeJson("moderntech:nuclear_item_hatch", 1, "AAA", "ACA", "AAA")
                        .addInput('A', "#c:plates/nuclear_alloy")
                        .addInput('C', "moderntech:titanium_item_input_hatch"));

        shaped(consumer, "electric_age/casing/control_rod_asbl",
                new ShapedRecipeJson("moderntech:control_rod", 4, "ARA", "RCR", "ARA")
                        .addInput('A', "#c:plates/nuclear_alloy")
                        .addInput('R', "moderntech:cadmium_control_rod")
                        .addInput('C', "moderntech:nuclear_casing"));

        shaped(consumer, "electric_age/casing/plasma_handling_iridium_machine_casing_asbl",
                new ShapedRecipeJson("moderntech:plasma_handling_iridium_machine_casing", 1, "AAA", "ACA", "AAA")
                        .addInput('A', "#c:plates/iridium")
                        .addInput('C', "moderntech:superconductor_coil"));

        shaped(consumer, "electric_age/casing/solid_titanium_machine_casing_asbl",
                new ShapedRecipeJson("moderntech:solid_titanium_machine_casing", 1, "AAA", "AGA", "AAA")
                        .addInput('A', "#c:plates/titanium")
                        .addInput('G', "#c:gears/titanium"));

        shaped(consumer, "electric_age/casing/stainless_steel_machine_casing_asbl",
                new ShapedRecipeJson("moderntech:stainless_steel_machine_casing", 1, "AAA", "AHA", "AAA")
                        .addInput('A', "#c:plates/stainless_steel")
                        .addInput('H', "moderntech:aluminum_alloy_machine_hull"));

        shaped(consumer, "electric_age/casing/titanium_machine_casing_asbl",
                new ShapedRecipeJson("moderntech:titanium_machine_casing", 1, "ACA", "CHC", "ACA")
                        .addInput('A', "#c:plates/titanium")
                        .addInput('C', "#c:plates/chromium")
                        .addInput('H', "moderntech:stainless_steel_machine_hull"));





        new MachineRecipeBuilder(MIMachineRecipeTypes.ASSEMBLER, 16, 500)
                .level("bronze")
                .addItemInput("moderntech:aluminum_wire", 6, 1.0f)
                .addItemInput("moderntech:silicon_battery", 1, 1.0f)
                .addItemInput("moderntech:stainless_steel_plate", 4, 1.0f)
                .addFluidInput("moderntech:polyethylene", 750, 1.0f)
                .addItemOutput("moderntech:digital_circuit_board", 1, 1.0f)
                .offerTo(consumer, "electric_age/circuit/assembler/digital_circuit_board");

        shaped(consumer, "electric_age/circuit/craft/digital_circuit_asbl",
                new ShapedRecipeJson("moderntech:digital_circuit", 1, "cAc", "wbw", "cAc")
                        .addInput('A', "moderntech:op_amp")
                        .addInput('c', "moderntech:electronic_circuit")
                        .addInput('w', "moderntech:aluminum_wire")
                        .addInput('b', "moderntech:digital_circuit_board"));

        shaped(consumer, "electric_age/circuit/craft/electronic_circuit_asbl",
                new ShapedRecipeJson("moderntech:electronic_circuit", 1, "cdc", "tbt", "cdc")
                        .addInput('d', "#c:plates/steel")
                        .addInput('c', "moderntech:analog_circuit")
                        .addInput('t', "#c:plates/silicon")
                        .addInput('b', "moderntech:electronic_circuit_board"));

        new MachineRecipeBuilder(MIMachineRecipeTypes.ASSEMBLER, 8, 200)
                .level("electric")
                .addItemInput("moderntech:electrum_wire", 4, 1.0f)
                .addItemInput("moderntech:redstone_battery", 1, 1.0f)
                .addItemInput("#c:plates/aluminum", 2, 1.0f)
                .addItemOutput("moderntech:electronic_circuit_board", 1, 1.0f)
                .offerTo(consumer, "assembler_generated/electric_age/circuit/craft/electronic_circuit_board");

        shaped(consumer, "electric_age/circuit/craft/lv_circuit_asbl",
                new ShapedRecipeJson("moderntech:analog_circuit", 1, "RLR", "CBC", "www")
                        .addInput('L', "moderntech:inductor")
                        .addInput('w', "moderntech:copper_wire")
                        .addInput('R', "moderntech:resistor")
                        .addInput('C', "moderntech:capacitor")
                        .addInput('B', "moderntech:analog_circuit_board"));

        shaped(consumer, "electric_age/circuit/craft/lv_circuit_board_asbl",
                new ShapedRecipeJson("moderntech:analog_circuit_board", 1, "r", "c", "r")
                        .addInput('r', "moderntech:rubber_sheet")
                        .addInput('c', "#c:plates/copper"));



        new MachineRecipeBuilder(MIMachineRecipeTypes.ASSEMBLER, 8, 200)
                .level("bronze")
                .addItemInput("#c:plates/gold", 2, 1.0f)
                .addItemInput("moderntech:copper_wire", 2, 1.0f)
                .addItemInput("moderntech:rubber_sheet", 1, 1.0f)
                .addItemOutput("moderntech:capacitor", 4, 1.0f)
                .offerTo(consumer, "electric_age/component/assembler/capacitor");

        new MachineRecipeBuilder(MIMachineRecipeTypes.ASSEMBLER, 16, 400)
                .level("bronze")
                .addItemInput("moderntech:bronze_plate", 3, 1.0f)
                .addItemInput("moderntech:small_heat_exchanger", 2, 1.0f)
                .addItemInput("#c:plates/tin", 6, 1.0f)
                .addFluidInput("moderntech:cryofluid", 1000, 1.0f)
                .addItemOutput("moderntech:cooling_cell", 1, 1.0f)
                .offerTo(consumer, "electric_age/component/assembler/cooling_cell");

        new MachineRecipeBuilder(MIMachineRecipeTypes.ASSEMBLER, 8, 200)
                .level("bronze")
                .addItemInput("moderntech:copper_wire", 8, 1.0f)
                .addItemInput("#c:rods/steel", 1, 1.0f)
                .addItemOutput("moderntech:inductor", 4, 1.0f)
                .offerTo(consumer, "electric_age/component/assembler/inductor");

        new MachineRecipeBuilder(MIMachineRecipeTypes.ASSEMBLER, 8, 200)
                .level("bronze")
                .addItemInput("moderntech:bronze_plate", 3, 1.0f)
                .addItemInput("moderntech:tin_rotor", 3, 1.0f)
                .addItemInput("moderntech:motor", 1, 1.0f)
                .addFluidInput("moderntech:soldering_alloy", 100, 1.0f)
                .addItemOutput("moderntech:pump", 1, 1.0f)
                .offerTo(consumer, "electric_age/component/assembler/pump");


        new MachineRecipeBuilder(MIMachineRecipeTypes.ASSEMBLER, 8, 200)
                .level("bronze")
                .addItemInput("moderntech:copper_fine_wire", 2, 1.0f)
                .addItemInput("#c:dusts/coal", 1, 1.0f)
                .addItemOutput("moderntech:resistor", 12, 1.0f)
                .offerTo(consumer, "electric_age/component/assembler/resistor");

        new MachineRecipeBuilder(MIMachineRecipeTypes.BLAST_FURNACE, 32, 1500)
                .level("electric")
                .addItemInput("#c:dusts/silicon", 32, 1.0f)
                .addItemOutput("moderntech:monocrystalline_silicon", 1, 1.0f)
                .offerTo(consumer, "electric_age/component/blast_furnace/monocrystalline_silicon");


        shaped(consumer, "electric_age/component/craft/air_intake_asbl",
                new ShapedRecipeJson("moderntech:air_intake", 1, "ara", "rRr", "ara")
                        .addInput('r', "#c:plates/stainless_steel")
                        .addInput('R', "moderntech:stainless_steel_rotor")
                        .addInput('a', "#c:plates/aluminum"));



        shaped(consumer, "electric_age/component/craft/op_amp_asbl",
                new ShapedRecipeJson("moderntech:op_amp", 1, "rTT", "rdr", "rTT")
                        .addInput('r', "moderntech:resistor")
                        .addInput('T', "#c:plates/silicon")
                        .addInput('d', "#c:plates/steel"));

        shaped(consumer, "electric_age/component/craft/capacitor",
                new ShapedRecipeJson("moderntech:capacitor", 1, " P ", "wSw", " P ")
                        .addInput('P', "#c:plates/gold")
                        .addInput('w', "moderntech:copper_wire")
                        .addInput('S', "moderntech:rubber_sheet"));

        shaped(consumer, "electric_age/component/craft/conveyor_asbl",
                new ShapedRecipeJson("moderntech:conveyor", 1, "rrr", "mcm", "rrr")
                        .addInput('m', "moderntech:motor")
                        .addInput('r', "moderntech:rubber_sheet")
                        .addInput('c', "moderntech:tin_wire"));

        shaped(consumer, "electric_age/component/craft/inductor",
                new ShapedRecipeJson("moderntech:inductor", 1, "www", "wrw", "www")
                        .addInput('r', "#c:rods/steel")
                        .addInput('w', "moderntech:copper_wire"));

        shaped(consumer, "electric_age/component/craft/invar_rotary_blade_asbl",
                new ShapedRecipeJson("moderntech:invar_rotary_blade", 1, " d ", "dGd", " d ")
                        .addInput('d', "#c:dusts/diamond")
                        .addInput('G', "#c:gears/invar"));

        shaped(consumer, "electric_age/component/craft/large_heat_exchanger_asbl",
                new ShapedRecipeJson("moderntech:large_heat_exchanger", 1, "ghg", "RLR", "ghg")
                        .addInput('h', "moderntech:small_heat_exchanger")
                        .addInput('L', "moderntech:large_pump")
                        .addInput('R', "moderntech:aluminum_rotor")
                        .addInput('g', "#c:gears/invar"));

        shaped(consumer, "electric_age/component/craft/large_motor_asbl",
                new ShapedRecipeJson("moderntech:large_motor", 1, "mmm", "Crr", "mmm")
                        .addInput('C', "moderntech:electronic_circuit")
                        .addInput('r', "#c:rods/aluminum")
                        .addInput('m', "moderntech:motor"));

        shaped(consumer, "electric_age/component/craft/large_pump_asbl",
                new ShapedRecipeJson("moderntech:large_pump", 1, "ppp", "rMr", "CrC")
                        .addInput('C', "moderntech:electronic_circuit")
                        .addInput('r', "moderntech:aluminum_rotor")
                        .addInput('M', "moderntech:large_motor")
                        .addInput('p', "moderntech:pump"));


        shaped(consumer, "electric_age/component/craft/motor_asbl",
                new ShapedRecipeJson("moderntech:motor", 1, "cwr", "wmw", "rwc")
                        .addInput('m', "moderntech:steel_rod_magnetic")
                        .addInput('r', "#c:rods/steel")
                        .addInput('c', "moderntech:tin_wire")
                        .addInput('w', "moderntech:copper_wire"));




        shaped(consumer, "electric_age/component/craft/overclocker_unit_asbl",
                new ShapedRecipeJson("moderntech:overclocker_unit", 1, "wdw", "tbt", "wdw")
                        .addInput('d', "#c:plates/steel")
                        .addInput('t', "#c:plates/silicon")
                        .addInput('w', "moderntech:electrum_fine_wire")
                        .addInput('b', "moderntech:electronic_circuit_board"));

        shaped(consumer, "electric_age/component/craft/piston_asbl",
                new ShapedRecipeJson("moderntech:piston", 1, "ppp", "crr", "cmg")
                        .addInput('m', "moderntech:motor")
                        .addInput('r', "#c:rods/steel")
                        .addInput('c', "moderntech:tin_wire")
                        .addInput('p', "#c:plates/steel")
                        .addInput('g', "#c:gears/steel"));

        shaped(consumer, "electric_age/component/craft/pump",
                new ShapedRecipeJson("moderntech:pump", 1, "brb", "rmr", "brb")
                        .addInput('m', "moderntech:motor")
                        .addInput('r', "moderntech:tin_rotor")
                        .addInput('b', "moderntech:tin_bolt"));

        shaped(consumer, "electric_age/component/craft/resistor",
                new ShapedRecipeJson("moderntech:resistor", 3, " P ", "FCF", " P ")
                        .addInput('P', "minecraft:paper")
                        .addInput('F', "moderntech:copper_fine_wire")
                        .addInput('C', "#c:dusts/coal"));

        shaped(consumer, "electric_age/component/craft/robot_arm_asbl",
                new ShapedRecipeJson("moderntech:robot_arm", 1, "ccc", "mrm", "pCr")
                        .addInput('m', "moderntech:motor")
                        .addInput('r', "#c:rods/steel")
                        .addInput('c', "moderntech:tin_wire")
                        .addInput('p', "moderntech:piston")
                        .addInput('C', "moderntech:analog_circuit"));

        shaped(consumer, "electric_age/component/craft/small_heat_exchanger_asbl",
                new ShapedRecipeJson("moderntech:small_heat_exchanger", 1, "pPp", "PLP", "pPp")
                        .addInput('P', "#c:plates/invar")
                        .addInput('L', "moderntech:stainless_steel_large_plate")
                        .addInput('p', "moderntech:pump"));

        shaped(consumer, "electric_age/component/craft/steel_rod_magnetic",
                new ShapedRecipeJson("moderntech:steel_rod_magnetic", 1, " RR", "RrR", "RR ")
                        .addInput('R', "minecraft:redstone")
                        .addInput('r', "#c:rods/steel"));

        shaped(consumer, "electric_age/component/craft/ultradense_metal_ball_asbl",
                new ShapedRecipeJson("moderntech:ultradense_metal_ball", 1, "TlT", "lIl", "TlT")
                        .addInput('T', "moderntech:tungsten_large_plate")
                        .addInput('l', "#c:plates/lead")
                        .addInput('I', "#c:plates/iridium"));

        new MachineRecipeBuilder(MIMachineRecipeTypes.CUTTING_MACHINE, 16, 600)
                .level("aluminum_alloy")
                .addItemInput("moderntech:monocrystalline_silicon", 1, 1.0f)
                .addItemOutput("moderntech:silicon_wafer", 32, 1.0f)
                .offerTo(consumer, "electric_age/component/cutting_machine/silicon_waffer");

        new MachineRecipeBuilder(MIMachineRecipeTypes.IMPLOSION_COMPRESSOR, 1, 10)
                .level("bronze")
                .addItemInput("moderntech:ultradense_metal_ball", 1, 1.0f)
                .addItemInput("moderntech:nuke", 64, 1.0f)
                .addItemOutput("moderntech:singularity", 1, 1.0f)
                .offerTo(consumer, "electric_age/component/implosion_compressor/singularity");

        new MachineRecipeBuilder(MIMachineRecipeTypes.POLARIZER, 16, 400)
                .level("aluminum_alloy")
                .addItemInput("#c:rods/stainless_steel", 1, 1.0f)
                .addItemInput("moderntech:neodymium_dust", 1, 1.0f)
                .addItemOutput("moderntech:stainless_steel_rod_magnetic", 1, 1.0f)
                .offerTo(consumer, "electric_age/component/polarizer/stainless_steel_rod_magnetic");

        shaped(consumer, "electric_age/hull/aluminum_alloy_machine_hull_asbl",
                new ShapedRecipeJson("moderntech:aluminum_alloy_machine_hull", 1, " c ", "BCB", "ppp")
                        .addInput('B', "moderntech:silicon_battery")
                        .addInput('c', "moderntech:electronic_circuit")
                        .addInput('C', "moderntech:aluminum_alloy_machine_casing")
                        .addInput('p', "moderntech:electrum_wire"));

        shaped(consumer, "electric_age/hull/electric_machine_hull_asbl",
                new ShapedRecipeJson("moderntech:electric_machine_hull", 1, " A ", "BCB", "ccc")
                        .addInput('A', "moderntech:analog_circuit")
                        .addInput('c', "moderntech:tin_wire")
                        .addInput('B', "moderntech:redstone_battery")
                        .addInput('C', "moderntech:steel_machine_casing"));

        shaped(consumer, "electric_age/hull/iridium_machine_hull_asbl",
                new ShapedRecipeJson("moderntech:iridium_machine_hull", 1, " c ", "BCB", "ppp")
                        .addInput('B', "moderntech:plutonium_battery")
                        .addInput('c', MIItem.QUANTUM_PROCESSOR.id())
                        .addInput('C', "moderntech:iridium_machine_casing")
                        .addInput('p', "moderntech:superconductor_wire"));

        shaped(consumer, "electric_age/hull/stainless_steel_machine_hull_asbl",
                new ShapedRecipeJson("moderntech:stainless_steel_machine_hull", 1, " c ", "BCB", "ppp")
                        .addInput('B', "moderntech:sodium_battery")
                        .addInput('c', MIItem.DIGITAL_CIRCUIT.id())
                        .addInput('C', "moderntech:stainless_steel_machine_casing")
                        .addInput('p', "moderntech:aluminum_wire"));

        shaped(consumer, "electric_age/hull/titanium_machine_hull_asbl",
                new ShapedRecipeJson("moderntech:titanium_machine_hull", 1, " c ", "BCB", "ppp")
                        .addInput('B', "moderntech:cadmium_battery")
                        .addInput('c', MIItem.PROCESSOR.id())
                        .addInput('C', "moderntech:titanium_machine_casing")
                        .addInput('p', "moderntech:annealed_copper_wire"));







        new MachineRecipeBuilder(MIMachineRecipeTypes.ASSEMBLER, 32, 8000)
                .level("bronze")
                .addItemInput("moderntech:iridium_machine_hull", 1, 1.0f)
                .addItemInput("moderntech:quantum_upgrade", 2, 1.0f)
                .addItemInput(MIItem.QUANTUM_PROCESSOR, 4, 1.0f)
                .addFluidInput("moderntech:uu_matter", 500, 1.0f)
                .addItemOutput("moderntech:replicator", 1, 1.0f)
                .offerTo(consumer, "electric_age/machine/assembler/replicator");









































        new ShapelessRecipeBuilder("moderntech:large_tank_hatch", 1)
                .requires("moderntech:steel_machine_hull")
                .requires("minecraft:bucket")
                .offerTo(consumer, "electric_age/machine/large_tank_hatch");

        shaped(consumer, "electric_age/storage_unit/aluminum_alloy_storage_unit_asbl",
                new ShapedRecipeJson("moderntech:aluminum_alloy_storage_unit", 1, "bbb", "LHL", "bbb")
                        .addInput('b', "moderntech:silicon_battery")
                        .addInput('H', "moderntech:aluminum_alloy_machine_hull")
                        .addInput('L', "#c:plates/battery_alloy"));

        shaped(consumer, "electric_age/storage_unit/electric_storage_unit_asbl",
                new ShapedRecipeJson("moderntech:electric_storage_unit", 1, "bbb", "LHL", "bbb")
                        .addInput('b', "moderntech:redstone_battery")
                        .addInput('H', "moderntech:electric_machine_hull")
                        .addInput('L', "#c:plates/battery_alloy"));

        shaped(consumer, "electric_age/storage_unit/iridium_storage_unit_asbl",
                new ShapedRecipeJson("moderntech:iridium_storage_unit", 1, "bbb", "LHL", "bbb")
                        .addInput('b', "moderntech:plutonium_battery")
                        .addInput('H', "moderntech:iridium_machine_hull")
                        .addInput('L', "#c:plates/battery_alloy"));

        shaped(consumer, "electric_age/storage_unit/stainless_steel_storage_unit_asbl",
                new ShapedRecipeJson("moderntech:stainless_steel_storage_unit", 1, "bbb", "LHL", "bbb")
                        .addInput('b', "moderntech:sodium_battery")
                        .addInput('H', "moderntech:stainless_steel_machine_hull")
                        .addInput('L', "#c:plates/battery_alloy"));

        shaped(consumer, "electric_age/storage_unit/titanium_storage_unit_asbl",
                new ShapedRecipeJson("moderntech:titanium_storage_unit", 1, "bbb", "LHL", "bbb")
                        .addInput('b', "moderntech:cadmium_battery")
                        .addInput('H', "moderntech:titanium_machine_hull")
                        .addInput('L', "#c:plates/battery_alloy"));

        new MachineRecipeBuilder(MIMachineRecipeTypes.ASSEMBLER, 32, 1000)
                .level("bronze")
                .addItemInput(MIItem.QUANTUM_PROCESSOR, 8, 1.0f)
                .addItemInput("moderntech:singularity", 1, 1.0f)
                .addFluidInput("moderntech:uu_matter", 50, 1.0f)
                .addItemOutput("moderntech:quantum_upgrade", 1, 1.0f)
                .offerTo(consumer, "electric_age/upgrades/quantum");
    }
}
