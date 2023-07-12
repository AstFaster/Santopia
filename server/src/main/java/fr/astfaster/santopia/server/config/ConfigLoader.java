package fr.astfaster.santopia.server.config;

import fr.astfaster.santopia.api.SantopiaException;
import fr.astfaster.santopia.api.config.Config;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Supplier;

public class ConfigLoader<T extends Config> {

    private final Yaml yaml;

    private final Class<T> configClass;
    private final Supplier<T> defaultConfig;

    public ConfigLoader(Class<T> configClass, Supplier<T> defaultConfig) {
        this.configClass = configClass;
        this.defaultConfig = defaultConfig;

        final DumperOptions options = new DumperOptions();

        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndent(2);
        options.setPrettyFlow(false);
        options.setCanonical(false);

        this.yaml = new Yaml(new CustomClassLoaderConstructor(configClass.getClassLoader(), new LoaderOptions()), new MapRepresenter(options), options);
        this.yaml.setBeanAccess(BeanAccess.FIELD);
    }

    public T load(Path path) {
        if (!Files.exists(path)) {
            final T config = this.defaultConfig.get();

            try {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            } catch (IOException e) {
                throw new SantopiaException("Couldn't save config!", e);
            }

            save(path, config);

            return config;
        }

        try (final InputStream inputStream = Files.newInputStream(path)) {
            return this.yaml.loadAs(inputStream, configClass);
        } catch (IOException e) {
            throw new SantopiaException("Couldn't load config!", e);
        }
    }

    public void save(Path path, T config) {
        try (final PrintWriter writer = new PrintWriter(Files.newOutputStream(path))) {
            this.yaml.dump(config, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class MapRepresenter extends Representer {

        public MapRepresenter(DumperOptions options) {
            super(options);
        }

        @Override
        protected MappingNode representJavaBean(Set<Property> properties, Object javaBean) {
            if (!this.classTags.containsKey(javaBean.getClass())) {
                this.addClassTag(javaBean.getClass(), Tag.MAP);
            }
            return super.representJavaBean(properties, javaBean);
        }

    }

}
