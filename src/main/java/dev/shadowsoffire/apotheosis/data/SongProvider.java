package dev.shadowsoffire.apotheosis.data;

import dev.shadowsoffire.apotheosis.Apoth.Songs;
import dev.shadowsoffire.apotheosis.Apoth.Sounds;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.JukeboxSong;

public class SongProvider {

    private static void register(
        BootstrapContext<JukeboxSong> context, ResourceKey<JukeboxSong> key, Holder<SoundEvent> soundEvent, int lengthInSeconds, int comparatorOutput) {
        context.register(
            key,
            new JukeboxSong(soundEvent, Component.translatable(Util.makeDescriptionId("jukebox_song", key.location())), (float) lengthInSeconds, comparatorOutput));
    }

    public static void bootstrap(BootstrapContext<JukeboxSong> context) {
        register(context, Songs.FLASH, Sounds.MUSIC_DISC_FLASH, 1 * 60 + 37, 6);
        register(context, Songs.GLIMMER, Sounds.MUSIC_DISC_GLIMMER, 2 * 60 + 21, 7);
        register(context, Songs.SHIMMER, Sounds.MUSIC_DISC_SHIMMER, 2 * 60 + 46, 8);
    }

}
