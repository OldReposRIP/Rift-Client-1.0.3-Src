package live.rift.module.modules.misc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import live.rift.module.Category;
import live.rift.module.Module;
import live.rift.setting.Setting;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

public class Announcer extends Module {

    public Setting movement;
    public Setting eating;
    public Setting placing;
    public Setting delayChange;
    public Setting language;
    public ArrayList languages = new ArrayList();
    List currentHotbar = new ArrayList();
    List lastHotbar = new ArrayList();
    private Vec3d lastPosition;
    private ItemStack destroyingBlock;
    int delay;
    int minedBlock;
    boolean shouldSendMessage;
    private List changedItems;

    public Announcer() {
        super("Announcer", 0, Category.MISC);
        this.destroyingBlock = new ItemStack(Items.AIR);
        this.delay = -1;
        this.minedBlock = 0;
        this.shouldSendMessage = false;
        this.changedItems = new ArrayList();
        this.delayChange = new Setting("Delay", this, 10.0D, 10.0D, 60.0D, true);
        this.language = new Setting("Language", this, "Japanese", this.languages);
        this.languages.add("Japanese");
        this.languages.add("English");
    }

    public void onEnable() {
        this.delay = 400;
        this.minedBlock = 0;
        this.changedItems = new ArrayList();
    }

    public void onUpdate() {
        --this.delay;
        if (Announcer.mc.world != null) {
            if (this.delay <= 0) {
                this.updateHotbar(this.currentHotbar);
                if (this.lastHotbar.size() < 1) {
                    this.updateHotbar(this.lastHotbar);
                }

                this.changedItems = this.getChangedItems(this.currentHotbar, this.lastHotbar);
                if (this.lastPosition == null) {
                    this.lastPosition = Announcer.mc.player.getPositionVector();
                }

                int distance = (int) Math.sqrt(Announcer.mc.player.getPosition().distanceSq(this.lastPosition.x, this.lastPosition.y, this.lastPosition.z));
                Random rand = new Random();
                int random1 = 0;
                String[] move = new String[] { new String("I just moved " + distance + " meters thanks to RIFT"), new String("RIFTã?®ã?Šã?‹ã?’ã?§" + distance + "ãƒ¡ãƒ¼ãƒˆãƒ«ç§»å‹•ã?—ã?¾ã?—ã?Ÿï¼?")};

                if (this.changedItems.size() == 0) {
                    if (distance != 0) {
                        Announcer.mc.player.sendChatMessage(move[this.getLanguage(this.language.getValString())]);
                    }

                    this.lastPosition = Announcer.mc.player.getPositionVector();
                    this.updateHotbar(this.lastHotbar);
                    this.delay = (int) (this.delayChange.getValDouble() * 40.0D);
                    return;
                }

                if (this.changedItems.size() > 1) {
                    random1 = rand.nextInt(this.changedItems.size());
                }

                int used = ((Announcer.StackInfo) this.changedItems.get(random1)).getUsed();
                Announcer.ItemAction action = ((Announcer.StackInfo) this.changedItems.get(random1)).getAction();
                String name = ((Announcer.StackInfo) this.changedItems.get(random1)).getName();
                String[] place = new String[] { new String("I just placed " + used + " " + name + " thanks to RIFT!"), new String("RIFTã?®ã?Šã?‹ã?’ã?§" + used + " " + name + "ã‚’é…?ç½®ã?—ã?¾ã?—ã?Ÿï¼?")};
                String[] eat = new String[] { new String("I just ate " + used + " " + name + " thanks to RIFT!"), new String("RIFTã?®ã?Šã?‹ã?’ã?§" + used + " " + name + "ã‚’é£Ÿã?¹ã?¾ã?—ã?Ÿï¼?")};

                if (action == Announcer.ItemAction.EAT) {
                    Announcer.mc.player.sendChatMessage(eat[this.getLanguage(this.language.getValString())]);
                } else if (action == Announcer.ItemAction.PLACE) {
                    Announcer.mc.player.sendChatMessage(place[this.getLanguage(this.language.getValString())]);
                }

                this.delay = (int) (this.delayChange.getValDouble() * 40.0D);
                this.updateHotbar(this.lastHotbar);
            }

        }
    }

    public void updateHotbar(List hotbarList) {
        hotbarList.clear();

        for (int i = 0; i < 9; ++i) {
            if (Announcer.mc.player.inventory.getStackInSlot(i) != null) {
                hotbarList.add(Announcer.mc.player.inventory.getStackInSlot(i));
            }
        }

        if (Announcer.mc.player.inventory.getStackInSlot(45) != null) {
            hotbarList.add(Announcer.mc.player.inventory.getStackInSlot(45));
        }

    }

    public int getLanguage(String s) {
        return s.equalsIgnoreCase("English") ? 0 : 1;
    }

    List getChangedItems(List currentInv, List lastInv) {
        ArrayList changedItems = new ArrayList();
        int i = 0;
        Iterator iterator = currentInv.iterator();

        while (iterator.hasNext()) {
            ItemStack c = (ItemStack) iterator.next();

            if (i < 10) {
                ++i;
            }

            ItemStack s = (ItemStack) lastInv.get(i - 1);

            if ((s.stackSize - c.stackSize <= 0 || !(c.getItem() instanceof ItemBlock)) && (s.stackSize - c.stackSize <= 0 || c.getItem() != Items.END_CRYSTAL)) {
                if (s.stackSize - c.stackSize > 0 && c.getItem() instanceof ItemFood) {
                    changedItems.add(new Announcer.StackInfo(s.stackSize - c.stackSize, Announcer.ItemAction.EAT, c.getDisplayName()));
                }
            } else {
                changedItems.add(new Announcer.StackInfo(s.stackSize - c.stackSize, Announcer.ItemAction.PLACE, c.getDisplayName()));
            }

            if (this.minedBlock > 0) {
                changedItems.add(new Announcer.StackInfo(this.minedBlock, Announcer.ItemAction.BREAK, this.destroyingBlock.getDisplayName()));
                this.minedBlock = 0;
            }
        }

        return changedItems;
    }

    class StackInfo {

        int i;
        Announcer.ItemAction action;
        String name;

        public StackInfo(int i, Announcer.ItemAction action, String name) {
            this.i = i;
            this.action = action;
            this.name = name;
        }

        public int getUsed() {
            return this.i;
        }

        public Announcer.ItemAction getAction() {
            return this.action;
        }

        public String getName() {
            return this.name;
        }
    }

    static enum ItemAction {

        EAT, BREAK, PLACE;
    }
}
