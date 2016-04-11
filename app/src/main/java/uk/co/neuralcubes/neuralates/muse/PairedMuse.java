package uk.co.neuralcubes.neuralates.muse;

import android.support.annotation.NonNull;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.interaxon.libmuse.Muse;
import com.interaxon.libmuse.MuseManager;

import java.util.List;

/**
 * Manages the list of paired muses so they are easily accesible by the
 * ui
 */
public class PairedMuse {

    /**
     * Returns the list of paired muses
     */
    public static List<PairedMuse> getPairedMuses() {
        MuseManager.refreshPairedMuses();
        List<PairedMuse> pairedMuses = Lists.newArrayList();
        for (Muse m: MuseManager.getPairedMuses()){
            pairedMuses.add(new PairedMuse(m));
        }
        return pairedMuses;
    }

    final private Muse muse;

    PairedMuse(@NonNull Muse muse) {
        this.muse = muse;
    }

    public MuseHandler connect(@NonNull EventBus bus) {
        MuseHandler handler = new MuseHandler(this.muse, bus);
        handler.connect();
        return handler;
    }

    @Override
    public String toString() {
        return this.muse.getName();
    }

}
