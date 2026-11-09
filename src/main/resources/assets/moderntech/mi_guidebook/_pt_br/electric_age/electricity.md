---
navigation:
  title: "Eletricidade"
  icon: "moderntech:electric_steam_turbine"
  position: 104
  parent: moderntech:electric_age.md
item_ids:
  - moderntech:electric_steam_turbine
  - moderntech:electric_diesel_generator
---

# Eletricidade

A Turbina de Vapor Elétrica usa vapor para produzir eletricidade. Ela converte cada mb de vapor em 1 FE, até 32 mb convertidos por tick.

<Recipe id="moderntech:processing_table/electric_steam_turbine" />

A Turbina a Vapor Elétrica envia FE automaticamente para máquinas junto à sua face de saída. Seu buffer interno armazena 3.200 FE.

O Gerador à Diesel Elétrica é uma alternativa à Turbina de Vapor Elétrica. Ele usa vários combustíveis para produzir eletricidade. Por enquanto, você pode queimar Creosoto. (Consulte o REI para a lista de combustíveis que podem ser queimados.)

<Recipe id="moderntech:processing_table/electric_diesel_generator" />

## Energia no ModernTech

O ModernTech **não** adiciona cabos nem transformadores próprios. Toda máquina elétrica simplesmente expõe a capacidade de energia padrão do NeoForge, então pode ser alimentada por um cabo de FE, tubo de energia ou bloco de armazenamento de energia de **qualquer outro mod**.

Isso significa que:

- Alimente uma máquina diretamente a partir do cabo ou tubo de outro mod, ou coloque um gerador bem ao lado dela.
- Não há mais voltagem para combinar: você não precisa mais melhorar cascos de máquina nem passar energia por transformadores só para movê-la entre tiers.
- O nível da máquina define as receitas disponíveis e a velocidade de processamento. A carcaça define a capacidade do buffer de energia. O buffer cheio deixa de aceitar FE.

Lembre-se de que as máquinas só aceitam energia pelos lados marcados como entradas, e que os geradores só enviam energia para fora pelo seu lado de saída.
