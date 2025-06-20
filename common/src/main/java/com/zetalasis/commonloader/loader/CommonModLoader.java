package com.zetalasis.commonloader.loader;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zetalasis.commonloader.CommonLoader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.stream.Stream;

import java.io.InputStream;
import java.net.URLClassLoader;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;

public class CommonModLoader {
    private static final HashSet<ICommonMod> MODLIST = new HashSet<>();
    private static final Gson GSON = new Gson();

    public static void bootstrap(Path path) {
        try {
            Path executingPath = path;

            if (executingPath == null)
                executingPath = Path.of(CommonModLoader.class.getProtectionDomain()
                        .getCodeSource().getLocation().toURI()).getParent();

            Path modsFolder;

            if (executingPath.endsWith("mods"))
                modsFolder = executingPath;
            else
                modsFolder = executingPath.resolve("mods");

            if (!Files.isDirectory(modsFolder)) {
                throw new RuntimeException("\"mods\" folder was not a directory!\n" + modsFolder);
            }

            try (Stream<Path> modFiles = Files.list(modsFolder)) {
                modFiles.filter(file -> file.toString().endsWith(".jar")).forEach(file -> {
                    try (JarFile jar = new JarFile(file.toFile())) {
                        JarEntry modJsonEntry = jar.getJarEntry("common.mod.json");
                        if (modJsonEntry == null) {
                            CommonLoader.LOGGER.info("Skipping {} (no common.mod.json)", file.getFileName());
                            return;
                        }

                        try (InputStream jsonStream = jar.getInputStream(modJsonEntry);
                             InputStreamReader jsonStreamReader = new InputStreamReader(jsonStream)) {
                            JsonObject root = JsonParser.parseReader(jsonStreamReader).getAsJsonObject();

                            String entryPoint = root.get("entrypoint").getAsString();

                            URL jarUrl = file.toUri().toURL();
                            URLClassLoader loader = new URLClassLoader(new URL[]{jarUrl}, CommonModLoader.class.getClassLoader());

                            Class<?> modClass = Class.forName(entryPoint, true, loader);
                            Object instance = modClass.getDeclaredConstructor().newInstance();

                            if (!(instance instanceof ICommonMod)) {
                                CommonLoader.LOGGER.error("Entry point {} does not implement ICommonMod", entryPoint);
                                return;
                            }

                            ICommonMod mod = (ICommonMod) instance;
                            MODLIST.add(mod);

                            CommonLoader.LOGGER.info("Loaded mod {} successfully", mod.getModId());

                        } catch (Exception e) {
                            CommonLoader.LOGGER.error("Failed to load mod from {}", file.getFileName());
                            e.printStackTrace();
                        }

                    } catch (IOException e) {
                        CommonLoader.LOGGER.error("Error reading JAR file {}", file.getFileName());
                        e.printStackTrace();
                    }
                });
            }

            initalizeMods();
        } catch (Exception e) {
            throw new RuntimeException("Mod bootstrap failed: " + e.getMessage(), e);
        }
    }

    public static void initalizeMods() {
        for (ICommonMod mod : MODLIST) {
            try {
                mod.init();
            } catch (Exception e) {
                CommonLoader.LOGGER.error("Failed to load mod {}:{} (Exception Caught)\n{}",
                        mod.getModId(), mod.getDisplayName(), e);
                e.printStackTrace();
            }
        }
    }
}