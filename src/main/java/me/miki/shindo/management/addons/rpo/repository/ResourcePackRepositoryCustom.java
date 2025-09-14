package me.miki.shindo.management.addons.rpo.repository;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import me.miki.shindo.injection.interfaces.IMixinMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.client.settings.GameSettings;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.List;

public class ResourcePackRepositoryCustom extends ResourcePackRepository {
    private static Constructor<Entry> entryConstructor;
    private final List<Entry> repositoryEntries = Lists.newArrayList();
    private final boolean isReady;
    private List<Entry> repositoryEntriesAll = Lists.newArrayList();

    public ResourcePackRepositoryCustom(File dirResourcepacks, File dirServerResourcepacks, IResourcePack rprDefaultResourcePack, IMetadataSerializer rprMetadataSerializer, GameSettings settings, List<String> enabledPacks) {
        super(dirResourcepacks, dirServerResourcepacks, rprDefaultResourcePack, rprMetadataSerializer, settings);
        isReady = true;
        updateRepositoryEntriesAll();

        repositoryEntries.clear(); // ⬅️ limpa antes de adicionar
        for (String pack : enabledPacks) {
            for (Entry entry : repositoryEntriesAll) {
                if (entry.getResourcePackName().equals(pack)) {
                    if (!repositoryEntries.contains(entry)) {
                        repositoryEntries.add(entry);
                    }
                }
            }
        }
    }

    public static void overrideRepository(List<String> enabledPacks) {
        Minecraft mc = Minecraft.getMinecraft();

        try {
            File fileResourcepacks = ((IMixinMinecraft) mc).getFileResourcepacks();

            ResourcePackRepository originalRepo = ((IMixinMinecraft) mc).getMcResourcePackRepository();

            ResourcePackRepositoryCustom customRepo = new ResourcePackRepositoryCustom(
                    fileResourcepacks,
                    new File(mc.mcDataDir, "server-resource-packs"),
                    ((IMixinMinecraft) mc).getMcDefaultResourcePack(),
                    originalRepo.rprMetadataSerializer,
                    mc.gameSettings,
                    enabledPacks
            );

            ((IMixinMinecraft) mc).setMcResourcePackRepository(customRepo);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to override resource pack repository", t);
        }
    }

    public static Entry createEntryInstance(ResourcePackRepository repository, File file) {
        try {
            if (entryConstructor == null) {
                entryConstructor = Entry.class.getDeclaredConstructor(ResourcePackRepository.class, File.class);
                entryConstructor.setAccessible(true);
            }

            return entryConstructor.newInstance(repository, file);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    private List<File> getResourcePackFiles(File root) {
        if (root.isDirectory()) {
            List<File> packFiles = Lists.newArrayList();
            for (File file : root.listFiles()) {
                if (file.isDirectory() && !new File(file, "pack.mcmeta").isFile()) {
                    packFiles.addAll(getResourcePackFiles(file));
                } else {
                    packFiles.add(file);
                }
            }
            return packFiles;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public void updateRepositoryEntriesAll() {
        if (!isReady) return;

        List<Entry> list = Lists.newArrayList();

        for (File file : getResourcePackFiles(getDirResourcepacks())) {
            Entry entry = createEntryInstance(this, file);

            if (!repositoryEntriesAll.contains(entry)) {
                try {
                    entry.updateResourcePack();
                    list.add(entry);
                } catch (Exception e) {
                    // falha ao carregar, ignora
                }
            } else {
                int index = repositoryEntriesAll.indexOf(entry);
                if (index >= 0 && index < repositoryEntriesAll.size()) {
                    list.add(repositoryEntriesAll.get(index));
                }
            }
        }

        // Fecha pacotes antigos
        for (ResourcePackRepository.Entry entry : repositoryEntriesAll) {
            if (!list.contains(entry)) {
                entry.closeResourcePack();
            }
        }

        repositoryEntriesAll = list;
    }

    @Override
    public List<Entry> getRepositoryEntriesAll() {
        return ImmutableList.copyOf(repositoryEntriesAll);
    }

    @Override
    public List<Entry> getRepositoryEntries() {
        return ImmutableList.copyOf(repositoryEntries);
    }

    @Override
    public void setRepositories(List<Entry> repositories) {
        repositoryEntries.clear();
        for (Entry entry : repositories) {
            if (!repositoryEntries.contains(entry)) {
                repositoryEntries.add(entry);
            }
        }
    }
}