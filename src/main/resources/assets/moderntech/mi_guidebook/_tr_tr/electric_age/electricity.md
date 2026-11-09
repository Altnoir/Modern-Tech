---
navigation:
  title: "Elektrik"
  icon: "moderntech:electric_steam_turbine"
  position: 104
  parent: moderntech:electric_age.md
item_ids:
  - moderntech:electric_steam_turbine
  - moderntech:electric_diesel_generator
---

# Elektrik

Elektrik Buhar Türbini elektrik üretmek için buhar kullanır. Her mb buharı 1 FE'ye dönüştürür ve her tikte en fazla 32 mb dönüştürür.

<Recipe id="moderntech:processing_table/electric_steam_turbine" />

Elektrikli buhar türbini, çıkış yüzüne bitişik makinelere otomatik olarak FE gönderir. Dahili arabelleği 3.200 FE tutar.

Elektrik Dizel Jeneratör, Elektrik Buhar Türbinine bir alternatiftir. Elektrik üretmek için çeşitli yakıtlar kullanır. Şimdilik Kreozot yakabilirsiniz. (Yakılabilir yakıtların listesi için REI'ye bakın).

<Recipe id="moderntech:processing_table/electric_diesel_generator" />

## ModernTech'ta Enerji

ModernTech **kendine ait kablo veya transformatör eklemez**. Her elektrikli makine yalnızca standart NeoForge enerji yeteneğini sunar, bu yüzden **başka herhangi bir modun** FE kablosu, enerji borusu veya enerji depolama bloğu ile beslenebilir.

Yani:

- Bir makineyi doğrudan başka bir modun kablosundan veya borusundan besleyin ya da bir jeneratörü makinenin hemen yanına koyun.
- Artık eşleştirilecek bir voltaj yok: gücü kademeler arasında taşımak için makine gövdelerini yükseltmenize veya gücü transformatörlerden geçirmenize gerek yok.
- Makine kademesi, kullanılabilen tarifleri ve işlem hızını belirler. Gövde, enerji arabelleğinin kapasitesini belirler. Arabellek dolunca FE alımı durur.

Makinelerin enerjiyi yalnızca giriş olarak işaretlenmiş yüzlerden kabul ettiğini ve jeneratörlerin enerjiyi yalnızca çıkış tarafından dışarı verdiğini unutmayın.
