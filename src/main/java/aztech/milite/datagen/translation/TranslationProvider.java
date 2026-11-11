package aztech.milite.datagen.translation;

import aztech.milite.compat.rei.machines.ReiMachineRecipes;
import aztech.milite.config.MIConfigBuilder;
import aztech.milite.datagen.tag.TagsToGenerate;
import aztech.milite.definition.Definition;
import aztech.milite.machines.init.MachineLevel;
import aztech.milite.machines.models.MachineCasings;
import aztech.milite.MI;
import aztech.milite.MIFluids;
import aztech.milite.MIText;
import aztech.milite.MITooltips;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.Util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.Map;
import java.util.TreeMap;

public final class TranslationProvider implements DataProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String OUTPUT_PATH = "assets/moderntech/lang/en_us.json";

    private final PackOutput packOutput;
    private final Map<String, String> translationPairs = new TreeMap<>();

    public TranslationProvider(PackOutput packOutput) {
        this.packOutput = packOutput;
    }

    public void addTranslation(String key, String englishValue) {
        if (!translationPairs.containsKey(key)) {
            translationPairs.put(key, englishValue);
        } else {
            throw new IllegalArgumentException(
                    String.format("Error adding translation key %s for translation %s : already registered for translation %s", key, englishValue,
                            translationPairs.get(key)));
        }
    }

    private void addManualEntries() {
        addTranslation("block.moderntech.pipe", "Pipe(s)");

        // The machine levels, shown on a recipe in the viewer in place of the energy it costs.
        for (MachineLevel level : MachineLevel.values()) {
            addTranslation(level.getTranslationKey(), level.getEnglishName());
        }

        addTranslation("entity.minecraft.villager.moderntech.industrialist", "Industrialist");
        addTranslation("ftbquests.task.moderntech.built_multiblock", "Build Multiblock");
        addTranslation("ftbquests.task.moderntech.built_multiblock.machine", "Machine");

        addTranslation("key.categories.moderntech.moderntech", "ModernTech");
        addTranslation("key.moderntech.toggle_flight", "Toggle Flight");
        addTranslation("text.autoconfig.moderntech.title", "ModernTech Menu");
        addTranslation("tag.moderntech.replicator_blacklist", "Replicator Blacklist");

        addTranslation("config.jade.plugin_moderntech.overclock", "Machine Overclock");
        addTranslation("config.jade.plugin_moderntech.pipe", "Pipe Information");

        addTranslation(MIText.AttributeInfiniteDamage.getTranslationKey() + ".desc",
                "Deals an effectively infinite amount of damage.");
        addTranslation(MIText.AttributeQuantumArmor.getTranslationKey() + ".desc",
                "Grants a chance to completely nullify all damage. When a full set is equipped, all damage taken is negated.");
    }

    private void collectTranslationEntries() {
        addManualEntries();

        for (var entry : MIText.values()) {
            addTranslation(entry.getTranslationKey(), entry.getEnglishText());
            for (String additionalKey : entry.getAdditionalTranslationKey()) {
                addTranslation(additionalKey, entry.getEnglishText());
            }
        }

        for (var entry : MIConfigBuilder.configTranslations.entrySet()) {
            addTranslation(entry.getKey(), entry.getValue());
        }

        for (Definition definition : Definition.TRANSLATABLE_DEFINITION) {
            addTranslation(definition.getTranslationKey(), definition.getEnglishName());
        }

        for (var entry : MITooltips.TOOLTIPS_ENGLISH_TRANSLATION.entrySet()) {
            addTranslation(entry.getKey(), entry.getValue());
        }

        for (var entry : ReiMachineRecipes.categories.entrySet()) {
            if (entry.getKey().getNamespace().equals(MI.ID)) {
                addTranslation("rei_categories.moderntech." + entry.getKey().getPath(), entry.getValue().englishName);
            }
        }

        for (var entry : TagsToGenerate.tagTranslations.entrySet()) {
            addTranslation(entry.getKey(), entry.getValue());
        }


        for (var casingName : MachineCasings.translations) {
            addTranslation(casingName.casing().getTranslationKey(), casingName.englishName());
        }

        for (var fluid : MIFluids.FLUID_DEFINITIONS.values()) {
            addTranslation("tag.fluid.c." + fluid.getId().getPath(), fluid.getEnglishName());
        }
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return CompletableFuture.runAsync(() -> {
            try {
                innerRun(output);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, Util.backgroundExecutor());
    }

    public void innerRun(CachedOutput cache) throws IOException {
        collectTranslationEntries();

        customJsonSave(cache, GSON.toJsonTree(translationPairs), packOutput.getOutputFolder().resolve(OUTPUT_PATH));
    }

    private void customJsonSave(CachedOutput cache, JsonElement jsonElement, Path path) throws IOException {
        String sortedJson = GSON.toJson(jsonElement);
        String prettyPrinted = sortedJson.replace("\\u0027", "'");
        cache.writeIfNeeded(path, prettyPrinted.getBytes(StandardCharsets.UTF_8), Hashing.sha1().hashString(prettyPrinted, StandardCharsets.UTF_8));
    }

    @Override
    public String getName() {
        return "Translations";
    }
}
