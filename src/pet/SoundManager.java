package pet;

import javafx.scene.media.AudioClip;
import java.net.URL;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    private static SoundManager instance;
    private Map<String, AudioClip> sounds;
    private boolean enabled = true;

    private SoundManager() {
        sounds = new HashMap<>();
        loadSound("meow", "/sound/meow.wav");
        loadSound("bark", "/sound/bark.wav");
        loadSound("chirp", "/sound/chirp.wav");
        loadSound("eat", "/sound/eat.wav");
        loadSound("happy", "/sound/happy.wav");
        loadSound("water", "/sound/water.wav");
        loadSound("chime", "/sound/chime.wav");
        loadSound("snore", "/sound/snore.wav");
        loadSound("sad", "/sound/sad.wav");
        loadSound("click", "/sound/click.wav");
    }

    public static SoundManager getInstance() {
        if (instance == null) instance = new SoundManager();
        return instance;
    }

    private void loadSound(String key, String path) {
        try {
            URL url = getClass().getResource(path);
            if (url == null) {
                File f = new File("sound/" + key + ".wav");
                if (f.exists()) {
                    url = f.toURI().toURL();
                }
            }
            if (url != null) {
                AudioClip clip = new AudioClip(url.toString());
                sounds.put(key, clip);
            }
        } catch (Exception e) {
            // file suara tidak ditemukan, skip
        }
    }

    public void play(String key) {
        if (!enabled) return;
        AudioClip clip = sounds.get(key);
        if (clip != null) {
            clip.play();
        }
    }

    public boolean hasSounds() {
        return !sounds.isEmpty();
    }

    public void playSpeciesSound(String species) {
        switch (species.toLowerCase()) {
            case "kucing": play("meow"); break;
            case "anjing": play("bark"); break;
            case "burung": play("chirp"); break;
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}

