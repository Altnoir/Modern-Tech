---
navigation:
  title: "전력 생산"
  icon: "moderntech:electric_steam_turbine"
  position: 103
  parent: moderntech:electric_age.md
item_ids:
---

# 전력 생산

전력 증기 터빈은 증기로 전력을 생산합니다. 증기 1 mb당 1 RF로 변환하며, 1틱당 최대 32 mb까지 변환합니다.

<Recipe id="moderntech:processing_table/electric_steam_turbine" />

전력 증기 터빈은 출력면에 바로 붙어 있는 모든 기계에 자동으로 전력을 보냅니다. 내부 버퍼는 256 RF를 저장할 수 있으며, 저전압(LV) *티어*입니다.

전력 디젤 발전기는 전력 증기 터빈의 대안입니다. 다양한 연료로 전력을 생산합니다. 현재로서는 크레오소트를 태울 수 있습니다. (태울 수 있는 연료 목록은 REI를 참고하세요.)

<Recipe id="moderntech:processing_table/electric_diesel_generator" />

## ModernTech의 전력

ModernTech는 **자체적인 전선이나 변압기를 추가하지 않습니다**. 모든 전력 기계는 표준 NeoForge 에너지 기능을 노출할 뿐이므로, **다른 어떤 모드**의 RF 전선, 에너지 파이프, 에너지 저장 블록으로도 전력을 공급할 수 있습니다.

즉:

- 다른 모드의 전선이나 파이프에서 기계로 직접 전력을 공급하거나, 발전기를 기계 바로 옆에 두면 됩니다.
- 더 이상 전압을 맞출 필요가 없습니다. 티어 사이로 전력을 옮기기 위해 기계 외장을 업그레이드하거나 전력을 변압기로 보낼 필요가 없습니다.
- 티어(LV, MV, HV, EV, SV)는 여전히 존재하지만, 이는 기계가 몇 RF/t에 맞춰 만들어졌는지와 내부 버퍼가 얼마나 큰지만 나타냅니다. 버퍼가 가득 찬 기계는 더 이상 RF를 받지 않을 뿐이며, 폭발하지는 않습니다.

기계는 입력으로 표시된 면에서만 에너지를 받아들이고, 발전기는 출력면으로만 에너지를 내보낸다는 점을 기억하세요.
