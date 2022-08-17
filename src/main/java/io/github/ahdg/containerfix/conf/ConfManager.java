package io.github.ahdg.containerfix.conf;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.SneakyThrows;
import lombok.val;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.config.DefaultConfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@Singleton
public class ConfManager {

    @Inject @DefaultConfig(sharedRoot = true) private Path cp;
    @Inject private Logger logger;

    private Conf conf;

    @SneakyThrows
    private void loadNewConf() {
        if (!Files.exists(cp)) {
            conf = new Conf();
            save();
        } else {
            val load = HoconConfigurationLoader.builder().setPath(cp).build().load();
            conf = load.getValue(TypeToken.of(Conf.class));
            save();
        }
        Objects.requireNonNull(conf);
        logger.info("Configurations loaded ...");
    }

    public Conf get() {
        if (conf == null) loadNewConf();
        return conf;
    }

    public Conf reload() {
        conf = null;
        return get();
    }

    @SneakyThrows
    public void save() {
        val loader = HoconConfigurationLoader.builder().setPath(cp).build();
        val emptyNode = loader.createEmptyNode();
        emptyNode.setValue(TypeToken.of(Conf.class), conf);
        loader.save(emptyNode);
    }

}