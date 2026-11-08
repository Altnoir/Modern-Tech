---
navigation:
  title: "Processors and Lithography"
  icon: "moderntech:stainless_steel_lithography"
  position: 202
  parent: moderntech:midgame.md
item_ids:
  - moderntech:stainless_steel_lithography
  - moderntech:titanium_lithography
  - moderntech:iridium_lithography
  - moderntech:digital_circuit
  - moderntech:digital_circuit_board
  - moderntech:processor
  - moderntech:quantum_processor
  - moderntech:silicon_plate
---

# Processors and Lithography

Craft Analog, Electronic, and then Digital Circuits. Stainless Steel Machine Hulls use Digital Circuits. Lithography Machines make processors directly from Silicon Plates and metal plates, using two item inputs, one output and no fluids.

Assemble a Digital Circuit Board, then combine it with Electronic Circuits, Op Amps and Aluminum Wire to craft a Digital Circuit.

<Recipe id="moderntech:electric_age/circuit/assembler/digital_circuit_board" />
<Recipe id="moderntech:electric_age/circuit/craft/digital_circuit_asbl" />

## Preparing Silicon Plates

Use a Compressor to turn one Silicon Ingot into one Silicon Plate. Lithography recipes use Silicon Plates and do not need Monocrystalline Silicon or Silicon Wafers.

<Recipe id="moderntech:materials/silicon/compressor/main" />

## The First Lithography Machine

Build the stainless steel machine with an Aluminum Alloy Machine Hull, two Stainless Steel Plates, a Glass Block, a Robot Arm and an Electronic Circuit. It does not require any of the processors it produces.

<Recipe id="moderntech:processing_table/stainless_steel_lithography" />
<Recipe id="moderntech:lithography/processor" />

One Silicon Plate and two Annealed Copper Plates produce one Processor in a Stainless Steel Lithography Machine. Processors are used in titanium machinery.

## Upgrading

Upgrade your existing machine with plates of the next material, a Glass Block, a Robot Arm and the previous stage's circuit or processor. Higher-tier machines also run lower-tier recipes.

<Recipe id="moderntech:processing_table/titanium_lithography" />
Titanium Lithography Machines use one Silicon Plate and two Superconductor Plates to make one Quantum Processor.

<Recipe id="moderntech:lithography/quantum_processor" />

<Recipe id="moderntech:processing_table/iridium_lithography" />

Iridium Lithography Machines process both recipes faster. With sufficient power and no overclocking, the Processor takes 10 seconds in a stainless steel machine, and the Quantum Processor takes 10 seconds in a titanium machine.
