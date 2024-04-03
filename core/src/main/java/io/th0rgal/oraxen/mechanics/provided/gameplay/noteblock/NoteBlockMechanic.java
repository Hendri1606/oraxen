package io.th0rgal.oraxen.mechanics.provided.gameplay.noteblock;

import io.th0rgal.oraxen.config.Settings;
import io.th0rgal.oraxen.mechanics.MechanicFactory;
import io.th0rgal.oraxen.mechanics.provided.gameplay.custom_block.CustomBlockMechanic;
import io.th0rgal.oraxen.mechanics.provided.gameplay.noteblock.directional.DirectionalBlock;
import io.th0rgal.oraxen.mechanics.provided.gameplay.noteblock.logstrip.LogStripping;
import io.th0rgal.oraxen.mechanics.provided.gameplay.storage.StorageMechanic;
import io.th0rgal.oraxen.utils.actions.ClickAction;
import org.bukkit.Instrument;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public class NoteBlockMechanic extends CustomBlockMechanic {

    private final boolean canIgnite;
    private final boolean isFalling;
    private final LogStripping logStripping;
    private final DirectionalBlock directionalBlock;
    private final List<ClickAction> clickActions;
    private final StorageMechanic storage;

    public NoteBlockMechanic(MechanicFactory mechanicFactory, ConfigurationSection section) {
        // Creates an instance of CustomBlockMechanic and applies the below
        super(mechanicFactory, section);

        clickActions = ClickAction.parseList(section);
        canIgnite = section.getBoolean("can_ignite", false);
        isFalling = section.getBoolean("is_falling", false);

        ConfigurationSection logStripSection = section.getConfigurationSection("logStrip");
        logStripping = logStripSection != null ? new LogStripping(logStripSection) : null;

        ConfigurationSection directionalSection = section.getConfigurationSection("directional");
        directionalBlock = directionalSection != null ? new DirectionalBlock(directionalSection) : null;

        ConfigurationSection storageSection = section.getConfigurationSection("storage");
        storage = storageSection != null ? new StorageMechanic(storageSection) : null;
    }

    @Override
    public NoteBlock createBlockData() {
        Instrument instrument;
        Note note;
        boolean powered;
        if (Settings.LEGACY_NOTEBLOCKS.toBool()) {
            /* We have 16 instruments with 25 notes. All of those blocks can be powered.
             * That's: 16*25*2 = 800 variations. The first 25 variations of PIANO (not powered)
             * will be reserved for the vanilla behavior. We still have 800-25 = 775 variations
             */
            int customVariation = customVariation() + 26;
            instrument = Instrument.getByType((byte) ((customVariation % 400) / 25));
            note = new Note(customVariation % 25);
            powered = customVariation >= 400;
        } else {
            instrument = Instrument.getByType((byte)Math.min(Instrument.values().length, customVariation() / 50));
            note = new Note(customVariation() % 25);
            powered = (customVariation() % 50 >= 25);
        }
        if (instrument == null) return null;

        NoteBlock noteBlock = (NoteBlock) Material.NOTE_BLOCK.createBlockData();
        noteBlock.setInstrument(instrument);
        noteBlock.setNote(note);
        noteBlock.setPowered(powered);

        return noteBlock;
    }

    @Override
    public NoteBlock blockData() {
        return (NoteBlock) super.blockData();
    }

    public boolean isStorage() { return storage != null; }
    public StorageMechanic storage() { return storage; }

    public boolean isLog() {
        if (isDirectional() && !directionalBlock.isParentBlock()) {
            return logStripping != null || directionalBlock.getParentMechanic().isLog();
        } else return logStripping != null;
    }
    public LogStripping log() { return logStripping; }

    public boolean isFalling() {
        if (isDirectional() && !directionalBlock.isParentBlock()) {
            return isFalling || directionalBlock.getParentMechanic().isFalling();
        } else return isFalling;
    }

    public boolean isDirectional() { return directionalBlock != null; }
    public DirectionalBlock directional() { return directionalBlock; }

    public boolean hasHardness() {
        if (isDirectional() && !directionalBlock.isParentBlock()) {
            return hardness() != -1 || directionalBlock.getParentMechanic().hasHardness();
        } else return hardness() != -1;
    }

    @Override
    public boolean hasLight() {
        if (isDirectional() && !directionalBlock.isParentBlock()) {
            return light().hasLightLevel() || directionalBlock.getParentMechanic().light().hasLightLevel();
        } else return light().hasLightLevel();
    }

    public boolean canIgnite() {
        if (isDirectional() && !directionalBlock.isParentBlock()) {
            return canIgnite || directionalBlock.getParentMechanic().canIgnite();
        } else return canIgnite;
    }

    public boolean isInteractable() {
        return hasClickActions() || isStorage();
    }

}
